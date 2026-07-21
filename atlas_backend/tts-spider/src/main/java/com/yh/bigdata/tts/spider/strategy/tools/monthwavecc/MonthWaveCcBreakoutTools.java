package com.yh.bigdata.tts.spider.strategy.tools.monthwavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MonthWaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 月凹凸突破：月 MACD&gt;0；月 K 凹/凸边沿破波段 High 或凸边沿回踩（信号限末根月 K）。
 */
public final class MonthWaveCcBreakoutTools {

    private static final double EPS = 1e-6;
    private static final int MONTH_MACD_LOOKBACK = 36;

    private MonthWaveCcBreakoutTools() {
    }

    public enum HitMode {
        CONCAVE_BREAKOUT, CONVEX_BREAKOUT, CONVEX_RETEST
    }

    @Getter
    public static final class Hit {
        private final HitMode hitMode;
        private final YangBandTools.CompleteYangBand lastBand;
        private final YangBandTools.CompleteYangBand prevBand;
        private final YangBandTools.CompleteYangBand referenceBand;
        private final Trade signalBar;
        private final Trade prevBar;
        private final Trade prevPrevBar;
        private final Trade retestBar;
        private final double breakLine;

        Hit(HitMode hitMode,
            YangBandTools.CompleteYangBand lastBand,
            YangBandTools.CompleteYangBand prevBand,
            YangBandTools.CompleteYangBand referenceBand,
            Trade signalBar, Trade prevBar, Trade prevPrevBar,
            Trade retestBar, double breakLine) {
            this.hitMode = hitMode;
            this.lastBand = lastBand;
            this.prevBand = prevBand;
            this.referenceBand = referenceBand;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.prevPrevBar = prevPrevBar;
            this.retestBar = retestBar;
            this.breakLine = breakLine;
        }

        public boolean isBreakout() {
            return hitMode == HitMode.CONCAVE_BREAKOUT || hitMode == HitMode.CONVEX_BREAKOUT;
        }
    }

    public static Hit findHit(StockBase stock, MonthWaveCcBreakoutStrategyParams params) {
        MonthWaveCcBreakoutStrategyParams p = params != null ? params : MonthWaveCcBreakoutStrategyParams.defaults();
        if (stock == null || !passesMonthMacdGate(stock)) {
            return null;
        }
        int lookback = Math.max(p.getLookbackBars(), 6);
        int fetchBars = Math.max(lookback + 40, lookback + 2);
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.MONTH, fetchBars);
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }
        return resolveHitOnBars(trades, p);
    }

    static Hit resolveHitOnBars(List<Trade> trades, MonthWaveCcBreakoutStrategyParams params) {
        MonthWaveCcBreakoutStrategyParams p = params != null ? params : MonthWaveCcBreakoutStrategyParams.defaults();
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }
        int lookback = Math.max(p.getLookbackBars(), 6);
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

        if (!convex) {
            Hit concaveHit = tryConcaveBreakout(lastBand, prevBand, signalBar, prevBar, prevPrevBar);
            if (concaveHit != null) {
                return concaveHit;
            }
        } else {
            Hit convexHit = tryConvexBreakout(lastBand, prevBand, signalBar, prevBar, prevPrevBar);
            if (convexHit != null) {
                return convexHit;
            }
            return tryConvexRetest(lastBand, prevBand, signalBar, prevBar, p.getMaxRetestGapPct());
        }
        return null;
    }

    static Hit tryConcaveBreakout(YangBandTools.CompleteYangBand lastBand,
                                  YangBandTools.CompleteYangBand prevBand,
                                  Trade signalBar, Trade prevBar, Trade prevPrevBar) {
        double breakLine = lastBand.getBandHigh();
        if (Double.isNaN(breakLine)) {
            return null;
        }
        if (!passesBandHighEdge(prevBar, signalBar, breakLine)) {
            return null;
        }
        if (!passesAmplitudeExpand(signalBar, prevBar, prevPrevBar)) {
            return null;
        }
        return new Hit(HitMode.CONCAVE_BREAKOUT, lastBand, prevBand, lastBand,
                signalBar, prevBar, prevPrevBar, null, breakLine);
    }

    static Hit tryConvexBreakout(YangBandTools.CompleteYangBand lastBand,
                                 YangBandTools.CompleteYangBand prevBand,
                                 Trade signalBar, Trade prevBar, Trade prevPrevBar) {
        double breakLine = prevBand.getBandHigh();
        if (Double.isNaN(breakLine)) {
            return null;
        }
        if (!passesBandHighEdge(prevBar, signalBar, breakLine)) {
            return null;
        }
        if (!passesAmplitudeExpand(signalBar, prevBar, prevPrevBar)) {
            return null;
        }
        return new Hit(HitMode.CONVEX_BREAKOUT, lastBand, prevBand, prevBand,
                signalBar, prevBar, prevPrevBar, null, breakLine);
    }

    static Hit tryConvexRetest(YangBandTools.CompleteYangBand lastBand,
                              YangBandTools.CompleteYangBand prevBand,
                              Trade signalBar, Trade prevBar, double maxRetestGapPct) {
        if (!isYangBar(signalBar)) {
            return null;
        }
        double breakLine = prevBand.getBandHigh();
        if (Double.isNaN(breakLine)) {
            return null;
        }
        Trade retestBar = null;
        if (passesLowRetestBandHigh(signalBar, breakLine, maxRetestGapPct)) {
            retestBar = signalBar;
        } else if (passesLowRetestBandHigh(prevBar, breakLine, maxRetestGapPct)) {
            retestBar = prevBar;
        }
        if (retestBar == null) {
            return null;
        }
        return new Hit(HitMode.CONVEX_RETEST, lastBand, prevBand, prevBand,
                signalBar, prevBar, null, retestBar, breakLine);
    }

    public static boolean passesMonthMacdGate(StockBase stock) {
        return lastMacd(stock) > 0;
    }

    static double lastMacd(StockBase stock) {
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.MONTH, MONTH_MACD_LOOKBACK);
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

    static boolean passesLowRetestBandHigh(Trade bar, double bandHigh, double maxGapPct) {
        if (bar == null || bar.getLow() == null || Double.isNaN(bandHigh) || bandHigh <= 0) {
            return false;
        }
        double gapPct = Math.abs(bar.getLow() - bandHigh) / bandHigh;
        return gapPct <= maxGapPct + EPS;
    }

    static boolean isYangBar(Trade bar) {
        if (bar == null || bar.getOpen() == null || bar.getClose() == null) {
            return false;
        }
        return bar.getClose() >= bar.getOpen() - EPS;
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

    static boolean passesAmplitudeExpand(Trade signalBar, Trade prevBar, Trade prevPrevBar) {
        double sigRate = barAmplitudeRate(signalBar, prevBar);
        double prevRate = barAmplitudeRate(prevBar, prevPrevBar);
        if (Double.isNaN(sigRate) || Double.isNaN(prevRate)) {
            return false;
        }
        return sigRate > prevRate + EPS;
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MonthWaveCcBreakoutStrategyParams params, Hit hit) {
        MonthWaveCcBreakoutStrategyParams p = params != null ? params : MonthWaveCcBreakoutStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, "成交额不足");
            return false;
        }
        if (p.isEnableSignalRiseGate() && hit != null && hit.isBreakout()) {
            double rise = BodyBarTierTools.risePct(hit.getSignalBar(), hit.getPrevBar());
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
                "月凹凸突破,strategyTag=MWCCB,period=month,hitMode=%s,"
                        + "refBandFirst=%s,refBandHigh=%.2f,breakLine=%.2f,"
                        + "retestDay=%s,sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f,"
                        + "risePct=%.4f,sigAmpRate=%.4f,prevAmpRate=%.4f",
                hit.getHitMode().name(),
                hit.getReferenceBand() != null && hit.getReferenceBand().getFirstYang() != null
                        ? dayOf(hit.getReferenceBand().getFirstYang()) : "",
                hit.getReferenceBand() != null ? hit.getReferenceBand().getBandHigh() : 0,
                hit.getBreakLine(),
                dayOf(hit.getRetestBar()),
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
            return "[MWCCB]月凹凸突破";
        }
        String modeLabel;
        switch (hit.getHitMode()) {
            case CONVEX_BREAKOUT:
                modeLabel = "凸破波段High";
                break;
            case CONVEX_RETEST:
                modeLabel = "凸边沿回踩";
                break;
            default:
                modeLabel = "凹破波段High";
                break;
        }
        return String.format("[MWCCB]月凹凸突破|月MACD>0,%s|breakLine=%.2f",
                modeLabel, hit.getBreakLine());
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, String msg) {
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, "[MWCCB]" + msg);
        }
    }
}
