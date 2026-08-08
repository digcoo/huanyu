package com.binance.client.strategy.ma4m;

import com.binance.client.dto.LongCandlestickDTO;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.LongCandlestickMA;
import com.binance.client.utils.LongStrategyUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * Crypto 1小时 MA多头4M突破：末K多头排列(MA7≥MA14≥MA28≥MA42)，
 * 边沿突破金叉/死叉(须在关键K之后)或关键K High；基准须在末K之前。
 */
public final class HourMaBull4mTools {

    private static final double EPS = 1e-10;

    private HourMaBull4mTools() {
    }

    public enum RefKind {
        GOLDEN,
        DEATH,
        CRITICAL
    }

    public static final class Hit {
        private final LongCandlestickMA signalBar;
        private final LongCandlestickMA prevBar;
        private final LongCandlestickMA refBar;
        private final RefKind refKind;
        private final double breakLine;

        Hit(LongCandlestickMA signalBar, LongCandlestickMA prevBar, LongCandlestickMA refBar,
            RefKind refKind, double breakLine) {
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.refBar = refBar;
            this.refKind = refKind;
            this.breakLine = breakLine;
        }

        public LongCandlestickMA getSignalBar() {
            return signalBar;
        }

        public LongCandlestickMA getPrevBar() {
            return prevBar;
        }

        public LongCandlestickMA getRefBar() {
            return refBar;
        }

        public RefKind getRefKind() {
            return refKind;
        }

        public double getBreakLine() {
            return breakLine;
        }
    }

    public static Hit findHit(List<Candlestick> hour1Bars) {
        if (hour1Bars == null || hour1Bars.size() < 45) {
            return null;
        }
        LongCandlestickDTO dto = LongStrategyUtil.buildLongCandlestickDTO(hour1Bars, PeriodTypeEnum.HOUR1);
        if (dto == null || dto.getCandlestickMAS() == null || dto.getCandlestickMAS().size() < 3) {
            return null;
        }
        return findHitOnBars(dto.getCandlestickMAS());
    }

    static Hit findHitOnBars(List<LongCandlestickMA> bars) {
        if (bars == null || bars.size() < 3) {
            return null;
        }
        int lastIdx = bars.size() - 1;
        LongCandlestickMA signalBar = bars.get(lastIdx);
        LongCandlestickMA prevBar = bars.get(lastIdx - 1);
        if (!passesBullAlign(signalBar)) {
            return null;
        }

        int criticalIdx = findCriticalBullAlignIndex(bars, lastIdx);
        int goldenIdx = findLatestGoldenCrossIndex(bars, lastIdx, criticalIdx);
        Hit golden = tryEdgeBreakHigh(signalBar, prevBar, bars, goldenIdx, RefKind.GOLDEN);
        if (golden != null) {
            return golden;
        }
        int deathIdx = findLatestDeathCrossKIndex(bars, lastIdx, criticalIdx);
        Hit death = tryEdgeBreakHigh(signalBar, prevBar, bars, deathIdx, RefKind.DEATH);
        if (death != null) {
            return death;
        }
        return tryEdgeBreakHigh(signalBar, prevBar, bars, criticalIdx, RefKind.CRITICAL);
    }

    private static Hit tryEdgeBreakHigh(LongCandlestickMA signalBar, LongCandlestickMA prevBar,
                                        List<LongCandlestickMA> bars, int refIdx, RefKind kind) {
        if (refIdx < 0 || refIdx >= bars.size() - 1) {
            return null;
        }
        LongCandlestickMA refBar = bars.get(refIdx);
        if (refBar == null || refBar.getHigh() == null) {
            return null;
        }
        double breakLine = refBar.getHigh().doubleValue();
        if (!passesBandHighEdge(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(signalBar, prevBar, refBar, kind, breakLine);
    }

    /** MA7≥MA14≥MA28≥MA42 */
    static boolean passesBullAlign(LongCandlestickMA bar) {
        if (bar == null || bar.getMa7() == null || bar.getMa14() == null
                || bar.getMa28() == null || bar.getMa42() == null) {
            return false;
        }
        return ge(bar.getMa7(), bar.getMa14())
                && ge(bar.getMa14(), bar.getMa28())
                && ge(bar.getMa28(), bar.getMa42());
    }

    static int findCriticalBullAlignIndex(List<LongCandlestickMA> bars, int lastIdx) {
        if (bars == null || lastIdx < 0 || lastIdx >= bars.size()) {
            return -1;
        }
        if (!passesBullAlign(bars.get(lastIdx))) {
            return -1;
        }
        int i = lastIdx;
        while (i - 1 >= 0 && passesBullAlign(bars.get(i - 1))) {
            i--;
        }
        return i < lastIdx ? i : -1;
    }

    static int findLatestGoldenCrossIndex(List<LongCandlestickMA> bars, int lastIdx, int criticalIdx) {
        if (criticalIdx < 0) {
            return -1;
        }
        for (int i = lastIdx - 1; i > criticalIdx; i--) {
            if (isGoldenCrossAt(bars, i)) {
                return i;
            }
        }
        return -1;
    }

    static int findLatestDeathCrossKIndex(List<LongCandlestickMA> bars, int lastIdx, int criticalIdx) {
        if (criticalIdx < 0) {
            return -1;
        }
        for (int i = lastIdx - 1; i > criticalIdx; i--) {
            if (isDeathCrossKAt(bars, i)) {
                return i;
            }
        }
        return -1;
    }

    static boolean isGoldenCrossAt(List<LongCandlestickMA> bars, int i) {
        if (bars == null || i < 1 || i >= bars.size()) {
            return false;
        }
        if (!isMa7GeMa14(bars.get(i))) {
            return false;
        }
        return !isMa7GeMa14(bars.get(i - 1));
    }

    static boolean isDeathCrossKAt(List<LongCandlestickMA> bars, int i) {
        if (bars == null || i < 0 || i + 1 >= bars.size()) {
            return false;
        }
        if (!isMa7GeMa14(bars.get(i))) {
            return false;
        }
        return !isMa7GeMa14(bars.get(i + 1));
    }

    static boolean isMa7GeMa14(LongCandlestickMA bar) {
        if (bar == null || bar.getMa7() == null || bar.getMa14() == null) {
            return false;
        }
        return ge(bar.getMa7(), bar.getMa14());
    }

    static boolean passesBandHighEdge(LongCandlestickMA prevBar, LongCandlestickMA signalBar, double bandHigh) {
        if (prevBar == null || signalBar == null || prevBar.getClose() == null || signalBar.getClose() == null) {
            return false;
        }
        double prevClose = prevBar.getClose().doubleValue();
        double sigClose = signalBar.getClose().doubleValue();
        return prevClose <= bandHigh + EPS && sigClose > bandHigh + EPS;
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        LongCandlestickMA signalBar = hit.getSignalBar();
        return String.format(
                "1hMA多头4M突破,strategyTag=H1M4MB,ref=%s,breakLine=%.8f,sigTime=%s,sigClose=%.8f,refTime=%s",
                hit.getRefKind() != null ? hit.getRefKind().name() : "",
                hit.getBreakLine(),
                timeOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose().doubleValue() : 0,
                timeOf(hit.getRefBar()));
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[H1M4MB]1小时MA多头4M突破";
        }
        return String.format("[H1M4MB]1小时MA多头4M突破|多头排列+边沿破%sHigh|breakLine=%.8f",
                refLabel(hit.getRefKind()), hit.getBreakLine());
    }

    private static boolean ge(BigDecimal a, BigDecimal b) {
        return a.doubleValue() + EPS >= b.doubleValue();
    }

    private static String refLabel(RefKind kind) {
        if (kind == RefKind.GOLDEN) {
            return "金叉交叉K";
        }
        if (kind == RefKind.DEATH) {
            return "死叉交叉K";
        }
        if (kind == RefKind.CRITICAL) {
            return "多头关键K";
        }
        return "基准K";
    }

    private static String timeOf(LongCandlestickMA bar) {
        if (bar == null || bar.getOpenTime() == null) {
            return "";
        }
        return String.valueOf(bar.getOpenTime());
    }
}
