package com.yh.bigdata.tts.spider.strategy.tools.retest;

import com.yh.bigdata.tts.common.model.Trade;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 分形 swing 高低点识别
 */
public final class SwingPointTools {

    public enum SwingKind {
        LOW, HIGH
    }

    @Getter
    public static final class SwingPoint {
        private final int index;
        private final Trade bar;
        private final SwingKind kind;

        SwingPoint(int index, Trade bar, SwingKind kind) {
            this.index = index;
            this.bar = bar;
            this.kind = kind;
        }
    }

    private SwingPointTools() {
    }

    public static List<SwingPoint> findFractalSwings(List<Trade> bars, int fractalBars) {
        List<SwingPoint> swings = new ArrayList<>();
        if (bars == null || bars.size() < fractalBars * 2 + 1) {
            return swings;
        }
        int n = fractalBars;
        for (int i = n; i < bars.size() - n; i++) {
            if (isSwingLow(bars, i, n)) {
                swings.add(new SwingPoint(i, bars.get(i), SwingKind.LOW));
            }
            if (isSwingHigh(bars, i, n)) {
                swings.add(new SwingPoint(i, bars.get(i), SwingKind.HIGH));
            }
        }
        return swings;
    }

    static boolean isSwingLow(List<Trade> bars, int i, int n) {
        Double low = bars.get(i).getLow();
        if (low == null) {
            return false;
        }
        for (int j = i - n; j <= i + n; j++) {
            if (j == i) {
                continue;
            }
            Double other = bars.get(j).getLow();
            if (other == null || other < low) {
                return false;
            }
        }
        return true;
    }

    static boolean isSwingHigh(List<Trade> bars, int i, int n) {
        Double high = bars.get(i).getHigh();
        if (high == null) {
            return false;
        }
        for (int j = i - n; j <= i + n; j++) {
            if (j == i) {
                continue;
            }
            Double other = bars.get(j).getHigh();
            if (other == null || other > high) {
                return false;
            }
        }
        return true;
    }

    static SwingPoint firstAfter(List<SwingPoint> swings, int afterIndex, SwingKind kind) {
        for (SwingPoint sp : swings) {
            if (sp.getKind() == kind && sp.getIndex() > afterIndex) {
                return sp;
            }
        }
        return null;
    }

    static SwingPoint lastOfKind(List<SwingPoint> swings, SwingKind kind) {
        SwingPoint last = null;
        for (SwingPoint sp : swings) {
            if (sp.getKind() == kind) {
                last = sp;
            }
        }
        return last;
    }

    static SwingPoint lastTwoCompareHighs(List<SwingPoint> swings) {
        SwingPoint prev = null;
        SwingPoint last = null;
        for (SwingPoint sp : swings) {
            if (sp.getKind() != SwingKind.HIGH) {
                continue;
            }
            prev = last;
            last = sp;
        }
        if (prev == null || last == null) {
            return null;
        }
        return last;
    }

    static boolean hasLowerHigh(List<SwingPoint> swings) {
        SwingPoint prev = null;
        SwingPoint last = null;
        for (SwingPoint sp : swings) {
            if (sp.getKind() != SwingKind.HIGH) {
                continue;
            }
            prev = last;
            last = sp;
        }
        if (prev == null || last == null
                || prev.getBar().getHigh() == null || last.getBar().getHigh() == null) {
            return false;
        }
        return last.getBar().getHigh() < prev.getBar().getHigh();
    }

    static boolean hasHigherLow(List<SwingPoint> swings) {
        SwingPoint prev = null;
        SwingPoint last = null;
        for (SwingPoint sp : swings) {
            if (sp.getKind() != SwingKind.LOW) {
                continue;
            }
            prev = last;
            last = sp;
        }
        if (prev == null || last == null
                || prev.getBar().getLow() == null || last.getBar().getLow() == null) {
            return false;
        }
        return last.getBar().getLow() > prev.getBar().getLow();
    }
}
