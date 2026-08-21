package com.yh.bigdata.tts.spider.strategy.tools.prevbandhigh;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class PrevBandHighToolsTest {

    @Test
    public void findHitOnBars_edgeBreaksLastBandHigh() {
        // 末完整波段 high=10.5；边沿破 10.5
        List<Trade> bars = Arrays.asList(
                ohlc("d1", 10.0, 11.0, 9.9, 10.5), // 阳
                ohlc("d2", 10.5, 10.6, 10.0, 10.1), // 阴完结 → 可有可无的前波段
                ohlc("d3", 10.0, 10.5, 9.8, 10.2), // 阳
                ohlc("d4", 10.2, 10.3, 9.9, 10.0), // 阴完结 → 末波段 high=10.5
                ohlc("d5", 10.0, 10.4, 9.9, 10.2), // prev close <= 10.5
                ohlc("d6", 10.2, 11.0, 10.1, 10.8)  // edge break 10.5
        );
        PrevBandHighTools.Hit hit = PrevBandHighTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 40);
        Assert.assertNotNull(hit);
        Assert.assertEquals(10.5, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void findHitOnBars_hitsWithSingleCompleteBand() {
        List<Trade> bars = Arrays.asList(
                ohlc("d1", 10.0, 10.5, 9.9, 10.2),
                ohlc("d2", 10.2, 10.3, 9.9, 10.0),
                ohlc("d3", 10.0, 10.4, 9.8, 10.1),
                ohlc("d4", 10.1, 10.8, 10.0, 10.6)
        );
        PrevBandHighTools.Hit hit = PrevBandHighTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 40);
        Assert.assertNotNull(hit);
        Assert.assertEquals(10.5, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void findHitOnBars_missWhenNoEdge() {
        List<Trade> bars = Arrays.asList(
                ohlc("d1", 10.0, 10.5, 9.9, 10.2),
                ohlc("d2", 10.2, 10.3, 9.9, 10.0),
                ohlc("d3", 10.0, 10.8, 9.9, 10.6),
                ohlc("d4", 10.6, 11.0, 10.5, 10.9)
        );
        Assert.assertNull(PrevBandHighTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 40));
    }

    @Test
    public void findHitOnBars_missWhenNoCompleteBand() {
        List<Trade> bars = Arrays.asList(
                ohlc("d1", 10.0, 10.5, 9.9, 10.2),
                ohlc("d2", 10.2, 10.6, 10.0, 10.4),
                ohlc("d3", 10.4, 10.8, 10.1, 10.7)
        );
        Assert.assertNull(PrevBandHighTools.findHitOnBars(bars, PeriodTypeEnum.DAY, 40));
    }

    private static Trade ohlc(String day, double open, double high, double low, double close) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        return t;
    }
}
