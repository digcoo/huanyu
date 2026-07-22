package com.binance.client.utils;

import com.binance.client.enums.PeriodTypeEnum;
import org.apache.commons.lang3.tuple.Pair;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

public class DateUtil {

    public static Pair<Long, Long> getFirstAndLastTimeOfPeriod(PeriodTypeEnum periodType, long timestamp) {

        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault());
        ZonedDateTime tmpPeriodStart = null;
        ZonedDateTime tmpPeriodEnd = null;

        switch (periodType) {
//            case HOUR1:
//                tmpPeriodStart = zonedDateTime.withMinute(0).withSecond(0).withNano(0);
//                tmpPeriodEnd = tmpPeriodStart.plusHours(1).minusNanos(1);
//                break;
//            case HOUR2:
//                tmpPeriodStart = zonedDateTime.withHour(zonedDateTime.getHour() / 2 * 2).withMinute(0).withSecond(0).withNano(0);
//                tmpPeriodEnd = tmpPeriodStart.plusHours(2).minusNanos(1);
//                break;
//            case HOUR12:
//                tmpPeriodStart = zonedDateTime.withHour(zonedDateTime.getHour() / 12 * 12).withMinute(0).withSecond(0).withNano(0);
//                tmpPeriodEnd = tmpPeriodStart.plusHours(12).minusNanos(1);
//                break;
            case DAY1:
                tmpPeriodStart = zonedDateTime.toLocalDate().atStartOfDay(ZoneId.systemDefault());
                tmpPeriodEnd = tmpPeriodStart.plusDays(1).minusNanos(1);
                break;
//            case DAY3:
//                // 获取当前年份的第一天
//                LocalDate firstDayOfYear = LocalDate.of(zonedDateTime.getYear(), 1, 1);
//                // 计算当前日期是第几个3天周期
//                long daysSinceFirst = java.time.temporal.ChronoUnit.DAYS.between(firstDayOfYear, zonedDateTime.toLocalDate());
//                long currentPeriodIndex = daysSinceFirst / 3;
//
//                // 计算当前3天周期的起始日期和结束日期
//                LocalDate periodStartDate = firstDayOfYear.plusDays(currentPeriodIndex * 3);
//
//                // 获取周期的起始和结束时间
//                tmpPeriodStart = periodStartDate.atStartOfDay(ZoneId.systemDefault());
//                tmpPeriodEnd = tmpPeriodStart.plusDays(3).minusNanos(1);
//                break;
            case WEEK:
                tmpPeriodStart = zonedDateTime.with(DayOfWeek.MONDAY)
                        .truncatedTo(java.time.temporal.ChronoUnit.DAYS);
                tmpPeriodEnd = tmpPeriodStart.plusDays(6).with(LocalTime.MAX);
                break;
//            case MONTH:
//                tmpPeriodStart = zonedDateTime.with(TemporalAdjusters.firstDayOfMonth())
//                        .truncatedTo(java.time.temporal.ChronoUnit.DAYS);
//                tmpPeriodEnd = zonedDateTime.with(TemporalAdjusters.lastDayOfMonth())
//                        .with(LocalTime.MAX);
//                break;
            default:
                break;
        }
        if (tmpPeriodStart == null || tmpPeriodEnd == null) {
            System.out.println("==");
        }
        return Pair.of(tmpPeriodStart.toInstant().toEpochMilli(), tmpPeriodEnd.toInstant().toEpochMilli());

    }


    public static void calculateTwoHourPeriod(LocalDate date, LocalTime time) {
        // 计算当前时间处于哪个2小时段
        int currentHour = time.getHour();
        int startHour = (currentHour / 2) * 2;  // 向下取偶数，表示周期开始

        // 确定起始时间和结束时间
        LocalTime startTime = LocalTime.of(startHour, 0);
        LocalTime endTime = startTime.plusHours(2).minusNanos(1);  // 减去一纳秒确保前闭后开区间

        // 打印结果
        System.out.println("Current 2-hour period: ");
        System.out.println("Start: " + LocalDateTime.of(date, startTime));
        System.out.println("End: " + LocalDateTime.of(date, endTime));
    }

    public static String formatDate(long timestamp, String format) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return zonedDateTime.format(formatter);

    }

    public static void main(String[] args) {
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        calculateTwoHourPeriod(date, time);
    }
}
