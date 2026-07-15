package com.yh.bigdata.tts.common.backtest;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PeriodBarAsOfToolsTest {

    @Test
    public void buildWeekSeriesPurelyFromDayBars() {
        List<Trade> dayBars = Arrays.asList(
                day("sh600000", "2026-06-02", 10.0, 10.5, 10.8, 9.9),
                day("sh600000", "2026-06-03", 10.5, 10.2, 10.6, 10.0),
                day("sh600000", "2026-06-04", 10.2, 10.9, 11.0, 10.1)
        );

        List<Trade> weekSeries = PeriodBarAsOfTools.buildPeriodSeriesFromDayBars(
                dayBars, "2026-06-04", PeriodTypeEnum.WEEK);

        Assert.assertEquals(1, weekSeries.size());
        Trade derived = weekSeries.get(0);
        Assert.assertEquals("2026-06-05", derived.getDay());
        Assert.assertTrue(PeriodBarAsOfTools.isInProgressBar("2026-06-04", "2026-06-05", PeriodTypeEnum.WEEK));
        Assert.assertEquals(10.0, derived.getOpen(), 1e-6);
        Assert.assertEquals(10.9, derived.getClose(), 1e-6);
    }

    @Test
    public void deriveInProgressWeekFromDayBars() {
        List<Trade> dayBars = Arrays.asList(
                day("sh600000", "2026-06-02", 10.0, 10.5, 10.8, 9.9),
                day("sh600000", "2026-06-03", 10.5, 10.2, 10.6, 10.0),
                day("sh600000", "2026-06-04", 10.2, 10.9, 11.0, 10.1)
        );

        List<Trade> weekSeries = PeriodBarAsOfTools.sliceWithDerivedInProgress(
                completedWeek("sh600000", "2026-05-30", 9.0, 9.5),
                dayBars,
                "2026-06-04",
                PeriodTypeEnum.WEEK);

        Assert.assertEquals(2, weekSeries.size());
        Trade derived = weekSeries.get(1);
        Assert.assertEquals("2026-06-05", derived.getDay());
        Assert.assertEquals(10.0, derived.getOpen(), 1e-6);
        Assert.assertEquals(10.9, derived.getClose(), 1e-6);
        Assert.assertEquals(11.0, derived.getHigh(), 1e-6);
        Assert.assertEquals(9.9, derived.getLow(), 1e-6);
    }

    @Test
    public void skipDeriveWhenCompletedWeekAlreadyInSlice() {
        List<Trade> dayBars = Arrays.asList(
                day("sh600000", "2026-06-05", 10.0, 10.5, 10.8, 9.9)
        );
        List<Trade> rawWeek = completedWeek("sh600000", "2026-06-05", 10.0, 10.5);

        List<Trade> weekSeries = PeriodBarAsOfTools.sliceWithDerivedInProgress(
                rawWeek, dayBars, "2026-06-05", PeriodTypeEnum.WEEK);

        Assert.assertEquals(1, weekSeries.size());
        Assert.assertEquals("2026-06-05", weekSeries.get(0).getDay());
        Assert.assertEquals(10.5, weekSeries.get(0).getClose(), 1e-6);
    }

    @Test
    public void deriveInProgressMonthFromDayBars() {
        List<Trade> dayBars = Arrays.asList(
                day("sh600000", "2026-06-02", 10.0, 10.5, 10.8, 9.9),
                day("sh600000", "2026-06-05", 10.5, 11.0, 11.2, 10.4)
        );

        List<Trade> monthSeries = PeriodBarAsOfTools.sliceWithDerivedInProgress(
                completedMonth("sh600000", "2026-05-31", 9.0, 9.5),
                dayBars,
                "2026-06-05",
                PeriodTypeEnum.MONTH);

        Assert.assertEquals(2, monthSeries.size());
        Trade derived = monthSeries.get(1);
        Assert.assertEquals("2026-06-30", derived.getDay());
        Assert.assertEquals(11.0, derived.getClose(), 1e-6);
    }

    private static Trade day(String code, String day, double open, double close, double high, double low) {
        Trade t = new Trade();
        t.setCode(code);
        t.setDay(day);
        t.setOpen(open);
        t.setClose(close);
        t.setHigh(high);
        t.setLow(low);
        return t;
    }

    private static List<Trade> completedWeek(String code, String friday, double open, double close) {
        List<Trade> list = new ArrayList<>();
        Trade t = day(code, friday, open, close, close, open);
        list.add(t);
        return list;
    }

    private static List<Trade> completedMonth(String code, String monthEnd, double open, double close) {
        return completedWeek(code, monthEnd, open, close);
    }
}
