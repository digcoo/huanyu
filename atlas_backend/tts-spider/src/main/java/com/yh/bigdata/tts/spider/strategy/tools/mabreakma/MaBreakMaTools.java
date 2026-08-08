package com.yh.bigdata.tts.spider.strategy.tools.mabreakma;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MaBreakMaStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.ma3m.Ma3mCore;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MA多头破MA：3M1/3M2 多头排列 OR，且边沿/开盘突破均线 MAX；两路径都命中优先 3M1。
 */
public final class MaBreakMaTools {

    private MaBreakMaTools() {
    }

    @Getter
    public static final class Hit {
        private final PeriodTypeEnum period;
        private final Trade signalBar;
        private final Trade prevBar;
        private final Ma3mCore.AlignKind alignKind;
        private final Ma3mCore.BreakKind breakKind;
        private final double breakLine;

        Hit(PeriodTypeEnum period, Trade signalBar, Trade prevBar,
            Ma3mCore.AlignKind alignKind, Ma3mCore.BreakKind breakKind, double breakLine) {
            this.period = period;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.alignKind = alignKind;
            this.breakKind = breakKind;
            this.breakLine = breakLine;
        }
    }

    public static Hit findHit(StockBase stock, MaBreakMaStrategyParams params) {
        MaBreakMaStrategyParams p = params != null ? params : MaBreakMaStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, Math.max(resolveLookback(p), 40));
        return findHitOnBars(trades, period);
    }

    public static Hit findHitOnBars(List<Trade> trades, PeriodTypeEnum period) {
        Hit ma20 = findHitOnBarsForAlign(trades, period, Ma3mCore.AlignKind.MA20);
        if (ma20 != null) {
            return ma20;
        }
        return findHitOnBarsForAlign(trades, period, Ma3mCore.AlignKind.MA30);
    }

    static Hit findHitOnBarsForAlign(List<Trade> trades, PeriodTypeEnum period, Ma3mCore.AlignKind align) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 2 || period == null || align == null) {
            return null;
        }
        int lastIdx = trades.size() - 1;
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        if (!Ma3mCore.passesBullAlign(signalBar, align)) {
            return null;
        }
        Ma3mCore.BreakKind breakKind = Ma3mCore.resolveMaMaxBreak(prevBar, signalBar);
        if (breakKind == null) {
            return null;
        }
        Double max = Ma3mCore.maMax(signalBar);
        if (max == null) {
            return null;
        }
        return new Hit(period, signalBar, prevBar, align, breakKind, max);
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MaBreakMaStrategyParams params) {
        MaBreakMaStrategyParams p = params != null ? params : MaBreakMaStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, "成交额不足");
            return false;
        }
        if (p.isEnableMin30BreakFilter() && p.getTier() != MaBreakMaStrategyParams.Tier.MIN30) {
            List<Trade> min30 = RealtimeStockCache.getLastTrades(
                    stock, PeriodTypeEnum.MIN30, Math.max(p.getLookbackMin30(), 40));
            if (findHitOnBars(min30, PeriodTypeEnum.MIN30) == null) {
                appendMessage(checkResult, PeriodTypeEnum.MIN30, "未满足30分3M多头+破MAX");
                return false;
            }
        }
        if (p.isRequireDayAlign() && !passesPeriodBullAlign(stock, PeriodTypeEnum.DAY, p.getLookbackDay())) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, "日线未满足3M多头排列");
            return false;
        }
        if (p.isRequireWeekAlign() && !passesPeriodBullAlign(stock, PeriodTypeEnum.WEEK, p.getLookbackWeek())) {
            appendMessage(checkResult, PeriodTypeEnum.WEEK, "周线未满足3M多头排列");
            return false;
        }
        if (p.isRequireMonthAlign() && !passesPeriodBullAlign(stock, PeriodTypeEnum.MONTH, p.getLookbackMonth())) {
            appendMessage(checkResult, PeriodTypeEnum.MONTH, "月线未满足3M多头排列");
            return false;
        }
        return true;
    }

    private static boolean passesPeriodBullAlign(StockBase stock, PeriodTypeEnum period, int lookback) {
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, Math.max(lookback, 40));
        if (CollectionUtils.isEmpty(trades)) {
            return false;
        }
        return Ma3mCore.passesAnyBullAlign(trades.get(trades.size() - 1));
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[MBM]MA多头破MA";
        }
        return String.format("[MBM]MA多头破MA|%s%s多头+%s破MAX|breakLine=%.2f",
                periodLabel(hit.getPeriod()),
                alignLabel(hit.getAlignKind()),
                breakLabel(hit.getBreakKind()),
                hit.getBreakLine());
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        return String.format(
                "MA多头破MA,strategyTag=MBM,period=%s,align=%s,break=%s,sigDay=%s,sigClose=%.2f,breakLine=%.2f",
                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",
                hit.getAlignKind() != null ? hit.getAlignKind().name() : "",
                hit.getBreakKind() != null ? hit.getBreakKind().name() : "",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                hit.getBreakLine());
    }

    public static PeriodTypeEnum resolvePeriod(MaBreakMaStrategyParams.Tier tier) {
        if (tier == MaBreakMaStrategyParams.Tier.MIN30) {
            return PeriodTypeEnum.MIN30;
        }
        if (tier == MaBreakMaStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == MaBreakMaStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(MaBreakMaStrategyParams params) {
        MaBreakMaStrategyParams p = params != null ? params : MaBreakMaStrategyParams.defaults();
        if (p.getTier() == MaBreakMaStrategyParams.Tier.MIN30) {
            return p.getLookbackMin30();
        }
        if (p.getTier() == MaBreakMaStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == MaBreakMaStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        return p.getLookbackDay();
    }

    private static String alignLabel(Ma3mCore.AlignKind align) {
        return align == Ma3mCore.AlignKind.MA30 ? "3M2/" : "3M1/";
    }

    private static String breakLabel(Ma3mCore.BreakKind kind) {
        return kind == Ma3mCore.BreakKind.OPEN ? "开盘" : "边沿";
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MIN30) {
            return "30分";
        }
        if (period == PeriodTypeEnum.MONTH) {
            return "月";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周";
        }
        return "日";
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[MBM]" + msg);
        }
    }
}
