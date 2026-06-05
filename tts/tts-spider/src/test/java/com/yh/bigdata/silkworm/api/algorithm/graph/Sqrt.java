package com.yh.bigdata.silkworm.api.algorithm.graph;

public class Sqrt {
	public static void main(String []args) {
		System.out.println(sqrt(4));
	}
	
	public static double sqrt(int number) {
		double low = 0, high = number;
		double mid = (low + high) / 2;
		double accuracy = 1e-5;
		while(Math.abs(mid * mid - number) > accuracy) {
			if ((mid * mid - number) > accuracy) {
				high = mid;
			}else {
				low = mid;
			}
			mid = (low + high) / 2;
		}
		return mid;
		
	}
}
