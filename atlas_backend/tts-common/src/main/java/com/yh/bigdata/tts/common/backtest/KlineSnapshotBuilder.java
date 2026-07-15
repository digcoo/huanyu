package com.yh.bigdata.tts.common.backtest;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.KlineSnapshotBar;
import com.yh.bigdata.tts.common.model.Trade;

import java.util.ArrayList;
import java.util.List;

/**
 * 由日 K 回放生成时点快照行（供 day/week/month/year_snapshot 落库）。
 */
public final class KlineSnapshotBuilder {

    private KlineSnapshotBuilder() {
    }

    public static List<KlineSnapshotBar> buildDayRows(List<? extends Trade> dayBarsUpToAsOf, String asOfDay) {
        List<KlineSnapshotBar> rows = new ArrayList<>();
        if (dayBarsUpToAsOf == null || asOfDay == null) {
            return rows;
        }
        for (Trade bar : dayBarsUpToAsOf) {
            if (bar == null || bar.getDay() == null) {
                continue;
            }
            String barDay = PeriodBarAsOfTools.normalizeDay(bar.getDay());
            if (barDay.compareTo(asOfDay) > 0) {
                continue;
            }
            rows.add(KlineSnapshotBar.fromTrade(bar, asOfDay, barDay, false));
        }
        return rows;
    }

    public static List<KlineSnapshotBar> buildPeriodRows(List<Trade> dayBarsUpToAsOf,
                                                         String asOfDay,
                                                         PeriodTypeEnum period) {
        List<KlineSnapshotBar> rows = new ArrayList<>();
        if (dayBarsUpToAsOf == null || asOfDay == null || period == null) {
            return rows;
        }
        List<Trade> series = PeriodBarAsOfTools.buildPeriodSeriesFromDayBars(dayBarsUpToAsOf, asOfDay, period);
        for (Trade bar : series) {
            if (bar == null || bar.getDay() == null) {
                continue;
            }
            boolean inProgress = PeriodBarAsOfTools.isInProgressBar(asOfDay, bar.getDay(), period);
            rows.add(KlineSnapshotBar.fromTrade(bar, asOfDay, bar.getDay(), inProgress));
        }
        return rows;
    }
}
