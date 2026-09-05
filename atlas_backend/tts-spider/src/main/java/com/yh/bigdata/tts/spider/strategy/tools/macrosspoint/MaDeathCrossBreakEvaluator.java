package com.yh.bigdata.tts.spider.strategy.tools.macrosspoint;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macrosspoint.MaCrossPointEvaluator.MaCrossPointEvaluation;

/**
 * 策略1：边沿破任意死叉交叉点 DC10/DC20/DC60；
 * 本档 MACD&gt;0 或 MA5&gt;MA10；收盘价≥max(MA5,MA10)。
 */
public final class MaDeathCrossBreakEvaluator {

    private static final String TAG = "MDX";

    private MaDeathCrossBreakEvaluator() {
    }

    public static MaCrossPointEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                  MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = MaCrossPointTools.resolvePeriod(p.getTier());
        String miss = "未满足边沿破DC10/DC20/DC60";

        MaCrossPointTools.Hit hit = MaCrossPointTools.findAnyDeathCrossBreakHit(stock, p);
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
            checkResult.addTrendPeriod(period, MaCrossPointTools.buildDeathCrossBreakTrendMessage(hit));
            checkResult.addSignal(period, MaCrossPointTools.buildDeathCrossBreakSignalMessage(hit));
        }
        return MaCrossPointEvaluation.hit(period);
    }
}
