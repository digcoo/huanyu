package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.model.Trade;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 突破阶梯 · 背景窗口 + 信号桶截取
 */
public final class BreakoutScanWindowTools {

    private BreakoutScanWindowTools() {
    }

    static final class ScanWindow {
        private final List<Trade> priorBars;
        private final List<Trade> signalBars;
        private final String signalUnitKey;

        ScanWindow(List<Trade> priorBars, List<Trade> signalBars, String signalUnitKey) {
            this.priorBars = priorBars;
            this.signalBars = signalBars;
            this.signalUnitKey = signalUnitKey;
        }

        List<Trade> getPriorBars() {
            return priorBars;
        }

        List<Trade> getSignalBars() {
            return signalBars;
        }

        String getSignalUnitKey() {
            return signalUnitKey;
        }

        int totalSize() {
            return priorBars.size() + signalBars.size();
        }
    }

    static ScanWindow buildScanWindow(List<Trade> allBars, Function<Trade, String> unitKeyFn,
                                      int prevUnits, int maxBarsPerUnit) {
        if (CollectionUtils.isEmpty(allBars) || prevUnits < 0 || maxBarsPerUnit <= 0) {
            return new ScanWindow(new ArrayList<>(), new ArrayList<>(), null);
        }
        Map<String, List<Trade>> byUnit = groupByKey(allBars, unitKeyFn);
        List<String> units = new ArrayList<>(byUnit.keySet());
        if (units.isEmpty()) {
            return new ScanWindow(new ArrayList<>(), new ArrayList<>(), null);
        }

        int n = units.size();
        String signalUnit = units.get(n - 1);
        List<Trade> prior = new ArrayList<>();
        int prevStart = Math.max(0, n - 1 - prevUnits);
        for (int i = prevStart; i < n - 1; i++) {
            appendLastBars(prior, byUnit.get(units.get(i)), maxBarsPerUnit);
        }
        List<Trade> signal = new ArrayList<>(byUnit.getOrDefault(signalUnit, new ArrayList<>()));
        return new ScanWindow(prior, signal, signalUnit);
    }

    private static Map<String, List<Trade>> groupByKey(List<Trade> bars, Function<Trade, String> unitKeyFn) {
        Map<String, List<Trade>> byUnit = new LinkedHashMap<>();
        for (Trade bar : bars) {
            String key = unitKeyFn.apply(bar);
            if (key == null) {
                continue;
            }
            byUnit.computeIfAbsent(key, k -> new ArrayList<>()).add(bar);
        }
        return byUnit;
    }

    private static void appendLastBars(List<Trade> target, List<Trade> unitBars, int maxBarsPerUnit) {
        if (CollectionUtils.isEmpty(unitBars)) {
            return;
        }
        int from = Math.max(0, unitBars.size() - maxBarsPerUnit);
        target.addAll(unitBars.subList(from, unitBars.size()));
    }
}
