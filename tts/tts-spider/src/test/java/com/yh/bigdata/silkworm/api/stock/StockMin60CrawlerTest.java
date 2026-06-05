package com.yh.bigdata.silkworm.api.stock;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.crawler.StockMin60Crawler;
import com.yh.bigdata.tts.spider.xueqiu.StockMin60XueQiuCrawler;

public class StockMin60CrawlerTest extends BaseTest{

	@Autowired
	StockMin60XueQiuCrawler stockMin60Crawler;
	
	
	@Test
	public void spiderMin60() {
		stockMin60Crawler.run();
	}
}
