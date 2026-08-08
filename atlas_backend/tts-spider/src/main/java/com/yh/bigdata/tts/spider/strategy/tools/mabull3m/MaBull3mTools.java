package com.yh.bigdata.tts.spider.strategy.tools.mabull3m;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MaBull3mStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveCcMin60BreakoutCore;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MA多头3M突破：3M1(MA5/MA10≥MA20) 与 3M2(MA5/MA10≥MA30) 两条完整路径取 OR。
 * 各路径内：边沿突破金叉/死叉(须在该路径关键K之后)或关键K High；基准须在末K之前。
 */
public final class MaBull3mTools {

    private static final double EPS = 1e-6;

    private MaBull3mTools() {
    }

    public enum AlignKind {
        /** 3M1：相对 MA20 */
        MA20,
        /** 3M2：相对 MA30 */
        MA30
    }

    public enum RefKind {
        GOLDEN,
        DEATH,
        CRITICAL
    }

    @Getter
    public static final class Hit {
        private final PeriodTypeEnum period;
        private final Trade signalBar;
        private final Trade prevBar;
        private final Trade refBar;
        private final RefKind refKind;
        private final AlignKind alignKind;
        private final double breakLine;

        Hit(PeriodTypeEnum period, Trade signalBar, Trade prevBar, Trade refBar,
            RefKind refKind, AlignKind alignKind, double breakLine) {
            this.period = period;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.refBar = refBar;
            this.refKind = refKind;
            this.alignKind = alignKind;
            this.breakLine = breakLine;
        }
    }

    public static Hit findHit(StockBase stock, MaBull3mStrategyParams params) {
        MaBull3mStrategyParams p = params != null ? params : MaBull3mStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        int lookback = resolveLookback(p);
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, Math.max(lookback, 40));
        return findHitOnBars(trades, period);
    }

    static Hit findHitOnBars(List<Trade> trades, PeriodTypeEnum period) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3 || period == null) {
            return null;
        }
        Hit ma20 = findHitOnBarsForAlign(trades, period, AlignKind.MA20);
        if (ma20 != null) {
            return ma20;
        }
        return findHitOnBarsForAlign(trades, period, AlignKind.MA30);
    }

    static Hit findHitOnBarsForAlign(List<Trade> trades, PeriodTypeEnum period, AlignKind align) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3 || period == null || align == null) {
            return null;
        }
        int lastIdx = trades.size() - 1;
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        if (!passesBullAlign(signalBar, align)) {
            return null;
        }

        int criticalIdx = findCriticalBullAlignIndex(trades, lastIdx, align);
        int goldenIdx = findLatestGoldenCrossIndex(trades, lastIdx, criticalIdx);
        Hit golden = tryEdgeBreak(period, signalBar, prevBar, trades, goldenIdx, RefKind.GOLDEN, align);
        if (golden != null) {
            return golden;
        }
        int deathIdx = findLatestDeathCrossKIndex(trades, lastIdx, criticalIdx);
        Hit death = tryEdgeBreak(period, signalBar, prevBar, trades, deathIdx, RefKind.DEATH, align);
        if (death != null) {
            return death;
        }
        return tryEdgeBreak(period, signalBar, prevBar, trades, criticalIdx, RefKind.CRITICAL, align);
    }

    private static Hit tryEdgeBreak(PeriodTypeEnum period, Trade signalBar, Trade prevBar,
                                    List<Trade> trades, int refIdx, RefKind kind, AlignKind align) {
        if (refIdx < 0 || refIdx >= trades.size() - 1) {
            return null;
        }
        Trade refBar = trades.get(refIdx);
        if (refBar == null || refBar.getHigh() == null) {
            return null;
        }
        double breakLine = refBar.getHigh();
        if (!WaveCcMin60BreakoutCore.passesBandHighEdge(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(period, signalBar, prevBar, refBar, kind, align, breakLine);
    }

    static boolean passesBullAlign(Trade bar, AlignKind align) {
        if (bar == null || bar.getMa5() == null || bar.getMa10() == null || align == null) {
            return false;
        }
        if (align == AlignKind.MA30) {
            if (bar.getMa30() == null) {
                return false;
            }
            return bar.getMa5() + EPS >= bar.getMa30() && bar.getMa10() + EPS >= bar.getMa30();
        }
        if (bar.getMa20() == null) {
            return false;
        }
        return bar.getMa5() + EPS >= bar.getMa20() && bar.getMa10() + EPS >= bar.getMa20();
    }

    static int findCriticalBullAlignIndex(List<Trade> trades, int lastIdx, AlignKind align) {
        if (CollectionUtils.isEmpty(trades) || lastIdx < 0 || lastIdx >= trades.size() || align == null) {
            return -1;
        }
        if (!passesBullAlign(trades.get(lastIdx), align)) {
            return -1;
        }
        int i = lastIdx;
        while (i - 1 >= 0 && passesBullAlign(trades.get(i - 1), align)) {
            i--;
        }
        return i < lastIdx ? i : -1;
    }

    static int findLatestGoldenCrossIndex(List<Trade> trades, int lastIdx, int criticalIdx) {
        if (criticalIdx < 0) {
            return -1;
        }
        for (int i = lastIdx - 1; i > criticalIdx; i--) {
            if (isGoldenCrossAt(trades, i)) {
                return i;
            }
        }
        return -1;
    }

    static int findLatestDeathCrossKIndex(List<Trade> trades, int lastIdx, int criticalIdx) {
        if (criticalIdx < 0) {
            return -1;
        }
        for (int i = lastIdx - 1; i > criticalIdx; i--) {
            if (isDeathCrossKAt(trades, i)) {
                return i;
            }
        }
        return -1;
    }

    static boolean isGoldenCrossAt(List<Trade> trades, int i) {
        if (trades == null || i < 1 || i >= trades.size()) {
            return false;
        }
        if (!isMa5GeMa10(trades.get(i))) {
            return false;
        }
        return !isMa5GeMa10(trades.get(i - 1));
    }

    static boolean isDeathCrossKAt(List<Trade> trades, int i) {
        if (trades == null || i < 0 || i + 1 >= trades.size()) {
            return false;
        }
        if (!isMa5GeMa10(trades.get(i))) {
            return false;
        }
        return !isMa5GeMa10(trades.get(i + 1));
    }

    static boolean isMa5GeMa10(Trade bar) {
        if (bar == null || bar.getMa5() == null || bar.getMa10() == null) {
            return false;
        }
        return bar.getMa5() + EPS >= bar.getMa10();
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MaBull3mStrategyParams params) {
        MaBull3mStrategyParams p = params != null ? params : MaBull3mStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, "成交额不足");
            return false;
        }
        return true;
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[M3M]MA多头3M突破";
        }
        return String.format("[M3M]MA多头3M突破|%s%s多头+边沿破%sHigh|breakLine=%.2f,refDay=%s",
                periodLabel(hit.getPeriod()),
                alignLabel(hit.getAlignKind()),
                refLabel(hit.getRefKind()),
                hit.getBreakLine(),
                dayOf(hit.getRefBar()));
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        return String.format(
                "MA多头3M突破,strategyTag=M3M,period=%s,align=%s,sigDay=%s,sigClose=%.2f,breakLine=%.2f,ref=%s,refDay=%s,refLabel=%s",
                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",
                hit.getAlignKind() != null ? hit.getAlignKind().name() : "",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                hit.getBreakLine(),
                hit.getRefKind() != null ? hit.getRefKind().name() : "",
                dayOf(hit.getRefBar()),
                refLabel(hit.getRefKind()));
    }

    public static PeriodTypeEnum resolvePeriod(MaBull3mStrategyParams.Tier tier) {
        if (tier == MaBull3mStrategyParams.Tier.MIN30) {
            return PeriodTypeEnum.MIN30;
        }
        if (tier == MaBull3mStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == MaBull3mStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(MaBull3mStrategyParams params) {
        MaBull3mStrategyParams p = params != null ? params : MaBull3mStrategyParams.defaults();
        if (p.getTier() == MaBull3mStrategyParams.Tier.MIN30) {
            return p.getLookbackMin30();
        }
        if (p.getTier() == MaBull3mStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == MaBull3mStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        return p.getLookbackDay();
    }

    private static String alignLabel(AlignKind align) {
        if (align == AlignKind.MA30) {
            return "3M2/";
        }
        return "3M1/";
    }

    private static String refLabel(RefKind kind) {
        if (kind == RefKind.GOLDEN) {
            return "金叉交叉K";
        }
        if (kind == RefKind.DEATH) {
            return "死叉交叉K";
        }
        if (kind == RefKind.CRITICAL) {
            return "多头关键K";
        }
        return "基准K";
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
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

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[M3M]" + msg);
        }
    }
}
