package com.yh.bigdata.tts.spider.realtime;

import java.util.Calendar;

/**
 * A-share intraday session guard for realtime spiders (Mon–Fri 09:26–15:01).
 */
public final class TradingSessionUtils {

    private TradingSessionUtils() {
    }

    public static boolean isInTradingSession() {
        return isInTradingSession(System.currentTimeMillis());
    }

    public static boolean isInTradingSession(long nowMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(nowMillis);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return false;
        }

        Calendar start = (Calendar) cal.clone();
        start.set(Calendar.HOUR_OF_DAY, 9);
        start.set(Calendar.MINUTE, 26);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) cal.clone();
        end.set(Calendar.HOUR_OF_DAY, 15);
        end.set(Calendar.MINUTE, 1);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        return nowMillis >= start.getTimeInMillis() && nowMillis <= end.getTimeInMillis();
    }
}
