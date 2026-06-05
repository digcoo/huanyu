package com.yh.bigdata.tts.common.indicator;

import com.yh.bigdata.tts.common.model.Trade;

import java.util.List;

public final class KlineIndicatorUtils {

    public static Trade getLastRedTrade(List<Trade> trades) {
        for (int i = trades.size() - 1; i > 0; i --) {
            Trade trade = trades.get(i);
            if (trade.getShitiRate() > 0 && trade.getChangeRate() > 0) {
                trades.get(i).setPreTrade(trades.get(i - 1));
                return trades.get(i);
            }
        }
        return null;
    }
}
