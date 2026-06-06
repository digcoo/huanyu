package com.yh.bigdata.tts.common.indicator;

import com.yh.bigdata.tts.common.model.Trade;

import java.util.ArrayList;
import java.util.List;

public class ZaoPanUtils {

    private final static String ZAOPAN_10_00 = "10:00";

    public static List<Trade> getAllZaoPanList(List<Trade> trades){
        List<Trade> allZaoPanMinList = new ArrayList<>();
        for (Trade trade: trades) {
            if(trade.getDay().contains(ZAOPAN_10_00)) {
                allZaoPanMinList.add(trade);
            }
        }

        return allZaoPanMinList;
    }

}
