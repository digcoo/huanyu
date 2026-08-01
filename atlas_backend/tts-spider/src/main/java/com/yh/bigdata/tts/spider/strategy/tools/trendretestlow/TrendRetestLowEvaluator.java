package com.yh.bigdata.tts.spider.strategy.tools.trendretestlow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.TrendRetestLowStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.DayLastBarYangGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import lombok.Getter;

public final class TrendRetestLowEvaluator {

    private TrendRetestLowEvaluator() {
    }

    public static TrendRetestLowEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                    TrendRetestLowStrategyParams params) {
        TrendRetestLowStrategyParams p = params != null ? params : TrendRetestLowStrategyParams.defaults();
        PeriodTypeEnum period = TrendRetestLowTools.resolvePeriod(p.getTier());
        if (!UnilateralMacdTools.isMacdPositive(stock, period)) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[TRL]" + periodLabel(period) + "MACD≤0");
            }
            return TrendRetestLowEvaluation.miss();
        }
        TrendRetestLowTools.Hit hit = TrendRetestLowTools.findHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[TRL]未满足回踩Low+末K收阳+Low<close≤High");
            }
            return TrendRetestLowEvaluation.miss();
        }
        if (!DayLastBarYangGateTools.passWithMessage(stock, checkResult)) {
            return TrendRetestLowEvaluation.miss();
        }
        if (!TrendRetestLowTools.passesOptionalGates(stock, checkResult, p)) {
            return TrendRetestLowEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(period, TrendRetestLowTools.buildTrendMessage(hit));
            checkResult.addSignal(period, TrendRetestLowTools.buildSignalMessage(hit));
        }
        return TrendRetestLowEvaluation.hit(period);
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
    public static final class TrendRetestLowEvaluation {
        private final boolean hit;
        private final PeriodTypeEnum period;

        TrendRetestLowEvaluation(boolean hit, PeriodTypeEnum period) {
            this.hit = hit;
            this.period = period;
        }

        static TrendRetestLowEvaluation miss() {
            return new TrendRetestLowEvaluation(false, null);
        }

        static TrendRetestLowEvaluation hit(PeriodTypeEnum period) {
            return new TrendRetestLowEvaluation(true, period);
        }
    }
}
