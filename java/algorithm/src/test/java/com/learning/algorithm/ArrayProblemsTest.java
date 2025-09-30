package com.learning.algorithm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayProblemsTest {

    private final ArrayProblems arrayProblems = new ArrayProblems();

    @Test
    void testTwoSum() {
        // TODO: 添加两数之和测试用例
        int[] nums = {2, 7, 11, 15};
        int target = 9;
        int[] result = arrayProblems.twoSum(nums, target);
        // assertArrayEquals(new int[]{0, 1}, result);
    }

    @Test
    void testMaxSubArray() {
        // TODO: 添加最大子数组和测试用例
        int[] nums = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        int result = arrayProblems.maxSubArray(nums);
        // assertEquals(6, result);
    }
}