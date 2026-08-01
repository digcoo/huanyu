package com.yh.bigdata.tts.spider.strategy.tools.trendma;

import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

public class TrendMaToolsTest {

    @Test
    public void passesTier_requiresMacdYangAndCloseAboveMaxMa() {
        Trade bar = bar("2026-01-10", 10.0, 11.0, 9.5, 11.5, 10.5, 10.2, 10.0, 9.8);
        Assert.assertTrue(TrendMaTools.passesCloseAboveMaxMa(bar));
        Assert.assertFalse(TrendMaTools.passesCloseAboveMaxMa(
                bar("2026-01-10", 10.0, 10.2, 9.5, 10.1, 10.5, 10.2, 10.0, 9.8)));
    }

    @Test
    public void countPassedTiers_requiresAtLeastTwo() {
        Assert.assertTrue(TrendMaTools.countPassedTiers(true, true, false) >= 2);
        Assert.assertFalse(TrendMaTools.countPassedTiers(true, false, false) >= 2);
    }

    private static Trade bar(String day, double open, double close, double low, double high,
                             double ma5, double ma10, double ma20, double ma30) {
        Trade t = new Trade(open, close, high, low);
        t.setDay(day);
        t.setMa5(ma5);
        t.setMa10(ma10);
        t.setMa20(ma20);
        t.setMa30(ma30);
        return t;
    }
}
