package com.yh.bigdata.tts.spider.strategy.tools.macdgcwhu;

import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdGcWaveHighLiftStrategyParams;
import com.yh.bigdata.tts.common.param.MacdPositiveGateParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MacdGcWaveHighLiftToolsTest {

    @Test
    public void hitsWhenCloseAbovePrevHighAndPrevYang() {
        List<Trade> bars = Arrays.asList(
                bar("d1", 10, 10.4, 10.6, 10),
                bar("d2", 10.5, 10.7, 10.65, 10.4));
        MacdGcWaveHighLiftTools.Hit hit = MacdGcWaveHighLiftTools.resolveHitOnBars(bars);
        Assert.assertNotNull(hit);
    }

    @Test
    public void rejectsWhenCloseNotAbovePrevHigh() {
        List<Trade> bars = Arrays.asList(
                bar("d1", 10, 10.4, 10.6, 10),
                bar("d2", 10.5, 10.55, 10.58, 10.4));
        Assert.assertNull(MacdGcWaveHighLiftTools.resolveHitOnBars(bars));
    }

    @Test
    public void rejectsWhenPrevNotYang() {
        List<Trade> bars = Arrays.asList(
                bar("d1", 10.5, 10.2, 10.6, 10),
                bar("d2", 10.3, 10.7, 10.75, 10.2));
        Assert.assertNull(MacdGcWaveHighLiftTools.resolveHitOnBars(bars));
    }

    @Test
    public void passesCloseAbovePrevHigh() {
        Trade prev = bar("d1", 10, 10.4, 10.6, 10);
        Trade signal = bar("d2", 10.5, 10.7, 10.65, 10.4);
        Assert.assertTrue(MacdGcWaveHighLiftTools.passesCloseAbovePrevHigh(signal, prev));
        Trade signalFail = bar("d2", 10.5, 10.59, 10.61, 10.4);
        Assert.assertFalse(MacdGcWaveHighLiftTools.passesCloseAbovePrevHigh(signalFail, prev));
    }

    @Test
    public void isYangBarAllowsFlatClose() {
        Trade flat = bar("d1", 10, 10, 10.2, 9.9);
        Assert.assertTrue(MacdGcWaveHighLiftTools.isYangBar(flat));
    }

    @Test
    public void toMacdPositiveGateDefaultsAllEnabled() {
        MacdPositiveGateParams gate = MacdGcWaveHighLiftTools.toMacdPositiveGate(
                MacdGcWaveHighLiftStrategyParams.defaults());
        Assert.assertTrue(gate.isRequireDayMacd());
        Assert.assertTrue(gate.isRequireWeekMacd());
        Assert.assertTrue(gate.isRequireMonthMacd());
    }

    @Test
    public void buildTrendMessageReflectsMacdGates() {
        MacdGcWaveHighLiftStrategyParams params = MacdGcWaveHighLiftStrategyParams.builder()
                .requireDayMacd(true)
                .requireWeekMacd(false)
                .requireMonthMacd(true)
                .build();
        String msg = MacdGcWaveHighLiftTools.buildTrendMessage(params);
        Assert.assertTrue(msg.contains("日MACD>0"));
        Assert.assertFalse(msg.contains("周MACD>0"));
        Assert.assertTrue(msg.contains("月MACD>0"));
    }

    @Test
    public void buildTrendMessageIncludesMin60MacdGate() {
        MacdGcWaveHighLiftStrategyParams params = MacdGcWaveHighLiftStrategyParams.builder()
                .requireMin60Macd(true)
                .requireDayMacd(false)
                .requireWeekMacd(false)
                .requireMonthMacd(false)
                .build();
        String msg = MacdGcWaveHighLiftTools.buildTrendMessage(params);
        Assert.assertTrue(msg.contains("Min60MACD>0"));
        Assert.assertFalse(msg.contains("日MACD>0"));
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
}
