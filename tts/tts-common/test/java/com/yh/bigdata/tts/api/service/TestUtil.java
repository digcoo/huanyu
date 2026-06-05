package com.yh.bigdata.tts.api.service;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhou1 on 2019/1/21.
 */
public class TestUtil {
    //获取某天某时间点
    public static Date getSomeDaySomeTime(long numDayAfter,long designatedHour) throws Exception{
        if (designatedHour>=24){
            throw new Exception("not expected hour");
        }
        Calendar cal = Calendar.getInstance();

        //如果两个时间都为null，或者有一个时间和今天相同就要重新生成时间
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        //时分秒（毫秒数）
        long millisecond = hour*60*60*1000 + minute*60*1000 + second*1000;
//        long millisecond = hour*60*60*1000 + minute*60*1000 + second*1000;
        final long tmrwSomeTime = (designatedHour + 24 *numDayAfter )*60*60*1000;
        //第二天凌晨00:00:00
        cal.setTimeInMillis(cal.getTimeInMillis() - millisecond + tmrwSomeTime);
        return cal.getTime();
    }
}
