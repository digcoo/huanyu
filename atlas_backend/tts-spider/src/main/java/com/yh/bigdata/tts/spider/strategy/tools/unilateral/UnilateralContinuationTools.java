package com.yh.bigdata.tts.spider.strategy.tools.unilateral;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.indicator.MAIndicatorUtils;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.UnilateralStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.IndicatorCommonUtils;

import java.util.List;

/**
 * 单边趋势 v1.0 · 模式 B（趋势延续，主路径）
 */
public final class UnilateralContinuationTools {

    private static final PeriodTypeEnum WEEK = PeriodTypeEnum.WEEK;
    private static final PeriodTypeEnum MONTH = PeriodTypeEnum.MONTH;
    private static final PeriodTypeEnum DAY = PeriodTypeEnum.DAY;

    private UnilateralContinuationTools() {
    }

    public static ContinuationContext evaluateContext(StockBase stock) {
        ContinuationContext ctx = new ContinuationContext();
        ctx.weekOverMa20 = hit(IndicatorCommonUtils.checkOverMA20(stock, WEEK));
        ctx.weekMa5OverMa20 = hit(IndicatorCommonUtils.checkMA5OverMA20(stock, WEEK));
        ctx.weekOverLastLow = hit(IndicatorCommonUtils.checkOverLastLow(stock, WEEK));
        ctx.monthOverMa20 = hit(IndicatorCommonUtils.checkOverMA20(stock, MONTH));
        ctx.monthDirectionOk = checkMonthDirectionOk(stock);
        return ctx;
    }

    public static ContinuationTrigger evaluateTrigger(StockBase stock, UnilateralStrategyParams params) {
        UnilateralStrategyParams p = params != null ? params : UnilateralStrategyParams.defaults();
        ContinuationTrigger trigger = new ContinuationTrigger();
        trigger.platformBreakout = checkDayPlatformBreakout(stock, p.getDayPlatformLookback());
        trigger.ma5Reclaim = checkDayMa5Reclaim(stock, p.getStrongYangRate());
        trigger.macdOk = hit(IndicatorCommonUtils.checkCrossGoldMACD(stock, DAY))
                || hit(IndicatorCommonUtils.checkOverMACD(stock, DAY));
        return trigger;
    }

    public static boolean isFullModeB(ContinuationContext ctx, ContinuationTrigger trigger,
                                      UnilateralStrategyParams params) {
        UnilateralStrategyParams p = params != null ? params : UnilateralStrategyParams.defaults();
        return ctx.weekContextCount() >= p.getWeekContextMin() && trigger.triggerCount() >= 1;
    }

    public static boolean isWeakModeB(ContinuationContext ctx, ContinuationTrigger trigger,
                                      UnilateralStrategyParams params) {
        UnilateralStrategyParams p = params != null ? params : UnilateralStrategyParams.defaults();
        return ctx.weekContextCount() >= p.getWeekContextMin() && trigger.triggerCount() == 0;
    }

    private static boolean checkMonthDirectionOk(StockBase stock) {
        Trade m0 = RealtimeStockCache.getLastTrade(stock, MONTH, 0);
        Trade m1 = RealtimeStockCache.getLastTrade(stock, MONTH, 1);
        if (m0 == null || m1 == null || m0.getClose() == null || m1.getLow() == null) {
            return false;
        }
        boolean nonYin = m0.getOpen() != null && m0.getClose() > m0.getOpen();
        boolean aboveLastLow = m0.getClose() >= m1.getLow();
        return nonYin || aboveLastLow;
    }

    private static boolean checkDayPlatformBreakout(StockBase stock, int lookback) {
        Trade d0 = RealtimeStockCache.getLastTrade(stock, DAY, 0);
        if (d0 == null || d0.getClose() == null || d0.getOpen() == null) {
            return false;
        }
        if (d0.getClose() <= d0.getOpen()) {
            return false;
        }
        List<Trade> days = RealtimeStockCache.getLastTrades(stock, DAY, lookback + 1);
        if (days == null || days.size() < 3) {
            return false;
        }
        double platformHigh = Double.NEGATIVE_INFINITY;
        for (int i = 1; i < days.size() && i <= lookback; i++) {
            Trade t = days.get(i);
            if (t.getHigh() != null) {
                platformHigh = Math.max(platformHigh, t.getHigh());
            }
        }
        return platformHigh > 0 && d0.getClose() > platformHigh;
    }

    private static boolean checkDayMa5Reclaim(StockBase stock, double strongYangRate) {
        Trade d0 = RealtimeStockCache.getLastTrade(stock, DAY, 0);
        Trade d1 = RealtimeStockCache.getLastTrade(stock, DAY, 1);
        if (d0 == null || d1 == null || d0.getClose() == null) {
            return false;
        }
        List<Trade> days = RealtimeStockCache.getLastTrades(stock, DAY, 30);
        if (days == null || days.size() < 6) {
            return false;
        }
        double ma5 = MAIndicatorUtils.calLatestMA(days, 5);
        if (d0.getClose() <= ma5) {
            return false;
        }
        Double prevRate = d1.getShitiRate();
        return prevRate == null || prevRate <= strongYangRate;
    }

    private static boolean hit(CheckResponse response) {
        return response != null && response.isSuccess();
    }

    public static final class ContinuationContext {
        public boolean weekOverMa20;
        public boolean weekMa5OverMa20;
        public boolean weekOverLastLow;
        public boolean monthOverMa20;
        public boolean monthDirectionOk;

        public int weekContextCount() {
            int n = 0;
            if (weekOverMa20) n++;
            if (weekMa5OverMa20) n++;
            if (weekOverLastLow) n++;
            return n;
        }

        public int monthBonusCount() {
            int n = 0;
            if (monthOverMa20) n++;
            if (monthDirectionOk) n++;
            return n;
        }
    }

    public static final class ContinuationTrigger {
        public boolean platformBreakout;
        public boolean ma5Reclaim;
        public boolean macdOk;

        public int triggerCount() {
            int n = 0;
            if (platformBreakout) n++;
            if (ma5Reclaim) n++;
            if (macdOk) n++;
            return n;
        }
    }
}
