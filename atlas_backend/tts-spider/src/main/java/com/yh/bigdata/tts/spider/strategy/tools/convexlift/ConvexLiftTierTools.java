package com.yh.bigdata.tts.spider.strategy.tools.convexlift;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.ConvexLiftTierStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 凸波段上移：日档末波段为凸，且 close &gt; 前 K high；上级周期（周/月/年）同规则。
 */
public final class ConvexLiftTierTools {

    private static final double EPS = 1e-6;

    private ConvexLiftTierTools() {
    }

    public static boolean passesDayConvexLift(StockBase stock, CheckResult checkResult, int lookbackDay) {
        if (stock == null || lookbackDay < 3) {
            return false;
        }
        int fetchBars = Math.max(lookbackDay + 2, lookbackDay);
        List<Trade> dayBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, fetchBars);
        return passesDayConvexLiftOnBars(dayBars, stock, checkResult, lookbackDay);
    }

    static boolean passesDayConvexLiftOnBars(List<Trade> dayBars, StockBase stock, CheckResult checkResult,
                                              int lookbackDay) {
        if (CollectionUtils.isEmpty(dayBars) || dayBars.size() < 2 || lookbackDay < 3) {
            return false;
        }
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(dayBars, lookbackDay);
        if (bands.size() < 2) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, "日波段不足");
            return false;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        if (!WaveShapeTools.isConvex(lastBand, prevBand)) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, "日非凸波段");
            return false;
        }
        if (!passesCloseAbovePrevHigh(dayBars, stock, PeriodTypeEnum.DAY)) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, "日未破前K high");
            return false;
        }
        appendMessage(checkResult, PeriodTypeEnum.DAY, "日凸波段上移");
        return true;
    }

    public static boolean passesUpperPeriodLift(StockBase stock, CheckResult checkResult,
                                                ConvexLiftTierStrategyParams params) {
        ConvexLiftTierStrategyParams p = params != null ? params : ConvexLiftTierStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return false;
        }
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, period, 2);
        return passesPeriodCloseAbovePrevHighOnBars(bars, stock, checkResult, period);
    }

    static boolean passesPeriodCloseAbovePrevHighOnBars(List<Trade> bars, StockBase stock,
                                                        CheckResult checkResult, PeriodTypeEnum period) {
        if (CollectionUtils.isEmpty(bars) || bars.size() < 2) {
            appendMessage(checkResult, period, periodLabel(period) + "K不足");
            return false;
        }
        if (!passesCloseAbovePrevHigh(bars, stock, period)) {
            appendMessage(checkResult, period, periodLabel(period) + "未破前K high");
            return false;
        }
        appendMessage(checkResult, period, periodLabel(period) + "破前K high");
        return true;
    }

    static boolean passesCloseAbovePrevHigh(List<Trade> bars, StockBase stock, PeriodTypeEnum period) {
        if (CollectionUtils.isEmpty(bars) || bars.size() < 2) {
            return false;
        }
        Trade prev = bars.get(bars.size() - 2);
        Trade signal = bars.get(bars.size() - 1);
        Double signalClose = resolveClose(stock, period, signal);
        if (prev == null || prev.getHigh() == null || signalClose == null) {
            return false;
        }
        return signalClose > prev.getHigh() + EPS;
    }

    public static PeriodTypeEnum resolvePeriod(ConvexLiftTierStrategyParams.Tier tier) {
        if (tier == ConvexLiftTierStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        if (tier == ConvexLiftTierStrategyParams.Tier.YEAR) {
            return PeriodTypeEnum.YEAR;
        }
        return PeriodTypeEnum.WEEK;
    }

    public static String buildTierLabel(ConvexLiftTierStrategyParams params) {
        if (params == null || params.getTier() == null) {
            return "周";
        }
        switch (params.getTier()) {
            case MONTH:
                return "月";
            case YEAR:
                return "年";
            default:
                return "周";
        }
    }

    private static Double resolveClose(StockBase stock, PeriodTypeEnum period, Trade current) {
        if (current != null && current.getClose() != null) {
            return current.getClose();
        }
        if (period == PeriodTypeEnum.DAY && stock != null && stock.getClose() != null) {
            return stock.getClose();
        }
        return null;
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null) {
            checkResult.addTrendPeriod(period, msg);
        }
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.YEAR) {
            return "年";
        }
        if (period == PeriodTypeEnum.MONTH) {
            return "月";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周";
        }
        return "日";
    }
}
