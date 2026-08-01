package com.yh.bigdata.tts.spider.strategy.tools.trendrelay2yang;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.TrendRelay2YangStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.trendretest.TrendRetestBandSupport;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 末波段后未回踩 bandLow，窗口内仅有末二阳（倒数第三阴），末 K bandLow&lt;close≤bandHigh。
 */
public final class TrendRelay2YangTools {

    private static final double EPS = 1e-6;

    private TrendRelay2YangTools() {
    }

    @Getter
    public static final class Hit {
        private final PeriodTypeEnum period;
        private final YangBandTools.CompleteYangBand lastBand;
        private final Trade signalBar;
        private final Trade prevBar;
        private final double bandLow;

        Hit(PeriodTypeEnum period, YangBandTools.CompleteYangBand lastBand,
            Trade signalBar, Trade prevBar, double bandLow) {
            this.period = period;
            this.lastBand = lastBand;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.bandLow = bandLow;
        }
    }

    public static Hit findHit(StockBase stock, TrendRelay2YangStrategyParams params) {
        TrendRelay2YangStrategyParams p = params != null ? params : TrendRelay2YangStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        int lookback = resolveLookback(p);
        int fetchBars = Math.max(lookback + 40, lookback + 2);
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        return findHitOnBars(trades, period, lookback);
    }

    static Hit findHitOnBars(List<Trade> trades, PeriodTypeEnum period, int lookback) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 4 || period == null || lookback < 3) {
            return null;
        }
        TrendRetestBandSupport.Window window = TrendRetestBandSupport.resolveWindow(trades, lookback);
        if (window == null) {
            return null;
        }
        if (TrendRetestBandSupport.hasRetestLowInWindow(trades, window)) {
            return null;
        }
        int lastIdx = window.getLastIdx();
        int termIdx = window.getTerminatorIdx();
        if (lastIdx < termIdx + 2) {
            return null;
        }
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        Trade thirdLastBar = trades.get(lastIdx - 2);
        if (!YangBandTools.isStrictYang(signalBar) || !YangBandTools.isStrictYang(prevBar)) {
            return null;
        }
        if (!YangBandTools.isStrictYin(thirdLastBar)) {
            return null;
        }
        for (int i = termIdx + 1; i <= lastIdx - 3; i++) {
            Trade bar = trades.get(i);
            if (YangBandTools.isStrictYang(bar)) {
                return null;
            }
        }
        if (signalBar.getClose() == null || signalBar.getClose() <= window.getBandLow() + EPS) {
            return null;
        }
        double bandHigh = window.getBandHigh();
        if (Double.isNaN(bandHigh) || signalBar.getClose() > bandHigh + EPS) {
            return null;
        }
        return new Hit(period, window.getLastBand(), signalBar, prevBar, window.getBandLow());
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              TrendRelay2YangStrategyParams params) {
        TrendRelay2YangStrategyParams p = params != null ? params : TrendRelay2YangStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, "成交额不足");
            return false;
        }
        return true;
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[TR2Y]趋势中转二阳";
        }
        return String.format("[TR2Y]趋势中转二阳|%sMACD>0,未回踩Low,仅末二阳(第三阴),close≤High|bandLow=%.2f,bandHigh=%.2f",
                periodLabel(hit.getPeriod()),
                hit.getBandLow(),
                hit.getLastBand() != null ? hit.getLastBand().getBandHigh() : Double.NaN);
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        return String.format(
                "趋势中转二阳,strategyTag=TR2Y,period=%s,sigDay=%s,sigClose=%.2f,bandLow=%.2f,bandHigh=%.2f",
                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                hit.getBandLow(),
                hit.getLastBand() != null ? hit.getLastBand().getBandHigh() : 0);
    }

    public static PeriodTypeEnum resolvePeriod(TrendRelay2YangStrategyParams.Tier tier) {
        if (tier == TrendRelay2YangStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == TrendRelay2YangStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(TrendRelay2YangStrategyParams params) {
        TrendRelay2YangStrategyParams p = params != null ? params : TrendRelay2YangStrategyParams.defaults();
        if (p.getTier() == TrendRelay2YangStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == TrendRelay2YangStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        return p.getLookbackDay();
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MONTH) {
            return "月";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周";
        }
        return "日";
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[TR2Y]" + msg);
        }
    }
}
