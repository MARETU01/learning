package com.learning.algorithm.leetcode.editor.cn;

import java.util.*;

/**
 * @author luoding
 * @date 2025-09-19 11:34:33
 * @description 3484.设计电子表格
 */
public class 设计电子表格 {    
    //leetcode submit region begin(Prohibit modification and deletion)
    class Spreadsheet {
        private List<List<Integer>> data = new ArrayList<>();

        public Spreadsheet(int rows) {
            for (int i = 0; i < rows; i++) {
                List<Integer> row = new ArrayList<>();
                for (int j = 0; j < 26; j++) {
                    row.add(0);
                }
                data.add(row);
            }
        }

        public void setCell(String cell, int value) {
            int[] pos = computeCell(cell);
            data.get(pos[0]).set(pos[1], value);
        }

        public void resetCell(String cell) {
            int[] pos = computeCell(cell);
            data.get(pos[0]).set(pos[1], 0);
        }

        public int getValue(String formula) {
            String[] tokens = formula.substring(1).split("\\+");
            int result = 0;
            for (String token : tokens) {
                if (token.isEmpty()) {
                    continue;
                }
                if (token.charAt(0) >= 'A' && token.charAt(0) <= 'Z') {
                    int[] pos = computeCell(token);
                    result += data.get(pos[0]).get(pos[1]);
                } else {
                    result += Integer.parseInt(token);
                }
            }
            return result;
        }

        private int[] computeCell(String cell) {
            int row = Integer.parseInt(cell.substring(1)) - 1;
            int col = cell.charAt(0) - 'A';
            return new int[]{row, col};
        }
    }

/**
 * Your Spreadsheet object will be instantiated and called as such:
 * Spreadsheet obj = new Spreadsheet(rows);
 * obj.setCell(cell,value);
 * obj.resetCell(cell);
 * int param_3 = obj.getValue(formula);
 */
//leetcode submit region end(Prohibit modification and deletion)

}