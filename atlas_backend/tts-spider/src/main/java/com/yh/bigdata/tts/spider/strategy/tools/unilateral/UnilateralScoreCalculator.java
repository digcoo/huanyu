package com.yh.bigdata.tts.spider.strategy.tools.unilateral;



import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;



/**

 * 金叉策略 v4.0 · 评分与展示档位（S=短线 B=长线）

 */

public final class UnilateralScoreCalculator {



    private UnilateralScoreCalculator() {

    }



    public static int computeScore(UnilateralTrendEvaluator.UnilateralEvaluation eval) {

        int score = 0;

        if (eval.isShortHit()) {

            score += 40;

            if (eval.isMonthMacdPositive() && eval.isWeekMacdPositive()) {

                score += 10;

            }

        }

        if (eval.isLongHit()) {

            score += 35;

            if (eval.isYearMacdPositive() && eval.isMonthMacdPositive()) {

                score += 10;

            }

        }

        if (eval.isShortHit() && eval.isLongHit()) {

            score += 15;

        }

        return score;

    }



    /** S=短线 B=长线；同时命中时优先展示短线 */

    public static char computeTier(UnilateralTrendEvaluator.UnilateralEvaluation eval) {

        if (eval.isShortHit()) {

            return 'S';

        }

        if (eval.isLongHit()) {

            return 'B';

        }

        return 'N';

    }



    public static PeriodTypeEnum trendPeriodForTier(char tier) {

        switch (tier) {

            case 'S':

                return PeriodTypeEnum.MONTH;

            case 'B':

                return PeriodTypeEnum.YEAR;

            default:

                return PeriodTypeEnum.WEEK;

        }

    }



    public static PeriodTypeEnum signalPeriodForTier(char tier) {

        switch (tier) {

            case 'S':

                return PeriodTypeEnum.DAY;

            case 'B':

                return PeriodTypeEnum.WEEK;

            default:

                return PeriodTypeEnum.DAY;

        }

    }



    public static String buildTrendLabel(char tier) {

        switch (tier) {

            case 'S':

                return "短线金叉";

            case 'B':

                return "长线金叉";

            default:

                return "MACD金叉";

        }

    }



    public static String buildTrendDetail(UnilateralTrendEvaluator.UnilateralEvaluation eval) {

        if (eval.isShortHit()) {

            return buildShortMacroDetail(eval);

        }

        if (eval.isLongHit()) {

            return buildLongMacroDetail(eval);

        }

        return "MACD";

    }



    public static String buildSignalDetail(UnilateralTrendEvaluator.UnilateralEvaluation eval) {

        StringBuilder sb = new StringBuilder();

        if (eval.isShortHit()) {

            sb.append("日K MACD金叉,");

        }

        if (eval.isLongHit()) {

            sb.append("周K MACD金叉,");

        }

        if (sb.length() == 0) {

            return "";

        }

        return sb.substring(0, sb.length() - 1);

    }



    private static String buildShortMacroDetail(UnilateralTrendEvaluator.UnilateralEvaluation eval) {

        if (eval.isMonthMacdPositive() && eval.isWeekMacdPositive()) {

            return "月/周MACD>0";

        }

        if (eval.isMonthMacdPositive()) {

            return "月MACD>0";

        }

        return "周MACD>0";

    }



    private static String buildLongMacroDetail(UnilateralTrendEvaluator.UnilateralEvaluation eval) {

        if (eval.isYearMacdPositive() && eval.isMonthMacdPositive()) {

            return "年/月MACD>0";

        }

        if (eval.isYearMacdPositive()) {

            return "年MACD>0";

        }

        return "月MACD>0";

    }

}


