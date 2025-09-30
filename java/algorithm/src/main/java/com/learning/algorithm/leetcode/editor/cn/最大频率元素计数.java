package com.learning.algorithm.leetcode.editor.cn;

import java.util.*;

/**
 * @author luoding
 * @date 2025-09-22 16:04:50
 * @description 3005.最大频率元素计数
 */
public class 最大频率元素计数 {    
    //leetcode submit region begin(Prohibit modification and deletion)
class Solution {
    public int maxFrequencyElements(int[] nums) {
        Map<Integer, Integer> map = new HashMap<>();
        int maxFreq = 0, sum = 0;
        for (int num : nums) {
            int freq = map.getOrDefault(num, 0) + 1;
            map.put(num, freq);
            if (freq > maxFreq) {
                maxFreq = freq;
                sum = freq;
            } else if (freq == maxFreq) {
                sum += freq;
            }
        }
        return sum;
    }
}
//leetcode submit region end(Prohibit modification and deletion)

}