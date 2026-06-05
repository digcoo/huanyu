package com.yh.bigdata.silkworm.api.utils;

import java.util.Comparator;
import java.util.TreeSet;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;

public class CollectionTest {
	
	@Test
	public void sortSetTest() {
		
		
		Double ma5 = 5.438;
		Double ma10 = 5.427;
		Double ma20 = 5.43;
		Double ma30 = 5.418;
		System.out.println("=====");
		System.out.println("=====");
		TreeSet<Double> mas = Sets.newTreeSet(new Comparator<Double>() {		//递减排序
			@Override
			public int compare(Double o1, Double o2) {
				return (int)((o2 - o1) * 1000);
			}
		});
		
		if (ma5 != null) {
			mas.add(ma5);
		}
		if (ma10 != null) {
			mas.add(ma10);
		}
		if (ma20 != null) {
			mas.add(ma20);
		}
		if (ma30 != null) {
			mas.add(ma30);
		}
		
		System.out.println(JSON.toJSONString(mas));		
	}


}
