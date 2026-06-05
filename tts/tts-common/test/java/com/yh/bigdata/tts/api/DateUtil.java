package com.yh.bigdata.tts.api;

import java.util.Calendar;

import org.junit.Test;

public class DateUtil {
	
	@Test
	public void testWeekDay() {
		System.out.println(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
	}
}
