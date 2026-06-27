package com.yh.bigdata.tts.common.backtest;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.DateUtil;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 回测时点快照：将 K 线截断到 asOfDay；周/月/季/年 K 在周期未完成时从日 K 派生进行中 bar。
 */
public final class BacktestSnapshotContext {

    private static final ThreadLocal<Map<String, Map<PeriodTypeEnum, List<Trade>>>> SNAPSHOT =
            ThreadLocal.withInitial(() -> null);

    private BacktestSnapshotContext() {
    }

    public static boolean isActive() {
        return SNAPSHOT.get() != null;
    }

    public static List<Trade> getTrades(String code, PeriodTypeEnum period) {
        Map<String, Map<PeriodTypeEnum, List<Trade>>> holder = SNAPSHOT.get();
        if (holder == null) {
            return null;
        }
        Map<PeriodTypeEnum, List<Trade>> perCode = holder.get(code);
        if (perCode == null) {
            return null;
        }
        return perCode.get(period);
    }

    public static <T> T runWithSnapshot(String code, String asOfDay, Supplier<T> action) {
        Map<String, Map<PeriodTypeEnum, List<Trade>>> previous = SNAPSHOT.get();
        Map<String, Map<PeriodTypeEnum, List<Trade>>> next = previous != null
                ? new java.util.HashMap<>(previous)
                : new java.util.HashMap<>();
        next.put(code, buildSnapshot(code, asOfDay));
        SNAPSHOT.set(next);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                SNAPSHOT.remove();
            } else {
                SNAPSHOT.set(previous);
            }
        }
    }

    public static void runWithSnapshot(String code, String asOfDay, Runnable action) {
        runWithSnapshot(code, asOfDay, () -> {
            action.run();
            return null;
        });
    }

    private static Map<PeriodTypeEnum, List<Trade>> buildSnapshot(String code, String asOfDay) {
        long asOfTime = DateUtil.parseDate(asOfDay).getTime();
        List<Trade> dayBars = PeriodBarAsOfTools.slice(RealtimeStockCache.dayMap.get(code), asOfTime);
        Map<PeriodTypeEnum, List<Trade>> map = new EnumMap<>(PeriodTypeEnum.class);
        map.put(PeriodTypeEnum.DAY, dayBars);
        map.put(PeriodTypeEnum.WEEK, PeriodBarAsOfTools.sliceWithDerivedInProgress(
                RealtimeStockCache.weekMap.get(code), dayBars, asOfDay, PeriodTypeEnum.WEEK));
        map.put(PeriodTypeEnum.MONTH, PeriodBarAsOfTools.sliceWithDerivedInProgress(
                RealtimeStockCache.monthMap.get(code), dayBars, asOfDay, PeriodTypeEnum.MONTH));
        map.put(PeriodTypeEnum.QUARTER, PeriodBarAsOfTools.sliceWithDerivedInProgress(
                RealtimeStockCache.quarterMap.get(code), dayBars, asOfDay, PeriodTypeEnum.QUARTER));
        map.put(PeriodTypeEnum.YEAR, PeriodBarAsOfTools.sliceWithDerivedInProgress(
                RealtimeStockCache.yearMap.get(code), dayBars, asOfDay, PeriodTypeEnum.YEAR));
        map.put(PeriodTypeEnum.MIN30, PeriodBarAsOfTools.slice(RealtimeStockCache.min30Map.get(code), asOfTime));
        return map;
    }
}
