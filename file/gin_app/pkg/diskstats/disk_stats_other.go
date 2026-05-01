//go:build !windows

package diskstats

import (
	"gin_app/internal/model"
	"gin_app/internal/utils"
)

type SyscallStat struct {
	Total uint64
	Free  uint64
}

func getDiskStats(path string, stat *SyscallStat) error {
	// 非Windows平台需使用 syscall.Statfs 实现
	return nil
}

// 获取磁盘使用情况（非Windows占位实现）
func GetDiskUsage(path string) model.DiskUsageInfo {
	return model.DiskUsageInfo{
		TotalDisplay: utils.FormatFileSize(0),
		UsedDisplay:  utils.FormatFileSize(0),
		FreeDisplay:  utils.FormatFileSize(0),
	}
}
