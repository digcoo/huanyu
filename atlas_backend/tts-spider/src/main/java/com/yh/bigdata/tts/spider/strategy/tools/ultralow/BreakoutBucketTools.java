package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.DateUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Date;

/**
 * 突破阶梯 · 时间桶分组键（交易日 / 周 / 月 / 年）
 */
public final class BreakoutBucketTools {

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter Y = DateTimeFormatter.ofPattern("yyyy");
    private static final WeekFields ISO_WEEK = WeekFields.ISO;

    private BreakoutBucketTools() {
    }

    static String dayKey(Trade bar) {
        if (bar == null || bar.getDay() == null) {
            return null;
        }
        String day = bar.getDay().trim();
        return day.length() >= 10 ? day.substring(0, 10) : day;
    }

    static String weekKey(Trade bar) {
        LocalDate d = parseDay(bar);
        if (d == null) {
            return null;
        }
        int year = d.get(ISO_WEEK.weekBasedYear());
        int week = d.get(ISO_WEEK.weekOfWeekBasedYear());
        return year + "-W" + String.format("%02d", week);
    }

    static String monthKey(Trade bar) {
        LocalDate d = parseDay(bar);
        return d != null ? d.format(YM) : null;
    }

    static String yearKey(Trade bar) {
        LocalDate d = parseDay(bar);
        return d != null ? d.format(Y) : null;
    }

    /** 交易周标识（周一日期），用于日K背景分组 */
    static String tradeWeekKey(Trade bar) {
        LocalDate d = parseDay(bar);
        return d != null ? d.with(DayOfWeek.MONDAY).format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
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
