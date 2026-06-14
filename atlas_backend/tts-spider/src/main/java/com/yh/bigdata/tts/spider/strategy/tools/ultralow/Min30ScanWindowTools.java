package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.model.Trade;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 30m 扫描窗口：前 N 个交易日各最多 8 根 + 当日已走完根数（盘中动态）
 */
public final class Min30ScanWindowTools {

    public static final int DEFAULT_BARS_PER_DAY = 8;
    public static final int DEFAULT_PREV_DAYS = 2;

    private Min30ScanWindowTools() {
    }

    public static List<Trade> buildScanWindow(List<Trade> allBars, int prevDays, int barsPerDay) {
        if (CollectionUtils.isEmpty(allBars) || prevDays < 0 || barsPerDay <= 0) {
            return new ArrayList<>();
        }
        Map<String, List<Trade>> byDay = groupByTradeDay(allBars);
        List<String> dates = new ArrayList<>(byDay.keySet());
        if (dates.isEmpty()) {
            return new ArrayList<>();
        }

        int n = dates.size();
        List<Trade> window = new ArrayList<>();
        int prevStart = Math.max(0, n - 1 - prevDays);
        for (int i = prevStart; i < n - 1; i++) {
            appendLastBars(window, byDay.get(dates.get(i)), barsPerDay);
        }
        window.addAll(byDay.get(dates.get(n - 1)));
        return window;
    }

    public static int countTodayBars(List<Trade> allBars) {
        if (CollectionUtils.isEmpty(allBars)) {
            return 0;
        }
        String today = dayKey(allBars.get(allBars.size() - 1));
        int count = 0;
        for (int i = allBars.size() - 1; i >= 0; i--) {
            if (!today.equals(dayKey(allBars.get(i)))) {
                break;
            }
            count++;
        }
        return count;
    }

    private static void appendLastBars(List<Trade> target, List<Trade> dayBars, int barsPerDay) {
        if (CollectionUtils.isEmpty(dayBars)) {
            return;
        }
        int from = Math.max(0, dayBars.size() - barsPerDay);
        target.addAll(dayBars.subList(from, dayBars.size()));
    }

    private static Map<String, List<Trade>> groupByTradeDay(List<Trade> bars) {
        Map<String, List<Trade>> byDay = new LinkedHashMap<>();
        for (Trade bar : bars) {
            String key = dayKey(bar);
            if (key == null) {
                continue;
            }
            byDay.computeIfAbsent(key, k -> new ArrayList<>()).add(bar);
        }
        return byDay;
    }

    static String dayKey(Trade bar) {
        if (bar == null || bar.getDay() == null) {
            return null;
        }
        String day = bar.getDay().trim();
        return day.length() >= 10 ? day.substring(0, 10) : day;
    }
}
