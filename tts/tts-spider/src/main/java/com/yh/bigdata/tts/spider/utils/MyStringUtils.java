package com.yh.bigdata.tts.spider.utils;

import org.apache.commons.lang3.StringUtils;

public class MyStringUtils extends org.apache.commons.lang3.StringUtils{
	
	public static String filter(String source) {
		if (StringUtils.isNotEmpty(source)) {
			return source
					.replaceAll("&#xECD9;", "0")
					.replaceAll("&#xEBED;", "1")
					.replaceAll("&#xE8BC;", "2")
					.replaceAll("&#xEBC0;", "3")
					.replaceAll("&#xE7A3;", "4")
					.replaceAll("&#xF4CD;", "5")
					.replaceAll("&#xECEA;", "6")
					.replaceAll("&#xEE3A;", "7")
					.replaceAll("&#xE268;", "8")
					.replaceAll("&#xE80C;", "9");
		}
		return source;
	}

}
