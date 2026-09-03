package com.yh.bigdata.tts.spider.strategy.tools.macrosspoint;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macrosspoint.MaCrossPointEvaluator.MaCrossPointEvaluation;

/**
 * 策略7：边沿破任意金叉波段顶 GH10/GH20/GH30/GH60；
 * 父级 (MA10&gt;MA60) or (MA10≤MA60 且 close&gt;max(MA5,MA10))。
 * 可选均线多头为各周期 MA10≥MA60，可选均线空头为各周期 MA10&lt;MA60。
 */
public final class MaGoldenHighBreakEvaluator {

    private static final String TAG = "MGH";

    private MaGoldenHighBreakEvaluator() {
    }

    public static MaCrossPointEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                  MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = MaCrossPointTools.resolvePeriod(p.getTier());
        String miss = "未满足边沿破GH10/GH20/GH30/GH60";

        MaCrossPointTools.Hit hit = MaCrossPointTools.findAnyGoldenHighBreakHit(stock, p);
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
            checkResult.addTrendPeriod(period, MaCrossPointTools.buildGoldenHighBreakTrendMessage(hit));
            checkResult.addSignal(period, MaCrossPointTools.buildGoldenHighBreakSignalMessage(hit));
        }
        return MaCrossPointEvaluation.hit(period);
    }
}
