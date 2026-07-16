package com.yh.bigdata.tts.spider.strategy.tools.macdgcwhu;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdGcWaveHighLiftStrategyParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MacdGcWaveHighLiftToolsTest {

    @Test
    public void hitsWithBandHighAndFirstPrevHighLift() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 13.6, 11),
                bar("d5", 13.2, 13.25, 13.4, 13.1),
                bar("d6", 13.35, 13.5, 13.55, 13.3));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                false, false,
                true, false,
                false, false,
                false, false,
                0.5, false);
        MacdGcWaveHighLiftTools.TierHit hit = MacdGcWaveHighLiftTools.resolveHitOnBars(
                trades, points, MacdGcWaveHighLiftStrategyParams.builder()
                        .enableMinAmountFilter(false)
                        .enableSignalRiseGate(false)
                        .build());
        Assert.assertNotNull(hit);
        Assert.assertFalse(hit.isGcHighFallback());
        Assert.assertEquals(13.0, hit.getPriceFloor(), 1e-6);
    }

    @Test
    public void hitsWithGcHighFallbackWhenBandIncomplete() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12.2, 12.5, 11),
                bar("d3", 12.0, 12.1, 13.0, 12.0),
                bar("d4", 12.55, 12.58, 12.6, 12.52),
                bar("d5", 12.58, 12.62, 12.65, 12.57));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                true, false,
                false, false,
                false, false,
                0.5, false);
        MacdGcWaveHighLiftTools.TierHit hit = MacdGcWaveHighLiftTools.resolveHitOnBars(
                trades, points, MacdGcWaveHighLiftStrategyParams.builder().build());
        Assert.assertNotNull(hit);
        Assert.assertTrue(hit.isGcHighFallback());
        Assert.assertEquals(12.5, hit.getPriceFloor(), 1e-6);
    }

    @Test
    public void rejectsWhenPrevCloseNotAboveBandHigh() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 13.6, 11),
                bar("d5", 11.3, 11.4, 11.6, 11.2),
                bar("d6", 13.0, 13.1, 13.15, 12.9));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                false, false,
                true, false,
                false, false,
                false, false,
                0.5, false);
        Assert.assertNull(MacdGcWaveHighLiftTools.resolveHitOnBars(
                trades, points, MacdGcWaveHighLiftStrategyParams.builder().build()));
    }

    @Test
    public void rejectsWhenPrevAlreadyAbovePrevPrevHigh() {
        List<Trade> trades = Arrays.asList(
                bar("d1", 10, 11, 11.5, 10),
                bar("d2", 11, 12, 12.5, 11),
                bar("d3", 12, 11.5, 13, 11),
                bar("d4", 11.5, 11.2, 11.5, 11),
                bar("d5", 12.95, 13.05, 13.1, 12.9),
                bar("d6", 13.1, 13.2, 13.25, 13.05));
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                false, false,
                true, false,
                false, false,
                false, false,
                0.5, false);
        Assert.assertNull(MacdGcWaveHighLiftTools.resolveHitOnBars(
                trades, points, MacdGcWaveHighLiftStrategyParams.builder().build()));
    }

    @Test
    public void passesFirstPrevHighLift() {
        Trade prevPrev = bar("d1", 10, 10, 10.5, 9.8);
        Trade prev = bar("d2", 10, 10.4, 10.6, 10);
        Trade signal = bar("d3", 10.5, 10.7, 10.8, 10.5);
        Assert.assertTrue(MacdGcWaveHighLiftTools.passesFirstPrevHighLift(signal, prev, prevPrev));
        Trade prevBroken = bar("d2", 10, 10.6, 10.8, 10);
        Assert.assertFalse(MacdGcWaveHighLiftTools.passesFirstPrevHighLift(signal, prevBroken, prevPrev));
    }

    private static Trade bar(String day, double open, double close, double high, double low) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setClose(close);
        t.setHigh(high);
        t.setLow(low);
        return t;
    }

    private static List<MACDIndicatorUtils.MACDPoint> points(List<Trade> trades, Object... flags) {
        List<MACDIndicatorUtils.MACDPoint> list = new ArrayList<>();
        for (int i = 0; i < trades.size(); i++) {
            Object f0 = flags[i * 2];
            Object f1 = flags[i * 2 + 1];
            if (f0 instanceof Number) {
                list.add(point(trades.get(i), ((Number) f0).doubleValue(), false, false));
            } else {
                list.add(point(trades.get(i), 0, (Boolean) f0, (Boolean) f1));
            }
        }
        return list;
    }

    private static MACDIndicatorUtils.MACDPoint point(Trade trade, double macd,
                                                      boolean redGold, boolean greenGold) {
        Ticker ticker = Ticker.from(trade);
        MACDIndicatorUtils.MACDPoint pt = new MACDIndicatorUtils.MACDPoint(
                ticker, macd, 0, 0, false, false, false);
        if (redGold) {
            pt.setIfGoldCross(true);
            pt.setIfGreenGoldCross(false);
        } else if (greenGold) {
            pt.setIfGoldCross(true);
            pt.setIfGreenGoldCross(true);
        }
        return pt;
    }
}
