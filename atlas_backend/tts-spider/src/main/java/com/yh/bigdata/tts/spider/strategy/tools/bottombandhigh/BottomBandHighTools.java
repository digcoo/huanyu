package com.yh.bigdata.tts.spider.strategy.tools.bottombandhigh;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

import com.yh.bigdata.tts.common.constants.RealtimeStockCache;

import com.yh.bigdata.tts.common.model.StockBase;

import com.yh.bigdata.tts.common.model.Trade;

import com.yh.bigdata.tts.common.param.BottomBandHighStrategyParams;

import com.yh.bigdata.tts.spider.response.CheckResult;

import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;

import com.yh.bigdata.tts.spider.strategy.tools.trendretest.TrendRetestBandSupport;

import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveCcMin60BreakoutCore;

import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;

import lombok.Getter;

import org.springframework.util.CollectionUtils;



import java.util.List;



/**

 * 末 K 首次边沿突破末波段 bandHigh（不要求回踩/未回踩窗口）。

 */

public final class BottomBandHighTools {



    private static final double EPS = 1e-6;



    private BottomBandHighTools() {

    }



    @Getter

    public static final class Hit {

        private final PeriodTypeEnum period;

        private final YangBandTools.CompleteYangBand lastBand;

        private final Trade signalBar;

        private final Trade prevBar;

        private final double bandLow;

        private final double bandHigh;



        Hit(PeriodTypeEnum period, YangBandTools.CompleteYangBand lastBand,

            Trade signalBar, Trade prevBar, double bandLow, double bandHigh) {

            this.period = period;

            this.lastBand = lastBand;

            this.signalBar = signalBar;

            this.prevBar = prevBar;

            this.bandLow = bandLow;

            this.bandHigh = bandHigh;

        }

    }



    public static Hit findHit(StockBase stock, BottomBandHighStrategyParams params) {

        BottomBandHighStrategyParams p = params != null ? params : BottomBandHighStrategyParams.defaults();

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

        int lastIdx = window.getLastIdx();

        if (lastIdx <= 0) {

            return null;

        }

        double bandHigh = window.getBandHigh();

        if (Double.isNaN(bandHigh)) {

            return null;

        }

        for (int i = window.getTerminatorIdx() + 1; i < lastIdx; i++) {

            Trade bar = trades.get(i);

            if (bar != null && bar.getClose() != null && bar.getClose() > bandHigh + EPS) {

                return null;

            }

        }

        Trade prevBar = trades.get(lastIdx - 1);

        Trade signalBar = trades.get(lastIdx);

        if (!WaveCcMin60BreakoutCore.passesBandHighEdge(prevBar, signalBar, bandHigh)) {

            return null;

        }

        return new Hit(period, window.getLastBand(), signalBar, prevBar,

                window.getBandLow(), bandHigh);

    }



    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,

                                              BottomBandHighStrategyParams params) {

        BottomBandHighStrategyParams p = params != null ? params : BottomBandHighStrategyParams.defaults();

        if (p.isEnableMinAmountFilter()

                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {

            appendMessage(checkResult, PeriodTypeEnum.DAY, "成交额不足");

            return false;

        }

        return true;

    }



    public static String buildTrendMessage(Hit hit) {

        if (hit == null) {

            return "[BBH]底部波段突破";

        }

        return String.format("[BBH]底部波段突破|%sMACD<0,父级MACD>0,末K首次破bandHigh|bandHigh=%.2f",

                periodLabel(hit.getPeriod()),

                hit.getBandHigh());

    }



    public static String buildSignalMessage(Hit hit) {

        if (hit == null) {

            return "";

        }

        Trade signalBar = hit.getSignalBar();

        return String.format(

                "底部波段突破,strategyTag=BBH,period=%s,sigDay=%s,sigClose=%.2f,bandHigh=%.2f,bandLow=%.2f",

                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",

                dayOf(signalBar),

                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,

                hit.getBandHigh(),

                hit.getBandLow());

    }



    public static PeriodTypeEnum resolvePeriod(BottomBandHighStrategyParams.Tier tier) {

        if (tier == BottomBandHighStrategyParams.Tier.WEEK) {

            return PeriodTypeEnum.WEEK;

        }

        if (tier == BottomBandHighStrategyParams.Tier.MONTH) {

            return PeriodTypeEnum.MONTH;

        }

        return PeriodTypeEnum.DAY;

    }



    public static int resolveLookback(BottomBandHighStrategyParams params) {

        BottomBandHighStrategyParams p = params != null ? params : BottomBandHighStrategyParams.defaults();

        if (p.getTier() == BottomBandHighStrategyParams.Tier.WEEK) {

            return p.getLookbackWeek();

        }

        if (p.getTier() == BottomBandHighStrategyParams.Tier.MONTH) {

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

            checkResult.addTrendPeriod(period, "[BBH]" + msg);

        }

    }

}

