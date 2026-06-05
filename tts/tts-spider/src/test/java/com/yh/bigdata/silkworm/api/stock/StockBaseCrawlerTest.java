package com.yh.bigdata.silkworm.api.stock;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.crawler.StockBaseCrawler;

public class StockBaseCrawlerTest extends BaseTest{

	@Autowired
	StockBaseCrawler stockBaseCrawler;
	
	
	@Test
	public void spiderStockBase() {
		stockBaseCrawler.run();
	}
}
