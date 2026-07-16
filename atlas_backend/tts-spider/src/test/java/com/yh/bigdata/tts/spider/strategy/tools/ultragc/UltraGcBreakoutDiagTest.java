package com.yh.bigdata.tts.spider.strategy.tools.ultragc;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.indicator.XueQiuUtils;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.UltraGcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBucketTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutScanWindowTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * 单票诊断：拉雪球 Min30 复现 ultragc 命中路径。
 */
public class UltraGcBreakoutDiagTest {

    @Test
    public void diagnoseSz003021() throws Exception {
        String code = "sz003021";
        List<Trade> allBars = XueQiuUtils.getXueQiuJson(code, "30m");
        Assert.assertFalse("no min30 bars", allBars.isEmpty());

        UltraGcBreakoutStrategyParams p = UltraGcBreakoutStrategyParams.defaults();
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(allBars));

        int n = allBars.size();
        System.out.println("=== " + code + " min30 bars=" + n + " ===");
        printTail(allBars, points, 12);

        MACDIndicatorUtils.MACDPoint last = points.get(n - 1);
        System.out.printf("lastBar day=%s close=%.2f macd=%.4f%n",
                allBars.get(n - 1).getDay(), allBars.get(n - 1).getClose(), last.getMacd());

        BreakoutScanWindowTools.ScanWindow window = BreakoutScanWindowTools.buildScanWindow(
                allBars, BreakoutBucketTools::dayKey, p.getPrevDays(), p.getMaxBarsPerDay());
        System.out.println("signalUnit=" + window.getSignalUnitKey()
                + " signalBars=" + window.getSignalBars().size()
                + " windowSize=" + window.totalSize());

        UltraGcBreakoutTools.Hit hit = UltraGcBreakoutTools.findHitOnBars(allBars, points, p);
        if (hit == null) {
            System.out.println("RESULT: miss");
            explainMiss(allBars, points, p, window);
            return;
        }

        System.out.println("RESULT: hit");
        System.out.println(UltraGcBreakoutTools.buildTrendMessage(hit));
        System.out.println(UltraGcBreakoutTools.buildSignalMessage(hit));
    }

    private static void explainMiss(List<Trade> allBars, List<MACDIndicatorUtils.MACDPoint> points,
                                    UltraGcBreakoutStrategyParams p,
                                    BreakoutScanWindowTools.ScanWindow window) {
        int n = allBars.size();
        MACDIndicatorUtils.MACDPoint last = points.get(n - 1);
        if (last.getMacd() <= 0) {
            System.out.println("gate fail: last macd <= 0");
            return;
        }
        int lookback = Math.max(p.getGcLookbackBars(), 5);
        for (Trade signalBar : window.getSignalBars()) {
            int signalIdx = indexOf(allBars, signalBar);
            if (signalIdx <= 0) continue;
            Trade prevBar = allBars.get(signalIdx - 1);
            MacdCrossStructureTools.CrossBar cross = UltraGcBreakoutTools.resolveGoldenCrossBefore(
                    allBars, points, signalIdx, lookback);
            if (cross == null) {
                System.out.printf("signal %s: no golden cross in lookback%n", signalBar.getDay());
                continue;
            }
            Trade ref = cross.getBar();
            double refHigh = ref.getHigh();
            boolean edge = UltraGcBreakoutTools.passesRefHighEdge(signalBar, prevBar, refHigh);
            double rise = com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools
                    .risePct(signalBar, prevBar);
            System.out.printf("signal %s close=%.2f prev=%.2f ref=%s high=%.2f edge=%s rise=%.4f%n",
                    signalBar.getDay(), signalBar.getClose(), prevBar.getClose(),
                    ref.getDay(), refHigh, edge, rise);
        }
    }

    private static void printTail(List<Trade> bars, List<MACDIndicatorUtils.MACDPoint> points, int tail) {
        int start = Math.max(0, bars.size() - tail);
        for (int i = start; i < bars.size(); i++) {
            Trade b = bars.get(i);
            MACDIndicatorUtils.MACDPoint pt = points.get(i);
            String cross = pt.isIfRedGoldCross() ? " GOLDEN" : (pt.isIfGreenGoldCross() ? " DEATH" : "");
            System.out.printf("%3d %s o=%.2f h=%.2f l=%.2f c=%.2f macd=%.4f%s%n",
                    i, b.getDay(), b.getOpen(), b.getHigh(), b.getLow(), b.getClose(), pt.getMacd(), cross);
        }
    }

    private static int indexOf(List<Trade> bars, Trade target) {
        for (int i = 0; i < bars.size(); i++) {
            if (target.getDay().equals(bars.get(i).getDay())) return i;
        }
        return -1;
    }
}
