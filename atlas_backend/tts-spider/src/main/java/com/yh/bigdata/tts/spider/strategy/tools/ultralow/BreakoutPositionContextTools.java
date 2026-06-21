package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 突破信号 · 大周期位置 Context（底部须强突破 ref.high）
 */
public final class BreakoutPositionContextTools {

    public static final double DEFAULT_BOTTOM_PCT = 0.30;
    public static final int MIN_BARS_FOR_CONTEXT = 10;

    public enum MacroTier {
        /** 超短 30m → 日 K 区间位置 */
        ULTRA(PeriodTypeEnum.DAY, 60),
        /** 短 日K → 周 K 区间位置 */
        SHORT(PeriodTypeEnum.WEEK, 26),
        /** 中 周K → 月 K 区间位置 */
        MEDIUM(PeriodTypeEnum.MONTH, 12),
        /** 长 月K → 更长月 K 区间位置 */
        LONG(PeriodTypeEnum.MONTH, 36);

        private final PeriodTypeEnum period;
        private final int defaultLookback;

        MacroTier(PeriodTypeEnum period, int defaultLookback) {
            this.period = period;
            this.defaultLookback = defaultLookback;
        }

        public PeriodTypeEnum getPeriod() {
            return period;
        }

        public int getDefaultLookback() {
            return defaultLookback;
        }
    }

    @Getter
    public static final class ContextSnapshot {
        private final double positionPct;
        private final boolean atBottom;
        private final boolean reliable;

        ContextSnapshot(double positionPct, boolean atBottom, boolean reliable) {
            this.positionPct = positionPct;
            this.atBottom = atBottom;
            this.reliable = reliable;
        }

        static ContextSnapshot unreliable() {
            return new ContextSnapshot(0.5, false, false);
        }
    }

    private BreakoutPositionContextTools() {
    }

    public static ContextSnapshot evaluate(StockBase stock, MacroTier tier) {
        return evaluate(stock, tier, tier.getDefaultLookback(), DEFAULT_BOTTOM_PCT);
    }

    public static ContextSnapshot evaluate(StockBase stock, MacroTier tier,
                                           int lookback, double bottomPct) {
        if (stock == null || tier == null || lookback < MIN_BARS_FOR_CONTEXT) {
            return ContextSnapshot.unreliable();
        }
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, tier.getPeriod(), lookback);
        if (CollectionUtils.isEmpty(bars) || bars.size() < MIN_BARS_FOR_CONTEXT) {
            return ContextSnapshot.unreliable();
        }

        double rangeLow = Double.MAX_VALUE;
        double rangeHigh = -Double.MAX_VALUE;
        for (Trade bar : bars) {
            if (bar.getLow() != null) {
                rangeLow = Math.min(rangeLow, bar.getLow());
            }
            if (bar.getHigh() != null) {
                rangeHigh = Math.max(rangeHigh, bar.getHigh());
            }
        }

        Trade latest = bars.get(bars.size() - 1);
        Double close = latest.getClose();
        if (close == null || rangeLow >= rangeHigh) {
            return ContextSnapshot.unreliable();
        }

        double positionPct = (close - rangeLow) / (rangeHigh - rangeLow);
        boolean atBottom = positionPct < bottomPct;
        return new ContextSnapshot(positionPct, atBottom, true);
    }
}
