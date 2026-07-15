package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.AllYangGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortGateTools;

public final class WaveShapeOptionalGateTools {

    private WaveShapeOptionalGateTools() {
    }

    public static boolean passGate(StockBase stock, CheckResult checkResult,
                                   boolean enableAllYangGate, boolean enableMin30Gate) {
        return passGate(stock, checkResult, enableAllYangGate, enableMin30Gate,
                false, false, 0, 0, 0);
    }

    public static boolean passGate(StockBase stock, CheckResult checkResult,
                                   boolean enableAllYangGate, boolean enableMin30Gate,
                                   boolean enableBandLastYangLowGate,
                                   int lookbackDay, int lookbackWeek, int lookbackMonth) {
        return passGate(stock, checkResult, enableAllYangGate, enableMin30Gate,
                enableBandLastYangLowGate, false, lookbackDay, lookbackWeek, lookbackMonth);
    }

    public static boolean passGate(StockBase stock, CheckResult checkResult,
                                   boolean enableAllYangGate, boolean enableMin30Gate,
                                   boolean enableBandLastYangLowGate, boolean enableYangBandTrendGate,
                                   int lookbackDay, int lookbackWeek, int lookbackMonth) {
        return passGate(stock, checkResult, enableAllYangGate, enableMin30Gate,
                enableBandLastYangLowGate, enableYangBandTrendGate, null,
                lookbackDay, lookbackWeek, lookbackMonth);
    }

    public static boolean passGate(StockBase stock, CheckResult checkResult,
                                   boolean enableAllYangGate, boolean enableMin30Gate,
                                   boolean enableBandLastYangLowGate, boolean enableYangBandTrendGate,
                                   PeriodTypeEnum yangBandTrendPeriod,
                                   int lookbackDay, int lookbackWeek, int lookbackMonth) {
        if (enableAllYangGate && !passesAllYangDwm(stock, checkResult)) {
            return false;
        }
        if (enableMin30Gate
                && !UltraShortGateTools.passes(stock, true, UltraShortStrategyParams.defaults())) {
            return false;
        }
        if (enableBandLastYangLowGate && !passesBandLastYangLowDwm(stock, checkResult,
                lookbackDay, lookbackWeek, lookbackMonth)) {
            return false;
        }
        if (enableYangBandTrendGate && !passesYangBandTrend(stock, checkResult, yangBandTrendPeriod,
                lookbackDay, lookbackWeek, lookbackMonth)) {
            return false;
        }
        return true;
    }

    private static boolean passesYangBandTrend(StockBase stock, CheckResult checkResult,
                                               PeriodTypeEnum signalPeriod,
                                               int lookbackDay, int lookbackWeek, int lookbackMonth) {
        if (signalPeriod == null) {
            return false;
        }
        boolean ok = YangBandTrendGateTools.passesSignalTier(stock, signalPeriod,
                lookbackDay, lookbackWeek, lookbackMonth);
        if (checkResult != null) {
            String label = trendGateLabel(signalPeriod, ok);
            checkResult.addTrendPeriod(signalPeriod, label);
        }
        return ok;
    }

    private static String trendGateLabel(PeriodTypeEnum period, boolean ok) {
        if (period == PeriodTypeEnum.DAY) {
            return ok ? "日趋势门" : "日趋势门未过";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return ok ? "周趋势门" : "周趋势门未过";
        }
        if (period == PeriodTypeEnum.MONTH) {
            return ok ? "月趋势门" : "月趋势门未过";
        }
        return ok ? "趋势门" : "趋势门未过";
    }

    private static boolean passesBandLastYangLowDwm(StockBase stock, CheckResult checkResult,
                                                    int lookbackDay, int lookbackWeek, int lookbackMonth) {
        boolean dayOk = WaveShapeLastLowGateTools.passesPeriod(stock, PeriodTypeEnum.DAY, lookbackDay);
        boolean weekOk = WaveShapeLastLowGateTools.passesPeriod(stock, PeriodTypeEnum.WEEK, lookbackWeek);
        boolean monthOk = WaveShapeLastLowGateTools.passesPeriod(stock, PeriodTypeEnum.MONTH, lookbackMonth);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, dayOk ? "日末阳低门" : "日末阳低门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, weekOk ? "周末阳低门" : "周末阳低门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, monthOk ? "月末阳低门" : "月末阳低门未过");
        }
        return dayOk && weekOk && monthOk;
    }

    private static boolean passesAllYangDwm(StockBase stock, CheckResult checkResult) {
        boolean dayOk = AllYangGateTools.passesPeriod(stock, PeriodTypeEnum.DAY);
        boolean weekOk = AllYangGateTools.passesPeriod(stock, PeriodTypeEnum.WEEK);
        boolean monthOk = AllYangGateTools.passesPeriod(stock, PeriodTypeEnum.MONTH);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, dayOk ? "日全阳" : "日全阳门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, weekOk ? "周全阳" : "周全阳门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, monthOk ? "月全阳" : "月全阳门未过");
        }
        return dayOk && weekOk && monthOk;
    }
}
