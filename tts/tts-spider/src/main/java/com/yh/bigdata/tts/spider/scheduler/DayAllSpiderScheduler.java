package com.yh.bigdata.tts.spider.scheduler;

import com.yh.bigdata.tts.spider.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yh.bigdata.tts.spider.crawler.StockBaseCrawler;
import com.yh.bigdata.tts.spider.xueqiu.StockDayXueQiuCrawler;
import com.yh.bigdata.tts.spider.xueqiu.StockMin30XueQiuCrawler;
import com.yh.bigdata.tts.spider.xueqiu.StockMin60XueQiuCrawler;
import com.yh.bigdata.tts.spider.xueqiu.StockMonthXueQiuCrawler;
import com.yh.bigdata.tts.spider.xueqiu.StockQuarterXueQiuCrawler;
import com.yh.bigdata.tts.spider.xueqiu.StockWeekXueQiuCrawler;
import com.yh.bigdata.tts.spider.xueqiu.StockYearXueQiuCrawler;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment
 */

@Component
public class DayAllSpiderScheduler {

	Logger logger = LoggerFactory.getLogger(DayAllSpiderScheduler.class);

	@Autowired
	StockBaseCrawler stockBaseCrawler;

	@Autowired
	StockDayXueQiuCrawler stockDayCrawler;

	@Autowired
	StockWeekXueQiuCrawler stockWeekCrawler;

	@Autowired
	StockMonthXueQiuCrawler stockMonthCrawler;

	@Autowired
	StockQuarterXueQiuCrawler stockQuarterCrawler;

	@Autowired
	StockYearXueQiuCrawler stockYearCrawler;

	@Autowired
	StockMin30XueQiuCrawler stockMin30Crawler;

	@Autowired
	StockMin60XueQiuCrawler stockMin60Crawler;

    @Autowired
    StockService stockService;


//	@Scheduled(cron="${day.all.spider.cron}")
	public void run() {

		long start = System.currentTimeMillis();
		logger.info("DayAllSpiderScheduler loop start=========================================================");
		try {
//			if (!SinaHttpUtils.isTradeOfCurrentDay()) {
//				return;
//			}
			stockBaseCrawler.run();
            stockService.deleteAllMin30s();
			stockMin30Crawler.run(null, 24);
			stockDayCrawler.run(null, 40);
			stockWeekCrawler.run(null, 8);
			stockMonthCrawler.run(null, 2);
			stockQuarterCrawler.run(null, 2);
			stockYearCrawler.run(null, 2);
		} catch (Exception e) {
			logger.error("DayAllSpiderScheduler exception..... ", e);
		}
		logger.info("=========================================================DayAllSpiderScheduler loop finish({}s)",
				(System.currentTimeMillis() - start) / 1000);

		System.exit(0);
	}

}
