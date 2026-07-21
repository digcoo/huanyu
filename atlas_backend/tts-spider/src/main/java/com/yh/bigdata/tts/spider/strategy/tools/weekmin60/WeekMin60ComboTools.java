package com.yh.bigdata.tts.spider.strategy.tools.weekmin60;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.WeekMin60ComboStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.service.KlineLoadService;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;
import com.yh.bigdata.tts.spider.strategy.tools.macdgcwave.MacdGcWaveBandTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBucketTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutScanWindowTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 小时周组合：周 MACD&gt;0、close&gt;近2周 low 高值；Min60 金叉波段 High 边沿突破（信号限末周线）。
 */
public final class WeekMin60ComboTools {

    private static final double EPS = 1e-6;
    private static final int WEEK_MACD_LOOKBACK = 40;

    private WeekMin60ComboTools() {
    }

    @Getter
    public static final class Hit {
        private final MacdCrossStructureTools.CrossBar crossBar;
        private final YangBandTools.CompleteYangBand referenceBand;
        private final Trade signalBar;
        private final Trade prevBar;

        Hit(MacdCrossStructureTools.CrossBar crossBar,
            YangBandTools.CompleteYangBand referenceBand,
            Trade signalBar, Trade prevBar) {
            this.crossBar = crossBar;
            this.referenceBand = referenceBand;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
        }
    }

    public static Hit findHit(StockBase stock, WeekMin60ComboStrategyParams params) {
        WeekMin60ComboStrategyParams p = params != null ? params : WeekMin60ComboStrategyParams.defaults();
        if (stock == null) {
            return null;
        }
        if (!passesWeekGate(stock)) {
            return null;
        }
        int fetchBars = Math.max(200, (p.getPrevWeeks() + 1) * p.getMaxBarsPerWeek() + p.getGcLookbackBars() + 20);
        List<Trade> allBars = KlineLoadService.getLastTrades(stock, PeriodTypeEnum.MIN60, fetchBars);
        if (CollectionUtils.isEmpty(allBars) || allBars.size() < 3) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(allBars));
        return findHitOnBars(allBars, points, p);
    }

    static Hit findHitOnBars(List<Trade> allBars, List<MACDIndicatorUtils.MACDPoint> points,
                             WeekMin60ComboStrategyParams params) {
        WeekMin60ComboStrategyParams p = params != null ? params : WeekMin60ComboStrategyParams.defaults();
        if (CollectionUtils.isEmpty(allBars) || allBars.size() < 3) {
            return null;
        }
        if (CollectionUtils.isEmpty(points) || points.size() != allBars.size()) {
            return null;
        }
        MACDIndicatorUtils.MACDPoint lastPoint = points.get(points.size() - 1);
        if (lastPoint == null || lastPoint.getMacd() <= 0) {
            return null;
        }
        if (lastPoint.isIfRedGoldCross()) {
            return null;
        }

        BreakoutScanWindowTools.ScanWindow window = BreakoutScanWindowTools.buildScanWindow(
                allBars,
                BreakoutBucketTools::weekKey,
                p.getPrevWeeks(),
                p.getMaxBarsPerWeek());
        List<Trade> signalBars = window.getSignalBars();
        if (CollectionUtils.isEmpty(signalBars)) {
            return null;
        }

        int lookback = Math.max(p.getGcLookbackBars(), 5);
        int bandLookback = lookback + 40;
        Hit lastHit = null;
        for (Trade signalBar : signalBars) {
            int signalIdx = indexOfBar(allBars, signalBar);
            if (signalIdx <= 0) {
                continue;
            }
            MACDIndicatorUtils.MACDPoint signalPoint = points.get(signalIdx);
            if (signalPoint != null && signalPoint.isIfRedGoldCross()) {
                continue;
            }
            Trade prevBar = allBars.get(signalIdx - 1);
            MacdCrossStructureTools.CrossBar cross = resolveGoldenCrossBefore(
                    allBars, points, signalIdx, lookback);
            if (cross == null || cross.getKind() != MacdCrossStructureTools.CrossKind.GOLDEN) {
                continue;
            }
            if (sameBar(cross.getBar(), signalBar)) {
                continue;
            }
            YangBandTools.CompleteYangBand referenceBand = MacdGcWaveBandTools.resolveReferenceBand(
                    allBars, cross.getBar(), bandLookback);
            if (referenceBand == null || Double.isNaN(referenceBand.getBandHigh())) {
                continue;
            }
            if (!passesBandHighEdge(prevBar, signalBar, referenceBand.getBandHigh())) {
                continue;
            }
            if (!passesAmplitudeExpand(signalBar, prevBar)) {
                continue;
            }
            lastHit = new Hit(cross, referenceBand, signalBar, prevBar);
        }
        return lastHit;
    }

    public static boolean passesWeekGate(StockBase stock) {
        List<Trade> weekBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.WEEK, WEEK_MACD_LOOKBACK);
        if (CollectionUtils.isEmpty(weekBars) || weekBars.size() < 3) {
            return false;
        }
        List<MACDIndicatorUtils.MACDPoint> weekPoints =
                MACDIndicatorUtils.calculateMACD(Ticker.from(weekBars));
        if (CollectionUtils.isEmpty(weekPoints)) {
            return false;
        }
        MACDIndicatorUtils.MACDPoint lastWeekPoint = weekPoints.get(weekPoints.size() - 1);
        if (lastWeekPoint == null || lastWeekPoint.getMacd() <= 0) {
            return false;
        }
        return passesWeekCloseAboveRecentLows(weekBars);
    }

    static boolean passesWeekCloseAboveRecentLows(List<Trade> weekBars) {
        int n = weekBars.size();
        Trade last = weekBars.get(n - 1);
        Trade w1 = weekBars.get(n - 2);
        Trade w2 = weekBars.get(n - 3);
        if (last.getClose() == null || w1.getLow() == null || w2.getLow() == null) {
            return false;
        }
        double floor = Math.max(w1.getLow(), w2.getLow());
        return last.getClose() > floor + EPS;
    }

    static MacdCrossStructureTools.CrossBar resolveGoldenCrossBefore(
            List<Trade> allBars, List<MACDIndicatorUtils.MACDPoint> points,
            int signalIdx, int lookback) {
        if (signalIdx < 1) {
            return null;
        }
        List<Trade> prefixBars = allBars.subList(0, signalIdx + 1);
        List<MACDIndicatorUtils.MACDPoint> prefixPoints = points.subList(0, signalIdx + 1);
        return MacdCrossStructureTools.findLatestCrossBar(
                prefixBars, prefixPoints, lookback, true, false);
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

    /** 振幅率 (high-low)/low */
    static double barRangePct(Trade bar) {
        if (bar == null || bar.getLow() == null || bar.getHigh() == null || bar.getLow() <= 0) {
            return Double.NaN;
        }
        return (bar.getHigh() - bar.getLow()) / bar.getLow();
    }

    static boolean passesAmplitudeExpand(Trade signalBar, Trade prevBar) {
        double sigRange = barRangePct(signalBar);
        double prevRange = barRangePct(prevBar);
        if (Double.isNaN(sigRange) || Double.isNaN(prevRange)) {
            return false;
        }
        return sigRange > prevRange + EPS;
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              WeekMin60ComboStrategyParams params,
                                              Trade signalBar, Trade prevBar) {
        WeekMin60ComboStrategyParams p = params != null ? params : WeekMin60ComboStrategyParams.defaults();
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
        Trade gcBar = hit.getCrossBar() != null ? hit.getCrossBar().getBar() : null;
        Trade signalBar = hit.getSignalBar();
        Trade prevBar = hit.getPrevBar();
        double bandHigh = hit.getReferenceBand() != null ? hit.getReferenceBand().getBandHigh() : 0;
        double risePct = BodyBarTierTools.risePct(signalBar, prevBar);
        double sigRange = barRangePct(signalBar);
        double prevRange = barRangePct(prevBar);
        return String.format(
                "小时周组合,strategyTag=WM60,period=min60,gcDay=%s,gcClose=%.2f,"
                        + "bandHigh=%.2f,sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f,"
                        + "risePct=%.4f,sigRangePct=%.4f,prevRangePct=%.4f",
                dayOf(gcBar),
                gcBar != null && gcBar.getClose() != null ? gcBar.getClose() : 0,
                bandHigh,
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                dayOf(prevBar),
                prevBar != null && prevBar.getClose() != null ? prevBar.getClose() : 0,
                Double.isNaN(risePct) ? 0 : risePct,
                Double.isNaN(sigRange) ? 0 : sigRange,
                Double.isNaN(prevRange) ? 0 : prevRange);
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[WM60]小时周组合";
        }
        return String.format("[WM60]小时周组合|周MACD>0,Min60破波段High|bandHigh=%.2f",
                hit.getReferenceBand() != null ? hit.getReferenceBand().getBandHigh() : 0);
    }

    private static int indexOfBar(List<Trade> bars, Trade target) {
        if (CollectionUtils.isEmpty(bars) || target == null || target.getDay() == null) {
            return -1;
        }
        for (int i = 0; i < bars.size(); i++) {
            Trade bar = bars.get(i);
            if (bar != null && target.getDay().equals(bar.getDay())) {
                return i;
            }
        }
        return -1;
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, String msg) {
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MIN60, "[WM60]" + msg);
        }
    }
}
