package com.yh.bigdata.silkworm.api.stock;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.scheduler.StockTargetScheduler;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;


public class StockTargetScanTest extends BaseTest{
	
	@Autowired
    StockTargetScheduler stockTargetScheduler;
	
	
	@Test
	public void recommendSave() {
        stockTargetScheduler.recommendSave();
	}
}
