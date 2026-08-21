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
 *   <li>金叉：边沿破金叉波段顶（阳线金叉取所在完整波段 High；阴线金叉取往前第一个完整波段 High），且末K MA5&gt;MA10</li>
 *   <li>死叉：边沿破死叉交叉点价（MA5/MA10 插值）</li>
 * </ul>
 */
public final class MaCrossPointTools {

    private MaCrossPointTools() {
    }

    @Getter
    public static final class Hit {
        private final PeriodTypeEnum period;
        private final MaCrossPointCore.CrossKind crossKind;
        private final Trade signalBar;
        private final Trade prevBar;
        private final Trade crossBar;
        private final double breakLine;
        /** 金叉命中时的波段顶参考 K，可空 */
        private final Trade bandHighBar;

        Hit(PeriodTypeEnum period, MaCrossPointCore.CrossKind crossKind,
            Trade signalBar, Trade prevBar, Trade crossBar, double breakLine, Trade bandHighBar) {
            this.period = period;
            this.crossKind = crossKind;
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
            return findGoldenBandTopHit(trades, period);
        }
        return findDeathCrossPointHit(trades, period);
    }

    /** 金叉：边沿破金叉波段顶，且本档末K MA5&gt;MA10 */
    static Hit findGoldenBandTopHit(List<Trade> trades, PeriodTypeEnum period) {
        int lastIdx = trades.size() - 1;
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        if (!MaCrossPointCore.passesMa5AboveMa10(signalBar)) {
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
        return new Hit(period, MaCrossPointCore.CrossKind.GOLDEN,
                signalBar, prevBar, crossBar, breakLine, band.getBandHighBar());
    }

    /**
     * 阳线金叉：所在完整波段；未完结则 null。
     * 阴线金叉：从该阴往前（含完结于本 K）第一个完整波段。
     */
    static YangBandTools.CompleteYangBand resolveGoldenBand(List<Trade> trades, int crossIdx, Trade crossBar) {
        if (YangBandTools.isStrictYang(crossBar)) {
            return YangBandTools.findBandContainingYangBar(trades, crossIdx);
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
        return new Hit(period, MaCrossPointCore.CrossKind.DEATH,
                signalBar, prevBar, trades.get(crossIdx), breakLine, null);
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MaCrossPointStrategyParams params, Hit hit) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        String tag = hit != null && hit.getCrossKind() == MaCrossPointCore.CrossKind.DEATH ? "MDB" : "MGB";
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, tag, "成交额不足");
            return false;
        }
        if (p.isEnableRightTrend()) {
            Trade signal = hit != null ? hit.getSignalBar() : null;
            if (!MaCrossPointCore.passesRightTrend(signal)) {
                appendMessage(checkResult, hit != null ? hit.getPeriod() : PeriodTypeEnum.DAY,
                        tag, "未满足右侧趋势MA5>MA60");
                return false;
            }
        }
        return true;
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
        return String.format("[%s]%s|%s边沿破%s%s|breakLine=%.2f,crossDay=%s",
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
        return p.getLookbackDay();
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

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period,
                                      String tag, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[" + tag + "]" + msg);
        }
    }
}
