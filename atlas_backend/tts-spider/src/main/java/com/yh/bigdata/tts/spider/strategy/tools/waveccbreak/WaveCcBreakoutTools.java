package com.yh.bigdata.tts.spider.strategy.tools.waveccbreak;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.WaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 凹凸突破：末波段凸 → 边沿破倒数第二波段 High；凹 → 边沿破末波段 High（MACD≤0 时凹无信号）。
 */
public final class WaveCcBreakoutTools {

    private static final double EPS = 1e-6;

    private WaveCcBreakoutTools() {
    }

    public enum BandShape {
        CONVEX, CONCAVE
    }

    @Getter
    public static final class TierHit {
        private final BandShape shape;
        private final boolean macdPositive;
        private final YangBandTools.CompleteYangBand lastBand;
        private final YangBandTools.CompleteYangBand prevBand;
        private final YangBandTools.CompleteYangBand referenceBand;
        private final Trade signalBar;
        private final Trade prevBar;
        private final double breakLine;

        TierHit(BandShape shape, boolean macdPositive,
                YangBandTools.CompleteYangBand lastBand, YangBandTools.CompleteYangBand prevBand,
                YangBandTools.CompleteYangBand referenceBand,
                Trade signalBar, Trade prevBar, double breakLine) {
            this.shape = shape;
            this.macdPositive = macdPositive;
            this.lastBand = lastBand;
            this.prevBand = prevBand;
            this.referenceBand = referenceBand;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.breakLine = breakLine;
        }
    }

    public static TierHit resolveHit(StockBase stock, WaveCcBreakoutStrategyParams params) {
        WaveCcBreakoutStrategyParams p = params != null ? params : WaveCcBreakoutStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        int lookback = resolveLookback(p);
        int fetchBars = Math.max(lookback + 40, lookback + 2);
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(trades));
        return resolveHitOnBars(trades, points, p);
    }

    static TierHit resolveHitOnBars(List<Trade> trades, List<MACDIndicatorUtils.MACDPoint> points,
                                    WaveCcBreakoutStrategyParams params) {
        WaveCcBreakoutStrategyParams p = params != null ? params : WaveCcBreakoutStrategyParams.defaults();
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }
        if (CollectionUtils.isEmpty(points) || points.size() != trades.size()) {
            return null;
        }
        int lookback = resolveLookback(p);
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(trades, lookback);
        if (bands.size() < 2) {
            return null;
        }
        Trade signalBar = trades.get(trades.size() - 1);
        Trade prevBar = trades.get(trades.size() - 2);
        MACDIndicatorUtils.MACDPoint signalPoint = points.get(points.size() - 1);
        if (signalPoint == null) {
            return null;
        }
        boolean macdPositive = signalPoint.getMacd() > 0;

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
            if (!macdPositive) {
                return null;
            }
            referenceBand = lastBand;
            breakLine = lastBand.getBandHigh();
        }
        if (Double.isNaN(breakLine)) {
            return null;
        }
        if (!passesBandHighEdge(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new TierHit(shape, macdPositive, lastBand, prevBand, referenceBand, signalBar, prevBar, breakLine);
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              PeriodTypeEnum period, WaveCcBreakoutStrategyParams params,
                                              Trade signalBar, Trade prevBar) {
        WaveCcBreakoutStrategyParams p = params != null ? params : WaveCcBreakoutStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, period, "成交额不足");
            return false;
        }
        if (p.isEnableSignalRiseGate()) {
            double signalRise = BodyBarTierTools.risePct(signalBar, prevBar);
            if (Double.isNaN(signalRise) || signalRise <= p.getSignalRisePct() + EPS) {
                appendMessage(checkResult, period, periodLabel(period) + "末K涨幅不足");
                return false;
            }
        }
        return true;
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

    public static String buildSignalMessage(PeriodTypeEnum period, TierHit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        Trade prevBar = hit.getPrevBar();
        double risePct = BodyBarTierTools.risePct(signalBar, prevBar);
        YangBandTools.CompleteYangBand refBand = hit.getReferenceBand();
        return String.format(
                "凹凸突破,strategyTag=WCCB,signalTier=%s,shape=%s,macdPositive=%s,"
                        + "refBandFirst=%s,refBandHigh=%.2f,breakLine=%.2f,"
                        + "sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f,risePct=%.4f",
                period != null ? period.getCode() : "",
                hit.getShape().name(),
                hit.isMacdPositive(),
                refBand != null && refBand.getFirstYang() != null ? dayOf(refBand.getFirstYang()) : "",
                refBand != null ? refBand.getBandHigh() : 0,
                hit.getBreakLine(),
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                dayOf(prevBar),
                prevBar != null && prevBar.getClose() != null ? prevBar.getClose() : 0,
                Double.isNaN(risePct) ? 0 : risePct);
    }

    public static PeriodTypeEnum resolvePeriod(WaveCcBreakoutStrategyParams.Tier tier) {
        if (tier == WaveCcBreakoutStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == WaveCcBreakoutStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(WaveCcBreakoutStrategyParams params) {
        WaveCcBreakoutStrategyParams p = params != null ? params : WaveCcBreakoutStrategyParams.defaults();
        if (p.getTier() == WaveCcBreakoutStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == WaveCcBreakoutStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        return p.getLookbackDay();
    }

    public static String buildTierLabel(WaveCcBreakoutStrategyParams params) {
        if (params == null || params.getTier() == null) {
            return "日";
        }
        switch (params.getTier()) {
            case WEEK:
                return "周";
            case MONTH:
                return "月";
            default:
                return "日";
        }
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[WCCB]" + msg);
        }
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
}
