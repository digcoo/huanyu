package com.yh.bigdata.silkworm.api.stock;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.xueqiu.StockYearXueQiuCrawler;

public class StockYearCrawlerTest extends BaseTest{

	@Autowired
	StockYearXueQiuCrawler stockYearCrawler;
	
	
	@Test
	public void spiderYear() {
		stockYearCrawler.run("sz300938", 2);
	}
}
