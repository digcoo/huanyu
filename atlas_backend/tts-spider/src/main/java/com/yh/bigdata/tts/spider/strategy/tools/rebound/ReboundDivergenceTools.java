package com.yh.bigdata.tts.spider.strategy.tools.rebound;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 深坑反弹 v1.0 · MACD 底背离（月 K 绿柱区）
 */
public final class ReboundDivergenceTools {

    public static final String SIGNAL_MSG = "月MACD底背离";

    private ReboundDivergenceTools() {
    }

    public static boolean checkMonthMacdBullishDivergence(StockBase stock) {
        List<Trade> monthTrades = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.MONTH, 60);
        if (monthTrades == null || monthTrades.size() < 15) {
            return false;
        }
        List<MACDIndicatorUtils.MACDPoint> macdPoints =
                MACDIndicatorUtils.calculateMACD(Ticker.from(monthTrades));
        if (macdPoints == null || macdPoints.size() < 10) {
            return false;
        }

        Pair<MACDIndicatorUtils.MACDPoint, MACDIndicatorUtils.MACDPoint> greenGold =
                MACDIndicatorUtils.getLatestGreenGlodMACDPoint(macdPoints);
        if (greenGold == null || greenGold.getLeft() == null || greenGold.getRight() == null) {
            return false;
        }

        MACDIndicatorUtils.MACDPoint left = greenGold.getLeft();
        MACDIndicatorUtils.MACDPoint right = greenGold.getRight();
        double leftLow = left.getTicker().getLow();
        double rightLow = right.getTicker().getLow();
        if (rightLow >= leftLow) {
            return false;
        }
        return right.getDif() > left.getDif();
    }
}
