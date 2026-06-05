package com.yh.bigdata.silkworm.api.algorithm.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;

public class SyncTest {
	
	public synchronized static void test2() {
		System.out.println("test2");
	}

	public void test5() {
		synchronized (this.getClass()) {
			System.out.println("test5");
		}
	}
	
	public synchronized void test1() {
		System.out.println("test1");
	}
	
	public void test3() {
		
		synchronized (this) {
			System.out.println("test3");
		}
	}

	public void test4() {
		Object obj = new Object();
		synchronized (obj) {
			System.out.println("test4");
		}
	}
	
	public static void main(String [] args) {
		System.out.println("hello world");
		
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("a", "a");	
		map.put("b", "b");	
		map.put("c", "c");	
		map.put("d", "d");	
		
	}
}

