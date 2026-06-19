package com.yh.bigdata.tts.spider.strategy.tools.ultralow;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

import java.util.List;

/**
 * 超短线策略 · 成交额 / MACD 可选过滤
 */
public final class UltraShortFilterTools {

    private UltraShortFilterTools() {
    }

    public static boolean passFilters(StockBase stock, CheckResult checkResult, UltraShortStrategyParams params) {
        if (stock == null) {
            return false;
        }
        UltraShortStrategyParams p = params != null ? params : UltraShortStrategyParams.defaults();

        if (!checkMinAvgAmount(stock, p.getMinAvgAmount())) {
            return false;
        }
        if (checkResult != null) {
            checkResult.addRiskPeriod(PeriodTypeEnum.DAY,
                    "成交额≥" + (long) (p.getMinAvgAmount() / 10_000) + "万");
        }

        if (p.isRequireMonthMacd()) {
            if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH)) {
                return false;
            }
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, "MACD>0");
            }
        }
        if (p.isRequireWeekMacd()) {
            if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.WEEK)) {
                return false;
            }
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, "MACD>0");
            }
        }
        if (p.isRequireDayMacd()) {
            if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.DAY)) {
                return false;
            }
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "MACD>0");
            }
        }
        return true;
    }

    private static boolean checkMinAvgAmount(StockBase stock, double minAmount) {
        List<Trade> lastTrades = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 10);
        if (lastTrades == null || lastTrades.isEmpty()) {
            return false;
        }
        int from = lastTrades.size() < 6 ? 0 : lastTrades.size() - 6;
        double avg = lastTrades.subList(from, lastTrades.size() - 1).stream()
                .filter(t -> t.getAmount() != null && t.getAmount() > 0.1)
                .mapToDouble(Trade::getAmount)
                .average()
                .orElse(0);
        return avg > minAmount;
    }
}
