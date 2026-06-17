package com.yh.bigdata.tts.spider.strategy.tools.gc2;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.Gc2StrategyParams;
import com.yh.bigdata.tts.common.utils.DateUtil;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 金叉二次突破 · ref 金叉 K + 信号桶首破 K
 */
public final class Gc2BreakoutTools {

    private static final double HIGH_EPS = 1e-6;
    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");

    private Gc2BreakoutTools() {
    }

    public enum Gc2TierKind {
        SHORT, MEDIUM, LONG
    }

    @Getter
    public static final class TierHit {
        private final Gc2TierKind kind;
        private final Trade referenceBar;
        private final Trade signalBar;

        TierHit(Gc2TierKind kind, Trade referenceBar, Trade signalBar) {
            this.kind = kind;
            this.referenceBar = referenceBar;
            this.signalBar = signalBar;
        }
    }

    public static TierHit findShortHit(StockBase stock, Gc2StrategyParams p) {
        Gc2StrategyParams params = p != null ? p : Gc2StrategyParams.defaults();
        return findHit(stock, Gc2TierKind.SHORT, PeriodTypeEnum.DAY,
                params.getLookbackShort(), Gc2BreakoutTools::dayKey, params);
    }

    public static TierHit findMediumHit(StockBase stock, Gc2StrategyParams p) {
        Gc2StrategyParams params = p != null ? p : Gc2StrategyParams.defaults();
        return findHit(stock, Gc2TierKind.MEDIUM, PeriodTypeEnum.WEEK,
                params.getLookbackMedium(), Gc2BreakoutTools::tradeWeekKey, params);
    }

    public static TierHit findLongHit(StockBase stock, Gc2StrategyParams p) {
        Gc2StrategyParams params = p != null ? p : Gc2StrategyParams.defaults();
        return findHit(stock, Gc2TierKind.LONG, PeriodTypeEnum.MONTH,
                params.getLookbackLong(), Gc2BreakoutTools::monthKey, params);
    }

    private static TierHit findHit(StockBase stock, Gc2TierKind kind, PeriodTypeEnum period,
                                   int lookback, Function<Trade, String> signalBucketKey,
                                   Gc2StrategyParams params) {
        int fetchBars = lookback + 40;
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(trades)) {
            return null;
        }

        Trade ref = Gc2StructureTools.findLatestGoldenCrossBar(trades, lookback);
        if (ref == null || ref.getHigh() == null) {
            return null;
        }

        List<Trade> signalBars = currentSignalBucket(trades, signalBucketKey);
        if (signalBars.isEmpty()) {
            return null;
        }

        Trade signal = null;
        for (Trade bar : signalBars) {
            if (bar.getClose() != null && bar.getClose() > ref.getHigh() + HIGH_EPS) {
                signal = bar;
                break;
            }
        }
        if (signal == null) {
            return null;
        }

        Trade lastInBucket = signalBars.get(signalBars.size() - 1);
        if (!sameBar(signal, lastInBucket)) {
            return null;
        }

        Trade latest = trades.get(trades.size() - 1);
        if (!sameBar(signal, latest)) {
            return null;
        }

        int refIdx = Gc2StructureTools.indexOfBar(trades, ref);
        int sigIdx = trades.size() - 1;
        if (refIdx < 0 || sigIdx - refIdx < params.getMinBarsAfterRef()) {
            return null;
        }

        if (!isCurrentCloseAboveBreakoutHigh(stock, signal)) {
            return null;
        }

        return new TierHit(kind, ref, signal);
    }

    private static List<Trade> currentSignalBucket(List<Trade> trades, Function<Trade, String> unitKeyFn) {
        Map<String, List<Trade>> byUnit = new LinkedHashMap<>();
        for (Trade bar : trades) {
            String key = unitKeyFn.apply(bar);
            if (key == null) {
                continue;
            }
            byUnit.computeIfAbsent(key, k -> new ArrayList<>()).add(bar);
        }
        if (byUnit.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> units = new ArrayList<>(byUnit.keySet());
        return new ArrayList<>(byUnit.get(units.get(units.size() - 1)));
    }

    private static boolean isCurrentCloseAboveBreakoutHigh(StockBase stock, Trade signalBar) {
        if (stock == null || signalBar == null || signalBar.getHigh() == null) {
            return false;
        }
        Double close = stock.getClose();
        return close != null && close > signalBar.getHigh() + HIGH_EPS;
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }

    private static String dayKey(Trade bar) {
        if (bar == null || bar.getDay() == null) {
            return null;
        }
        String day = bar.getDay().trim();
        return day.length() >= 10 ? day.substring(0, 10) : day;
    }

    private static String tradeWeekKey(Trade bar) {
        LocalDate d = parseDay(bar);
        return d != null ? d.with(DayOfWeek.MONDAY).format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    private static String monthKey(Trade bar) {
        LocalDate d = parseDay(bar);
        return d != null ? d.format(YM) : null;
    }

    private static LocalDate parseDay(Trade bar) {
        String day = dayKey(bar);
        if (day == null) {
            return null;
        }
        Date parsed = DateUtil.parseDate(day);
        if (parsed == null) {
            return null;
        }
        return parsed.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
