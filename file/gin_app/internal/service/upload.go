package service

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math"
	"os"
	"path/filepath"
	"sort"
	"strconv"
	"strings"
	"sync"
	"time"

	"gin_app/internal/config"
	"gin_app/internal/model"
	"gin_app/internal/utils"

	"github.com/google/uuid"
)

// 上传服务，管理上传会话和文件操作
type UploadService struct {
	sessions map[string]*model.UploadSession
	mu       sync.RWMutex
}

func NewUploadService() *UploadService {
	return &UploadService{
		sessions: make(map[string]*model.UploadSession),
	}
}

// 初始化上传会话
func (s *UploadService) InitSession(req model.InitRequest) (*model.UploadSession, error) {
	safeName := utils.SanitizeFilenameKeepUnicode(req.Filename, 200)
	if safeName == "" {
		safeName = "upload_" + uuid.New().String()[:8]
	}

	finalName := utils.ResolveNameConflict(config.UploadFolder, safeName)

	sessionID := uuid.New().String()
	chunksDir := filepath.Join(config.TempFolder, sessionID)
	os.MkdirAll(chunksDir, 0755)

	totalChunks := int(math.Ceil(float64(req.TotalSize) / float64(config.ChunkSize)))
	if totalChunks < 1 {
		totalChunks = 1
	}

	session := &model.UploadSession{
		SessionID:        sessionID,
		OriginalFilename: req.Filename,
		FinalFilename:    finalName,
		TotalSize:        req.TotalSize,
		ChunkSize:        config.ChunkSize,
		TotalChunks:      totalChunks,
		ReceivedChunks:   []int{},
		ChunksDir:        chunksDir,
		CreatedAt:        time.Now().Format(time.RFC3339),
		ReceivedSet:      make(map[int]bool),
	}

	saveSessionMeta(session)

	s.mu.Lock()
	s.sessions[sessionID] = session
	s.mu.Unlock()

	return session, nil
}

// 获取会话，若内存中不存在则尝试从磁盘恢复
func (s *UploadService) GetSession(sessionID string) (*model.UploadSession, error) {
	s.mu.RLock()
	session, exists := s.sessions[sessionID]
	s.mu.RUnlock()

	if !exists {
		s.mu.Lock()
		recovered, loadErr := loadSessionFromMeta(sessionID)
		if loadErr != nil {
			s.mu.Unlock()
			return nil, fmt.Errorf("无效或已过期的会话")
		}
		s.sessions[sessionID] = recovered
		session = recovered
		s.mu.Unlock()
	}

	return session, nil
}

// 记录已接收的分块
func (s *UploadService) AddChunkToSession(sessionID string, chunkIndex int) error {
	session, err := s.GetSession(sessionID)
	if err != nil {
		return err
	}

	if chunkIndex < 0 || chunkIndex >= session.TotalChunks {
		return fmt.Errorf("分块索引 %d 超出范围 [0, %d)", chunkIndex, session.TotalChunks)
	}

	s.mu.Lock()
	session.AddChunk(chunkIndex)
	// 定期更新持久化的元数据
	if session.ReceivedCount()%10 == 0 || session.ReceivedCount() == session.TotalChunks {
		saveSessionMeta(session)
	}
	s.mu.Unlock()

	return nil
}

// 完成上传：合并分块为最终文件
func (s *UploadService) CompleteSession(sessionID string) (string, int64, error) {
	session, err := s.GetSession(sessionID)
	if err != nil {
		return "", 0, err
	}

	// 检查缺失的分块
	missing := []int{}
	for i := 0; i < session.TotalChunks; i++ {
		if !session.HasChunk(i) {
			missing = append(missing, i)
		}
	}

	if len(missing) > 0 {
		return "", 0, fmt.Errorf("缺少分块: %v (总数=%d, 已收=%d)",
			missing, session.TotalChunks, session.ReceivedCount())
	}

	// 合并分块
	finalPath := filepath.Join(config.UploadFolder, session.FinalFilename)
	outFile, err := os.Create(finalPath)
	if err != nil {
		return session.OriginalFilename, 0, fmt.Errorf("合并失败: %v", err)
	}

	for i := 0; i < session.TotalChunks; i++ {
		chunkPath := filepath.Join(session.ChunksDir, strconv.Itoa(i))
		chunkFile, err := os.Open(chunkPath)
		if err != nil {
			outFile.Close()
			return session.OriginalFilename, 0, fmt.Errorf("合并失败: 分块 %d 不存在", i)
		}
		_, err = io.Copy(outFile, chunkFile)
		chunkFile.Close()
		if err != nil {
			outFile.Close()
			return session.OriginalFilename, 0, fmt.Errorf("合并失败: %v", err)
		}
	}
	outFile.Close()

	// 校验文件大小
	actualSize, _ := utils.GetFileSize(finalPath)
	if actualSize != session.TotalSize {
		log.Printf("文件大小不一致 %s: 期望 %d, 实际 %d", session.FinalFilename, session.TotalSize, actualSize)
	}

	// 清理临时分块
	os.RemoveAll(session.ChunksDir)

	s.mu.Lock()
	delete(s.sessions, sessionID)
	s.mu.Unlock()

	return session.FinalFilename, actualSize, nil
}

// 取消上传：删除临时分块和会话
func (s *UploadService) CancelSession(sessionID string) error {
	session, err := s.GetSession(sessionID)
	if err != nil {
		return err
	}

	os.RemoveAll(session.ChunksDir)

	s.mu.Lock()
	delete(s.sessions, sessionID)
	s.mu.Unlock()

	return nil
}

// 获取已上传文件列表
func (s *UploadService) ListFiles() ([]model.FileInfo, int64, error) {
	entries := []model.FileInfo{}
	var totalSize int64

	files, err := os.ReadDir(config.UploadFolder)
	if err != nil {
		os.MkdirAll(config.UploadFolder, 0755)
		return entries, 0, nil
	}

	for _, f := range files {
		if f.IsDir() {
			continue
		}
		info, err := f.Info()
		if err != nil {
			continue
		}
		entries = append(entries, model.FileInfo{
			Name:        f.Name(),
			Size:        info.Size(),
			SizeDisplay: utils.FormatFileSize(info.Size()),
			Mtime:       info.ModTime().Format("2006-01-02 15:04:05"),
		})
		totalSize += info.Size()
	}

	sort.Slice(entries, func(i, j int) bool {
		return entries[i].Mtime > entries[j].Mtime
	})

	return entries, totalSize, nil
}

// 删除已上传的文件
func (s *UploadService) DeleteFile(filename string) error {
	rel := utils.SafeUploadRelpath(filename, config.UploadFolder)
	if rel == "" {
		return fmt.Errorf("无效文件名")
	}

	fullPath := filepath.Join(config.UploadFolder, rel)

	rootReal, _ := filepath.Abs(config.UploadFolder)
	fullReal, _ := filepath.Abs(fullPath)
	if fullReal != rootReal && !strings.HasPrefix(fullReal, rootReal+string(os.PathSeparator)) {
		return fmt.Errorf("拒绝访问")
	}

	if _, err := os.Stat(fullPath); os.IsNotExist(err) {
		return fmt.Errorf("文件不存在")
	}

	return os.Remove(fullPath)
}

// 保存会话元数据到磁盘
func saveSessionMeta(session *model.UploadSession) {
	metaPath := filepath.Join(session.ChunksDir, "_meta.json")
	saveable := map[string]interface{}{
		"session_id":        session.SessionID,
		"original_filename": session.OriginalFilename,
		"final_filename":    session.FinalFilename,
		"total_size":        session.TotalSize,
		"chunk_size":        session.ChunkSize,
		"total_chunks":      session.TotalChunks,
		"received_chunks":   session.ReceivedChunks,
		"chunks_dir":        session.ChunksDir,
		"created_at":        session.CreatedAt,
	}
	data, err := json.Marshal(saveable)
	if err != nil {
		return
	}
	os.WriteFile(metaPath, data, 0644)
}

// 从磁盘恢复会话元数据
func loadSessionFromMeta(sessionID string) (*model.UploadSession, error) {
	metaPath := filepath.Join(config.TempFolder, sessionID, "_meta.json")
	data, err := os.ReadFile(metaPath)
	if err != nil {
		return nil, err
	}

	var saveable map[string]interface{}
	if err := json.Unmarshal(data, &saveable); err != nil {
		return nil, err
	}

	session := &model.UploadSession{
		SessionID:        saveable["session_id"].(string),
		OriginalFilename: saveable["original_filename"].(string),
		FinalFilename:    saveable["final_filename"].(string),
		TotalSize:        int64(saveable["total_size"].(float64)),
		ChunkSize:        int64(saveable["chunk_size"].(float64)),
		TotalChunks:      int(saveable["total_chunks"].(float64)),
		ChunksDir:        saveable["chunks_dir"].(string),
		CreatedAt:        saveable["created_at"].(string),
		ReceivedSet:      make(map[int]bool),
	}

	if chunksArr, ok := saveable["received_chunks"].([]interface{}); ok {
		for _, c := range chunksArr {
			idx := int(c.(float64))
			session.ReceivedSet[idx] = true
			session.ReceivedChunks = append(session.ReceivedChunks, idx)
		}
	}

	return session, nil
}
