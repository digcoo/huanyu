package com.yh.bigdata.silkworm.api.algorithm.graph;

/**
 * 最大字串的和
 * @author junifer
 *
 */
public class SubArraySum {
	
	public static int max(int a, int b) {
		return a > b? a : b;
	}
	
	public static void main(String [] args) {
		System.out.println("hello world");
		int nums[] = {10, -1, 3, -11, -20, 33, 1, -6, 13};
		int maxSum = nums[0];
		int start = nums[0];
		for (int i = 1; i < nums.length; ++ i) {
			start = max(nums[i], start + nums[i]);
			maxSum = max(maxSum, start);
		}
		
		System.out.println(maxSum);
	}
}

