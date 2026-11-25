package com.yh.bigdata.tts.spider.strategy.tools.macrosspoint;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macrosspoint.MaCrossPointEvaluator.MaCrossPointEvaluation;

/**
 * 策略5：边沿破前1完整阳波段顶，且本档 MA5&lt;MA60；
 * 本档 MACD&gt;0 或 MA5&gt;MA10；收盘价≥max(MA5,MA10)；父级收盘≥MA5。
 */
public final class MaPrevHighBreakEvaluator {

    private static final String TAG = "MPH";

    private MaPrevHighBreakEvaluator() {
    }

    public static MaCrossPointEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                  MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = MaCrossPointTools.resolvePeriod(p.getTier());
        String miss = "未满足边沿破前1波段顶";

        MaCrossPointTools.Hit hit = MaCrossPointTools.findPrevBandHighHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[" + TAG + "]" + miss);
            }
            return MaCrossPointEvaluation.miss();
        }

        if (!MaCrossPointTools.passesCurrentMa5LtMa60(stock, checkResult, hit, TAG, period)) {
            return MaCrossPointEvaluation.miss();
        }

        if (!MaCrossPointTools.passesCurrentScanGates(stock, checkResult, p, hit, TAG, period)) {
            return MaCrossPointEvaluation.miss();
        }

        if (!MaCrossPointTools.passesParentCloseGeMa5(stock, checkResult, TAG, period)) {
            return MaCrossPointEvaluation.miss();
        }

        if (checkResult != null) {
            checkResult.addTrendPeriod(period, MaCrossPointTools.buildPrevBandHighTrendMessage(hit));
            checkResult.addSignal(period, MaCrossPointTools.buildPrevBandHighSignalMessage(hit));
        }
        return MaCrossPointEvaluation.hit(period);
    }
}
