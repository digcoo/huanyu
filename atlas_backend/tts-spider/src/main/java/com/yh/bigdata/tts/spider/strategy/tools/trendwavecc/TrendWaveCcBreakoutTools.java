package com.yh.bigdata.tts.spider.strategy.tools.trendwavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.TrendWaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.service.KlineLoadService;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdPositiveGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 趋势内凹凸突破：趋势 MACD&gt;0 交集 + 信号周期凹/凸边沿突破。
 */
public final class TrendWaveCcBreakoutTools {

    private static final double EPS = 1e-6;
    private static final double INTRINSIC_BREAKOUT_RISE_PCT = 0.015;

    private TrendWaveCcBreakoutTools() {
    }

    public enum HitMode {
        CONCAVE_BREAKOUT, CONVEX_BREAKOUT
    }

    @Getter
    public static final class Hit {
        private final PeriodTypeEnum signalPeriod;
        private final HitMode hitMode;
        private final YangBandTools.CompleteYangBand lastBand;
        private final YangBandTools.CompleteYangBand prevBand;
        private final YangBandTools.CompleteYangBand referenceBand;
        private final Trade signalBar;
        private final Trade prevBar;
        private final Trade prevPrevBar;
        private final double breakLine;

        Hit(PeriodTypeEnum signalPeriod, HitMode hitMode,
            YangBandTools.CompleteYangBand lastBand,
            YangBandTools.CompleteYangBand prevBand,
            YangBandTools.CompleteYangBand referenceBand,
            Trade signalBar, Trade prevBar, Trade prevPrevBar, double breakLine) {
            this.signalPeriod = signalPeriod;
            this.hitMode = hitMode;
            this.lastBand = lastBand;
            this.prevBand = prevBand;
            this.referenceBand = referenceBand;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.prevPrevBar = prevPrevBar;
            this.breakLine = breakLine;
        }
    }

    public static Hit findHit(StockBase stock, TrendWaveCcBreakoutStrategyParams params) {
        TrendWaveCcBreakoutStrategyParams p = params != null ? params : TrendWaveCcBreakoutStrategyParams.defaults();
        if (stock == null) {
            return null;
        }
        PeriodTypeEnum signalPeriod = resolveSignalPeriod(p.getSignalPeriod());
        List<Trade> bars = loadSignalBars(stock, p, signalPeriod);
        if (CollectionUtils.isEmpty(bars) || bars.size() < 3) {
            return null;
        }
        return resolveHitOnBars(bars, p, signalPeriod);
    }

    static Hit resolveHitOnBars(List<Trade> bars, TrendWaveCcBreakoutStrategyParams params,
                                PeriodTypeEnum signalPeriod) {
        TrendWaveCcBreakoutStrategyParams p = params != null ? params : TrendWaveCcBreakoutStrategyParams.defaults();
        if (CollectionUtils.isEmpty(bars) || bars.size() < 3) {
            return null;
        }
        int lookback = Math.max(p.getLookbackBars(), 10);
        Trade signalBar = bars.get(bars.size() - 1);
        Trade prevBar = bars.get(bars.size() - 2);
        Trade prevPrevBar = bars.size() >= 3 ? bars.get(bars.size() - 3) : null;

        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(bars, lookback);
        if (bands.size() < 2) {
            return null;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        boolean convex = WaveShapeTools.isConvex(lastBand, prevBand);

        if (!convex) {
            Hit concaveHit = tryConcaveBreakout(signalPeriod, lastBand, prevBand, signalBar, prevBar, prevPrevBar);
            if (concaveHit != null && passesBreakoutStrength(signalBar, prevBar, prevPrevBar)) {
                return concaveHit;
            }
            return null;
        }
        Hit convexHit = tryConvexBreakout(signalPeriod, lastBand, prevBand, signalBar, prevBar, prevPrevBar);
        if (convexHit != null && passesBreakoutStrength(signalBar, prevBar, prevPrevBar)) {
            return convexHit;
        }
        return null;
    }

    public static boolean passesTrendMacdGate(StockBase stock, CheckResult checkResult,
                                              TrendWaveCcBreakoutStrategyParams params) {
        TrendWaveCcBreakoutStrategyParams p = params != null ? params : TrendWaveCcBreakoutStrategyParams.defaults();
        return MacdPositiveGateTools.passGate(stock, checkResult, p.toMacdPositiveGateParams());
    }

    /** 趋势周期展示/归类：月 &gt; 周 &gt; 日，与 MACD 门勾选一致 */
    public static PeriodTypeEnum resolvePrimaryTrendPeriod(TrendWaveCcBreakoutStrategyParams params) {
        TrendWaveCcBreakoutStrategyParams p = params != null ? params : TrendWaveCcBreakoutStrategyParams.defaults();
        if (p.isRequireMonthMacd()) {
            return PeriodTypeEnum.MONTH;
        }
        if (p.isRequireWeekMacd()) {
            return PeriodTypeEnum.WEEK;
        }
        if (p.isRequireDayMacd()) {
            return PeriodTypeEnum.DAY;
        }
        return PeriodTypeEnum.DAY;
    }

    static Hit tryConcaveBreakout(PeriodTypeEnum signalPeriod,
                                  YangBandTools.CompleteYangBand lastBand,
                                  YangBandTools.CompleteYangBand prevBand,
                                  Trade signalBar, Trade prevBar, Trade prevPrevBar) {
        double breakLine = lastBand.getBandHigh();
        if (Double.isNaN(breakLine)) {
            return null;
        }
        if (!passesBandHighEdge(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(signalPeriod, HitMode.CONCAVE_BREAKOUT, lastBand, prevBand, lastBand,
                signalBar, prevBar, prevPrevBar, breakLine);
    }

    static Hit tryConvexBreakout(PeriodTypeEnum signalPeriod,
                                 YangBandTools.CompleteYangBand lastBand,
                                 YangBandTools.CompleteYangBand prevBand,
                                 Trade signalBar, Trade prevBar, Trade prevPrevBar) {
        double breakLine = prevBand.getBandHigh();
        if (Double.isNaN(breakLine)) {
            return null;
        }
        if (!passesBandHighEdge(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(signalPeriod, HitMode.CONVEX_BREAKOUT, lastBand, prevBand, prevBand,
                signalBar, prevBar, prevPrevBar, breakLine);
    }

    static List<Trade> loadSignalBars(StockBase stock, TrendWaveCcBreakoutStrategyParams params,
                                      PeriodTypeEnum signalPeriod) {
        int lookback = Math.max(params.getLookbackBars(), 10);
        if (signalPeriod == PeriodTypeEnum.MIN60) {
            int fetchBars = Math.max(200, (params.getPrevDays() + 1) * params.getMaxBarsPerDay() + lookback + 20);
            return KlineLoadService.getLastTrades(stock, PeriodTypeEnum.MIN60, fetchBars);
        }
        int fetchBars = Math.max(lookback + 40, lookback + 2);
        return RealtimeStockCache.getLastTrades(stock, signalPeriod, fetchBars);
    }

    public static PeriodTypeEnum resolveSignalPeriod(String raw) {
        if (raw == null) {
            return PeriodTypeEnum.MIN60;
        }
        switch (raw.toLowerCase()) {
            case "day":
                return PeriodTypeEnum.DAY;
            case "week":
                return PeriodTypeEnum.WEEK;
            default:
                return PeriodTypeEnum.MIN60;
        }
    }

    static boolean passesBandHighEdge(Trade prevBar, Trade signalBar, double bandHigh) {
        if (prevBar == null || signalBar == null || Double.isNaN(bandHigh)) {
            return false;
        }
        Double prevClose = prevBar.getClose();
        Double signalClose = signalBar.getClose();
        if (prevClose == null || signalClose == null) {
            return false;
        }
        return prevClose <= bandHigh + EPS && signalClose > bandHigh + EPS;
    }

    static double barAmplitudeRate(Trade bar, Trade prevBar) {
        if (bar == null || bar.getLow() == null || bar.getHigh() == null || bar.getLow() <= 0) {
            return Double.NaN;
        }
        double rangeRate = (bar.getHigh() - bar.getLow()) / bar.getLow();
        if (bar.getOpen() == null || prevBar == null || prevBar.getClose() == null || prevBar.getClose() <= 0) {
            return rangeRate;
        }
        double gapRate = (bar.getOpen() - prevBar.getClose()) / prevBar.getClose();
        return Math.max(rangeRate, gapRate);
    }

    static boolean passesBreakoutStrength(Trade signalBar, Trade prevBar, Trade prevPrevBar) {
        if (passesAmplitudeExpand(signalBar, prevBar, prevPrevBar)) {
            return true;
        }
        double rise = BodyBarTierTools.risePct(signalBar, prevBar);
        return !Double.isNaN(rise) && rise > INTRINSIC_BREAKOUT_RISE_PCT + EPS;
    }

    static boolean passesAmplitudeExpand(Trade signalBar, Trade prevBar, Trade prevPrevBar) {
        double sigRate = barAmplitudeRate(signalBar, prevBar);
        double prevRate = barAmplitudeRate(prevBar, prevPrevBar);
        if (Double.isNaN(sigRate) || Double.isNaN(prevRate)) {
            return false;
        }
        return sigRate > prevRate + EPS;
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              TrendWaveCcBreakoutStrategyParams params, Hit hit) {
        TrendWaveCcBreakoutStrategyParams p = params != null ? params : TrendWaveCcBreakoutStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, p.getSignalPeriod(), "成交额不足");
            return false;
        }
        if (p.isEnableSignalRiseGate() && hit != null) {
            double rise = BodyBarTierTools.risePct(hit.getSignalBar(), hit.getPrevBar());
            if (Double.isNaN(rise) || rise <= p.getSignalRisePct() + EPS) {
                appendMessage(checkResult, p.getSignalPeriod(), "信号K涨幅不足");
                return false;
            }
        }
        return true;
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        Trade prevBar = hit.getPrevBar();
        double risePct = BodyBarTierTools.risePct(signalBar, prevBar);
        double sigRate = barAmplitudeRate(signalBar, prevBar);
        double prevRate = barAmplitudeRate(prevBar, hit.getPrevPrevBar());
        return String.format(
                "趋势内凹凸突破,strategyTag=TWCCB,period=%s,hitMode=%s,"
                        + "refBandFirst=%s,refBandHigh=%.2f,breakLine=%.2f,"
                        + "retestDay=%s,sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f,"
                        + "risePct=%.4f,sigAmpRate=%.4f,prevAmpRate=%.4f",
                hit.getSignalPeriod() != null ? hit.getSignalPeriod().getCode() : "",
                hit.getHitMode().name(),
                hit.getReferenceBand() != null && hit.getReferenceBand().getFirstYang() != null
                        ? dayOf(hit.getReferenceBand().getFirstYang()) : "",
                hit.getReferenceBand() != null ? hit.getReferenceBand().getBandHigh() : 0,
                hit.getBreakLine(),
                "",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                dayOf(prevBar),
                prevBar != null && prevBar.getClose() != null ? prevBar.getClose() : 0,
                Double.isNaN(risePct) ? 0 : risePct,
                Double.isNaN(sigRate) ? 0 : sigRate,
                Double.isNaN(prevRate) ? 0 : prevRate);
    }

    public static String buildTrendMessage(Hit hit, TrendWaveCcBreakoutStrategyParams params) {
        if (hit == null) {
            return "[TWCCB]趋势内凹凸突破";
        }
        TrendWaveCcBreakoutStrategyParams p = params != null ? params : TrendWaveCcBreakoutStrategyParams.defaults();
        String macdLabel = buildMacdGateLabel(p);
        String modeLabel = hit.getHitMode() == HitMode.CONVEX_BREAKOUT ? "凸破波段High" : "凹破波段High";
        String periodLabel = hit.getSignalPeriod() != null ? hit.getSignalPeriod().getDesc() : "信号";
        return String.format("[TWCCB]趋势内凹凸突破|%s,末%s%s|breakLine=%.2f",
                macdLabel, periodLabel, modeLabel, hit.getBreakLine());
    }

    private static String buildMacdGateLabel(TrendWaveCcBreakoutStrategyParams params) {
        StringBuilder sb = new StringBuilder();
        if (params.isRequireDayMacd()) {
            sb.append("日MACD>0");
        }
        if (params.isRequireWeekMacd()) {
            if (sb.length() > 0) {
                sb.append('+');
            }
            sb.append("周MACD>0");
        }
        if (params.isRequireMonthMacd()) {
            if (sb.length() > 0) {
                sb.append('+');
            }
            sb.append("月MACD>0");
        }
        return sb.length() > 0 ? sb.toString() : "日MACD>0";
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, String signalPeriod, String msg) {
        if (checkResult != null) {
            PeriodTypeEnum period = resolveSignalPeriod(signalPeriod);
            checkResult.addTrendPeriod(period, "[TWCCB]" + msg);
        }
    }
}
