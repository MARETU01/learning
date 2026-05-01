//go:build windows

package diskstats

import (
	"os"
	"syscall"
	"unsafe"

	"gin_app/internal/model"
	"gin_app/internal/utils"
)

type SyscallStat struct {
	Total uint64
	Free  uint64
}

func getSyscallStat(path string, stat *SyscallStat) error {
	if _, err := os.Stat(path); err != nil {
		return err
	}
	return getDiskStats(path, stat)
}

func getDiskStats(path string, stat *SyscallStat) error {
	k32, err := syscall.LoadDLL("Kernel32.dll")
	if err != nil {
		return err
	}
	getDiskFreeSpaceEx, err := k32.FindProc("GetDiskFreeSpaceExW")
	if err != nil {
		return err
	}

	pathPtr, err := syscall.UTF16PtrFromString(path)
	if err != nil {
		return err
	}

	var freeBytes uint64
	var totalBytes uint64
	var totalFreeBytes uint64

	r1, _, e1 := syscall.Syscall6(
		getDiskFreeSpaceEx.Addr(),
		4,
		uintptr(unsafe.Pointer(pathPtr)),
		uintptr(unsafe.Pointer(&freeBytes)),
		uintptr(unsafe.Pointer(&totalBytes)),
		uintptr(unsafe.Pointer(&totalFreeBytes)),
		0,
		0,
	)

	if r1 == 0 {
		return e1
	}

	stat.Total = totalBytes
	stat.Free = freeBytes
	return nil
}

// 获取磁盘使用情况
func GetDiskUsage(path string) model.DiskUsageInfo {
	var stat SyscallStat
	if err := getSyscallStat(path, &stat); err != nil {
		return model.DiskUsageInfo{}
	}
	usage := model.DiskUsageInfo{
		Total:        stat.Total,
		Used:         stat.Total - stat.Free,
		Free:         stat.Free,
		TotalDisplay: utils.FormatFileSize(int64(stat.Total)),
		UsedDisplay:  utils.FormatFileSize(int64(stat.Total - stat.Free)),
		FreeDisplay:  utils.FormatFileSize(int64(stat.Free)),
	}
	if stat.Total > 0 {
		usage.UsedPercent = float64(stat.Total-stat.Free) / float64(stat.Total) * 100
	}
	return usage
}
