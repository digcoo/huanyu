package com.yh.bigdata.silkworm.api.stock;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.crawler.StockMA120Calculator;

public class StockMA120CalculatorTest extends BaseTest{

	@Autowired
	StockMA120Calculator stockMA120Calculator;
	
	
	@Test
	public void spider() {
		stockMA120Calculator.run();
	}
}
