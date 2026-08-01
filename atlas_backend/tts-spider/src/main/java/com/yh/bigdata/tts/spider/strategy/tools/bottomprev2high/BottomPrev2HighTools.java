package com.yh.bigdata.tts.spider.strategy.tools.bottomprev2high;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

import com.yh.bigdata.tts.common.constants.RealtimeStockCache;

import com.yh.bigdata.tts.common.model.StockBase;

import com.yh.bigdata.tts.common.model.Trade;

import com.yh.bigdata.tts.common.param.BottomPrev2HighStrategyParams;

import com.yh.bigdata.tts.spider.response.CheckResult;

import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;

import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveCcMin60BreakoutCore;

import lombok.Getter;

import org.springframework.util.CollectionUtils;



import java.util.List;



/**

 * 末 K 边沿突破末前 2 根 K 的 max(high)。

 */

public final class BottomPrev2HighTools {



    private BottomPrev2HighTools() {

    }



    @Getter

    public static final class Hit {

        private final PeriodTypeEnum period;

        private final Trade signalBar;

        private final Trade prevBar;

        private final Trade prev2Bar;

        private final double breakLine;



        Hit(PeriodTypeEnum period, Trade signalBar, Trade prevBar, Trade prev2Bar, double breakLine) {

            this.period = period;

            this.signalBar = signalBar;

            this.prevBar = prevBar;

            this.prev2Bar = prev2Bar;

            this.breakLine = breakLine;

        }

    }



    public static Hit findHit(StockBase stock, BottomPrev2HighStrategyParams params) {

        BottomPrev2HighStrategyParams p = params != null ? params : BottomPrev2HighStrategyParams.defaults();

        PeriodTypeEnum period = resolvePeriod(p.getTier());

        if (stock == null || period == null) {

            return null;

        }

        int lookback = resolveLookback(p);

        int fetchBars = Math.max(lookback + 40, lookback + 2);

        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, fetchBars);

        return findHitOnBars(trades, period);

    }



    static Hit findHitOnBars(List<Trade> trades, PeriodTypeEnum period) {

        if (CollectionUtils.isEmpty(trades) || trades.size() < 3 || period == null) {

            return null;

        }

        int lastIdx = trades.size() - 1;

        Trade signalBar = trades.get(lastIdx);

        Trade prevBar = trades.get(lastIdx - 1);

        Trade prev2Bar = trades.get(lastIdx - 2);

        if (prevBar == null || prev2Bar == null

                || prevBar.getHigh() == null || prev2Bar.getHigh() == null) {

            return null;

        }

        double breakLine = Math.max(prevBar.getHigh(), prev2Bar.getHigh());

        if (!WaveCcMin60BreakoutCore.passesBandHighEdge(prevBar, signalBar, breakLine)) {

            return null;

        }

        return new Hit(period, signalBar, prevBar, prev2Bar, breakLine);

    }



    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,

                                              BottomPrev2HighStrategyParams params) {

        BottomPrev2HighStrategyParams p = params != null ? params : BottomPrev2HighStrategyParams.defaults();

        if (p.isEnableMinAmountFilter()

                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {

            appendMessage(checkResult, PeriodTypeEnum.DAY, "成交额不足");

            return false;

        }

        return true;

    }



    public static String buildTrendMessage(Hit hit) {

        if (hit == null) {

            return "[BP2H]底部High突破";

        }

        return String.format("[BP2H]底部High突破|%sMACD<0,父级MACD>0,末K破前2K max(high)|breakLine=%.2f",

                periodLabel(hit.getPeriod()),

                hit.getBreakLine());

    }



    public static String buildSignalMessage(Hit hit) {

        if (hit == null) {

            return "";

        }

        Trade signalBar = hit.getSignalBar();

        return String.format(

                "底部High突破,strategyTag=BP2H,period=%s,sigDay=%s,sigClose=%.2f,breakLine=%.2f",

                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",

                dayOf(signalBar),

                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,

                hit.getBreakLine());

    }



    public static PeriodTypeEnum resolvePeriod(BottomPrev2HighStrategyParams.Tier tier) {

        if (tier == BottomPrev2HighStrategyParams.Tier.WEEK) {

            return PeriodTypeEnum.WEEK;

        }

        if (tier == BottomPrev2HighStrategyParams.Tier.MONTH) {

            return PeriodTypeEnum.MONTH;

        }

        return PeriodTypeEnum.DAY;

    }



    public static int resolveLookback(BottomPrev2HighStrategyParams params) {

        BottomPrev2HighStrategyParams p = params != null ? params : BottomPrev2HighStrategyParams.defaults();

        if (p.getTier() == BottomPrev2HighStrategyParams.Tier.WEEK) {

            return p.getLookbackWeek();

        }

        if (p.getTier() == BottomPrev2HighStrategyParams.Tier.MONTH) {

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

            checkResult.addTrendPeriod(period, "[BP2H]" + msg);

        }

    }

}

