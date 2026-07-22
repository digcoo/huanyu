package com.yh.bigdata.tts.spider.strategy.tools.min60wavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.Min60WaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.service.KlineLoadService;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 分时凹凸突破：Min60/日/周 MACD 至少 2 个 &gt;0；末根 Min60 凸凹边沿突破（c1 振幅扩张 或 c2 涨幅&gt;1.5%）。
 */
public final class Min60WaveCcBreakoutTools {

    private static final double EPS = 1e-6;
    private static final double INTRINSIC_BREAKOUT_RISE_PCT = 0.015;
    private static final int DAY_MACD_LOOKBACK = 40;
    private static final int WEEK_MACD_LOOKBACK = 40;

    private Min60WaveCcBreakoutTools() {
    }

    public enum BandShape {
        CONVEX, CONCAVE
    }

    @Getter
    public static final class Hit {
        private final BandShape shape;
        private final YangBandTools.CompleteYangBand lastBand;
        private final YangBandTools.CompleteYangBand prevBand;
        private final YangBandTools.CompleteYangBand referenceBand;
        private final Trade signalBar;
        private final Trade prevBar;
        private final Trade prevPrevBar;
        private final double breakLine;

        Hit(BandShape shape,
            YangBandTools.CompleteYangBand lastBand,
            YangBandTools.CompleteYangBand prevBand,
            YangBandTools.CompleteYangBand referenceBand,
            Trade signalBar, Trade prevBar, Trade prevPrevBar, double breakLine) {
            this.shape = shape;
            this.lastBand = lastBand;
            this.prevBand = prevBand;
            this.referenceBand = referenceBand;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.prevPrevBar = prevPrevBar;
            this.breakLine = breakLine;
        }
    }

    public static Hit findHit(StockBase stock, Min60WaveCcBreakoutStrategyParams params) {
        Min60WaveCcBreakoutStrategyParams p = params != null ? params : Min60WaveCcBreakoutStrategyParams.defaults();
        if (stock == null || !passesMultiPeriodMacdGate(stock)) {
            return null;
        }
        int fetchBars = Math.max(200, (p.getPrevDays() + 1) * p.getMaxBarsPerDay() + p.getLookbackBars() + 20);
        List<Trade> allBars = KlineLoadService.getLastTrades(stock, PeriodTypeEnum.MIN60, fetchBars);
        if (CollectionUtils.isEmpty(allBars) || allBars.size() < 3) {
            return null;
        }
        return findHitOnBars(allBars, p);
    }

    static Hit findHitOnBars(List<Trade> allBars, Min60WaveCcBreakoutStrategyParams params) {
        Min60WaveCcBreakoutStrategyParams p = params != null ? params : Min60WaveCcBreakoutStrategyParams.defaults();
        if (CollectionUtils.isEmpty(allBars) || allBars.size() < 3) {
            return null;
        }
        int signalIdx = allBars.size() - 1;
        Trade signalBar = allBars.get(signalIdx);
        Trade prevBar = allBars.get(signalIdx - 1);
        Trade prevPrevBar = signalIdx >= 2 ? allBars.get(signalIdx - 2) : null;
        int lookback = Math.max(p.getLookbackBars(), 10);
        Hit hit = resolveHitAtIndex(allBars, signalIdx, lookback);
        if (hit == null) {
            return null;
        }
        if (!passesBreakoutStrength(signalBar, prevBar, prevPrevBar)) {
            return null;
        }
        return new Hit(hit.getShape(), hit.getLastBand(), hit.getPrevBand(), hit.getReferenceBand(),
                hit.getSignalBar(), hit.getPrevBar(), prevPrevBar, hit.getBreakLine());
    }

    static Hit resolveHitAtIndex(List<Trade> allBars, int signalIdx, int lookback) {
        if (signalIdx <= 0 || CollectionUtils.isEmpty(allBars)) {
            return null;
        }
        List<Trade> prefix = allBars.subList(0, signalIdx + 1);
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(prefix, lookback);
        if (bands.size() < 2) {
            return null;
        }
        Trade signalBar = allBars.get(signalIdx);
        Trade prevBar = allBars.get(signalIdx - 1);
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        boolean convex = WaveShapeTools.isConvex(lastBand, prevBand);
        BandShape shape = convex ? BandShape.CONVEX : BandShape.CONCAVE;

        YangBandTools.CompleteYangBand referenceBand;
        double breakLine;
        if (convex) {
            referenceBand = prevBand;
            breakLine = prevBand.getBandHigh();
        } else {
            referenceBand = lastBand;
            breakLine = lastBand.getBandHigh();
        }
        if (Double.isNaN(breakLine)) {
            return null;
        }
        if (!passesBandHighEdge(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(shape, lastBand, prevBand, referenceBand, signalBar, prevBar, null, breakLine);
    }

    public static boolean passesMultiPeriodMacdGate(StockBase stock) {
        int positive = 0;
        if (lastMacd(stock, PeriodTypeEnum.MIN60) > 0) {
            positive++;
        }
        if (lastMacd(stock, PeriodTypeEnum.DAY) > 0) {
            positive++;
        }
        if (lastMacd(stock, PeriodTypeEnum.WEEK) > 0) {
            positive++;
        }
        return positive >= 2;
    }

    static double lastMacd(StockBase stock, PeriodTypeEnum period) {
        int lookback = period == PeriodTypeEnum.WEEK ? WEEK_MACD_LOOKBACK : DAY_MACD_LOOKBACK;
        if (period == PeriodTypeEnum.MIN60) {
            List<Trade> bars = KlineLoadService.getLastTrades(stock, PeriodTypeEnum.MIN60, 80);
            if (CollectionUtils.isEmpty(bars)) {
                return Double.NaN;
            }
            List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(bars));
            if (CollectionUtils.isEmpty(points)) {
                return Double.NaN;
            }
            MACDIndicatorUtils.MACDPoint last = points.get(points.size() - 1);
            return last != null ? last.getMacd() : Double.NaN;
        }
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, period, lookback);
        if (CollectionUtils.isEmpty(bars)) {
            return Double.NaN;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(bars));
        if (CollectionUtils.isEmpty(points)) {
            return Double.NaN;
        }
        MACDIndicatorUtils.MACDPoint last = points.get(points.size() - 1);
        return last != null ? last.getMacd() : Double.NaN;
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

    /** 振幅率 max((high-low)/low, (open-lastClose)/lastClose) */
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
                                              Min60WaveCcBreakoutStrategyParams params,
                                              Trade signalBar, Trade prevBar) {
        Min60WaveCcBreakoutStrategyParams p = params != null ? params : Min60WaveCcBreakoutStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, "成交额不足");
            return false;
        }
        if (p.isEnableSignalRiseGate()) {
            double rise = BodyBarTierTools.risePct(signalBar, prevBar);
            if (Double.isNaN(rise) || rise <= p.getSignalRisePct() + EPS) {
                appendMessage(checkResult, "突破K涨幅不足");
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
                "分时凹凸突破,strategyTag=M60WCCB,period=min60,shape=%s,"
                        + "refBandFirst=%s,refBandHigh=%.2f,breakLine=%.2f,"
                        + "sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f,"
                        + "risePct=%.4f,sigAmpRate=%.4f,prevAmpRate=%.4f",
                hit.getShape().name(),
                hit.getReferenceBand() != null && hit.getReferenceBand().getFirstYang() != null
                        ? dayOf(hit.getReferenceBand().getFirstYang()) : "",
                hit.getReferenceBand() != null ? hit.getReferenceBand().getBandHigh() : 0,
                hit.getBreakLine(),
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                dayOf(prevBar),
                prevBar != null && prevBar.getClose() != null ? prevBar.getClose() : 0,
                Double.isNaN(risePct) ? 0 : risePct,
                Double.isNaN(sigRate) ? 0 : sigRate,
                Double.isNaN(prevRate) ? 0 : prevRate);
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[M60WCCB]分时凹凸突破";
        }
        String shapeLabel = hit.getShape() == BandShape.CONVEX ? "凸" : "凹";
        return String.format("[M60WCCB]分时凹凸突破|Min60/日/周MACD≥2>0,末Min60%s破波段High|breakLine=%.2f",
                shapeLabel, hit.getBreakLine());
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, String msg) {
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MIN60, "[M60WCCB]" + msg);
        }
    }
}
