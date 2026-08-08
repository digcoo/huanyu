package com.yh.bigdata.tts.spider.strategy.tools.mabreakma;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.ma3m.Ma3mCore;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MaBreakMaToolsTest {

    @Test
    public void findHitOnBars_hitsEdgeBreakMaMax_ma20() {
        // 末K close>MAX，前K close<=MAX；3M1 多头
        List<Trade> bars = Arrays.asList(
                bar("d1", 10.0, 10.2, 9.8, 10.0, 10.2, 10.1, 10.0, 9.9),
                bar("d2", 10.0, 10.5, 9.9, 10.1, 10.3, 10.2, 10.0, 9.8)
        );
        // d2: maMax=max(10.3,10.2,10.0,9.8)=10.3; close=10.1 <= 10.3 → miss
        // fix: close above max
        bars = Arrays.asList(
                bar("d1", 10.0, 10.2, 9.8, 10.0, 10.2, 10.1, 10.0, 9.9),
                bar("d2", 10.0, 10.8, 9.9, 10.6, 10.3, 10.2, 10.0, 9.8)
        );
        // d2 maMax=10.3, close=10.6>10.3, d1 close=10.0<=10.3 → EDGE
        MaBreakMaTools.Hit hit = MaBreakMaTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(Ma3mCore.AlignKind.MA20, hit.getAlignKind());
        Assert.assertEquals(Ma3mCore.BreakKind.EDGE, hit.getBreakKind());
        Assert.assertEquals(10.3, hit.getBreakLine(), 1e-6);
    }

    @Test
    public void findHitOnBars_hitsOpenBreakWhenPrevAlreadyAbove() {
        // 前K已在 MAX 之上，靠开盘突破：open<=MAX < close
        List<Trade> bars = Arrays.asList(
                bar("d1", 10.0, 10.8, 9.8, 10.5, 10.2, 10.1, 10.0, 9.9),
                bar("d2", 10.0, 10.9, 9.9, 10.7, 10.3, 10.2, 10.0, 9.8)
        );
        // d2 maMax=10.3; close=10.7; prev close=10.5 > 10.3 → 非边沿；open=10.0<=10.3 → OPEN
        MaBreakMaTools.Hit hit = MaBreakMaTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(Ma3mCore.BreakKind.OPEN, hit.getBreakKind());
    }

    @Test
    public void findHitOnBars_prefersMa20WhenBothAlign() {
        List<Trade> bars = Arrays.asList(
                bar("d1", 10.0, 10.2, 9.8, 10.0, 10.2, 10.1, 10.0, 9.9),
                bar("d2", 10.0, 10.8, 9.9, 10.6, 10.3, 10.2, 10.0, 9.8)
        );
        MaBreakMaTools.Hit hit = MaBreakMaTools.findHitOnBars(bars, PeriodTypeEnum.DAY);
        Assert.assertNotNull(hit);
        Assert.assertEquals(Ma3mCore.AlignKind.MA20, hit.getAlignKind());
    }

    @Test
    public void findHitOnBars_missWhenNoBullAlign() {
        List<Trade> bars = Arrays.asList(
                bar("d1", 10.0, 10.2, 9.8, 10.0, 9.0, 9.1, 10.0, 10.5),
                bar("d2", 10.0, 10.8, 9.9, 10.6, 9.0, 9.1, 10.0, 10.5)
        );
        Assert.assertNull(MaBreakMaTools.findHitOnBars(bars, PeriodTypeEnum.DAY));
    }

    private static Trade bar(String day, double open, double high, double low, double close,
                             double ma5, double ma10, double ma20, double ma30) {
        Trade t = new Trade();
        t.setDay(day);
        t.setOpen(open);
        t.setHigh(high);
        t.setLow(low);
        t.setClose(close);
        t.setMa5(ma5);
        t.setMa10(ma10);
        t.setMa20(ma20);
        t.setMa30(ma30);
        return t;
    }
}
