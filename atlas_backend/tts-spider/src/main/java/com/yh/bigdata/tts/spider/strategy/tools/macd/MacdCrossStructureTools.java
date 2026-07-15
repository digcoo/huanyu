package com.yh.bigdata.tts.spider.strategy.tools.macd;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.Trade;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MACD 交叉 K 结构 · 最近一根符合开关的交叉 K（不含最后一根）。
 */
public final class MacdCrossStructureTools {

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

    private MacdCrossStructureTools() {
    }

    public static CrossBar findLatestCrossBar(List<Trade> trades, int lookback) {
        if (CollectionUtils.isEmpty(trades) || lookback < 1) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(trades));
        return findLatestCrossBar(trades, points, lookback, true, true);
    }

    static CrossBar findLatestCrossBar(List<Trade> trades, List<MACDIndicatorUtils.MACDPoint> points,
                                       int lookback) {
        return findLatestCrossBar(trades, points, lookback, true, true);
    }

    public static CrossBar findLatestCrossBar(List<Trade> trades, List<MACDIndicatorUtils.MACDPoint> points,
                                       int lookback, boolean enableGoldenCross, boolean enableDeathCross) {
        if (CollectionUtils.isEmpty(trades) || lookback < 1) {
            return null;
        }
        if (CollectionUtils.isEmpty(points) || points.size() != trades.size()) {
            return null;
        }
        if (!enableGoldenCross && !enableDeathCross) {
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
            if (enableGoldenCross && pt.isIfRedGoldCross()) {
                return new CrossBar(bar, CrossKind.GOLDEN);
            }
            if (enableDeathCross && pt.isIfGreenGoldCross()) {
                return new CrossBar(bar, CrossKind.DEATH);
            }
        }
        return null;
    }

    public static Trade findLatestDeathCrossBar(List<Trade> trades, List<MACDIndicatorUtils.MACDPoint> points,
                                                int lookback) {
        if (CollectionUtils.isEmpty(trades) || lookback < 1) {
            return null;
        }
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
            if (pt.isIfGreenGoldCross()) {
                return bar;
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
