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
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveCcMin60BreakoutCore;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveCcMin60BreakoutCore.BandShape;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveCcMin60BreakoutCore.Hit;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 分时凹凸突破：Min60/日/周 MACD 至少 2 个 &gt;0；Min60 凸凹边沿突破（c1 振幅扩张 或 c2 涨幅&gt;1.5%）。
 */
public final class Min60WaveCcBreakoutTools {

    private static final double EPS = 1e-6;
    private static final int DAY_MACD_LOOKBACK = 40;
    private static final int WEEK_MACD_LOOKBACK = 40;

    private Min60WaveCcBreakoutTools() {
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
        return WaveCcMin60BreakoutCore.findHitOnBars(
                allBars,
                p.getPrevDays(),
                p.getMaxBarsPerDay(),
                p.getLookbackBars(),
                p.isRequireCurrentBreakout());
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

    static double barAmplitudeRate(Trade bar, Trade prevBar) {
        return WaveCcMin60BreakoutCore.barAmplitudeRate(bar, prevBar);
    }

    static boolean passesBandHighEdge(Trade prevBar, Trade signalBar, double bandHigh) {
        return WaveCcMin60BreakoutCore.passesBandHighEdge(prevBar, signalBar, bandHigh);
    }

    static boolean passesAmplitudeExpand(Trade signalBar, Trade prevBar, Trade prevPrevBar) {
        return WaveCcMin60BreakoutCore.passesAmplitudeExpand(signalBar, prevBar, prevPrevBar);
    }

    static boolean passesBreakoutStrength(Trade signalBar, Trade prevBar, Trade prevPrevBar) {
        return WaveCcMin60BreakoutCore.passesBreakoutStrength(signalBar, prevBar, prevPrevBar);
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
