package com.yh.bigdata.tts.spider.strategy.tools.macrosspoint;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macrosspoint.MaCrossPointEvaluator.MaCrossPointEvaluation;

/**
 * 策略2：边沿破任意金叉交叉点 GC10/GC20/GC60；
 * 本档 MACD&gt;0 或 MA5&gt;MA10；收盘价≥max(MA5,MA10)。
 */
public final class MaGoldenCrossBreakEvaluator {

    private static final String TAG = "MGX";

    private MaGoldenCrossBreakEvaluator() {
    }

    public static MaCrossPointEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                  MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = MaCrossPointTools.resolvePeriod(p.getTier());
        String miss = "未满足边沿破GC10/GC20/GC60";

        MaCrossPointTools.Hit hit = MaCrossPointTools.findAnyGoldenCrossBreakHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[" + TAG + "]" + miss);
            }
            return MaCrossPointEvaluation.miss();
        }

        if (!MaCrossPointTools.passesCurrentScanGates(stock, checkResult, p, hit, TAG, period)) {
            return MaCrossPointEvaluation.miss();
        }

        if (checkResult != null) {
            checkResult.addTrendPeriod(period, MaCrossPointTools.buildGoldenCrossBreakTrendMessage(hit));
            checkResult.addSignal(period, MaCrossPointTools.buildGoldenCrossBreakSignalMessage(hit));
        }
        return MaCrossPointEvaluation.hit(period);
    }
}
