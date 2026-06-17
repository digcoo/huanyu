package com.yh.bigdata.tts.spider.strategy.tools.gc2;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.Trade;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 金叉二次突破 · 最近 MACD 红柱金叉 K（ref）
 */
public final class Gc2StructureTools {

    private Gc2StructureTools() {
    }

    public static Trade findLatestGoldenCrossBar(List<Trade> trades, int lookback) {
        if (CollectionUtils.isEmpty(trades) || lookback < 1) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(trades));
        if (CollectionUtils.isEmpty(points) || points.size() != trades.size()) {
            return null;
        }
        int start = Math.max(1, points.size() - lookback);
        for (int i = points.size() - 1; i >= start; i--) {
            if (points.get(i).isIfRedGoldCross()) {
                return trades.get(i);
            }
        }
        return null;
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
