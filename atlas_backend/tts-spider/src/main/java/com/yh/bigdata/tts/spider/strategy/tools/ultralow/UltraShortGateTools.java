package com.yh.bigdata.tts.spider.strategy.tools.ultralow;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

import com.yh.bigdata.tts.common.model.StockBase;

import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;

import com.yh.bigdata.tts.spider.response.CheckResult;

import lombok.Getter;



/**

 * 各策略 · 叠加 Min30 跨日桶柱内突破（共用 {@link UltraShortStrategyParams}）

 */

public final class UltraShortGateTools {



    private UltraShortGateTools() {

    }



    @Getter

    public static final class GateResult {

        private final boolean required;

        private final boolean passed;

        private final UltraShortBreakoutTools.Hit hit;



        private GateResult(boolean required, boolean passed, UltraShortBreakoutTools.Hit hit) {

            this.required = required;

            this.passed = passed;

            this.hit = hit;

        }



        static GateResult notRequired() {

            return new GateResult(false, true, null);

        }



        static GateResult passed(UltraShortBreakoutTools.Hit hit) {

            return new GateResult(true, true, hit);

        }



        static GateResult failed() {

            return new GateResult(true, false, null);

        }

    }



    public static GateResult evaluate(StockBase stock, boolean requireUltra, UltraShortStrategyParams ultraParams) {

        if (!requireUltra) {

            return GateResult.notRequired();

        }

        UltraShortStrategyParams p = ultraParams != null ? ultraParams : UltraShortStrategyParams.defaults();

        UltraShortBreakoutTools.Hit hit = UltraShortBreakoutTools.findHit(stock, p);

        return hit != null ? GateResult.passed(hit) : GateResult.failed();

    }



    public static boolean passes(StockBase stock, boolean requireUltra, UltraShortStrategyParams ultraParams) {

        return evaluate(stock, requireUltra, ultraParams).isPassed();

    }



    public static void appendMessages(CheckResult checkResult, UltraShortBreakoutTools.Hit hit) {

        if (checkResult == null || hit == null) {

            return;

        }

        UltraShortEvaluator.UltraShortEvaluation eval =

                new UltraShortEvaluator.UltraShortEvaluation(hit, true);

        checkResult.addTrendPeriod(PeriodTypeEnum.MIN30, UltraShortScoreCalculator.buildTrendMessage(eval));

        checkResult.addSignal(PeriodTypeEnum.MIN30, UltraShortScoreCalculator.buildSignalMessage(eval));

    }



    public static void appendMessages(CheckResult checkResult, StockBase stock, UltraShortStrategyParams ultraParams) {

        if (checkResult == null || stock == null) {

            return;

        }

        GateResult gate = evaluate(stock, true, ultraParams);

        if (gate.getHit() != null) {

            appendMessages(checkResult, gate.getHit());

        }

    }



    public static void appendMessages(CheckResult checkResult, GateResult gate) {

        if (checkResult == null || gate == null || gate.getHit() == null) {

            return;

        }

        appendMessages(checkResult, gate.getHit());

    }

}

