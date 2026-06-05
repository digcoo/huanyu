package com.yh.bigdata.silkworm.api.stock;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.xueqiu.StockMonthXueQiuCrawler;

public class StockMonthCrawlerTest extends BaseTest{

	@Autowired
	StockMonthXueQiuCrawler stockMonthCrawler;
	
	@Test
	public void spiderMonth() {
		stockMonthCrawler.run("sz300938", 2);
	}
}
