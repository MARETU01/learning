package com.learning.algorithm.leetcode;

import java.util.HashSet;
import java.util.Set;

/**
 * @author luoding
 * @date 2025-09-12
 * @description 160.相交链表
 */
public class 相交链表 {
    private static class ListNode {
        int val;
        ListNode next;
        ListNode(int x) {
            val = x;
            next = null;
        }
    }

    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        Set<ListNode> nodes = new HashSet<>();
        while (headA != null) {
            nodes.add(headA);
            headA = headA.next;
        }
        while (headB != null) {
            if (nodes.contains(headB)) {
                return headB;
            }
            headB = headB.next;
        }
        return null;
    }
}
