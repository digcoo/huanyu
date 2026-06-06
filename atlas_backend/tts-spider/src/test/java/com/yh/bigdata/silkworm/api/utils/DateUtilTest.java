package com.yh.bigdata.silkworm.api.utils;

import java.text.ParseException;

import org.junit.Test;

import com.yh.bigdata.tts.common.utils.DateUtil;

public class DateUtilTest {
	@Test
	public void parse2MonthLastDay() throws ParseException {
		System.out.println(DateUtil.parse2MonthLastDay("2020-10-18"));
	}
	
	@Test
	public void parse2QuarterLastDay() throws ParseException {
		System.out.println(DateUtil.parse2QuarterLastDay("2020-10-18"));
	}
	
	@Test
	public void hash(){
		System.out.println(new String("hello wolrd").hashCode());
	}
	
	@Test
	public void getMATime(){
		System.out.println("09:26:00\t" + DateUtil.getMATime("09:26:00", 60));
		System.out.println("09:31:00\t" + DateUtil.getMATime("09:31:00", 60));
		System.out.println("09:40:00\t" + DateUtil.getMATime("09:40:00", 60));
		System.out.println("09:55:00\t" + DateUtil.getMATime("09:55:00", 60));
		System.out.println("09:59:59\t" + DateUtil.getMATime("09:59:59", 60));
		System.out.println("10:00:00\t" + DateUtil.getMATime("10:00:00", 60));
		System.out.println("10:00:01\t" + DateUtil.getMATime("10:00:01", 60));
		System.out.println("11:29:00\t" + DateUtil.getMATime("11:29:00", 60));
		System.out.println("13:01:00\t" + DateUtil.getMATime("13:01:00", 60));
		System.out.println("13:31:00\t" + DateUtil.getMATime("13:31:00", 60));
		System.out.println("13:30:00\t" + DateUtil.getMATime("13:30:00", 60));
		System.out.println("14:35:00\t" + DateUtil.getMATime("14:35:00", 60));
		System.out.println("14:45:00\t" + DateUtil.getMATime("14:45:00", 60));
		System.out.println("15:01:00\t" + DateUtil.getMATime("15:01:00", 60));
	}
	
}

