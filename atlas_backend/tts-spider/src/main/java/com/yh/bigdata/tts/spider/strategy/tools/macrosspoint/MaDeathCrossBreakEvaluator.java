package com.yh.bigdata.tts.spider.strategy.tools.macrosspoint;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macrosspoint.MaCrossPointEvaluator.MaCrossPointEvaluation;

/**
 * 策略5：边沿破任意死叉交叉点 DC10/DC20/DC30/DC60；
 * 父级 (MA10&gt;MA60) or (MA10≤MA60 且 close&gt;max(MA5,MA10))。
 * 可选均线多头为各周期 MA10≥MA60，可选均线空头为各周期 MA10&lt;MA60。
 */
public final class MaDeathCrossBreakEvaluator {

    private static final String TAG = "MDX";

    private MaDeathCrossBreakEvaluator() {
    }

    public static MaCrossPointEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                  MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = MaCrossPointTools.resolvePeriod(p.getTier());
        String miss = "未满足边沿破DC10/DC20/DC30/DC60";

        MaCrossPointTools.Hit hit = MaCrossPointTools.findAnyDeathCrossBreakHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[" + TAG + "]" + miss);
            }
            return MaCrossPointEvaluation.miss();
        }

        if (!MaCrossPointTools.passesParentMdxGate(stock, checkResult, TAG, period)) {
            return MaCrossPointEvaluation.miss();
        }

        if (!MaCrossPointTools.passesOptionalGates(stock, checkResult, p, hit, TAG, true)) {
            return MaCrossPointEvaluation.miss();
        }

        if (checkResult != null) {
            checkResult.addTrendPeriod(period, MaCrossPointTools.buildDeathCrossBreakTrendMessage(hit));
            checkResult.addSignal(period, MaCrossPointTools.buildDeathCrossBreakSignalMessage(hit));
        }
        return MaCrossPointEvaluation.hit(period);
    }
}
