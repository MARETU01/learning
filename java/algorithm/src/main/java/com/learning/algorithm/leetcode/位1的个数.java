package com.learning.algorithm.leetcode;

/**
 * @author luoding
 * @date 2025-09-11
 * @description 191.位1的个数
 */
public class 位1的个数 {
    public int hammingWeight(int n) {
        return Integer.toBinaryString(n).replace("0", "").length();
    }
}
