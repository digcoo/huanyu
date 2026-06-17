package com.yh.bigdata.tts.spider.strategy.tools.retest;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * bear / bull 宏观 Gate
 */
public final class RetestMacroTools {

    private RetestMacroTools() {
    }

    public static boolean passBear(StockBase stock, PeriodTypeEnum macroPeriod,
                                   RetestStructureTools.StructureHit hit) {
        if (stock == null || hit == null) {
            return false;
        }
        int hits = 0;
        if (UnilateralMacdTools.isMacdNegative(stock, macroPeriod)) {
            hits++;
        }
        if (isDeepDrawdownOrBelowMa60(stock)) {
            hits++;
        }
        List<Trade> dayBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 40);
        List<SwingPointTools.SwingPoint> swings = SwingPointTools.findFractalSwings(dayBars, 2);
        if (SwingPointTools.hasLowerHigh(swings)) {
            hits++;
        }
        if (hits < 2) {
            return false;
        }
        return nearStageLow(stock, hit.getL0());
    }

    public static boolean passBull(StockBase stock, PeriodTypeEnum macroPeriod,
                                   RetestStructureTools.StructureHit hit) {
        if (stock == null || hit == null) {
            return false;
        }
        int hits = 0;
        if (UnilateralMacdTools.isMacdPositive(stock, macroPeriod)) {
            hits++;
        }
        if (isAboveMa20Ma60(stock)) {
            hits++;
        }
        List<Trade> dayBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 40);
        List<SwingPointTools.SwingPoint> swings = SwingPointTools.findFractalSwings(dayBars, 2);
        if (SwingPointTools.hasHigherLow(swings)) {
            hits++;
        }
        if (hits < 2) {
            return false;
        }
        return holdsMa20(stock, hit.getL1());
    }

    private static boolean isDeepDrawdownOrBelowMa60(StockBase stock) {
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 60);
        if (CollectionUtils.isEmpty(bars)) {
            return false;
        }
        Double close = stock.getClose();
        if (close == null) {
            close = bars.get(bars.size() - 1).getClose();
        }
        if (close == null) {
            return false;
        }
        double ma60 = simpleMaClose(bars, 60);
        if (ma60 > 0 && close < ma60) {
            return true;
        }
        double high60 = bars.stream().map(Trade::getHigh).filter(h -> h != null).max(Double::compare)
                .orElse(0D);
        return high60 > 0 && (high60 - close) / high60 >= 0.25;
    }

    private static boolean isAboveMa20Ma60(StockBase stock) {
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 60);
        if (CollectionUtils.isEmpty(bars)) {
            return false;
        }
        Double close = stock.getClose();
        if (close == null) {
            close = bars.get(bars.size() - 1).getClose();
        }
        if (close == null) {
            return false;
        }
        double ma20 = simpleMaClose(bars, 20);
        double ma60 = simpleMaClose(bars, 60);
        return ma20 > 0 && ma60 > 0 && close > ma20 && ma20 > ma60;
    }

    private static boolean holdsMa20(StockBase stock, Trade l1) {
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 25);
        if (CollectionUtils.isEmpty(bars) || l1 == null || l1.getLow() == null) {
            return true;
        }
        double ma20 = simpleMaClose(bars, 20);
        return ma20 <= 0 || l1.getLow() >= ma20 * 0.98;
    }

    private static boolean nearStageLow(StockBase stock, Trade l0) {
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 60);
        if (CollectionUtils.isEmpty(bars) || l0 == null || l0.getLow() == null) {
            return true;
        }
        double minLow = bars.stream().map(Trade::getLow).filter(v -> v != null).min(Double::compare).orElse(0D);
        if (minLow <= 0) {
            return true;
        }
        return l0.getLow() <= minLow * 1.05;
    }

    private static double simpleMaClose(List<Trade> bars, int period) {
        if (bars.isEmpty()) {
            return 0;
        }
        int from = Math.max(0, bars.size() - period);
        double sum = 0;
        int count = 0;
        for (int i = from; i < bars.size(); i++) {
            Double c = bars.get(i).getClose();
            if (c != null) {
                sum += c;
                count++;
            }
        }
        return count == 0 ? 0 : sum / count;
    }
}
