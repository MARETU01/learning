package com.learning.algorithm.leetcode.editor.cn;

import java.util.*;

/**
 * @author luoding
 * @date 2025-09-22 18:27:11
 * @description 1380.矩阵中的幸运数
 */
public class 矩阵中的幸运数 {    
    //leetcode submit region begin(Prohibit modification and deletion)
class Solution {
    public List<Integer> luckyNumbers(int[][] matrix) {
        int[][] minRow = new int[matrix.length][2];
        int[][] maxCol = new int[matrix[0].length][2];
        for (int i = 0; i < matrix.length; i++) {
            int minValue = Integer.MAX_VALUE;
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] < minValue) {
                    minValue = matrix[i][j];
                    minRow[i][0] = minValue;
                    minRow[i][1] = j;
                }
            }
        }
        for (int j = 0; j < matrix[0].length; j++) {
            int maxValue = Integer.MIN_VALUE;
            for (int i = 0; i < matrix.length; i++) {
                if (matrix[i][j] > maxValue) {
                    maxValue = matrix[i][j];
                    maxCol[j][0] = maxValue;
                    maxCol[j][1] = i;
                }
            }
        }
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (minRow[i][0] == maxCol[j][0] && minRow[i][1] == j && maxCol[j][1] == i) {
                    result.add(minRow[i][0]);
                }
            }
        }
        return result;
    }
}
//leetcode submit region end(Prohibit modification and deletion)

}