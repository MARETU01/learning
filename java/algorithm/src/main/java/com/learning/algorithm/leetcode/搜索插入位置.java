package com.learning.algorithm.leetcode;

import java.util.Arrays;

/**
 * @author luoding
 * @date 2025-09-18 18:37:45
 * @description 35.搜索插入位置
 */
public class 搜索插入位置 {
    public int searchInsert(int[] nums, int target) {
        int result = Arrays.binarySearch(nums, target);
        if (result < 0) {
            return -(result + 1);
        }
        return result;
    }
}