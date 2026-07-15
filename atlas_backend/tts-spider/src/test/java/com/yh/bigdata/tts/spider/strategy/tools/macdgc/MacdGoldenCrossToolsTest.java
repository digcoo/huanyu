package com.yh.bigdata.tts.spider.strategy.tools.macdgc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdGoldenCrossStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class MacdGoldenCrossToolsTest {

    @Test
    public void passesSignalRiseGateWhenAboveThreshold() {
        Trade prev = bar(10.0, 10.0);
        Trade signal = bar(10.2, 10.4);
        MacdGoldenCrossStrategyParams p = MacdGoldenCrossStrategyParams.builder()
                .enableMinAmountFilter(false)
                .enableHistoryRiseGate(false)
                .enableSignalRiseGate(true)
                .signalRisePct(0.03)
                .build();
        Assert.assertTrue(MacdGoldenCrossTools.passesTierGateOnBars(
                Arrays.asList(prev, signal), new CheckResult("sz000001", 0.01),
                PeriodTypeEnum.DAY, p));
    }

    @Test
    public void failsSignalRiseGateWhenBelowThreshold() {
        Trade prev = bar(10.0, 10.0);
        Trade signal = bar(10.1, 10.25);
        MacdGoldenCrossStrategyParams p = MacdGoldenCrossStrategyParams.builder()
                .enableMinAmountFilter(false)
                .enableHistoryRiseGate(false)
                .enableSignalRiseGate(true)
                .signalRisePct(0.03)
                .build();
        Assert.assertFalse(MacdGoldenCrossTools.passesTierGateOnBars(
                Arrays.asList(prev, signal), new CheckResult("sz000001", 0.01),
                PeriodTypeEnum.DAY, p));
    }

    @Test
    public void passesHistoryRiseGateWhenAnyBarQualifies() {
        Trade b0 = bar(9.0, 9.0);
        Trade b1 = bar(9.0, 9.0);
        Trade b2 = bar(9.0, 9.0);
        Trade b3 = bar(9.0, 9.0);
        Trade b4 = bar(9.0, 9.31);
        Trade b5 = bar(9.2, 9.25);
        Trade signal = bar(9.25, 9.3);
        MacdGoldenCrossStrategyParams p = MacdGoldenCrossStrategyParams.builder()
                .enableMinAmountFilter(false)
                .enableSignalRiseGate(false)
                .enableHistoryRiseGate(true)
                .historyLookbackBars(5)
                .historyRisePct(0.03)
                .build();
        Assert.assertTrue(MacdGoldenCrossTools.passesTierGateOnBars(
                Arrays.asList(b0, b1, b2, b3, b4, b5, signal), new CheckResult("sz000001", 0.01),
                PeriodTypeEnum.DAY, p));
    }

    @Test
    public void failsHistoryRiseGateWhenNoBarQualifies() {
        Trade b0 = bar(10.0, 10.0);
        Trade b1 = bar(10.0, 10.0);
        Trade b2 = bar(10.0, 10.0);
        Trade b3 = bar(10.0, 10.0);
        Trade b4 = bar(10.0, 10.0);
        Trade b5 = bar(10.0, 10.0);
        Trade signal = bar(10.0, 10.0);
        MacdGoldenCrossStrategyParams p = MacdGoldenCrossStrategyParams.builder()
                .enableMinAmountFilter(false)
                .enableSignalRiseGate(false)
                .enableHistoryRiseGate(true)
                .historyLookbackBars(5)
                .historyRisePct(0.03)
                .build();
        Assert.assertFalse(MacdGoldenCrossTools.passesTierGateOnBars(
                Arrays.asList(b0, b1, b2, b3, b4, b5, signal), new CheckResult("sz000001", 0.01),
                PeriodTypeEnum.DAY, p));
    }

    private static Trade bar(double open, double close) {
        Trade t = new Trade();
        t.setOpen(open);
        t.setClose(close);
        return t;
    }
}
