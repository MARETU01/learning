package main

import (
	"fmt"
	"time"
)

func main() {
	start := time.Now()
	mapa := make(map[int]int)
	for i := 0; i < 10000000; i++ {
		mapa[i] = i
	}
	fmt.Println(time.Since(start))

	start = time.Now()
	mapb := make(map[int]int, 10000000)
	for i := 0; i < 10000000; i++ {
		mapb[i] = i
	}
	fmt.Println(time.Since(start))
}
