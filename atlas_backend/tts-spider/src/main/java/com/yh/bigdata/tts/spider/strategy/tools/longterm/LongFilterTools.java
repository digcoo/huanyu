package com.yh.bigdata.tts.spider.strategy.tools.longterm;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.LongStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

import java.util.List;

/**
 * 长线策略 · 成交额 + 可选 MACD/年金叉（无大阴线过滤）
 */
public final class LongFilterTools {

    private LongFilterTools() {
    }

    public static boolean passFilters(StockBase stock, CheckResult checkResult, LongStrategyParams params) {
        if (stock == null) {
            return false;
        }
        LongStrategyParams p = params != null ? params : LongStrategyParams.defaults();

        if (!checkMinAvgAmount(stock, p.getMinAvgAmount())) {
            return false;
        }
        if (checkResult != null) {
            checkResult.addRiskPeriod(PeriodTypeEnum.DAY,
                    "成交额≥" + (long) (p.getMinAvgAmount() / 10_000) + "万");
        }

        if (p.isRequireYearMacd()) {
            if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.YEAR)) {
                return false;
            }
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.YEAR, "年MACD>0");
            }
        }

        if (p.isRequireMonthMacd()) {
            if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH)) {
                return false;
            }
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, "月MACD>0");
            }
        }

        if (p.isRequireYearGoldenCross()) {
            if (!UnilateralMacdTools.isGoldenCross(stock, PeriodTypeEnum.YEAR)) {
                return false;
            }
            if (checkResult != null) {
                checkResult.addSignal(PeriodTypeEnum.YEAR, "年K MACD金叉");
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
