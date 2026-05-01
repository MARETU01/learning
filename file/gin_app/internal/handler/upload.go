package handler

import (
	"fmt"
	"math"
	"net/http"
	"os"
	"path/filepath"
	"sort"
	"strconv"
	"strings"

	"gin_app/internal/config"
	"gin_app/internal/model"
	"gin_app/internal/service"
	"gin_app/internal/utils"
	"gin_app/pkg/diskstats"

	"github.com/gin-gonic/gin"
)

type UploadHandler struct {
	uploadService *service.UploadService
}

func NewUploadHandler(us *service.UploadService) *UploadHandler {
	return &UploadHandler{uploadService: us}
}

// 上传表单页面
func (h *UploadHandler) UploadForm(c *gin.Context) {
	c.HTML(http.StatusOK, "submit.html", gin.H{})
}

// 初始化分块上传
func (h *UploadHandler) UploadInit(c *gin.Context) {
	var req model.InitRequest
	if err := c.ShouldBindJSON(&req); err != nil || req.Filename == "" || req.TotalSize <= 0 {
		c.JSON(http.StatusBadRequest, gin.H{"error": "缺少 filename 或 total_size"})
		return
	}

	session, err := h.uploadService.InitSession(req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"session_id":     session.SessionID,
		"chunk_size":     session.ChunkSize,
		"total_chunks":   session.TotalChunks,
		"final_filename": session.FinalFilename,
	})
}

// 上传单个分块
func (h *UploadHandler) UploadChunk(c *gin.Context) {
	sessionID := c.PostForm("session_id")
	chunkIndexStr := c.PostForm("chunk_index")

	chunkIndex, err := strconv.Atoi(chunkIndexStr)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "无效的 chunk_index"})
		return
	}

	session, err := h.uploadService.GetSession(sessionID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	if chunkIndex < 0 || chunkIndex >= session.TotalChunks {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": fmt.Sprintf("分块索引 %d 超出范围 [0, %d)", chunkIndex, session.TotalChunks),
		})
		return
	}

	chunkFile, err := c.FormFile("chunk")
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "无分块数据"})
		return
	}

	chunkPath := filepath.Join(session.ChunksDir, strconv.Itoa(chunkIndex))
	if err := c.SaveUploadedFile(chunkFile, chunkPath); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "保存分块失败"})
		return
	}

	if err := h.uploadService.AddChunkToSession(sessionID, chunkIndex); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	session, _ = h.uploadService.GetSession(sessionID)

	c.JSON(http.StatusOK, gin.H{
		"session_id":   sessionID,
		"chunk_index":  chunkIndex,
		"received":     session.ReceivedCount(),
		"total_chunks": session.TotalChunks,
	})
}

// 查询上传进度
func (h *UploadHandler) UploadStatus(c *gin.Context) {
	sessionID := c.Param("session_id")

	session, err := h.uploadService.GetSession(sessionID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	progress := float64(session.ReceivedCount()) / float64(session.TotalChunks) * 100

	sortedChunks := session.ReceivedChunks
	sort.Ints(sortedChunks)

	c.JSON(http.StatusOK, gin.H{
		"session_id":        sessionID,
		"final_filename":    session.FinalFilename,
		"original_filename": session.OriginalFilename,
		"total_size":        session.TotalSize,
		"chunk_size":        session.ChunkSize,
		"total_chunks":      session.TotalChunks,
		"received_chunks":   sortedChunks,
		"progress":          math.Round(progress*100) / 100,
	})
}

// 完成上传
func (h *UploadHandler) UploadComplete(c *gin.Context) {
	var req model.CompleteRequest
	if err := c.ShouldBindJSON(&req); err != nil || req.SessionID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "缺少 session_id"})
		return
	}

	session, err := h.uploadService.GetSession(req.SessionID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	finalFilename, actualSize, err := h.uploadService.CompleteSession(req.SessionID)
	if err != nil {
		if strings.Contains(err.Error(), "缺少分块") {
			missing := []int{}
			for i := 0; i < session.TotalChunks; i++ {
				if !session.HasChunk(i) {
					missing = append(missing, i)
				}
			}
			c.JSON(http.StatusBadRequest, gin.H{
				"error":          "缺少分块",
				"missing_chunks": missing,
				"total_chunks":   session.TotalChunks,
				"received":       session.ReceivedCount(),
			})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{
			"original_filename": session.OriginalFilename,
			"saved_filename":    session.FinalFilename,
			"status":            "failed",
			"message":           err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"original_filename": session.OriginalFilename,
		"saved_filename":    finalFilename,
		"size":              actualSize,
		"status":            "success",
		"message":           "上传完成",
	})
}

// 取消上传
func (h *UploadHandler) UploadCancel(c *gin.Context) {
	var req model.CancelRequest
	if err := c.ShouldBindJSON(&req); err != nil || req.SessionID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "缺少 session_id"})
		return
	}

	if err := h.uploadService.CancelSession(req.SessionID); err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"status":     "cancelled",
		"session_id": req.SessionID,
	})
}

// 已上传文件列表页面
func (h *UploadHandler) ListUploads(c *gin.Context) {
	entries, totalSize, _ := h.uploadService.ListFiles()

	diskUsage := diskstats.GetDiskUsage(config.UploadFolder)

	c.HTML(http.StatusOK, "uploads.html", gin.H{
		"files":              entries,
		"total_size":         totalSize,
		"total_size_display": utils.FormatFileSize(totalSize),
		"disk_total":         diskUsage.Total,
		"disk_total_display": diskUsage.TotalDisplay,
		"disk_used":          diskUsage.Used,
		"disk_used_display":  diskUsage.UsedDisplay,
		"disk_free":          diskUsage.Free,
		"disk_free_display":  diskUsage.FreeDisplay,
		"disk_pct":           math.Round(diskUsage.UsedPercent*10) / 10,
		"disk_pct_warning":   diskUsage.UsedPercent > 85,
		"has_files":          len(entries) > 0,
	})
}

// 文件下载
func (h *UploadHandler) DownloadUpload(c *gin.Context) {
	filename := c.Param("filename")
	filename = strings.TrimPrefix(filename, "/")
	rel := utils.SafeUploadRelpath(filename, config.UploadFolder)
	if rel == "" {
		c.AbortWithStatus(http.StatusNotFound)
		return
	}

	fullPath := filepath.Join(config.UploadFolder, rel)

	rootReal, _ := filepath.Abs(config.UploadFolder)
	fullReal, _ := filepath.Abs(fullPath)
	if fullReal != rootReal && !strings.HasPrefix(fullReal, rootReal+string(os.PathSeparator)) {
		c.AbortWithStatus(http.StatusForbidden)
		return
	}

	if _, err := os.Stat(fullPath); os.IsNotExist(err) {
		c.AbortWithStatus(http.StatusNotFound)
		return
	}

	c.Header("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", filepath.Base(rel)))
	c.Header("Content-Type", "application/octet-stream")
	c.Header("Accept-Ranges", "bytes")

	http.ServeFile(c.Writer, c.Request, fullPath)
}

// 文件删除
func (h *UploadHandler) DeleteFile(c *gin.Context) {
	filename := c.Param("filename")
	filename = strings.TrimPrefix(filename, "/")

	rel := utils.SafeUploadRelpath(filename, config.UploadFolder)
	if rel == "" {
		c.AbortWithStatus(http.StatusNotFound)
		return
	}

	fullPath := filepath.Join(config.UploadFolder, rel)

	rootReal, _ := filepath.Abs(config.UploadFolder)
	fullReal, _ := filepath.Abs(fullPath)
	if fullReal != rootReal && !strings.HasPrefix(fullReal, rootReal+string(os.PathSeparator)) {
		c.AbortWithStatus(http.StatusForbidden)
		return
	}

	if _, err := os.Stat(fullPath); os.IsNotExist(err) {
		c.AbortWithStatus(http.StatusNotFound)
		return
	}

	if err := h.uploadService.DeleteFile(filename); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"status":   "deleted",
		"filename": rel,
	})
}

// 磁盘信息 API
func (h *UploadHandler) DiskInfo(c *gin.Context) {
	disk := diskstats.GetDiskUsage(config.UploadFolder)

	var totalSize int64
	var fileCount int

	files, err := os.ReadDir(config.UploadFolder)
	if err == nil {
		for _, f := range files {
			if !f.IsDir() {
				info, e := f.Info()
				if e == nil {
					totalSize += info.Size()
					fileCount++
				}
			}
		}
	}

	c.JSON(http.StatusOK, gin.H{
		"disk_total":           disk.Total,
		"disk_used":            disk.Used,
		"disk_free":            disk.Free,
		"uploads_size":         totalSize,
		"uploads_count":        fileCount,
		"disk_total_display":   disk.TotalDisplay,
		"disk_used_display":    disk.UsedDisplay,
		"disk_free_display":    disk.FreeDisplay,
		"uploads_size_display": utils.FormatFileSize(totalSize),
	})
}
