package com.learning.algorithm.leetcode.editor.cn;

import java.util.*;

/**
 * @author luoding
 * @date 2025-09-28 15:46:17
 * @description 976.三角形的最大周长
 */
public class 三角形的最大周长 {    
    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public int largestPerimeter(int[] nums) {
            List<Integer> list = new ArrayList<>();
            for (int i : nums) {
                list.add(i);
            }
            list.sort(Collections.reverseOrder());
            for (int i = 0; i < list.size() - 2; i++) {
                if (list.get(i) < list.get(i + 1) + list.get(i + 2)) {
                    return list.get(i) + list.get(i + 1) + list.get(i + 2);
                }
            }
            return 0;
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}