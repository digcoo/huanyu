package com.yh.bigdata.silkworm.api.stock;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.crawler.StockFinanceCrawler;

public class StockFinanceCrawlerTest extends BaseTest{

	@Autowired
	StockFinanceCrawler stockFinanceCrawler;
	
	
	@Test
	public void spiderStockFinance() {
		stockFinanceCrawler.run();
	}
}
