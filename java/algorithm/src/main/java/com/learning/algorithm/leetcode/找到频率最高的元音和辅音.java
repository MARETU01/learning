package com.learning.algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author luoding
 * @date 2025-09-13 18:15:46
 * @description 3541.找到频率最高的元音和辅音
 */
public class 找到频率最高的元音和辅音 {
    public int maxFreqSum(String s) {
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            map.put(s.charAt(i), map.getOrDefault(s.charAt(i), 0) + 1);
        }

        int a = 0, b = 0;
        for (Character c : map.keySet()) {
            if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u') {
                a = Math.max(a, map.get(c));
            } else {
                b = Math.max(b, map.get(c));
            }
        }
        return a + b;
    }
}