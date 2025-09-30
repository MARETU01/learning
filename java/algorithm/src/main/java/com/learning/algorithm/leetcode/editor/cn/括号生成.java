package com.learning.algorithm.leetcode.editor.cn;

import java.util.*;

/**
 * @author luoding
 * @date 2025-09-19 16:50:08
 * @description 22.括号生成
 */
public class 括号生成 {    
    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public List<String> generateParenthesis(int n) {
            List<String> result = new ArrayList<>();
            backtrack(result, "", 0, 0, n);
            return result;
        }

        private void backtrack(List<String> result, String current, int open, int close, int max) {
            if (current.length() == max * 2) {
                result.add(current);
                return;
            }
            if (open < max) {
                backtrack(result, current + "(", open + 1, close, max);
            }
            if (close < open) {
                backtrack(result, current + ")", open, close + 1, max);
            }
        }
    }
    //leetcode submit region end(Prohibit modification and deletion)

}