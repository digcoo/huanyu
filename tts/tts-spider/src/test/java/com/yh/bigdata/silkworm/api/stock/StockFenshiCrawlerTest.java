package com.yh.bigdata.silkworm.api.stock;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.crawler.StockFenshiCrawler;

public class StockFenshiCrawlerTest extends BaseTest{

	@Autowired
	StockFenshiCrawler stockFenshiCrawler;
	
	
	@Test
	public void spiderFenshi() {
		stockFenshiCrawler.run();
	}
}
