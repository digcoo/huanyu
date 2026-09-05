package com.yh.bigdata.tts.spider.strategy.tools.macrosspoint;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macrosspoint.MaCrossPointEvaluator.MaCrossPointEvaluation;

/**
 * 策略4：一阳穿多线（阳线 + low≤min(MA5,MA10) + close≥max(MA5,MA10)）；
 * 本档 MACD&gt;0 或 MA5&gt;MA10；收盘价≥max(MA5,MA10)。
 */
public final class MaYangPierceEvaluator {

    private static final String TAG = "MYP";

    private MaYangPierceEvaluator() {
    }

    public static MaCrossPointEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                  MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = MaCrossPointTools.resolvePeriod(p.getTier());
        String miss = "未满足一阳穿多线(MA5,MA10)";

        MaCrossPointTools.Hit hit = MaCrossPointTools.findYangPierceHit(stock, p);
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
            checkResult.addTrendPeriod(period, MaCrossPointTools.buildYangPierceTrendMessage(hit));
            checkResult.addSignal(period, MaCrossPointTools.buildYangPierceSignalMessage(hit));
        }
        return MaCrossPointEvaluation.hit(period);
    }
}
