package com.learning.algorithm.leetcode.editor.cn;

import java.util.*;

/**
 * @author luoding
 * @date 2025-09-15 16:21:46
 * @description 1935.可以输入的最大单词数
 */
public class 可以输入的最大单词数 {
    //leetcode submit region begin(Prohibit modification and deletion)
    class Solution {
        public int canBeTypedWords(String text, String brokenLetters) {
            String[] words = text.split(" ");
            Set<Character> brokenSet = new HashSet<>();
            for (int i = 0; i < brokenLetters.length(); i++) {
                brokenSet.add(brokenLetters.charAt(i));
            }
            int count = 0;
            for (String word : words) {
                boolean canType = true;
                for (int i = 0; i < word.length(); i++) {
                    if (brokenSet.contains(word.charAt(i))) {
                        canType = false;
                        break;
                    }
                }
                count += canType ? 1 : 0;
            }
            return count;
        }
    }
//leetcode submit region end(Prohibit modification and deletion)

}