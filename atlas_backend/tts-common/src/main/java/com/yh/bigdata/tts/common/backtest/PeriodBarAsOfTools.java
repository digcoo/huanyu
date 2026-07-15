package com.yh.bigdata.tts.common.backtest;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.DateUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 截至 asOfDay，从日 K 派生进行中的周/月/季/年 K（与 weekk/monthk 锚点 day 规则一致）。
 */
public final class PeriodBarAsOfTools {

    private PeriodBarAsOfTools() {
    }

    /**
     * 先按 bar.day 截断，再在不包含当前周期锚点 bar 时，用日 K 聚合补一根进行中 K。
     */
    public static List<Trade> sliceWithDerivedInProgress(List<?> rawPeriod,
                                                       List<Trade> dayBarsUpToAsOf,
                                                       String asOfDay,
                                                       PeriodTypeEnum period) {
        long asOfTime = DateUtil.parseDate(asOfDay).getTime();
        List<Trade> sliced = slice(rawPeriod, asOfTime);
        if (period == null
                || period == PeriodTypeEnum.DAY
                || period == PeriodTypeEnum.MIN30
                || dayBarsUpToAsOf == null
                || dayBarsUpToAsOf.isEmpty()) {
            return sliced;
        }

        String anchorDay = anchorDayFor(asOfDay, period);
        if (anchorDay == null) {
            return sliced;
        }
        if (containsBarDay(sliced, anchorDay)) {
            return sliced;
        }

        Trade derived = deriveFromDayBars(dayBarsUpToAsOf, asOfDay, anchorDay, period);
        if (derived == null) {
            return sliced;
        }

        List<Trade> merged = new ArrayList<>(sliced);
        merged.add(derived);
        return merged;
    }

    /**
     * 纯日 K 回放：聚合截至 asOfDay 的完整周/月/年序列（含进行中 bar）。
     */
    public static List<Trade> buildPeriodSeriesFromDayBars(List<Trade> dayBarsUpToAsOf,
                                                           String asOfDay,
                                                           PeriodTypeEnum period) {
        if (dayBarsUpToAsOf == null || dayBarsUpToAsOf.isEmpty() || period == null
                || period == PeriodTypeEnum.DAY || period == PeriodTypeEnum.MIN30) {
            return new ArrayList<>();
        }
        Map<String, List<Trade>> buckets = new TreeMap<>();
        for (Trade bar : dayBarsUpToAsOf) {
            if (bar == null || bar.getDay() == null) {
                continue;
            }
            String day = normalizeDay(bar.getDay());
            if (day.compareTo(asOfDay) > 0) {
                continue;
            }
            String anchor = anchorDayFor(day, period);
            if (anchor == null) {
                continue;
            }
            buckets.computeIfAbsent(anchor, k -> new ArrayList<>()).add(bar);
        }
        List<Trade> out = new ArrayList<>();
        for (Map.Entry<String, List<Trade>> entry : buckets.entrySet()) {
            Trade aggregated = aggregateBucket(entry.getValue(), entry.getKey());
            if (aggregated != null) {
                out.add(aggregated);
            }
        }
        return out;
    }

    public static boolean isInProgressBar(String asOfDay, String barDay, PeriodTypeEnum period) {
        String currentAnchor = anchorDayFor(asOfDay, period);
        return currentAnchor != null
                && currentAnchor.equals(barDay)
                && asOfDay.compareTo(barDay) < 0;
    }

    static Trade deriveFromDayBars(List<Trade> dayBarsUpToAsOf,
                                   String asOfDay,
                                   String anchorDay,
                                   PeriodTypeEnum period) {
        List<Trade> bucket = new ArrayList<>();
        for (Trade bar : dayBarsUpToAsOf) {
            if (bar == null || bar.getDay() == null) {
                continue;
            }
            String day = normalizeDay(bar.getDay());
            if (day.compareTo(asOfDay) > 0) {
                continue;
            }
            String barAnchor = anchorDayFor(day, period);
            if (anchorDay.equals(barAnchor)) {
                bucket.add(bar);
            }
        }
        if (bucket.isEmpty()) {
            return null;
        }
        return aggregateBucket(bucket, anchorDay);
    }

    static Trade aggregateBucket(List<Trade> bucket, String anchorDay) {
        if (bucket == null || bucket.isEmpty()) {
            return null;
        }
        bucket.sort((a, b) -> normalizeDay(a.getDay()).compareTo(normalizeDay(b.getDay())));

        Trade first = bucket.get(0);
        Trade last = bucket.get(bucket.size() - 1);

        Trade out = new Trade();
        out.setCode(first.getCode());
        out.setName(first.getName());
        out.setDay(anchorDay);
        out.setOpen(first.getOpen());
        out.setClose(last.getClose());
        out.setPrevClose(first.getPrevClose());

        double high = Double.NEGATIVE_INFINITY;
        double low = Double.POSITIVE_INFINITY;
        long volume = 0;
        double amount = 0;
        for (Trade bar : bucket) {
            if (bar.getHigh() != null) {
                high = Math.max(high, bar.getHigh());
            }
            if (bar.getLow() != null) {
                low = Math.min(low, bar.getLow());
            }
            if (bar.getVolume() != null) {
                volume += bar.getVolume();
            }
            if (bar.getAmount() != null) {
                amount += bar.getAmount();
            }
        }
        if (high != Double.NEGATIVE_INFINITY) {
            out.setHigh(high);
        }
        if (low != Double.POSITIVE_INFINITY) {
            out.setLow(low);
        }
        if (volume > 0) {
            out.setVolume(volume);
        }
        if (amount > 0) {
            out.setAmount(amount);
        }
        return out;
    }

    static String normalizeDay(String day) {
        return day.length() >= 10 ? day.substring(0, 10) : day;
    }

    static String anchorDayFor(String someDay, PeriodTypeEnum period) {
        if (someDay == null || someDay.isEmpty() || period == null) {
            return null;
        }
        try {
            switch (period) {
                case WEEK:
                    return DateUtil.parse2Friday(someDay);
                case MONTH:
                    return DateUtil.parse2MonthLastDay(someDay);
                case QUARTER:
                    return DateUtil.parse2QuarterLastDay(someDay);
                case YEAR:
                    return DateUtil.parse2YearLastDay(someDay);
                default:
                    return null;
            }
        } catch (ParseException e) {
            return null;
        }
    }

    private static boolean containsBarDay(List<Trade> bars, String day) {
        for (Trade bar : bars) {
            if (bar != null && day.equals(bar.getDay())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    static List<Trade> slice(List<?> raw, long asOfTime) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        List<Trade> source = (List<Trade>) raw;
        List<Trade> out = new ArrayList<>();
        for (Trade trade : source) {
            if (trade.getDay() == null) {
                continue;
            }
            long time = DateUtil.parseDate(trade.getDay()).getTime();
            if (time <= asOfTime) {
                out.add(trade);
            }
        }
        return out;
    }
}
