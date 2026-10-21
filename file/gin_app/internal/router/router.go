package router

import (
	"gin_app/internal/config"
	"gin_app/internal/handler"
	"gin_app/internal/template"

	"github.com/gin-gonic/gin"
)

// 注册所有路由
func SetupRouter(h *handler.UploadHandler) *gin.Engine {
	gin.SetMode(gin.ReleaseMode)
	r := gin.Default()

	r.MaxMultipartMemory = config.MaxContentLength
	r.SetFuncMap(template.FuncMap())
	r.LoadHTMLGlob(config.BaseDir + "/templates/*")

	r.GET("/", h.UploadForm)
	r.POST("/upload/init", h.UploadInit)
	r.POST("/upload/chunk", h.UploadChunk)
	r.GET("/upload/status/:session_id", h.UploadStatus)
	r.POST("/upload/complete", h.UploadComplete)
	r.POST("/upload/cancel", h.UploadCancel)
	r.GET("/uploads", h.ListUploads)
	r.GET("/uploads/*filename", h.DownloadUpload)
	r.POST("/delete/*filename", h.DeleteFile)
	r.GET("/api/disk-info", h.DiskInfo)

	api := r.Group("/api")
	{
		api.GET("/", h.UploadForm)
		api.POST("/upload/init", h.UploadInit)
		api.POST("/upload/chunk", h.UploadChunk)
		api.GET("/upload/status/:session_id", h.UploadStatus)
		api.POST("/upload/complete", h.UploadComplete)
		api.POST("/upload/cancel", h.UploadCancel)
		api.GET("/files", h.ListUploads)
		api.GET("/uploads/*filename", h.DownloadUpload)
		api.POST("/delete/*filename", h.DeleteFile)
	}

	return r
}
