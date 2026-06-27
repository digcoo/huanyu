package com.yh.bigdata.tts.common.backtest;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.DateUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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

    static Trade deriveFromDayBars(List<Trade> dayBarsUpToAsOf,
                                   String asOfDay,
                                   String anchorDay,
                                   PeriodTypeEnum period) {
        List<Trade> bucket = new ArrayList<>();
        for (Trade bar : dayBarsUpToAsOf) {
            if (bar == null || bar.getDay() == null) {
                continue;
            }
            String day = bar.getDay().length() >= 10 ? bar.getDay().substring(0, 10) : bar.getDay();
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
        bucket.sort((a, b) -> a.getDay().compareTo(b.getDay()));

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
