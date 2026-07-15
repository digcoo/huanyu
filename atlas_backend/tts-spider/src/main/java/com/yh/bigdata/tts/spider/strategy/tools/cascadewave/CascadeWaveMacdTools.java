package com.yh.bigdata.tts.spider.strategy.tools.cascadewave;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

/**
 * 级联 MACD 柱 &gt; 0：信号档 + 上一级档均须满足；
 * 且级联档最后一根 K 不得为 MACD 金叉柱（红柱由负转正）。
 */
public final class CascadeWaveMacdTools {

    private CascadeWaveMacdTools() {
    }

    public static boolean passesCascade(StockBase stock, PeriodTypeEnum signalPeriod) {
        if (stock == null || signalPeriod == null) {
            return false;
        }
        if (!UnilateralMacdTools.isMacdPositive(stock, signalPeriod)) {
            return false;
        }
        PeriodTypeEnum cascadePeriod = cascadePeriod(signalPeriod);
        if (cascadePeriod == null) {
            return true;
        }
        if (!UnilateralMacdTools.isMacdPositive(stock, cascadePeriod)) {
            return false;
        }
        if (UnilateralMacdTools.isGoldenCross(stock, cascadePeriod)) {
            return false;
        }
        return true;
    }

    public static PeriodTypeEnum cascadePeriod(PeriodTypeEnum signalPeriod) {
        if (signalPeriod == PeriodTypeEnum.DAY) {
            return PeriodTypeEnum.WEEK;
        }
        if (signalPeriod == PeriodTypeEnum.WEEK) {
            return PeriodTypeEnum.MONTH;
        }
        if (signalPeriod == PeriodTypeEnum.MONTH) {
            return PeriodTypeEnum.YEAR;
        }
        return null;
    }
}
