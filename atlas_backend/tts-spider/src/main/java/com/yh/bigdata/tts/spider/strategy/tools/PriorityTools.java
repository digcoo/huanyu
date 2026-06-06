package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.MathUtil;

import java.util.List;


/**
 * 偏好工具
 */
public final class PriorityTools {

    /**
     * 偏好： 安全线以上
     */
    public static CheckResponse checkBaseLinePriority(StockBase stockBase, PeriodTypeEnum trendPeriodType) {

        Trade trendTrade0 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 0);
        Trade trendTrade1 = RealtimeStockCache.getLastTrade(stockBase, trendPeriodType, 1);

        if (trendTrade0 == null) {
            return new CheckResponse(false, "安全线以上", trendPeriodType);
        }

        if(trendTrade1 == null) {
            if (trendTrade0.getShitiRate() > 0) {
                return new CheckResponse(true, "安全线以上", trendPeriodType);
            }
        } else {
            if (trendTrade0.getClose() > trendTrade1.getBaseline())  {
                return new CheckResponse(true, "安全线以上", trendPeriodType);
            }
        }

        return new CheckResponse(false, "安全线以上", trendPeriodType);

    }


    /**
     * 偏好： 平均成交额>8000万
     */
    public static CheckResponse checkBaseAmountPriority(StockBase stockBase) {

        List<Trade> lastTrades = RealtimeStockCache.getLastTrades(stockBase, PeriodTypeEnum.DAY, 10);
        double minAvgAmount = lastTrades.subList(lastTrades.size() < 6 ? 0 : lastTrades.size() - 6, lastTrades.size() - 1).stream().filter(tmp -> tmp.getAmount() != null && tmp.getAmount() > 0.1).mapToDouble(Trade::getAmount).average().orElse(0);
        if (minAvgAmount > 5_000_0000) {
            return new CheckResponse(true, "平均成交额>5000万", PeriodTypeEnum.DAY);
        }

        return new CheckResponse(false, "平均成交额>5000万", PeriodTypeEnum.DAY);

    }

}
