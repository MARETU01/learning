package com.learning.algorithm.leetcode.editor.cn;

import java.util.*;

/**
 * @author luoding
 * @date 2025-09-19 17:47:08
 * @description 70.爬楼梯
 */
public class 爬楼梯 {    
    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        private List<Integer> list = new ArrayList<>();

        public int climbStairs(int n) {
            list.addAll(Arrays.asList(1, 2, 3));
            while (list.size() < n) {
                int size = list.size();
                list.add(list.get(size - 1) + list.get(size - 2));
            }
            return list.get(n - 1);
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}