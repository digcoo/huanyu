package com.yh.bigdata.tts.spider.strategy.tools.bogo;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.Trade;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 底部机会 · 最近 MACD 金叉或死叉 K（基准 K）
 */
public final class BogoStructureTools {

    public enum CrossKind {
        GOLDEN, DEATH
    }

    @Getter
    public static final class CrossBar {
        private final Trade bar;
        private final CrossKind kind;

        CrossBar(Trade bar, CrossKind kind) {
            this.bar = bar;
            this.kind = kind;
        }
    }

    private BogoStructureTools() {
    }

    /**
     * 自最新 K 向前，取最近一根金叉或死叉柱（不得为当前 K）。
     */
    public static CrossBar findLatestCrossBar(List<Trade> trades, int lookback) {
        if (CollectionUtils.isEmpty(trades) || lookback < 1) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(trades));
        if (CollectionUtils.isEmpty(points) || points.size() != trades.size()) {
            return null;
        }
        Trade current = trades.get(trades.size() - 1);
        int start = Math.max(1, points.size() - lookback);
        for (int i = points.size() - 1; i >= start; i--) {
            MACDIndicatorUtils.MACDPoint pt = points.get(i);
            Trade bar = trades.get(i);
            if (sameBar(bar, current)) {
                continue;
            }
            if (pt.isIfRedGoldCross()) {
                return new CrossBar(bar, CrossKind.GOLDEN);
            }
            if (pt.isIfGreenGoldCross()) {
                return new CrossBar(bar, CrossKind.DEATH);
            }
        }
        return null;
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }

    public static int indexOfBar(List<Trade> trades, Trade bar) {
        if (CollectionUtils.isEmpty(trades) || bar == null || bar.getDay() == null) {
            return -1;
        }
        for (int i = 0; i < trades.size(); i++) {
            Trade t = trades.get(i);
            if (t != null && bar.getDay().equals(t.getDay())) {
                return i;
            }
        }
        return -1;
    }
}
