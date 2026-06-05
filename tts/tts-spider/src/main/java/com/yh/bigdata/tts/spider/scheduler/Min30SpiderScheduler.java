package com.yh.bigdata.tts.spider.scheduler;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yh.bigdata.tts.spider.xueqiu.StockMin30XueQiuCrawler;

/**
 * @author duyp
 * 
 * @date 2021/09/24
 * 
 * @comment
 */

@Component
@EnableScheduling
public class Min30SpiderScheduler {

	Logger logger = LoggerFactory.getLogger(Min30SpiderScheduler.class);
	
	
	@Autowired
	StockMin30XueQiuCrawler stockMin30Crawler;
	
	static long startTimestamp = 0l;
	static long endTimestamp = 0l;
	static boolean ifWeekendDay = false;

    @Value("${realtime.spider.on}")
    private boolean spiderEnable = true;
	
	static {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 9);
		calendar.set(Calendar.MINUTE, 26);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		startTimestamp = calendar.getTimeInMillis();
		   
		calendar.set(Calendar.HOUR_OF_DAY, 15);
		calendar.set(Calendar.MINUTE, 1);
		endTimestamp = calendar.getTimeInMillis();

		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		ifWeekendDay = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;

	}
	
	@Scheduled(cron="${min30.spider.cron}")
	public void run() {
		try {

            if (!spiderEnable) {
                return;
            }

			long taskStart = System.currentTimeMillis();

			if (taskStart < startTimestamp
					|| taskStart > endTimestamp
					|| ifWeekendDay
					) {
				return;
			}
			
			stockMin30Crawler.setPersist(false);
			stockMin30Crawler.run(null, 8);
						
		} catch (Exception e) {
			logger.error("Min30SpiderScheduler run exception...", e);
		}
	}

	
}
