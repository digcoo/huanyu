package com.yh.bigdata.tts.spider.scheduler;

import com.alibaba.fastjson.JSON;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockTarget;
import com.yh.bigdata.tts.common.utils.CommonUtil;
import com.yh.bigdata.tts.spider.service.StrategyService;
import com.yh.bigdata.tts.spider.ws.MyWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.List;

/**
 * @author duyp
 *
 * @date 2021/09/24
 *
 * @comment
 */

@Component
@EnableScheduling
@Slf4j
public class RealTimeCheckScheduler {

    @Autowired
    private MyWebSocketHandler myWebSocketHandler;

    @Autowired
    private StrategyService strategyService;

    @Value("${realtime.spider.on}")
    private boolean spiderEnable = true;

	static long startTimestamp = 0l;
	static long endTimestamp = 0l;
	static boolean ifWeekendDay = false;

    static {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 9);
		calendar.set(Calendar.MINUTE, 26);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		startTimestamp = calendar.getTimeInMillis();


		calendar.set(Calendar.HOUR_OF_DAY, 15);
		calendar.set(Calendar.MINUTE, 31);
		endTimestamp = calendar.getTimeInMillis();

		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		ifWeekendDay = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;

	}

	@PostConstruct
	public void init() {
//
//		this.oldDayRecCodeList = RealtimeStockCache.oldBidStocks.stream().map(StrategyStock::getCode).collect(Collectors.toSet());

//		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
//		executorService.scheduleAtFixedRate(() -> {
//			long taskStart = System.currentTimeMillis();
//
//			if (taskStart < startTimestamp
//					|| taskStart > endTimestamp
//					|| ifWeekendDay
//					) {
//				return;
//			}
//
//			LogUtils.log("recommendScan start.......");
//			recommendScan();
//			LogUtils.log("recommendScan end.......");
//		}, 0, 30, TimeUnit.SECONDS);

	}

//    /**
//     * 定时任务：30秒执行一次
//     * 如果策略命中，则发送消息给前端（30分钟内不要重复发送）
//     */
//    @Scheduled(cron="${realtime.check.cron}")
//    @Async
//	public void recommendScan() {
//		try {
//
//            if (!spiderEnable) {
//                return;
//            }
//
//            log.info("recommendScan...");
//            List<StockTarget> andUpdateNewTriggerStockTargets = strategyService.getAndUpdateTriggerStockTargets(StrategyTypeEnum.REALTIME_CROSS_MAX_HIGH);
//            if (andUpdateNewTriggerStockTargets.isEmpty()) {
//                return;
//            }
//
//            CommonUtil.beep();
//            for (StockTarget stockTarget : andUpdateNewTriggerStockTargets) {
//                myWebSocketHandler.broadcast(JSON.toJSONString(stockTarget));
//                log.info("send message to frontend: {} {}", stockTarget.getCode(), stockTarget.getName());
//
//            }
//        } catch (Exception e) {
//			log.error("recommendScan run exception...", e);
//		}
//	}

}
