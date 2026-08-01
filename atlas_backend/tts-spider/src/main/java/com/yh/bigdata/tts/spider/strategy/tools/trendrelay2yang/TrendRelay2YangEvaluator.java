package com.yh.bigdata.tts.spider.strategy.tools.trendrelay2yang;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.TrendRelay2YangStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.DayLastBarYangGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import lombok.Getter;

public final class TrendRelay2YangEvaluator {

    private TrendRelay2YangEvaluator() {
    }

    public static TrendRelay2YangEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                     TrendRelay2YangStrategyParams params) {
        TrendRelay2YangStrategyParams p = params != null ? params : TrendRelay2YangStrategyParams.defaults();
        PeriodTypeEnum period = TrendRelay2YangTools.resolvePeriod(p.getTier());
        if (!UnilateralMacdTools.isMacdPositive(stock, period)) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[TR2Y]" + periodLabel(period) + "MACD≤0");
            }
            return TrendRelay2YangEvaluation.miss();
        }
        TrendRelay2YangTools.Hit hit = TrendRelay2YangTools.findHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[TR2Y]未满足未回踩+仅末二阳(第三阴)+Low<close≤High");
            }
            return TrendRelay2YangEvaluation.miss();
        }
        if (!DayLastBarYangGateTools.passWithMessage(stock, checkResult)) {
            return TrendRelay2YangEvaluation.miss();
        }
        if (!TrendRelay2YangTools.passesOptionalGates(stock, checkResult, p)) {
            return TrendRelay2YangEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(period, TrendRelay2YangTools.buildTrendMessage(hit));
            checkResult.addSignal(period, TrendRelay2YangTools.buildSignalMessage(hit));
        }
        return TrendRelay2YangEvaluation.hit(period);
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MONTH) {
            return "月";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周";
        }
        return "日";
    }

    @Getter
    public static final class TrendRelay2YangEvaluation {
        private final boolean hit;
        private final PeriodTypeEnum period;

        TrendRelay2YangEvaluation(boolean hit, PeriodTypeEnum period) {
            this.hit = hit;
            this.period = period;
        }

        static TrendRelay2YangEvaluation miss() {
            return new TrendRelay2YangEvaluation(false, null);
        }

        static TrendRelay2YangEvaluation hit(PeriodTypeEnum period) {
            return new TrendRelay2YangEvaluation(true, period);
        }
    }
}
