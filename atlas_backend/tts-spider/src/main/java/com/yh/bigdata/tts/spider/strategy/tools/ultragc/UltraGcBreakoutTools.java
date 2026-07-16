package com.yh.bigdata.tts.spider.strategy.tools.ultragc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.UltraGcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBucketTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutScanWindowTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Min30 MACD 金叉 K high 边沿突破：ref 可来自前几日，信号须在末交易日 Min30 序列内。
 */
public final class UltraGcBreakoutTools {

    private static final double EPS = 1e-6;

    private UltraGcBreakoutTools() {
    }

    @Getter
    public static final class Hit {
        private final MacdCrossStructureTools.CrossBar crossBar;
        private final Trade signalBar;
        private final Trade prevBar;
        private final int scanWindowSize;
        private final int signalBarCount;

        Hit(MacdCrossStructureTools.CrossBar crossBar, Trade signalBar, Trade prevBar,
            int scanWindowSize, int signalBarCount) {
            this.crossBar = crossBar;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.scanWindowSize = scanWindowSize;
            this.signalBarCount = signalBarCount;
        }

        public Trade getReferenceBar() {
            return crossBar != null ? crossBar.getBar() : null;
        }
    }

    public static Hit findHit(StockBase stock, UltraGcBreakoutStrategyParams params) {
        UltraGcBreakoutStrategyParams p = params != null ? params : UltraGcBreakoutStrategyParams.defaults();
        if (stock == null) {
            return null;
        }
        int fetchBars = Math.max(50, (p.getPrevDays() + 1) * p.getMaxBarsPerDay() + 10);
        List<Trade> allBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.MIN30, fetchBars);
        if (CollectionUtils.isEmpty(allBars) || allBars.size() < 3) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(allBars));
        return findHitOnBars(allBars, points, p);
    }

    static Hit findHitOnBars(List<Trade> allBars, List<MACDIndicatorUtils.MACDPoint> points,
                             UltraGcBreakoutStrategyParams params) {
        UltraGcBreakoutStrategyParams p = params != null ? params : UltraGcBreakoutStrategyParams.defaults();
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

        BreakoutScanWindowTools.ScanWindow window = BreakoutScanWindowTools.buildScanWindow(
                allBars,
                BreakoutBucketTools::dayKey,
                p.getPrevDays(),
                p.getMaxBarsPerDay());
        List<Trade> signalBars = window.getSignalBars();
        if (CollectionUtils.isEmpty(signalBars)) {
            return null;
        }

        int lookback = Math.max(p.getGcLookbackBars(), 5);
        Hit lastHit = null;
        for (Trade signalBar : signalBars) {
            int signalIdx = indexOfBar(allBars, signalBar);
            if (signalIdx <= 0) {
                continue;
            }
            Trade prevBar = allBars.get(signalIdx - 1);
            MacdCrossStructureTools.CrossBar cross = resolveGoldenCrossBefore(
                    allBars, points, signalIdx, lookback);
            if (cross == null || cross.getBar() == null || cross.getBar().getHigh() == null) {
                continue;
            }
            Trade refBar = cross.getBar();
            int refIdx = indexOfBar(allBars, refBar);
            if (refIdx < 0 || signalIdx <= refIdx) {
                continue;
            }
            if (sameBar(refBar, signalBar) || sameBar(refBar, prevBar)) {
                continue;
            }
            if (!passesRefHighEdge(signalBar, prevBar, refBar.getHigh())) {
                continue;
            }
            double rise = BodyBarTierTools.risePct(signalBar, prevBar);
            if (Double.isNaN(rise) || rise <= p.getSignalRisePct() + EPS) {
                continue;
            }
            lastHit = new Hit(cross, signalBar, prevBar, window.totalSize(), signalBars.size());
        }
        return lastHit;
    }

    static MacdCrossStructureTools.CrossBar resolveGoldenCrossBefore(
            List<Trade> allBars, List<MACDIndicatorUtils.MACDPoint> points,
            int signalIdx, int lookback) {
        if (signalIdx < 1) {
            return null;
        }
        List<Trade> prefixBars = allBars.subList(0, signalIdx + 1);
        List<MACDIndicatorUtils.MACDPoint> prefixPoints = points.subList(0, signalIdx + 1);
        MacdCrossStructureTools.CrossBar cross = MacdCrossStructureTools.findLatestCrossBar(
                prefixBars, prefixPoints, lookback, true, false);
        if (cross == null || cross.getKind() != MacdCrossStructureTools.CrossKind.GOLDEN) {
            return null;
        }
        return cross;
    }

    static boolean passesRefHighEdge(Trade signalBar, Trade prevBar, double refHigh) {
        if (signalBar == null || prevBar == null || Double.isNaN(refHigh)) {
            return false;
        }
        Double signalClose = signalBar.getClose();
        Double prevClose = prevBar.getClose();
        if (signalClose == null || prevClose == null) {
            return false;
        }
        return prevClose <= refHigh + EPS && signalClose > refHigh + EPS;
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null || hit.getReferenceBar() == null || hit.getSignalBar() == null) {
            return "";
        }
        Trade ref = hit.getReferenceBar();
        Trade sig = hit.getSignalBar();
        Trade prev = hit.getPrevBar();
        double risePct = BodyBarTierTools.risePct(sig, prev);
        return String.format(
                "MACD金叉K突破,strategyTag=ULGC,period=min30,gcDay=%s,gcHigh=%.2f,"
                        + "sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f,risePct=%.4f",
                dayOf(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                dayOf(sig),
                sig.getClose() != null ? sig.getClose() : 0,
                dayOf(prev),
                prev != null && prev.getClose() != null ? prev.getClose() : 0,
                Double.isNaN(risePct) ? 0 : risePct);
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null || hit.getReferenceBar() == null) {
            return "[ULGC]MACD金叉K突破|30m边沿破金叉K high";
        }
        Trade ref = hit.getReferenceBar();
        return String.format("[ULGC]MACD金叉K突破|30m边沿破金叉K high|gcDay=%s,gcHigh=%.2f,窗口%d根",
                dayOf(ref),
                ref.getHigh() != null ? ref.getHigh() : 0,
                hit.getScanWindowSize());
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
}
