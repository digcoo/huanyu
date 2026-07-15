package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WeekMonthBandLowGateToolsTest {

    @Test
    public void passesWhenCloseAboveLastBandLow() {
        List<Trade> bars = buildTwoBandBars(8.0, 12.0, 15.0);
        Assert.assertTrue(WeekMonthBandLowGateTools.passesPeriodBandLowOnBars(
                bars, null, PeriodTypeEnum.WEEK, 20, "[WPG]"));
    }

    @Test
    public void failsWhenCloseBelowLastBandLow() {
        List<Trade> bars = buildTwoBandBars(8.0, 12.0, 7.0);
        Assert.assertFalse(WeekMonthBandLowGateTools.passesPeriodBandLowOnBars(
                bars, null, PeriodTypeEnum.WEEK, 20, "[WPG]"));
    }

    @Test
    public void weekMonthGateFailsWhenStockMissing() {
        Assert.assertFalse(WeekMonthBandLowGateTools.passesWeekMonthGate(
                null, null, 52, 36, "[WPG]"));
    }

    private static List<Trade> buildTwoBandBars(double band1Low, double band2Low, double lastClose) {
        List<Trade> bars = new ArrayList<>();
        bars.add(bar("2026-01-01", band1Low + 1, band1Low + 3, band1Low, band1Low + 2));
        bars.add(bar("2026-01-08", band1Low + 2, band1Low + 2.5, band1Low, band1Low));
        bars.add(bar("2026-01-15", band2Low + 1, band2Low + 3, band2Low, band2Low + 2));
        bars.add(bar("2026-01-22", band2Low + 2, band2Low + 2.5, band2Low, band2Low));
        bars.add(bar("2026-01-29", lastClose, lastClose + 1, lastClose - 1, lastClose));
        return bars;
    }

    private static Trade bar(String date, double open, double high, double low, double close) {
        Trade t = new Trade();
        t.setDay(date);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        return t;
    }
}
