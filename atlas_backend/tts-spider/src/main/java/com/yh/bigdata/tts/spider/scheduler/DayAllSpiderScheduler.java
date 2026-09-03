package com.yh.bigdata.tts.spider.scheduler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	StockMin30XueQiuCrawler stockMin30Crawler;

	@Autowired
	StockMin60XueQiuCrawler stockMin60Crawler;

	@Value("${spider.min30.default-count:16}")
	private int min30DefaultCount;

	@Autowired
	StockWeekXueQiuCrawler stockWeekCrawler;

	@Autowired
	StockMonthXueQiuCrawler stockMonthCrawler;

	@Autowired
	StockQuarterXueQiuCrawler stockQuarterCrawler;

	@Autowired
	StockYearXueQiuCrawler stockYearCrawler;

//	@Scheduled(cron="${day.all.spider.cron}")
	public void run() {

		long start = System.currentTimeMillis();
		logger.info("DayAllSpiderScheduler loop start=========================================================");
		try {
//			if (!SinaHttpUtils.isTradeOfCurrentDay()) {
//				return;
//			}
            String code = "sz002768";
            int days = 5;
            if (StringUtils.isEmpty(code)) {
                stockBaseCrawler.run();
                stockDayCrawler.run(null, days);
                stockMin30Crawler.run(null, days * 8);
//                stockMin60Crawler.run(null, days * 4);
                stockWeekCrawler.run(null, days / 5 + 1);
                stockMonthCrawler.run(null, 2);
                stockQuarterCrawler.run(null, 2);
                stockYearCrawler.run(null, 2);
            }else {
                stockDayCrawler.run(code, 100);
                stockMin30Crawler.run(code, 100);
//                stockMin60Crawler.run(code, 100);
                stockWeekCrawler.run(code, 100);
                stockMonthCrawler.run(code, 100);
                stockQuarterCrawler.run(code, 100);
                stockYearCrawler.run(code, 100);
            }
		} catch (Exception e) {
			logger.error("DayAllSpiderScheduler exception..... ", e);
		}
		logger.info("=========================================================DayAllSpiderScheduler loop finish({}s)",
				(System.currentTimeMillis() - start) / 1000);

		System.exit(0);
	}

}
