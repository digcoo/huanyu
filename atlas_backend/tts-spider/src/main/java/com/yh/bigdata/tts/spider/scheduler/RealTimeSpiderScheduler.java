package com.yh.bigdata.tts.spider.scheduler;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockFenshi;
import com.yh.bigdata.tts.spider.realtime.RealtimeKlineUpdater;
import com.yh.bigdata.tts.spider.realtime.TradingSessionUtils;
import com.yh.bigdata.tts.spider.utils.SinaHttpUtils;

/**
 * 新浪实时价 → 更新 StockBase → 内存合成 min30/min60/日/周/月/年 K。
 */
@Component
@EnableScheduling
public class RealTimeSpiderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeSpiderScheduler.class);

    private static final String STOCK_REALTIME_URL = "http://hq.sinajs.cn/list=%s";

    @Value("${realtime.spider.on:false}")
    private boolean spiderEnable;

    @Autowired
    private RealtimeKlineUpdater realtimeKlineUpdater;

    @Scheduled(cron = "${realtime.spider.cron:0/10 * 9-11,13-14 * * 1-5}")
    public void run() {
        try {
            if (!spiderEnable) {
                return;
            }
            long taskStart = System.currentTimeMillis();
            if (!TradingSessionUtils.isInTradingSession(taskStart)) {
                return;
            }

            spiderRealtime();
            logger.info("spider realtime fenshi cost : {}s", (System.currentTimeMillis() - taskStart) / 1000);
        } catch (Exception e) {
            logger.error("RealTimeSpider run exception...", e);
        }
    }

    private void spiderRealtime() throws IOException, InterruptedException {
        if (RealtimeStockCache.filterStockMap.isEmpty()) {
            return;
        }

        String time = "";
        List<List<StockBase>> stockSplits = Lists.partition(
                Lists.newArrayList(RealtimeStockCache.filterStockMap.values()), 100);

        for (List<StockBase> stocks : stockSplits) {
            try {
                List<String> codes = stocks.stream().map(StockBase::getCode).collect(Collectors.toList());
                String codestr = JSON.toJSONString(codes);
                String url = String.format(STOCK_REALTIME_URL,
                        codestr.substring(1, codestr.length() - 1).replace("\"", ""));
                String ret = Request.Get(url)
                        .addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sz002015/nc.shtml")
                        .execute().returnContent().asString();

                if (StringUtils.isBlank(ret) || "null".equals(ret)) {
                    continue;
                }

                String[] split = ret.split("\n");
                for (String line : split) {
                    StockFenshi fenshi = SinaHttpUtils.parseStockFenshi(line);
                    if (fenshi == null || fenshi.getCode() == null) {
                        continue;
                    }
                    if (fenshi.getTime() != null && fenshi.getTime().compareTo("15:00:00") >= 0) {
                        fenshi.setTime("15:00:00");
                    }
                    time = fenshi.getTime();

                    StockBase stockBase = RealtimeStockCache.filterStockMap.get(fenshi.getCode());
                    if (stockBase == null) {
                        continue;
                    }

                    stockBase.setOpen(fenshi.getOpen());
                    stockBase.setClose(fenshi.getClose());
                    stockBase.setDay(fenshi.getDay());
                    stockBase.setPrevClose(fenshi.getPrevClose());
                    stockBase.setHigh(fenshi.getHigh());
                    stockBase.setLow(fenshi.getLow());
                    stockBase.setAmount(fenshi.getAmount());
                    stockBase.setVolume(fenshi.getVolume());
                    stockBase.setIsTrade(fenshi.getOpen() != null && fenshi.getOpen() > 0.1
                            && stockBase.getName() != null && !stockBase.getName().contains("退"));
                }
            } catch (Exception e) {
                logger.error("spider realtime fenshi exception.. ", e);
            }
        }

        if (StringUtils.isBlank(time)) {
            return;
        }

        for (StockBase stockBase : RealtimeStockCache.filterStockMap.values()) {
            try {
                realtimeKlineUpdater.updateAll(stockBase, time);
            } catch (Exception e) {
                logger.error("cal realtime kline exception, code: {}", stockBase.getCode(), e);
            }
        }
    }
}
