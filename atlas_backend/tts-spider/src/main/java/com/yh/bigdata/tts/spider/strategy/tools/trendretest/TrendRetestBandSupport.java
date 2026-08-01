package com.yh.bigdata.tts.spider.strategy.tools.trendretest;

import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/** 末完整阳波段完结后至末 K 的窗口上下文。 */
public final class TrendRetestBandSupport {

    private static final double EPS = 1e-6;

    private TrendRetestBandSupport() {
    }

    @Getter
    public static final class Window {
        private final YangBandTools.CompleteYangBand lastBand;
        private final int terminatorIdx;
        private final int lastIdx;
        private final double bandLow;
        private final double bandHigh;

        Window(YangBandTools.CompleteYangBand lastBand, int terminatorIdx, int lastIdx,
               double bandLow, double bandHigh) {
            this.lastBand = lastBand;
            this.terminatorIdx = terminatorIdx;
            this.lastIdx = lastIdx;
            this.bandLow = bandLow;
            this.bandHigh = bandHigh;
        }
    }

    public static Window resolveWindow(List<Trade> trades, int lookback) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3 || lookback < 3) {
            return null;
        }
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(trades, lookback);
        if (bands.isEmpty()) {
            return null;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        double bandLow = lastBand.getBandLow();
        double bandHigh = lastBand.getBandHigh();
        if (Double.isNaN(bandLow) || Double.isNaN(bandHigh)) {
            return null;
        }
        int termIdx = indexOfBar(trades, lastBand.getTerminatorBar());
        if (termIdx < 0) {
            return null;
        }
        int lastIdx = trades.size() - 1;
        if (lastIdx <= termIdx) {
            return null;
        }
        return new Window(lastBand, termIdx, lastIdx, bandLow, bandHigh);
    }

    public static boolean hasRetestLowInWindow(List<Trade> trades, Window window) {
        if (window == null) {
            return false;
        }
        for (int i = window.getTerminatorIdx() + 1; i <= window.getLastIdx(); i++) {
            Trade bar = trades.get(i);
            if (bar != null && bar.getLow() != null && bar.getLow() <= window.getBandLow() + EPS) {
                return true;
            }
        }
        return false;
    }

    public static int indexOfBar(List<Trade> trades, Trade target) {
        if (target == null || target.getDay() == null || CollectionUtils.isEmpty(trades)) {
            return -1;
        }
        for (int i = 0; i < trades.size(); i++) {
            Trade bar = trades.get(i);
            if (bar != null && target.getDay().equals(bar.getDay())) {
                return i;
            }
        }
        return -1;
    }
}
