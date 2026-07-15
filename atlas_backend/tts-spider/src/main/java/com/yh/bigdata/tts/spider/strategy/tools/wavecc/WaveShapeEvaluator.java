package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;

public final class WaveShapeEvaluator {

    private WaveShapeEvaluator() {
    }

    public static WaveShapeEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                               boolean enableAllYangGate, boolean enableMin30Gate,
                                               double minAvgAmount,
                                               int lookbackDay, int lookbackWeek, int lookbackMonth,
                                               boolean enableDay, boolean enableWeek, boolean enableMonth,
                                               WaveShapeTools.BandShape requiredShape,
                                               WaveShapeTools.BreakLineConfig breakLine) {
        return evaluate(stock, checkResult, enableAllYangGate, enableMin30Gate, minAvgAmount,
                lookbackDay, lookbackWeek, lookbackMonth, enableDay, enableWeek, enableMonth,
                requiredShape, breakLine, WaveShapeTools.EdgeMode.TIER);
    }

    public static WaveShapeEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                               boolean enableAllYangGate, boolean enableMin30Gate,
                                               double minAvgAmount,
                                               int lookbackDay, int lookbackWeek, int lookbackMonth,
                                               boolean enableDay, boolean enableWeek, boolean enableMonth,
                                               WaveShapeTools.BandShape requiredShape,
                                               WaveShapeTools.BreakLineConfig breakLine,
                                               WaveShapeTools.EdgeMode edgeMode) {
        if (!WaveShapeOptionalGateTools.passGate(stock, checkResult, enableAllYangGate, enableMin30Gate)) {
            return WaveShapeEvaluation.miss();
        }
        if (!WaveShapeFilterTools.passFilters(stock, checkResult, minAvgAmount)) {
            return WaveShapeEvaluation.miss();
        }
        if (!WaveShapeLastLowGateTools.passesAll(stock, lookbackDay, lookbackWeek, lookbackMonth)) {
            return WaveShapeEvaluation.miss();
        }

        WaveShapeTools.TierHit dayHit = null;
        WaveShapeTools.TierHit weekHit = null;
        WaveShapeTools.TierHit monthHit = null;

        WaveShapeTools.EdgeMode mode = edgeMode != null ? edgeMode : WaveShapeTools.EdgeMode.TIER;
        WaveShapeTools.BreakLineConfig cfg = breakLine != null
                ? breakLine : WaveShapeTools.BreakLineConfig.of(false, false, false);

        if (enableDay) {
            dayHit = WaveShapeTools.findTierHit(stock, PeriodTypeEnum.DAY, lookbackDay,
                    requiredShape, cfg, mode);
        }
        if (enableWeek) {
            weekHit = WaveShapeTools.findTierHit(stock, PeriodTypeEnum.WEEK, lookbackWeek,
                    requiredShape, cfg, mode);
        }
        if (enableMonth) {
            monthHit = WaveShapeTools.findTierHit(stock, PeriodTypeEnum.MONTH, lookbackMonth,
                    requiredShape, cfg, mode);
        }
        if (dayHit == null && weekHit == null && monthHit == null) {
            return WaveShapeEvaluation.miss();
        }
        return new WaveShapeEvaluation(dayHit, weekHit, monthHit, true);
    }

    @Getter
    public static final class WaveShapeEvaluation {
        private final WaveShapeTools.TierHit dayHit;
        private final WaveShapeTools.TierHit weekHit;
        private final WaveShapeTools.TierHit monthHit;
        private final boolean hit;

        WaveShapeEvaluation(WaveShapeTools.TierHit dayHit,
                            WaveShapeTools.TierHit weekHit,
                            WaveShapeTools.TierHit monthHit,
                            boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static WaveShapeEvaluation miss() {
            return new WaveShapeEvaluation(null, null, null, false);
        }

        public boolean isHit() {
            return hit;
        }
    }
}
