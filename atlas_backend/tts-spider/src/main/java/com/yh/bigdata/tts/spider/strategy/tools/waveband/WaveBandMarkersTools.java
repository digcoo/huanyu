package com.yh.bigdata.tts.spider.strategy.tools.waveband;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dto.atlas.AtlasGc2MarkersVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import org.springframework.util.CollectionUtils;

import java.util.List;

public final class WaveBandMarkersTools {

    private WaveBandMarkersTools() {
    }

    public static AtlasGc2MarkersVo resolve(StockBase stock, PeriodTypeEnum period) {
        if (stock == null) {
            return null;
        }
        PeriodTypeEnum p = period != null ? period : PeriodTypeEnum.DAY;
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, p, 1);
        if (CollectionUtils.isEmpty(bars)) {
            return null;
        }
        Trade signal = bars.get(bars.size() - 1);
        if (signal == null || signal.getDay() == null) {
            return null;
        }
        return AtlasGc2MarkersVo.builder()
                .signalDay(signal.getDay())
                .signalHigh(signal.getHigh())
                .build();
    }
}
