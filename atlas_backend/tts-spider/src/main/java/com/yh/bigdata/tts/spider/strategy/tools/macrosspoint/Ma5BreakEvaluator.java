package com.yh.bigdata.tts.spider.strategy.tools.macrosspoint;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macrosspoint.MaCrossPointEvaluator.MaCrossPointEvaluation;

/**
 * 策略6：边沿破 max(MA5,MA10)，且 max(涨幅率,实体率)≥2%；
 * 本档 MACD&gt;0 或 MA5&gt;MA10；收盘价≥max(MA5,MA10)；父级收盘≥MA5。
 */
public final class Ma5BreakEvaluator {

    private static final String TAG = "MB5";

    private Ma5BreakEvaluator() {
    }

    public static MaCrossPointEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                  MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = MaCrossPointTools.resolvePeriod(p.getTier());
        String miss = "未满足边沿破max(MA5,MA10)且涨幅或实体>=2%";

        MaCrossPointTools.Hit hit = MaCrossPointTools.findMa5BreakHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[" + TAG + "]" + miss);
            }
            return MaCrossPointEvaluation.miss();
        }

        if (!MaCrossPointTools.passesCurrentScanGates(stock, checkResult, p, hit, TAG, period)) {
            return MaCrossPointEvaluation.miss();
        }

        if (!MaCrossPointTools.passesParentCloseGeMa5(stock, checkResult, TAG, period)) {
            return MaCrossPointEvaluation.miss();
        }

        if (checkResult != null) {
            checkResult.addTrendPeriod(period, MaCrossPointTools.buildMa5BreakTrendMessage(hit));
            checkResult.addSignal(period, MaCrossPointTools.buildMa5BreakSignalMessage(hit));
        }
        return MaCrossPointEvaluation.hit(period);
    }
}
