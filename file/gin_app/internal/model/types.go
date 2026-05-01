package model

// 上传会话
type UploadSession struct {
	SessionID        string       `json:"session_id"`
	OriginalFilename string       `json:"original_filename"`
	FinalFilename    string       `json:"final_filename"`
	TotalSize        int64        `json:"total_size"`
	ChunkSize        int64        `json:"chunk_size"`
	TotalChunks      int          `json:"total_chunks"`
	ReceivedChunks   []int        `json:"received_chunks"`
	ChunksDir        string       `json:"chunks_dir"`
	CreatedAt        string       `json:"created_at"`
	ReceivedSet      map[int]bool `json:"-"`
}

func (s *UploadSession) HasChunk(index int) bool {
	return s.ReceivedSet[index]
}

func (s *UploadSession) AddChunk(index int) {
	if !s.ReceivedSet[index] {
		s.ReceivedSet[index] = true
		s.ReceivedChunks = append(s.ReceivedChunks, index)
	}
}

func (s *UploadSession) ReceivedCount() int {
	return len(s.ReceivedSet)
}

// 请求类型
type InitRequest struct {
	Filename  string `json:"filename"`
	TotalSize int64  `json:"total_size"`
}

type CompleteRequest struct {
	SessionID string `json:"session_id"`
}

type CancelRequest struct {
	SessionID string `json:"session_id"`
}

// 文件信息
type FileInfo struct {
	Name        string `json:"name"`
	Size        int64  `json:"size"`
	SizeDisplay string `json:"size_display"`
	Mtime       string `json:"mtime"`
}

// 磁盘使用信息
type DiskUsageInfo struct {
	Total        uint64  `json:"total"`
	Used         uint64  `json:"used"`
	Free         uint64  `json:"free"`
	TotalDisplay string  `json:"total_display"`
	UsedDisplay  string  `json:"used_display"`
	FreeDisplay  string  `json:"free_display"`
	UsedPercent  float64 `json:"used_percent"`
}
