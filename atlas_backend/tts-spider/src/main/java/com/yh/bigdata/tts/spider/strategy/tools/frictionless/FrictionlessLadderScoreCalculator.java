package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.param.FrictionlessLadderStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.longterm.LongScoreCalculator;
import com.yh.bigdata.tts.spider.strategy.tools.medium.MediumScoreCalculator;
import com.yh.bigdata.tts.spider.strategy.tools.trend.TrendV2ScoreCalculator;

public final class FrictionlessLadderScoreCalculator {

    private static final String NRF_GATE = StrategyGlobalGateTools.FULL_GATE_LABEL;

    private FrictionlessLadderScoreCalculator() {
    }

    public static String buildTrendMessage(FrictionlessLadderEvaluator.Evaluation eval) {
        String tierLabel;
        String detail;
        switch (eval.getActiveTier()) {
            case MEDIUM:
                tierLabel = "周+min30梯子";
                detail = eval.getMediumEval() != null
                        ? MediumScoreCalculator.buildTrendMessage(eval.getMediumEval()) : "";
                break;
            case LONG:
                tierLabel = "月+min30梯子";
                detail = eval.getLongEval() != null
                        ? LongScoreCalculator.buildTrendMessage(eval.getLongEval()) : "";
                break;
            case SHORT:
            default:
                tierLabel = "日+min30梯子";
                detail = eval.getTrendEval() != null
                        ? TrendV2ScoreCalculator.buildTrendMessage(eval.getTrendEval()) : "";
                break;
        }
        return "[NRF]" + tierLabel + "|" + NRF_GATE
                + (detail.isEmpty() ? "" : "|" + stripLeadingTag(detail));
    }

    public static String buildSignalMessage(FrictionlessLadderEvaluator.Evaluation eval) {
        switch (eval.getActiveTier()) {
            case MEDIUM:
                return eval.getMediumEval() != null
                        ? MediumScoreCalculator.buildSignalMessage(eval.getMediumEval()) : "";
            case LONG:
                return eval.getLongEval() != null
                        ? LongScoreCalculator.buildSignalMessage(eval.getLongEval()) : "";
            case SHORT:
            default:
                return eval.getTrendEval() != null
                        ? TrendV2ScoreCalculator.buildSignalMessage(eval.getTrendEval()) : "";
        }
    }

    public static int computeScore(FrictionlessLadderEvaluator.Evaluation eval) {
        return eval.getScore();
    }

    private static String stripLeadingTag(String detail) {
        int bar = detail.indexOf('|');
        if (bar >= 0 && detail.startsWith("[")) {
            return detail.substring(bar + 1);
        }
        return detail;
    }
}
