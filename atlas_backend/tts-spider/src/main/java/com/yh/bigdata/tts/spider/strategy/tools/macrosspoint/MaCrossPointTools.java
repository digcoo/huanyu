package com.yh.bigdata.tts.spider.strategy.tools.macrosspoint;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MA 金叉波段顶 / 死叉交叉点 / 金叉交叉点 边沿突破。
 * <ul>
 *   <li>策略1 金叉：边沿破金叉波段顶，且末K MA5&gt;MA10</li>
 *   <li>策略2 死叉：边沿破死叉交叉点价（MA5/MA10 插值）</li>
 *   <li>策略3/4： (边沿破金叉波段顶 且 MA5≥MA10) or 边沿破死叉交叉点 or (边沿破金叉交叉点 且 MA5≥MA10)</li>
 * </ul>
 */
public final class MaCrossPointTools {

    private MaCrossPointTools() {
    }

    public enum BreakTarget {
        GOLDEN_BAND_TOP,
        DEATH_CROSS,
        GOLDEN_CROSS
    }

    @Getter
    public static final class Hit {
        private final PeriodTypeEnum period;
        private final MaCrossPointCore.CrossKind crossKind;
        private final BreakTarget breakTarget;
        private final Trade signalBar;
        private final Trade prevBar;
        private final Trade crossBar;
        private final double breakLine;
        /** 金叉命中时的波段顶参考 K，可空 */
        private final Trade bandHighBar;

        Hit(PeriodTypeEnum period, MaCrossPointCore.CrossKind crossKind,
            Trade signalBar, Trade prevBar, Trade crossBar, double breakLine, Trade bandHighBar) {
            this(period, crossKind,
                    crossKind == MaCrossPointCore.CrossKind.DEATH
                            ? BreakTarget.DEATH_CROSS : BreakTarget.GOLDEN_BAND_TOP,
                    signalBar, prevBar, crossBar, breakLine, bandHighBar);
        }

        Hit(PeriodTypeEnum period, MaCrossPointCore.CrossKind crossKind, BreakTarget breakTarget,
            Trade signalBar, Trade prevBar, Trade crossBar, double breakLine, Trade bandHighBar) {
            this.period = period;
            this.crossKind = crossKind;
            this.breakTarget = breakTarget;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.crossBar = crossBar;
            this.breakLine = breakLine;
            this.bandHighBar = bandHighBar;
        }
    }

    public static Hit findHit(StockBase stock, MaCrossPointStrategyParams params,
                              MaCrossPointCore.CrossKind kind) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null || kind == null) {
            return null;
        }
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, Math.max(resolveLookback(p), 40));
        return findHitOnBars(trades, period, kind);
    }

    static Hit findHitOnBars(List<Trade> trades, PeriodTypeEnum period, MaCrossPointCore.CrossKind kind) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3 || period == null || kind == null) {
            return null;
        }
        if (kind == MaCrossPointCore.CrossKind.GOLDEN) {
            return findGoldenBandTopHit(trades, period, true);
        }
        return findDeathCrossPointHit(trades, period);
    }

    /** 策略3/4：(破金叉波段顶 且 MA5≥MA10) or 破死叉点 or (破金叉交叉点 且 MA5≥MA10) */
    public static Hit findTrendBreakHit(StockBase stock, MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, Math.max(resolveLookback(p), 40));
        return findTrendBreakHitOnBars(trades, period);
    }

    static Hit findTrendBreakHitOnBars(List<Trade> trades, PeriodTypeEnum period) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3 || period == null) {
            return null;
        }
        Hit goldenBand = findGoldenBandTopHit(trades, period, false);
        if (goldenBand != null && MaCrossPointCore.isMa5GeMa10(goldenBand.getSignalBar())) {
            return goldenBand;
        }
        Hit death = findDeathCrossPointHit(trades, period);
        if (death != null) {
            return death;
        }
        Hit goldenCross = findGoldenCrossPointHit(trades, period);
        if (goldenCross != null && MaCrossPointCore.isMa5GeMa10(goldenCross.getSignalBar())) {
            return goldenCross;
        }
        return null;
    }

    /** 金叉：边沿破金叉波段顶；requireMa5AboveMa10 为策略1硬条件 */
    static Hit findGoldenBandTopHit(List<Trade> trades, PeriodTypeEnum period, boolean requireMa5AboveMa10) {
        int lastIdx = trades.size() - 1;
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        if (requireMa5AboveMa10 && !MaCrossPointCore.passesMa5AboveMa10(signalBar)) {
            return null;
        }
        int crossIdx = MaCrossPointCore.findLatestCrossIndex(trades, lastIdx, MaCrossPointCore.CrossKind.GOLDEN);
        if (crossIdx < 1) {
            return null;
        }
        Trade crossBar = trades.get(crossIdx);
        YangBandTools.CompleteYangBand band = resolveGoldenBand(trades, crossIdx, crossBar);
        if (band == null || Double.isNaN(band.getBandHigh())) {
            return null;
        }
        double breakLine = band.getBandHigh();
        if (!MaCrossPointCore.passesEdgeBreak(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(period, MaCrossPointCore.CrossKind.GOLDEN, BreakTarget.GOLDEN_BAND_TOP,
                signalBar, prevBar, crossBar, breakLine, band.getBandHighBar());
    }

    /**
     * 阳线且不在末未完结波段：所在完整波段 High。
     * 阳线落在末未完结波段，或阴线：此前一个完整波段 High。
     */
    static YangBandTools.CompleteYangBand resolveGoldenBand(List<Trade> trades, int crossIdx, Trade crossBar) {
        if (YangBandTools.isStrictYang(crossBar)) {
            YangBandTools.CompleteYangBand containing = YangBandTools.findBandContainingYangBar(trades, crossIdx);
            if (containing != null) {
                return containing;
            }
            return YangBandTools.findNearestCompleteBandAtOrBefore(trades, crossIdx);
        }
        if (YangBandTools.isStrictYin(crossBar)) {
            return YangBandTools.findNearestCompleteBandAtOrBefore(trades, crossIdx);
        }
        return null;
    }

    /** 死叉：边沿破死叉交叉点价 */
    static Hit findDeathCrossPointHit(List<Trade> trades, PeriodTypeEnum period) {
        int lastIdx = trades.size() - 1;
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        int crossIdx = MaCrossPointCore.findLatestCrossIndex(trades, lastIdx, MaCrossPointCore.CrossKind.DEATH);
        if (crossIdx < 1) {
            return null;
        }
        Double breakLine = MaCrossPointCore.crossPriceAt(trades, crossIdx);
        if (breakLine == null) {
            return null;
        }
        if (!MaCrossPointCore.passesEdgeBreak(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(period, MaCrossPointCore.CrossKind.DEATH, BreakTarget.DEATH_CROSS,
                signalBar, prevBar, trades.get(crossIdx), breakLine, null);
    }

    /** 金叉交叉点价边沿突破 */
    static Hit findGoldenCrossPointHit(List<Trade> trades, PeriodTypeEnum period) {
        int lastIdx = trades.size() - 1;
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        int crossIdx = MaCrossPointCore.findLatestCrossIndex(trades, lastIdx, MaCrossPointCore.CrossKind.GOLDEN);
        if (crossIdx < 1) {
            return null;
        }
        Double breakLine = MaCrossPointCore.crossPriceAt(trades, crossIdx);
        if (breakLine == null) {
            return null;
        }
        if (!MaCrossPointCore.passesEdgeBreak(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(period, MaCrossPointCore.CrossKind.GOLDEN, BreakTarget.GOLDEN_CROSS,
                signalBar, prevBar, trades.get(crossIdx), breakLine, null);
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MaCrossPointStrategyParams params, Hit hit) {
        String tag = hit != null && hit.getCrossKind() == MaCrossPointCore.CrossKind.DEATH ? "MDB" : "MGB";
        return passesOptionalGates(stock, checkResult, params, hit, tag);
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MaCrossPointStrategyParams params, Hit hit, String tag) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        String gateTag = tag != null ? tag : "MCP";
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, gateTag, "成交额不足");
            return false;
        }
        PeriodTypeEnum msgPeriod = hit != null ? hit.getPeriod() : PeriodTypeEnum.DAY;
        if (!passMaBullGate(stock, checkResult, gateTag, msgPeriod, p.isRequireDayMaBull(), PeriodTypeEnum.DAY, "日")) {
            return false;
        }
        if (!passMaBullGate(stock, checkResult, gateTag, msgPeriod, p.isRequireWeekMaBull(), PeriodTypeEnum.WEEK, "周")) {
            return false;
        }
        if (!passMaBullGate(stock, checkResult, gateTag, msgPeriod, p.isRequireMonthMaBull(), PeriodTypeEnum.MONTH, "月")) {
            return false;
        }
        if (!passMaBullGate(stock, checkResult, gateTag, msgPeriod, p.isRequireQuarterMaBull(), PeriodTypeEnum.QUARTER, "季")) {
            return false;
        }
        if (!passMaBullGate(stock, checkResult, gateTag, msgPeriod, p.isRequireYearMaBull(), PeriodTypeEnum.YEAR, "年")) {
            return false;
        }
        return true;
    }

    private static boolean passMaBullGate(StockBase stock, CheckResult checkResult, String tag,
                                          PeriodTypeEnum msgPeriod, boolean enabled,
                                          PeriodTypeEnum period, String label) {
        if (!enabled) {
            return true;
        }
        if (!MaCrossPointCore.passesMaBull(RealtimeStockCache.getLastTrades(stock, period, 80))) {
            appendMessage(checkResult, msgPeriod, tag, "未满足" + label + "均线多头MA5>MA60");
            return false;
        }
        return true;
    }

    /**
     * 父子周期：30分→日，日→周，周→月，月→季，季→年。
     */
    public static PeriodTypeEnum resolveParentPeriod(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MIN30) {
            return PeriodTypeEnum.DAY;
        }
        if (period == PeriodTypeEnum.DAY) {
            return PeriodTypeEnum.WEEK;
        }
        if (period == PeriodTypeEnum.WEEK) {
            return PeriodTypeEnum.MONTH;
        }
        if (period == PeriodTypeEnum.MONTH) {
            return PeriodTypeEnum.QUARTER;
        }
        if (period == PeriodTypeEnum.QUARTER) {
            return PeriodTypeEnum.YEAR;
        }
        return null;
    }

    /** 硬性：父周期末 K 收盘价 &gt; max(MA5, MA10) */
    public static boolean passesParentAboveMa(StockBase stock, CheckResult checkResult,
                                              String tag, PeriodTypeEnum period) {
        PeriodTypeEnum parent = resolveParentPeriod(period);
        if (parent == null) {
            appendMessage(checkResult, period != null ? period : PeriodTypeEnum.DAY, tag, "未满足父级均价之上");
            return false;
        }
        if (!MaCrossPointCore.passesAboveMa(lastBarOf(stock, parent))) {
            appendMessage(checkResult, period != null ? period : parent, tag,
                    "未满足父级均价之上" + periodLabel(parent) + "close>max(MA5,MA10)");
            return false;
        }
        return true;
    }

    private static Trade lastBarOf(StockBase stock, PeriodTypeEnum period) {
        if (stock == null || period == null) {
            return null;
        }
        return RealtimeStockCache.getLastTrade(stock, period, 0);
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        boolean death = hit.getCrossKind() == MaCrossPointCore.CrossKind.DEATH;
        String name = death ? "MA死叉点突破" : "MA金叉点突破";
        String tag = death ? "MDB" : "MGB";
        String target = death ? "死叉交叉点" : "金叉波段顶";
        String maGate = death ? "" : ",MA5>MA10";
        return String.format("[%s]%s|%s边沿破%s%s,父级均价之上|breakLine=%.2f,crossDay=%s",
                tag, name, periodLabel(hit.getPeriod()), target, maGate,
                hit.getBreakLine(),
                dayOf(hit.getCrossBar()));
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        boolean death = hit.getCrossKind() == MaCrossPointCore.CrossKind.DEATH;
        Trade signalBar = hit.getSignalBar();
        return String.format(
                "%s,strategyTag=%s,period=%s,cross=%s,target=%s,sigDay=%s,sigClose=%.2f,breakLine=%.2f,crossDay=%s,bandHighDay=%s",
                death ? "MA死叉点突破" : "MA金叉点突破",
                death ? "MDB" : "MGB",
                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",
                hit.getCrossKind() != null ? hit.getCrossKind().name() : "",
                death ? "CROSS_POINT" : "BAND_TOP",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                hit.getBreakLine(),
                dayOf(hit.getCrossBar()),
                dayOf(hit.getBandHighBar()));
    }

    public static String buildTrendBreakTrendMessage(Hit hit, boolean bull) {
        if (hit == null) {
            return "";
        }
        String tag = bull ? "MTB" : "MBS";
        String name = bull ? "多头趋势突破" : "空头趋势启动";
        String maGate = bull ? "MA10>=MA60" : "MA10<MA60";
        String goldenMa = hit.getBreakTarget() == BreakTarget.DEATH_CROSS ? "" : ",MA5>=MA10";
        return String.format("[%s]%s|%s边沿破%s%s,%s|breakLine=%.2f,crossDay=%s",
                tag, name, periodLabel(hit.getPeriod()), breakTargetLabel(hit.getBreakTarget()), goldenMa, maGate,
                hit.getBreakLine(), dayOf(hit.getCrossBar()));
    }

    public static String buildTrendBreakSignalMessage(Hit hit, boolean bull) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        return String.format(
                "%s,strategyTag=%s,period=%s,cross=%s,target=%s,sigDay=%s,sigClose=%.2f,breakLine=%.2f,crossDay=%s,bandHighDay=%s",
                bull ? "多头趋势突破" : "空头趋势启动",
                bull ? "MTB" : "MBS",
                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",
                hit.getCrossKind() != null ? hit.getCrossKind().name() : "",
                breakTargetCode(hit.getBreakTarget()),
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                hit.getBreakLine(),
                dayOf(hit.getCrossBar()),
                dayOf(hit.getBandHighBar()));
    }

    private static String breakTargetLabel(BreakTarget target) {
        if (target == BreakTarget.DEATH_CROSS) {
            return "死叉交叉点";
        }
        if (target == BreakTarget.GOLDEN_CROSS) {
            return "金叉交叉点";
        }
        return "金叉波段顶";
    }

    private static String breakTargetCode(BreakTarget target) {
        if (target == BreakTarget.DEATH_CROSS) {
            return "DEATH_CROSS";
        }
        if (target == BreakTarget.GOLDEN_CROSS) {
            return "GOLDEN_CROSS";
        }
        return "BAND_TOP";
    }

    public static PeriodTypeEnum resolvePeriod(MaCrossPointStrategyParams.Tier tier) {
        if (tier == MaCrossPointStrategyParams.Tier.MIN30) {
            return PeriodTypeEnum.MIN30;
        }
        if (tier == MaCrossPointStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == MaCrossPointStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        if (tier == MaCrossPointStrategyParams.Tier.QUARTER) {
            return PeriodTypeEnum.QUARTER;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        if (p.getTier() == MaCrossPointStrategyParams.Tier.MIN30) {
            return p.getLookbackMin30();
        }
        if (p.getTier() == MaCrossPointStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == MaCrossPointStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        if (p.getTier() == MaCrossPointStrategyParams.Tier.QUARTER) {
            return p.getLookbackQuarter();
        }
        return p.getLookbackDay();
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MIN30) {
            return "30分";
        }
        if (period == PeriodTypeEnum.MONTH) {
            return "月";
        }
        if (period == PeriodTypeEnum.YEAR) {
            return "年";
        }
        if (period == PeriodTypeEnum.QUARTER) {
            return "季";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周";
        }
        return "日";
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period,
                                      String tag, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[" + tag + "]" + msg);
        }
    }
}
