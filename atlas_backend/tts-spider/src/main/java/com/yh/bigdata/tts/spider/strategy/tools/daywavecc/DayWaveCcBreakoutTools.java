package com.yh.bigdata.tts.spider.strategy.tools.daywavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.DayWaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 日凹凸突破：日/周/月 MACD 至少 2 个 &gt;0；末根日 K 凸凹边沿突破（c1 振幅扩张 或 c2 涨幅&gt;1.5%）。
 */
public final class DayWaveCcBreakoutTools {

    private static final double EPS = 1e-6;
    private static final double INTRINSIC_BREAKOUT_RISE_PCT = 0.015;
    private static final int DAY_MACD_LOOKBACK = 40;
    private static final int WEEK_MACD_LOOKBACK = 40;
    private static final int MONTH_MACD_LOOKBACK = 36;

    private DayWaveCcBreakoutTools() {
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

    public static Hit findHit(StockBase stock, DayWaveCcBreakoutStrategyParams params) {
        DayWaveCcBreakoutStrategyParams p = params != null ? params : DayWaveCcBreakoutStrategyParams.defaults();
        if (stock == null || !passesMultiPeriodMacdGate(stock)) {
            return null;
        }
        int lookback = Math.max(p.getLookbackBars(), 10);
        int fetchBars = Math.max(lookback + 40, lookback + 2);
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, fetchBars);
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }
        return resolveHitOnBars(trades, p);
    }

    static Hit resolveHitOnBars(List<Trade> trades, DayWaveCcBreakoutStrategyParams params) {
        DayWaveCcBreakoutStrategyParams p = params != null ? params : DayWaveCcBreakoutStrategyParams.defaults();
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }
        int lookback = Math.max(p.getLookbackBars(), 10);
        Trade signalBar = trades.get(trades.size() - 1);
        Trade prevBar = trades.get(trades.size() - 2);
        Trade prevPrevBar = trades.size() >= 3 ? trades.get(trades.size() - 3) : null;

        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(trades, lookback);
        if (bands.size() < 2) {
            return null;
        }
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
        if (!passesBreakoutStrength(signalBar, prevBar, prevPrevBar)) {
            return null;
        }
        return new Hit(shape, lastBand, prevBand, referenceBand, signalBar, prevBar, prevPrevBar, breakLine);
    }

    public static boolean passesMultiPeriodMacdGate(StockBase stock) {
        int positive = 0;
        if (lastMacd(stock, PeriodTypeEnum.DAY) > 0) {
            positive++;
        }
        if (lastMacd(stock, PeriodTypeEnum.WEEK) > 0) {
            positive++;
        }
        if (lastMacd(stock, PeriodTypeEnum.MONTH) > 0) {
            positive++;
        }
        return positive >= 2;
    }

    static double lastMacd(StockBase stock, PeriodTypeEnum period) {
        int lookback = DAY_MACD_LOOKBACK;
        if (period == PeriodTypeEnum.WEEK) {
            lookback = WEEK_MACD_LOOKBACK;
        } else if (period == PeriodTypeEnum.MONTH) {
            lookback = MONTH_MACD_LOOKBACK;
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
                                              DayWaveCcBreakoutStrategyParams params,
                                              Trade signalBar, Trade prevBar) {
        DayWaveCcBreakoutStrategyParams p = params != null ? params : DayWaveCcBreakoutStrategyParams.defaults();
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
                "日凹凸突破,strategyTag=DWCCB,period=day,shape=%s,"
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
            return "[DWCCB]日凹凸突破";
        }
        String shapeLabel = hit.getShape() == BandShape.CONVEX ? "凸" : "凹";
        return String.format("[DWCCB]日凹凸突破|日/周/月MACD≥2>0,末日%s破波段High|breakLine=%.2f",
                shapeLabel, hit.getBreakLine());
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, String msg) {
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "[DWCCB]" + msg);
        }
    }
}
