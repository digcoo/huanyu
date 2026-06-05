package com.yh.bigdata.tts.spider.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhou1 on 2018/12/22.
 */
public class Timeformat {
    private static final SimpleDateFormat microSecondLevel
            =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

    public static String getMicroString(Date triggerTime){
        return microSecondLevel.format(triggerTime);
    }
}
