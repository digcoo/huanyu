package com.binance.client.examples.tasks;


import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.enums.SideTypeEnum;
import com.binance.client.examples.constants.GlobalConstants;
import com.binance.client.examples.constants.SymbolCacheData;
import com.binance.client.model.market.Candlestick;
import com.binance.client.strategy.StrategyCheckResult;
import com.binance.client.strategy.ma4m.HourMaBear4mTools;
import com.binance.client.strategy.ma4m.HourMaBull4mTools;
import com.binance.client.utils.MessageSenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1小时 MA 多头4M突破 / 空头4M跌破策略
 */
@Slf4j
public class StrategyCheckTask extends Thread {

    private static final String LINK_PREFIX = "https://www.binance.com/zh-CN/futures/";

    private final SymbolCacheData symbolData;

    public StrategyCheckTask(SymbolCacheData symbolData) {
        this.symbolData = symbolData;
    }

    @Override
    public void run() {
        log.info("开始执行1小时MA多头4M突破/空头4M跌破策略");
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            try {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                List<StrategyCheckResult> results = new ArrayList<>();
                for (String symbol : symbolData.getSymbols()) {
                    try {
                        results.addAll(checkSymbol(symbol));
                    } catch (Exception ex) {
                        log.error("[StrategyCheckTask] exception, symbol = {}", symbol, ex);
                    }
                }

                AtomicInteger newBidCnt = new AtomicInteger(0);
                for (StrategyCheckResult result : results) {
                    try {
                        if (result.isHit()) {
                            if (!symbolData.containsRecommend(result.getOpPeriodType(), result.getSideType(), result.getSymbol())) {
                                MessageSenderUtil.sendAsync(result.getSymbol() + ":" + result.getTrendMessage()
                                        + "  " + result.getSignalMessage() + "：" + result.getClose());

                                String changeRateMessage = result.getChangeRate()
                                        .multiply(new BigDecimal(100))
                                        .setScale(3, RoundingMode.HALF_DOWN) + "%";
                                log.warn("【【{}-{}】】 -【趋势: {}】-【策略: {}】-【价格: {}-({})】 {}\n",
                                        result.getSymbol(),
                                        result.getSideType().getDesc(),
                                        result.getTrendMessage(),
                                        result.getSignalMessage(),
                                        result.getClose(),
                                        changeRateMessage,
                                        LINK_PREFIX + result.getSymbol());
                                symbolData.addRecommend(result.getOpPeriodType(), result.getSideType(), result.getSymbol());
                                newBidCnt.incrementAndGet();
                            }
                        } else if (symbolData.removeRecommend(GlobalConstants.OP_PERIOD, result.getSideType(), result.getSymbol())) {
                            log.warn("【【移除-{}】】: {}", result.getSideType().getDesc(), result.getSymbol());
                        }
                    } catch (Exception exception) {
                        log.error("[StrategyCheckTask] notify failed, symbol={}", result.getSymbol(), exception);
                    }
                }

                if (newBidCnt.get() > 0) {
                    log.info("[StrategyCheckTask]总耗时[{}]秒, bid:{}", stopWatch.getTime() / 1000, newBidCnt.get());
                }
            } catch (Throwable ex) {
                log.error("策略执行异常", ex);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private List<StrategyCheckResult> checkSymbol(String symbol) {
        StrategyCheckResult longResult = new StrategyCheckResult(symbol, SideTypeEnum.LONG);
        StrategyCheckResult shortResult = new StrategyCheckResult(symbol, SideTypeEnum.SHORT);

        ConcurrentHashMap<PeriodTypeEnum, List<Candlestick>> data = symbolData.getData(symbol);
        if (data == null) {
            return Arrays.asList(longResult, shortResult);
        }

        List<Candlestick> hour1Bars = data.get(PeriodTypeEnum.HOUR1);
        if (hour1Bars == null || hour1Bars.size() < 45) {
            return Arrays.asList(longResult, shortResult);
        }

        Candlestick lastBar = hour1Bars.get(hour1Bars.size() - 1);

        HourMaBull4mTools.Hit longHit = HourMaBull4mTools.findHit(hour1Bars);
        if (longHit != null) {
            longResult.setHit(true);
            longResult.setClose(lastBar.getClose());
            longResult.setChangeRate(lastBar.getChangeRate());
            longResult.setLongMaHit(longHit);
            longResult.setTrendMessage(HourMaBull4mTools.buildTrendMessage(longHit));
            longResult.setSignalMessage(HourMaBull4mTools.buildSignalMessage(longHit));
        }

        HourMaBear4mTools.Hit shortHit = HourMaBear4mTools.findHit(hour1Bars);
        if (shortHit != null) {
            shortResult.setHit(true);
            shortResult.setClose(lastBar.getClose());
            shortResult.setChangeRate(lastBar.getChangeRate());
            shortResult.setShortMaHit(shortHit);
            shortResult.setTrendMessage(HourMaBear4mTools.buildTrendMessage(shortHit));
            shortResult.setSignalMessage(HourMaBear4mTools.buildSignalMessage(shortHit));
        }

        return Arrays.asList(longResult, shortResult);
    }
}
