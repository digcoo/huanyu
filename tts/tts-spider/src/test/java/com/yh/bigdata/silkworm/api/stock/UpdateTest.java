package com.yh.bigdata.silkworm.api.stock;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.buding.StockDayUpdate;
import com.yh.bigdata.tts.spider.buding.StockMin30Update;
import com.yh.bigdata.tts.spider.buding.StockMin60Update;
import com.yh.bigdata.tts.spider.buding.StockMonthUpdate;
import com.yh.bigdata.tts.spider.buding.StockWeekUpdate;
import com.yh.bigdata.tts.spider.buding.StockYearUpdate;

public class UpdateTest extends BaseTest{
	
	@Autowired
	StockMin30Update  stockMin30Update;
	
	@Autowired
	StockMin60Update  stockMin60Update;
	
	@Autowired
	StockDayUpdate stockDayUpdate;
	
	@Autowired
	StockWeekUpdate  stockWeekUpdate;
	
	@Autowired
	StockMonthUpdate stockMonthUpdate;
	
	@Autowired
	StockYearUpdate stockYearUpdate;

	
	@Test
	public void update() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();		
		
//		stockMin30Update.run();
		stockDayUpdate.run();
		stockWeekUpdate.run();
		stockMonthUpdate.run();
		stockYearUpdate.run();
		
		System.out.println("update cost : " + stopWatch.getTime() / (1000 *1000));
		
	
	}
}
