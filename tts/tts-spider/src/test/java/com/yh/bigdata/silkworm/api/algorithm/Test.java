package com.yh.bigdata.silkworm.api.algorithm;

import com.alibaba.fastjson.JSON;

public class Test {

	  public static void main(String [] args) {
		  System.out.println("this".compareToIgnoreCase("book"));
		  
	        String str = "this is my book";
	        String[] strArr = str.split(" ");
	        strArr = bubbleSort(strArr);
	        System.out.println(JSON.toJSONString(strArr));
	        
	  }
	    
	    public static String[] bubbleSort(String[] strArr ){
	    	System.out.println("===");
	        for(int i = 0; i < strArr.length; i ++){
	            for(int j = 0; j < strArr.length - i - 1; j ++){
	                if(strArr[j + 1].compareToIgnoreCase(strArr[j]) < 0){
	                    String tmp = strArr[j + 1];
	                    strArr[j + 1] = strArr[j];
	                    strArr[j] = tmp;
	                }
	            }
	        }
	        return strArr;
	    }
}
