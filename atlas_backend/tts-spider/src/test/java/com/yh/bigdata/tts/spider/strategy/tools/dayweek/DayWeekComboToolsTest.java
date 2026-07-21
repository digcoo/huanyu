package com.yh.bigdata.tts.spider.strategy.tools.dayweek;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.DayWeekComboStrategyParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DayWeekComboToolsTest {

    @Test
    public void passesWeekCloseAboveRecentLows() {
        List<Trade> weeks = Arrays.asList(
                bar("2026-07-04", 2.0, 2.1, 2.2, 1.9),
                bar("2026-07-11", 2.1, 2.0, 2.15, 1.95),
                bar("2026-07-18", 2.05, 2.3, 2.35, 2.0));
        Assert.assertTrue(DayWeekComboTools.passesWeekCloseAboveRecentLows(weeks));
        weeks.get(2).setClose(1.96);
        Assert.assertFalse(DayWeekComboTools.passesWeekCloseAboveRecentLows(weeks));
    }

    @Test
    public void hitsWhenDayBreaksBandHighInLastWeekWithAmplitudeExpand() {
        List<Trade> trades = buildDaySeries();
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                false, false,
                true, false,
                false, false,
                0.5, false);
        DayWeekComboTools.Hit hit = DayWeekComboTools.findHitOnBars(
                trades, points, DayWeekComboStrategyParams.builder()
                        .prevWeeks(0)
                        .maxBarsPerWeek(5)
                        .gcLookbackBars(20)
                        .enableSignalRiseGate(false)
                        .build());
        Assert.assertNotNull(hit);
        Assert.assertEquals("2026-07-17", hit.getCrossBar().getBar().getDay());
        Assert.assertEquals(13.0, hit.getReferenceBand().getBandHigh(), 1e-6);
        Assert.assertEquals("2026-07-18", hit.getSignalBar().getDay());
    }

    @Test
    public void rejectsWhenAmplitudeNotExpand() {
        List<Trade> trades = buildDaySeriesForReject();
        List<MACDIndicatorUtils.MACDPoint> points = points(trades,
                false, false,
                false, false,
                true, false,
                false, false,
                0.5, false);
        Assert.assertNull(DayWeekComboTools.findHitOnBars(
                trades, points, DayWeekComboStrategyParams.builder()
                        .prevWeeks(0)
                        .maxBarsPerWeek(5)
                        .gcLookbackBars(20)
                        .build()));
    }

    @Test
    public void passesAmplitudeExpand() {
        Trade prev = bar("2026-07-17", 11.0, 11.1, 13.5, 11);
        Trade sig = bar("2026-07-18", 13.05, 13.08, 13.12, 13.04);
        Assert.assertTrue(DayWeekComboTools.passesAmplitudeExpand(sig, prev));
    }

    private static List<Trade> buildDaySeries() {
        return Arrays.asList(
                bar("2026-07-14", 10, 11, 11.5, 10),
                bar("2026-07-15", 11, 12, 12.5, 11),
                bar("2026-07-16", 12, 11.5, 13, 11),
                bar("2026-07-17", 11.5, 11.2, 11.5, 11),
                bar("2026-07-18", 13.05, 13.08, 13.12, 13.04));
    }

    private static List<Trade> buildDaySeriesForReject() {
        return Arrays.asList(
                bar("2026-07-14", 10, 11, 11.5, 10),
                bar("2026-07-15", 11, 12, 12.5, 11),
                bar("2026-07-16", 12, 11.5, 13, 11),
                bar("2026-07-17", 11.0, 11.1, 13.5, 11),
                bar("2026-07-18", 13.05, 13.08, 13.12, 13.04));
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
