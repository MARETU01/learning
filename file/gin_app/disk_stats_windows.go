package main

import (
	"syscall"
	"unsafe"
)

type syscallStat struct {
	Total uint64
	Free  uint64
}

func getDiskStats(path string, stat *syscallStat) error {
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
