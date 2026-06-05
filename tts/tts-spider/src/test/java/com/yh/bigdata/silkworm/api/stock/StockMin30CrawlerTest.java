package com.yh.bigdata.silkworm.api.stock;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.crawler.StockMin30Crawler;
import com.yh.bigdata.tts.spider.xueqiu.StockMin30XueQiuCrawler;

public class StockMin30CrawlerTest extends BaseTest{

	@Autowired
	StockMin30XueQiuCrawler stockMin30Crawler;
	
	
	@Test
	public void spiderMin30() {
		stockMin30Crawler.run("sz000063", 10);
	}
}
