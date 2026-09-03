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
 * MA 金叉波段顶 / 死叉交叉点 边沿突破。
 * <ul>
 *   <li>策略1 金叉：边沿破金叉波段顶，且末K MA5&gt;MA10</li>
 *   <li>策略2 死叉：边沿破死叉交叉点价（MA5/MA10 插值）</li>
 *   <li>策略5：边沿破任意死叉交叉点 DC10/DC20/DC30/DC60；父级 (MA10&gt;MA60) 或均价之上</li>
 *   <li>策略6：边沿破任意金叉交叉点 GC10/GC20/GC30/GC60；同一父级门</li>
 *   <li>策略7：边沿破任意金叉波段顶 GH10/GH20/GH30/GH60；同一父级门</li>
 * </ul>
 */
public final class MaCrossPointTools {

    private MaCrossPointTools() {
    }

    public enum BreakTarget {
        GOLDEN_BAND_TOP,
        GOLDEN_CROSS,
        DEATH_CROSS
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
        /** 交叉慢线周期：10/20/30/60；策略1 金叉为 0 */
        private final int slowMa;

        Hit(PeriodTypeEnum period, MaCrossPointCore.CrossKind crossKind,
            Trade signalBar, Trade prevBar, Trade crossBar, double breakLine, Trade bandHighBar) {
            this(period, crossKind,
                    crossKind == MaCrossPointCore.CrossKind.DEATH
                            ? BreakTarget.DEATH_CROSS : BreakTarget.GOLDEN_BAND_TOP,
                    signalBar, prevBar, crossBar, breakLine, bandHighBar,
                    crossKind == MaCrossPointCore.CrossKind.DEATH ? 10 : 0);
        }

        Hit(PeriodTypeEnum period, MaCrossPointCore.CrossKind crossKind, BreakTarget breakTarget,
            Trade signalBar, Trade prevBar, Trade crossBar, double breakLine, Trade bandHighBar) {
            this(period, crossKind, breakTarget, signalBar, prevBar, crossBar, breakLine, bandHighBar,
                    breakTarget == BreakTarget.DEATH_CROSS ? 10 : 0);
        }

        Hit(PeriodTypeEnum period, MaCrossPointCore.CrossKind crossKind, BreakTarget breakTarget,
            Trade signalBar, Trade prevBar, Trade crossBar, double breakLine, Trade bandHighBar, int slowMa) {
            this.period = period;
            this.crossKind = crossKind;
            this.breakTarget = breakTarget;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.crossBar = crossBar;
            this.breakLine = breakLine;
            this.bandHighBar = bandHighBar;
            this.slowMa = slowMa;
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

    /** 策略5：边沿破 DC10/DC20/DC30/DC60 任一 */
    public static Hit findAnyDeathCrossBreakHit(StockBase stock, MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, Math.max(resolveLookback(p), 80));
        return findAnyDeathCrossBreakHitOnBars(trades, period);
    }

    /** 策略6：边沿破 GC10/GC20/GC30/GC60 任一 */
    public static Hit findAnyGoldenCrossBreakHit(StockBase stock, MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, Math.max(resolveLookback(p), 80));
        return findAnyGoldenCrossBreakHitOnBars(trades, period);
    }

    static Hit findAnyGoldenCrossBreakHitOnBars(List<Trade> trades, PeriodTypeEnum period) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3 || period == null) {
            return null;
        }
        for (int slowMa : MaCrossPointCore.GOLDEN_CROSS_SLOW_MAS) {
            Hit hit = findGoldenCrossPointHit(trades, period, slowMa);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    /** 策略7：边沿破 GH10/GH20/GH30/GH60 任一 */
    public static Hit findAnyGoldenHighBreakHit(StockBase stock, MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, Math.max(resolveLookback(p), 80));
        return findAnyGoldenHighBreakHitOnBars(trades, period);
    }

    static Hit findAnyGoldenHighBreakHitOnBars(List<Trade> trades, PeriodTypeEnum period) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3 || period == null) {
            return null;
        }
        for (int slowMa : MaCrossPointCore.GOLDEN_CROSS_SLOW_MAS) {
            Hit hit = findGoldenBandTopHit(trades, period, false, slowMa);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    static Hit findAnyDeathCrossBreakHitOnBars(List<Trade> trades, PeriodTypeEnum period) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3 || period == null) {
            return null;
        }
        for (int slowMa : MaCrossPointCore.DEATH_CROSS_SLOW_MAS) {
            Hit hit = findDeathCrossPointHit(trades, period, slowMa);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    /** 金叉：边沿破金叉波段顶；requireMa5AboveMa10 为策略1硬条件 */
    static Hit findGoldenBandTopHit(List<Trade> trades, PeriodTypeEnum period, boolean requireMa5AboveMa10) {
        return findGoldenBandTopHit(trades, period, requireMa5AboveMa10, 10);
    }

    static Hit findGoldenBandTopHit(List<Trade> trades, PeriodTypeEnum period,
                                    boolean requireMa5AboveMa10, int slowMa) {
        int lastIdx = trades.size() - 1;
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        if (requireMa5AboveMa10 && !MaCrossPointCore.passesMa5AboveMa10(signalBar)) {
            return null;
        }
        int crossIdx = requireMa5AboveMa10
                ? MaCrossPointCore.findLatestCrossIndex(trades, lastIdx, MaCrossPointCore.CrossKind.GOLDEN)
                : MaCrossPointCore.findLatestGoldenCrossIndex(trades, lastIdx, slowMa);
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
                signalBar, prevBar, crossBar, breakLine, band.getBandHighBar(),
                requireMa5AboveMa10 ? 0 : slowMa);
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
        return findDeathCrossPointHit(trades, period, 10);
    }

    static Hit findDeathCrossPointHit(List<Trade> trades, PeriodTypeEnum period, int slowMa) {
        int lastIdx = trades.size() - 1;
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        int crossIdx = MaCrossPointCore.findLatestDeathCrossIndex(trades, lastIdx, slowMa);
        if (crossIdx < 1) {
            return null;
        }
        Double breakLine = MaCrossPointCore.crossPriceAt(trades, crossIdx, slowMa);
        if (breakLine == null) {
            return null;
        }
        if (!MaCrossPointCore.passesEdgeBreak(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(period, MaCrossPointCore.CrossKind.DEATH, BreakTarget.DEATH_CROSS,
                signalBar, prevBar, trades.get(crossIdx), breakLine, null, slowMa);
    }

    static Hit findGoldenCrossPointHit(List<Trade> trades, PeriodTypeEnum period, int slowMa) {
        int lastIdx = trades.size() - 1;
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        int crossIdx = MaCrossPointCore.findLatestGoldenCrossIndex(trades, lastIdx, slowMa);
        if (crossIdx < 1) {
            return null;
        }
        Double breakLine = MaCrossPointCore.crossPriceAt(trades, crossIdx, slowMa);
        if (breakLine == null) {
            return null;
        }
        if (!MaCrossPointCore.passesEdgeBreak(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(period, MaCrossPointCore.CrossKind.GOLDEN, BreakTarget.GOLDEN_CROSS,
                signalBar, prevBar, trades.get(crossIdx), breakLine, null, slowMa);
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MaCrossPointStrategyParams params, Hit hit) {
        String tag = hit != null && hit.getCrossKind() == MaCrossPointCore.CrossKind.DEATH ? "MDB" : "MGB";
        return passesOptionalGates(stock, checkResult, params, hit, tag);
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MaCrossPointStrategyParams params, Hit hit, String tag) {
        return passesOptionalGates(stock, checkResult, params, hit, tag, false);
    }

    /**
     * @param ma10GeMa60Bull 策略5 可选均线多头用 MA10≥MA60；策略1/2 用 MA5&gt;MA60
     */
    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MaCrossPointStrategyParams params, Hit hit, String tag,
                                              boolean ma10GeMa60Bull) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        String gateTag = tag != null ? tag : "MCP";
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, gateTag, "成交额不足");
            return false;
        }
        PeriodTypeEnum msgPeriod = hit != null ? hit.getPeriod() : PeriodTypeEnum.DAY;
        if (!passMaBullGate(stock, checkResult, gateTag, msgPeriod, p.isRequireDayMaBull(),
                PeriodTypeEnum.DAY, "日", ma10GeMa60Bull)) {
            return false;
        }
        if (!passMaBullGate(stock, checkResult, gateTag, msgPeriod, p.isRequireWeekMaBull(),
                PeriodTypeEnum.WEEK, "周", ma10GeMa60Bull)) {
            return false;
        }
        if (!passMaBullGate(stock, checkResult, gateTag, msgPeriod, p.isRequireMonthMaBull(),
                PeriodTypeEnum.MONTH, "月", ma10GeMa60Bull)) {
            return false;
        }
        if (!passMaBullGate(stock, checkResult, gateTag, msgPeriod, p.isRequireQuarterMaBull(),
                PeriodTypeEnum.QUARTER, "季", ma10GeMa60Bull)) {
            return false;
        }
        if (!passMaBullGate(stock, checkResult, gateTag, msgPeriod, p.isRequireYearMaBull(),
                PeriodTypeEnum.YEAR, "年", ma10GeMa60Bull)) {
            return false;
        }
        if (!passMaBearGate(stock, checkResult, gateTag, msgPeriod, p.isRequireDayMaBear(),
                PeriodTypeEnum.DAY, "日")) {
            return false;
        }
        if (!passMaBearGate(stock, checkResult, gateTag, msgPeriod, p.isRequireWeekMaBear(),
                PeriodTypeEnum.WEEK, "周")) {
            return false;
        }
        if (!passMaBearGate(stock, checkResult, gateTag, msgPeriod, p.isRequireMonthMaBear(),
                PeriodTypeEnum.MONTH, "月")) {
            return false;
        }
        if (!passMaBearGate(stock, checkResult, gateTag, msgPeriod, p.isRequireQuarterMaBear(),
                PeriodTypeEnum.QUARTER, "季")) {
            return false;
        }
        if (!passMaBearGate(stock, checkResult, gateTag, msgPeriod, p.isRequireYearMaBear(),
                PeriodTypeEnum.YEAR, "年")) {
            return false;
        }
        return true;
    }

    private static boolean passMaBullGate(StockBase stock, CheckResult checkResult, String tag,
                                          PeriodTypeEnum msgPeriod, boolean enabled,
                                          PeriodTypeEnum period, String label, boolean ma10GeMa60Bull) {
        if (!enabled) {
            return true;
        }
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, period, 80);
        boolean ok = ma10GeMa60Bull
                ? MaCrossPointCore.passesMa10GeMa60(bars)
                : MaCrossPointCore.passesMaBull(bars);
        if (!ok) {
            String rule = ma10GeMa60Bull ? "MA10>=MA60" : "MA5>MA60";
            appendMessage(checkResult, msgPeriod, tag, "未满足" + label + "均线多头" + rule);
            return false;
        }
        return true;
    }

    private static boolean passMaBearGate(StockBase stock, CheckResult checkResult, String tag,
                                          PeriodTypeEnum msgPeriod, boolean enabled,
                                          PeriodTypeEnum period, String label) {
        if (!enabled) {
            return true;
        }
        if (!MaCrossPointCore.passesMa10LtMa60(RealtimeStockCache.getLastTrades(stock, period, 80))) {
            appendMessage(checkResult, msgPeriod, tag, "未满足" + label + "均线空头MA10<MA60");
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

    /**
     * 策略5 父级：(MA10&gt;MA60) or (MA10≤MA60 且 close&gt;max(MA5,MA10))。
     */
    public static boolean passesParentMdxGate(StockBase stock, CheckResult checkResult,
                                             String tag, PeriodTypeEnum period) {
        PeriodTypeEnum parent = resolveParentPeriod(period);
        PeriodTypeEnum msgPeriod = period != null ? period : PeriodTypeEnum.DAY;
        if (parent == null) {
            appendMessage(checkResult, msgPeriod, tag, "未满足父级MA10>MA60或均价之上");
            return false;
        }
        Trade parentBar = lastBarOf(stock, parent);
        List<Trade> parentBars = stock == null
                ? null
                : RealtimeStockCache.getLastTrades(stock, parent, 80);
        if (!passesParentMdxGateOnParent(parentBar, parentBars)) {
            appendMessage(checkResult, msgPeriod, tag,
                    "未满足父级" + periodLabel(parent) + "(MA10>MA60或均价之上)");
            return false;
        }
        return true;
    }

    static boolean passesParentMdxGateOnParent(Trade parentBar, List<Trade> parentBars) {
        int cmp = MaCrossPointCore.compareMa10ToMa60(parentBars, parentBar);
        if (cmp == 1) {
            return true;
        }
        if (cmp == 0) {
            return false;
        }
        return MaCrossPointCore.passesAboveMa(parentBar);
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

    public static String buildDeathCrossBreakTrendMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        return String.format("[MDX]死叉交叉点突破|%s边沿破DC%d,父级MA10>MA60或均价之上|breakLine=%.2f,crossDay=%s",
                periodLabel(hit.getPeriod()), hit.getSlowMa(),
                hit.getBreakLine(), dayOf(hit.getCrossBar()));
    }

    public static String buildDeathCrossBreakSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        return String.format(
                "死叉交叉点突破,strategyTag=MDX,period=%s,cross=DEATH,target=DC%d,sigDay=%s,sigClose=%.2f,breakLine=%.2f,crossDay=%s",
                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",
                hit.getSlowMa(),
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                hit.getBreakLine(),
                dayOf(hit.getCrossBar()));
    }

    public static String buildGoldenCrossBreakTrendMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        return String.format("[MGX]金叉交叉点突破|%s边沿破GC%d,父级MA10>MA60或均价之上|breakLine=%.2f,crossDay=%s",
                periodLabel(hit.getPeriod()), hit.getSlowMa(),
                hit.getBreakLine(), dayOf(hit.getCrossBar()));
    }

    public static String buildGoldenCrossBreakSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        return String.format(
                "金叉交叉点突破,strategyTag=MGX,period=%s,cross=GOLDEN,target=GC%d,sigDay=%s,sigClose=%.2f,breakLine=%.2f,crossDay=%s",
                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",
                hit.getSlowMa(),
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                hit.getBreakLine(),
                dayOf(hit.getCrossBar()));
    }

    public static String buildGoldenHighBreakTrendMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        return String.format("[MGH]金叉波段顶突破|%s边沿破GH%d,父级MA10>MA60或均价之上|breakLine=%.2f,crossDay=%s",
                periodLabel(hit.getPeriod()), hit.getSlowMa(),
                hit.getBreakLine(), dayOf(hit.getCrossBar()));
    }

    public static String buildGoldenHighBreakSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        return String.format(
                "金叉波段顶突破,strategyTag=MGH,period=%s,cross=GOLDEN,target=GH%d,sigDay=%s,sigClose=%.2f,breakLine=%.2f,crossDay=%s,bandHighDay=%s",
                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",
                hit.getSlowMa(),
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                hit.getBreakLine(),
                dayOf(hit.getCrossBar()),
                dayOf(hit.getBandHighBar()));
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
