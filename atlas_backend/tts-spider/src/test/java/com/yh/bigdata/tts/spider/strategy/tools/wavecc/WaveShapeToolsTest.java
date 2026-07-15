package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

public class WaveShapeToolsTest {

    @Test
    public void isConvex_usesBandHigh_notLastYangHigh() {
        Trade prevFirst = yang("2026-01-01", 10.0, 10.5, 9.8, 10.4);
        Trade prevLast = yang("2026-01-02", 10.4, 10.6, 10.2, 10.5);
        Trade prevTerm = yin("2026-01-03", 10.5, 10.4, 10.9, 10.3);
        YangBandTools.CompleteYangBand prev = band(prevFirst, prevLast, prevTerm, 10.9, 9.8);

        Trade lastFirst = yang("2026-01-04", 10.3, 10.8, 10.1, 10.7);
        Trade lastLast = yang("2026-01-05", 10.7, 11.0, 10.6, 10.9);
        Trade lastTerm = yin("2026-01-06", 10.9, 10.8, 10.7, 10.6);
        YangBandTools.CompleteYangBand last = band(lastFirst, lastLast, lastTerm, 11.0, 10.1);

        Assert.assertTrue(WaveShapeTools.isConvex(last, prev));
    }

    @Test
    public void isConvex_falseWhenBandHighNotHigher() {
        Trade prevFirst = yang("2026-01-01", 10.0, 10.5, 9.8, 10.4);
        Trade prevLast = yang("2026-01-02", 10.4, 11.5, 10.2, 11.4);
        Trade prevTerm = yin("2026-01-03", 11.4, 11.3, 11.6, 11.2);
        YangBandTools.CompleteYangBand prev = band(prevFirst, prevLast, prevTerm, 11.6, 9.8);

        Trade lastFirst = yang("2026-01-04", 11.0, 11.2, 10.9, 11.1);
        Trade lastLast = yang("2026-01-05", 11.1, 11.3, 11.0, 11.2);
        Trade lastTerm = yin("2026-01-06", 11.2, 11.1, 11.3, 11.0);
        YangBandTools.CompleteYangBand last = band(lastFirst, lastLast, lastTerm, 11.3, 10.9);

        Assert.assertFalse(WaveShapeTools.isConvex(last, prev));
    }

    private static YangBandTools.CompleteYangBand band(Trade first, Trade last, Trade term,
                                                       double high, double low) {
        return new YangBandTools.CompleteYangBand(first, last, term, term, high, low);
    }

    private static Trade yang(String day, double open, double high, double low, double close) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        return t;
    }

    private static Trade yin(String day, double open, double high, double low, double close) {
        return yang(day, open, high, low, close);
    }
}
