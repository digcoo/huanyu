package com.yh.bigdata.tts.spider.strategy.tools.cascadewave;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeOptionalGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTrendGateTools;
import lombok.Getter;

public final class CascadeWaveEvaluator {

    private CascadeWaveEvaluator() {
    }

    public static CascadeWaveEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                 boolean enableAllYangGate, boolean enableMin30Gate,
                                                 boolean enableBandLastYangLowGate, boolean enableYangBandTrendGate,
                                                 double minAvgAmount,
                                                 int lookbackDay, int lookbackWeek, int lookbackMonth,
                                                 boolean enableDay, boolean enableWeek, boolean enableMonth,
                                                 boolean enablePrevBandBreak,
                                                 WaveShapeTools.BandShape requiredShape) {
        PeriodTypeEnum trendPeriod = YangBandTrendGateTools.resolveSignalTier(
                enableDay, enableWeek, enableMonth);
        if (!WaveShapeOptionalGateTools.passGate(stock, checkResult, enableAllYangGate, enableMin30Gate,
                enableBandLastYangLowGate, enableYangBandTrendGate, trendPeriod,
                lookbackDay, lookbackWeek, lookbackMonth)) {
            return CascadeWaveEvaluation.miss();
        }
        if (!WaveShapeFilterTools.passFilters(stock, checkResult, minAvgAmount)) {
            return CascadeWaveEvaluation.miss();
        }

        CascadeWaveBreakoutTools.TierHit dayHit = null;
        CascadeWaveBreakoutTools.TierHit weekHit = null;
        CascadeWaveBreakoutTools.TierHit monthHit = null;

        if (enableDay) {
            dayHit = CascadeWaveBreakoutTools.findTierHit(stock, PeriodTypeEnum.DAY, lookbackDay,
                    requiredShape, enablePrevBandBreak);
        }
        if (enableWeek) {
            weekHit = CascadeWaveBreakoutTools.findTierHit(stock, PeriodTypeEnum.WEEK, lookbackWeek,
                    requiredShape, enablePrevBandBreak);
        }
        if (enableMonth) {
            monthHit = CascadeWaveBreakoutTools.findTierHit(stock, PeriodTypeEnum.MONTH, lookbackMonth,
                    requiredShape, enablePrevBandBreak);
        }
        if (dayHit == null && weekHit == null && monthHit == null) {
            return CascadeWaveEvaluation.miss();
        }
        return new CascadeWaveEvaluation(dayHit, weekHit, monthHit, true);
    }

    @Getter
    public static final class CascadeWaveEvaluation {
        private final CascadeWaveBreakoutTools.TierHit dayHit;
        private final CascadeWaveBreakoutTools.TierHit weekHit;
        private final CascadeWaveBreakoutTools.TierHit monthHit;
        private final boolean hit;

        CascadeWaveEvaluation(CascadeWaveBreakoutTools.TierHit dayHit,
                              CascadeWaveBreakoutTools.TierHit weekHit,
                              CascadeWaveBreakoutTools.TierHit monthHit,
                              boolean hit) {
            this.dayHit = dayHit;
            this.weekHit = weekHit;
            this.monthHit = monthHit;
            this.hit = hit;
        }

        static CascadeWaveEvaluation miss() {
            return new CascadeWaveEvaluation(null, null, null, false);
        }

        public boolean isHit() {
            return hit;
        }
    }
}
