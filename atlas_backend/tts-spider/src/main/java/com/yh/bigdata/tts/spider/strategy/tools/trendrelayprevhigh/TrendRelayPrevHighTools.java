package com.yh.bigdata.tts.spider.strategy.tools.trendrelayprevhigh;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

import com.yh.bigdata.tts.common.constants.RealtimeStockCache;

import com.yh.bigdata.tts.common.model.StockBase;

import com.yh.bigdata.tts.common.model.Trade;

import com.yh.bigdata.tts.common.param.TrendRelayPrevHighStrategyParams;

import com.yh.bigdata.tts.spider.response.CheckResult;

import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;

import com.yh.bigdata.tts.spider.strategy.tools.trendretest.TrendRetestBandSupport;

import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveCcMin60BreakoutCore;

import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;

import lombok.Getter;

import org.springframework.util.CollectionUtils;



import java.util.List;



/**

 * 末波段后未回踩 bandLow，末 K 收阳、边沿突破前一根 K 的 high，且 close≤bandHigh。

 */

public final class TrendRelayPrevHighTools {

    private static final double EPS = 1e-6;

    private TrendRelayPrevHighTools() {

    }



    @Getter

    public static final class Hit {

        private final PeriodTypeEnum period;

        private final YangBandTools.CompleteYangBand lastBand;

        private final Trade signalBar;

        private final Trade prevBar;

        private final double bandLow;

        private final double bandHigh;

        private final double prevHigh;



        Hit(PeriodTypeEnum period, YangBandTools.CompleteYangBand lastBand,

            Trade signalBar, Trade prevBar, double bandLow, double bandHigh, double prevHigh) {

            this.period = period;

            this.lastBand = lastBand;

            this.signalBar = signalBar;

            this.prevBar = prevBar;

            this.bandLow = bandLow;

            this.bandHigh = bandHigh;

            this.prevHigh = prevHigh;

        }

    }



    public static Hit findHit(StockBase stock, TrendRelayPrevHighStrategyParams params) {

        TrendRelayPrevHighStrategyParams p = params != null ? params : TrendRelayPrevHighStrategyParams.defaults();

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

        if (window == null) {

            return null;

        }

        if (TrendRetestBandSupport.hasRetestLowInWindow(trades, window)) {

            return null;

        }

        int lastIdx = window.getLastIdx();

        if (lastIdx <= 0) {

            return null;

        }

        Trade prevBar = trades.get(lastIdx - 1);

        Trade signalBar = trades.get(lastIdx);

        if (prevBar == null || prevBar.getHigh() == null) {

            return null;

        }
        if (!YangBandTools.isStrictYang(signalBar)) {
            return null;
        }

        double prevHigh = prevBar.getHigh();
        double bandHigh = window.getBandHigh();
        if (Double.isNaN(bandHigh)) {
            return null;
        }

        if (!WaveCcMin60BreakoutCore.passesBandHighEdge(prevBar, signalBar, prevHigh)) {

            return null;

        }
        if (signalBar.getClose() == null || signalBar.getClose() > bandHigh + EPS) {
            return null;
        }

        return new Hit(period, window.getLastBand(), signalBar, prevBar,

                window.getBandLow(), bandHigh, prevHigh);

    }



    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,

                                              TrendRelayPrevHighStrategyParams params) {

        TrendRelayPrevHighStrategyParams p = params != null ? params : TrendRelayPrevHighStrategyParams.defaults();

        if (p.isEnableMinAmountFilter()

                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {

            appendMessage(checkResult, PeriodTypeEnum.DAY, "成交额不足");

            return false;

        }

        return true;

    }



    public static String buildTrendMessage(Hit hit) {

        if (hit == null) {

            return "[TRPH]趋势中转破前High";

        }

        return String.format("[TRPH]趋势中转破前High|%sMACD>0,未回踩Low,末K阳,破前K high,close≤High|prevHigh=%.2f,bandHigh=%.2f",

                periodLabel(hit.getPeriod()),

                hit.getPrevHigh(),

                hit.getBandHigh());

    }



    public static String buildSignalMessage(Hit hit) {

        if (hit == null) {

            return "";

        }

        Trade signalBar = hit.getSignalBar();

        return String.format(

                "趋势中转破前High,strategyTag=TRPH,period=%s,sigDay=%s,sigClose=%.2f,prevHigh=%.2f,bandHigh=%.2f,bandLow=%.2f",

                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",

                dayOf(signalBar),

                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,

                hit.getPrevHigh(),

                hit.getBandHigh(),

                hit.getBandLow());

    }



    public static PeriodTypeEnum resolvePeriod(TrendRelayPrevHighStrategyParams.Tier tier) {

        if (tier == TrendRelayPrevHighStrategyParams.Tier.WEEK) {

            return PeriodTypeEnum.WEEK;

        }

        if (tier == TrendRelayPrevHighStrategyParams.Tier.MONTH) {

            return PeriodTypeEnum.MONTH;

        }

        return PeriodTypeEnum.DAY;

    }



    public static int resolveLookback(TrendRelayPrevHighStrategyParams params) {

        TrendRelayPrevHighStrategyParams p = params != null ? params : TrendRelayPrevHighStrategyParams.defaults();

        if (p.getTier() == TrendRelayPrevHighStrategyParams.Tier.WEEK) {

            return p.getLookbackWeek();

        }

        if (p.getTier() == TrendRelayPrevHighStrategyParams.Tier.MONTH) {

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

            checkResult.addTrendPeriod(period, "[TRPH]" + msg);

        }

    }

}

