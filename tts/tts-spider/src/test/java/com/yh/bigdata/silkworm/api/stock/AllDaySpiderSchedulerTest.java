package com.yh.bigdata.silkworm.api.stock;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.scheduler.DayAllSpiderScheduler;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class AllDaySpiderSchedulerTest extends BaseTest{
	
	@Autowired
	DayAllSpiderScheduler dayAllSpiderScheduler;

	
	@Test
	public void spider() throws IOException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();		
		
		
		dayAllSpiderScheduler.run();

		
		System.out.println("cost : " + stopWatch.getTime() / (1000 *1000));
		
	}

	
}
