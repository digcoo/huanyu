package com.yh.bigdata.tts.common.backtest;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.DateUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 回测时点快照：将 K 线截断到 asOfDay，供策略引擎在历史 bar 上复用现有 check 逻辑。
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
        Map<PeriodTypeEnum, List<Trade>> map = new EnumMap<>(PeriodTypeEnum.class);
        map.put(PeriodTypeEnum.DAY, slice(RealtimeStockCache.dayMap.get(code), asOfTime));
        map.put(PeriodTypeEnum.WEEK, slice(RealtimeStockCache.weekMap.get(code), asOfTime));
        map.put(PeriodTypeEnum.MONTH, slice(RealtimeStockCache.monthMap.get(code), asOfTime));
        map.put(PeriodTypeEnum.QUARTER, slice(RealtimeStockCache.quarterMap.get(code), asOfTime));
        map.put(PeriodTypeEnum.YEAR, slice(RealtimeStockCache.yearMap.get(code), asOfTime));
        map.put(PeriodTypeEnum.MIN30, slice(RealtimeStockCache.min30Map.get(code), asOfTime));
        return map;
    }

    @SuppressWarnings("unchecked")
    private static List<Trade> slice(List<?> raw, long asOfTime) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        List<Trade> source = (List<Trade>) raw;
        List<Trade> out = new ArrayList<>();
        for (Trade trade : source) {
            if (trade.getDay() == null) {
                continue;
            }
            long time = DateUtil.parseDate(trade.getDay()).getTime();
            if (time <= asOfTime) {
                out.add(trade);
            }
        }
        return out;
    }
}
