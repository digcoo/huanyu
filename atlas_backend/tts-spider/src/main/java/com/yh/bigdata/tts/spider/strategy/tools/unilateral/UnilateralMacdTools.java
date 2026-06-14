package com.yh.bigdata.tts.spider.strategy.tools.unilateral;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 金叉策略 v3.0 · MACD 判定
 */
public final class UnilateralMacdTools {

    private UnilateralMacdTools() {
    }

    /** 当前柱 MACD 柱 &gt; 0 */
    public static boolean isMacdPositive(StockBase stock, PeriodTypeEnum period) {
        MACDIndicatorUtils.MACDPoint latest = latestMacdPoint(stock, period);
        return latest != null && latest.getMacd() > 0;
    }

    /** 当前柱 MACD 金叉（红柱由负转正） */
    public static boolean isGoldenCross(StockBase stock, PeriodTypeEnum period) {
        MACDIndicatorUtils.MACDPoint latest = latestMacdPoint(stock, period);
        return latest != null && latest.isIfRedGoldCross();
    }

    /** 当前柱 MACD 柱 &gt; 0 且非金叉（红柱由负转正） */
    public static boolean isMacdPositiveNotGoldenCross(StockBase stock, PeriodTypeEnum period) {
        return isMacdPositive(stock, period) && !isGoldenCross(stock, period);
    }

    /** 当前柱 MACD 柱 &lt; 0 */
    public static boolean isMacdNegative(StockBase stock, PeriodTypeEnum period) {
        MACDIndicatorUtils.MACDPoint latest = latestMacdPoint(stock, period);
        return latest != null && latest.getMacd() < 0;
    }

    private static MACDIndicatorUtils.MACDPoint latestMacdPoint(StockBase stock, PeriodTypeEnum period) {
        if (stock == null || period == null) {
            return null;
        }
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, 100);
        if (CollectionUtils.isEmpty(trades)) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(trades));
        if (CollectionUtils.isEmpty(points)) {
            return null;
        }
        return points.get(points.size() - 1);
    }
}
