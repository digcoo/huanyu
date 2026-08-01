package com.yh.bigdata.tts.spider.strategy.tools.trendretesthigh;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.TrendRetestHighStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.DayLastBarYangGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import lombok.Getter;

public final class TrendRetestHighEvaluator {

    private TrendRetestHighEvaluator() {
    }

    public static TrendRetestHighEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                     TrendRetestHighStrategyParams params) {
        TrendRetestHighStrategyParams p = params != null ? params : TrendRetestHighStrategyParams.defaults();
        PeriodTypeEnum period = TrendRetestHighTools.resolvePeriod(p.getTier());
        if (!UnilateralMacdTools.isMacdPositive(stock, period)) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[TRH]" + periodLabel(period) + "MACD≤0");
            }
            return TrendRetestHighEvaluation.miss();
        }
        TrendRetestHighTools.Hit hit = TrendRetestHighTools.findHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[TRH]未满足回踩Low+末K首次破High");
            }
            return TrendRetestHighEvaluation.miss();
        }
        if (!DayLastBarYangGateTools.passWithMessage(stock, checkResult)) {
            return TrendRetestHighEvaluation.miss();
        }
        if (!TrendRetestHighTools.passesOptionalGates(stock, checkResult, p)) {
            return TrendRetestHighEvaluation.miss();
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(period, TrendRetestHighTools.buildTrendMessage(hit));
            checkResult.addSignal(period, TrendRetestHighTools.buildSignalMessage(hit));
        }
        return TrendRetestHighEvaluation.hit(period);
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
    public static final class TrendRetestHighEvaluation {
        private final boolean hit;
        private final PeriodTypeEnum period;

        TrendRetestHighEvaluation(boolean hit, PeriodTypeEnum period) {
            this.hit = hit;
            this.period = period;
        }

        static TrendRetestHighEvaluation miss() {
            return new TrendRetestHighEvaluation(false, null);
        }

        static TrendRetestHighEvaluation hit(PeriodTypeEnum period) {
            return new TrendRetestHighEvaluation(true, period);
        }
    }
}
