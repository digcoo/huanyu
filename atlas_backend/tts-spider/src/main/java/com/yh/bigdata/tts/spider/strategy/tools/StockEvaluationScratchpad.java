package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossBarResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 单股策略评估过程中的 ThreadLocal 暂存（MACD 序列等），评估结束须 {@link #clear()}。
 */
public final class StockEvaluationScratchpad {

    private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);

    private StockEvaluationScratchpad() {
    }

    public static State get() {
        return STATE.get();
    }

    public static void clear() {
        STATE.get().reset();
    }

    public static <T> T runWithScratchpad(Supplier<T> action) {
        try {
            return action.get();
        } finally {
            clear();
        }
    }

    public static final class State {
        private final Map<String, MacdCrossBarResolver.MacdSeries> macdSeriesByKey = new HashMap<>();

        void reset() {
            macdSeriesByKey.clear();
        }

        public MacdCrossBarResolver.MacdSeries macdSeries(String key) {
            return macdSeriesByKey.get(key);
        }

        public void putMacdSeries(String key, MacdCrossBarResolver.MacdSeries series) {
            macdSeriesByKey.put(key, series);
        }
    }
}
