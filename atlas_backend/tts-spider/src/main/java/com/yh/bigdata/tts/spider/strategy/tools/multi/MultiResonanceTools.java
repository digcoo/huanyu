package com.yh.bigdata.tts.spider.strategy.tools.multi;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.spider.strategy.tools.IndicatorCommonUtils;
import com.yh.bigdata.tts.spider.strategy.tools.TrendTools;

import java.util.Arrays;
import java.util.List;

/**
 * 多周期强势 · 日/周/月结构共振 Context
 */
public final class MultiResonanceTools {

    private static final List<PeriodTypeEnum> RESONANCE_PERIODS =
            Arrays.asList(PeriodTypeEnum.DAY, PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH);

    private MultiResonanceTools() {
    }

    public static ResonanceSnapshot evaluate(StockBase stock) {
        ResonanceSnapshot snap = new ResonanceSnapshot();
        for (PeriodTypeEnum period : RESONANCE_PERIODS) {
            PeriodResonance pr = evaluatePeriod(stock, period);
            snap.put(period, pr);
        }
        return snap;
    }

    private static PeriodResonance evaluatePeriod(StockBase stock, PeriodTypeEnum period) {
        PeriodResonance pr = new PeriodResonance();
        pr.period = period;
        pr.overBandLow = hit(TrendTools.checkOverBandLowTrend(stock, period));
        pr.overRevertBandLow = hit(TrendTools.checkOverRevertBandLowTrend(stock, period));
        pr.overMa20 = hit(IndicatorCommonUtils.checkOverMA20(stock, period));
        pr.macdPositive = hit(IndicatorCommonUtils.checkOverMACD(stock, period));
        return pr;
    }

    private static boolean hit(CheckResponse response) {
        return response != null && response.isSuccess();
    }

    public static final class PeriodResonance {
        public PeriodTypeEnum period;
        public boolean overBandLow;
        public boolean overRevertBandLow;
        public boolean overMa20;
        public boolean macdPositive;

        public boolean coreResonance() {
            return overBandLow && overRevertBandLow;
        }

        public int bonusCount() {
            int n = 0;
            if (overMa20) {
                n++;
            }
            if (macdPositive) {
                n++;
            }
            return n;
        }
    }

    public static final class ResonanceSnapshot {
        public PeriodResonance day;
        public PeriodResonance week;
        public PeriodResonance month;

        void put(PeriodTypeEnum period, PeriodResonance pr) {
            if (period == PeriodTypeEnum.DAY) {
                day = pr;
            } else if (period == PeriodTypeEnum.WEEK) {
                week = pr;
            } else if (period == PeriodTypeEnum.MONTH) {
                month = pr;
            }
        }

        public int coreCount() {
            int n = 0;
            if (day != null && day.coreResonance()) {
                n++;
            }
            if (week != null && week.coreResonance()) {
                n++;
            }
            if (month != null && month.coreResonance()) {
                n++;
            }
            return n;
        }

        public int bonusCount() {
            int n = 0;
            if (day != null) {
                n += day.bonusCount();
            }
            if (week != null) {
                n += week.bonusCount();
            }
            if (month != null) {
                n += month.bonusCount();
            }
            return n;
        }
    }
}
