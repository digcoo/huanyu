package com.yh.bigdata.silkworm.api.stock;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.crawler.StockQuarterCrawler;
import com.yh.bigdata.tts.spider.xueqiu.StockQuarterXueQiuCrawler;

public class StockQuarterCrawlerTest extends BaseTest{

	@Autowired
	StockQuarterXueQiuCrawler stockQuarterCrawler;
	
	
	@Test
	public void spiderQuarter() {
		stockQuarterCrawler.run("sz300938", 100);
	}
}
