package com.yh.bigdata.silkworm.api.stock;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.xueqiu.StockWeekXueQiuCrawler;

public class StockWeekCrawlerTest extends BaseTest{

	@Autowired
	StockWeekXueQiuCrawler stockWeekCrawler;
	
	
	@Test
	public void spiderWeek() {
		stockWeekCrawler.run("sz300938", 2);
	}
}
