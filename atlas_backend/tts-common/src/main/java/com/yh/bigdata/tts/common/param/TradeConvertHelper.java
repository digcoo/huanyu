package com.yh.bigdata.tts.common.param;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class TradeConvertHelper {

    public static <T extends Trade> Map<String, T> parseMap(List<T> days) {
        Map<String, T> maps = Maps.newHashMap();
        days.forEach(day -> {
            maps.put(day.getCode() + "_" + day.getDay(), day);
        });
        return maps;
    }


    public static Map<String, List> parseSortMapList(final List trades, PeriodTypeEnum periodTypeEnum) {
        Map<String, List> maps = Maps.newConcurrentMap();
        if (!CollectionUtils.isEmpty(trades)) {
            for (int i = 0; i <trades.size(); i++) {
                Trade trade = (Trade)trades.get(i);
                trade.setPeriodTypeEnum(periodTypeEnum);
                if (maps.get(trade.getCode()) == null) {
                    maps.put(trade.getCode(), Lists.newArrayList(trade));
                }else {
                    maps.get(trade.getCode()).add(trade);
                }
            }

            for (String key : maps.keySet()) {
                List<Trade> list = maps.get(key);
                Collections.sort(list);
                maps.put(key, list);
            }

        }
        return maps;
    }

}
