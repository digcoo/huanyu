package com.yh.bigdata.silkworm.api.utils;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import com.yh.bigdata.tts.common.utils.DateUtil;

public class DateUtilTest {
	@Test
	public void parse2MonthLastDay() throws ParseException {
		System.out.println(DateUtil.parse2MonthLastDay("2020-10-18"));
	}
	
	@Test
	public void parse2QuarterLastDay() throws ParseException {
		Assert.assertEquals("2026-03-31", DateUtil.parse2QuarterLastDay("2026-01-31"));
		Assert.assertEquals("2026-03-31", DateUtil.parse2QuarterLastDay("2026-02-28"));
		Assert.assertEquals("2026-06-30", DateUtil.parse2QuarterLastDay("2026-05-31"));
		Assert.assertEquals("2026-09-30", DateUtil.parse2QuarterLastDay("2026-07-31"));
		Assert.assertEquals("2026-09-30", DateUtil.parse2QuarterLastDay("2026-08-21 15:00:00"));
		Assert.assertEquals("2026-09-30", DateUtil.parse2QuarterLastDay("2026-08-31"));
		Assert.assertEquals("2026-09-30", DateUtil.parse2QuarterLastDay("2026-09-30"));
		Assert.assertEquals("2026-12-31", DateUtil.parse2QuarterLastDay("2026-10-31"));
		Assert.assertEquals("2020-12-31", DateUtil.parse2QuarterLastDay("2020-10-18"));
	}

	@Test
	public void isSameQuarterUsesZeroBasedCalendarMonth() {
		Assert.assertTrue(DateUtil.isSameQuarter("2026-07-31", "2026-08-21"));
		Assert.assertTrue(DateUtil.isSameQuarter("2026-01-15", "2026-03-31"));
		Assert.assertFalse(DateUtil.isSameQuarter("2026-01-15", "2026-04-01"));
		Assert.assertFalse(DateUtil.isSameQuarter("2026-09-30", "2026-10-31"));
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

