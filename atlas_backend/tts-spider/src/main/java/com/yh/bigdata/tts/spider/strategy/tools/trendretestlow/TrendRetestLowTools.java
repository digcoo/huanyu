package com.yh.bigdata.tts.spider.strategy.tools.trendretestlow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.TrendRetestLowStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.trendretest.TrendRetestBandSupport;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 末波段完结阴 K 之后至末 K（含）：存在 low≤bandLow；末 K 收阳且 bandLow&lt;close≤bandHigh。
 */
public final class TrendRetestLowTools {

    private static final double EPS = 1e-6;

    private TrendRetestLowTools() {
    }

    @Getter
    public static final class Hit {
        private final PeriodTypeEnum period;
        private final YangBandTools.CompleteYangBand lastBand;
        private final Trade signalBar;
        private final double bandLow;

        Hit(PeriodTypeEnum period, YangBandTools.CompleteYangBand lastBand,
            Trade signalBar, double bandLow) {
            this.period = period;
            this.lastBand = lastBand;
            this.signalBar = signalBar;
            this.bandLow = bandLow;
        }
    }

    public static Hit findHit(StockBase stock, TrendRetestLowStrategyParams params) {
        TrendRetestLowStrategyParams p = params != null ? params : TrendRetestLowStrategyParams.defaults();
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
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3 || period == null || lookback < 3) {
            return null;
        }
        TrendRetestBandSupport.Window window = TrendRetestBandSupport.resolveWindow(trades, lookback);
        if (window == null || !TrendRetestBandSupport.hasRetestLowInWindow(trades, window)) {
            return null;
        }
        Trade signalBar = trades.get(window.getLastIdx());
        if (!YangBandTools.isStrictYang(signalBar)) {
            return null;
        }
        if (signalBar.getClose() == null || signalBar.getClose() <= window.getBandLow() + EPS) {
            return null;
        }
        double bandHigh = window.getBandHigh();
        if (Double.isNaN(bandHigh) || signalBar.getClose() > bandHigh + EPS) {
            return null;
        }
        return new Hit(period, window.getLastBand(), signalBar, window.getBandLow());
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              TrendRetestLowStrategyParams params) {
        TrendRetestLowStrategyParams p = params != null ? params : TrendRetestLowStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, "成交额不足");
            return false;
        }
        return true;
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[TRL]趋势回踩破Low";
        }
        return String.format("[TRL]趋势回踩破Low|%sMACD>0,回踩Low,末K收阳,close≤High|bandLow=%.2f,bandHigh=%.2f",
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
                "趋势回踩破Low,strategyTag=TRL,period=%s,sigDay=%s,sigClose=%.2f,bandLow=%.2f,bandHigh=%.2f",
                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                hit.getBandLow(),
                hit.getLastBand() != null ? hit.getLastBand().getBandHigh() : 0);
    }

    public static PeriodTypeEnum resolvePeriod(TrendRetestLowStrategyParams.Tier tier) {
        if (tier == TrendRetestLowStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == TrendRetestLowStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(TrendRetestLowStrategyParams params) {
        TrendRetestLowStrategyParams p = params != null ? params : TrendRetestLowStrategyParams.defaults();
        if (p.getTier() == TrendRetestLowStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == TrendRetestLowStrategyParams.Tier.MONTH) {
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
            checkResult.addTrendPeriod(period, "[TRL]" + msg);
        }
    }
}
