package com.learning.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luoding
 * @date 2025-09-12
 * @description 234.回文链表
 */
public class 回文链表 {
    private static class ListNode {
        int val;
        ListNode next;
        ListNode(int x) {
            val = x;
            next = null;
        }
    }

    public boolean isPalindrome(ListNode head) {
        List<Integer> lst = new ArrayList<>();
        while (head != null) {
            lst.add(head.val);
            head = head.next;
        }
        int left = 0, right = lst.size() - 1;
        while (left < right) {
            if (!lst.get(left).equals(lst.get(right))) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }
}
