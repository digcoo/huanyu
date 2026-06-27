package com.yh.bigdata.tts.spider.strategy.tools.pillar;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

/**
 * 柱子内上移 · 各周期强 K 阈值
 */
public final class PillarThresholdTools {

    private PillarThresholdTools() {
    }

    public static double refBodyPct(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MONTH) {
            return 0.05;
        }
        if (period == PeriodTypeEnum.WEEK) {
            return 0.03;
        }
        return 0.02;
    }

    public static double signalStrongPct(PeriodTypeEnum period) {
        return refBodyPct(period);
    }
}
