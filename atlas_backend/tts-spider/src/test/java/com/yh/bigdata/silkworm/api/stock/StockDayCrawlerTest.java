package com.yh.bigdata.silkworm.api.stock;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.yh.bigdata.silkworm.api.BaseTest;
import com.yh.bigdata.tts.spider.xueqiu.StockDayXueQiuCrawler;

public class StockDayCrawlerTest extends BaseTest {

	@Autowired
	StockDayXueQiuCrawler stockDayCrawler;

	@Test
	public void spiderDay() {
		stockDayCrawler.run("sh600519", 1000);
	}
}
