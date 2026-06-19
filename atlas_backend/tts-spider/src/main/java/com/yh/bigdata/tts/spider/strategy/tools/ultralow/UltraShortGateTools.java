package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;

/**
 * 短/中/长策略 · 叠加超短 30m 突破门槛（共用 {@link UltraShortStrategyParams}）
 */
public final class UltraShortGateTools {

    private UltraShortGateTools() {
    }

    public static boolean passes(StockBase stock, boolean requireUltra, UltraShortStrategyParams ultraParams) {
        if (!requireUltra) {
            return true;
        }
        UltraShortStrategyParams p = ultraParams != null ? ultraParams : UltraShortStrategyParams.defaults();
        return UltraShortBreakoutTools.findHit(stock, p) != null;
    }

    public static void appendMessages(CheckResult checkResult, StockBase stock, UltraShortStrategyParams ultraParams) {
        if (checkResult == null || stock == null) {
            return;
        }
        UltraShortStrategyParams p = ultraParams != null ? ultraParams : UltraShortStrategyParams.defaults();
        UltraShortBreakoutTools.Hit hit = UltraShortBreakoutTools.findHit(stock, p);
        if (hit == null) {
            return;
        }
        UltraShortEvaluator.UltraShortEvaluation eval =
                new UltraShortEvaluator.UltraShortEvaluation(hit, true);
        checkResult.addTrendPeriod(PeriodTypeEnum.MIN30, UltraShortScoreCalculator.buildTrendMessage(eval));
        checkResult.addSignal(PeriodTypeEnum.MIN30, UltraShortScoreCalculator.buildSignalMessage(eval));
    }
}
