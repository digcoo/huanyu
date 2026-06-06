package com.yh.bigdata.tts.api;

import org.junit.jupiter.api.Test;

import java.util.Calendar;


public class DateUtil {
	
	@Test
	public void testWeekDay() {
		System.out.println(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
	}
}
