package com.yh.bigdata.tts.common.binance.utils;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;

public final class BinanceUtils {

    public static String getCandleKey(String symbol, PeriodTypeEnum periodTypeEnum) {
        return symbol + ":" + periodTypeEnum.getCryptoCode();
    }
}
