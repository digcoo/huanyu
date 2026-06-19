package com.yh.bigdata.tts.spider.strategy.tools.medium;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MediumStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

import java.util.List;

/**
 * 中线策略 · 成交额 + 可选 MACD/月金叉（无大阴线过滤）
 */
public final class MediumFilterTools {

    private MediumFilterTools() {
    }

    public static boolean passFilters(StockBase stock, CheckResult checkResult, MediumStrategyParams params) {
        if (stock == null) {
            return false;
        }
        MediumStrategyParams p = params != null ? params : MediumStrategyParams.defaults();

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
                checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, "月MACD>0");
            }
        }

        if (p.isRequireYearMacd()) {
            if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.YEAR)) {
                return false;
            }
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.YEAR, "年MACD>0");
            }
        }

        if (p.isRequireMonthGoldenCross()) {
            if (!UnilateralMacdTools.isGoldenCross(stock, PeriodTypeEnum.MONTH)) {
                return false;
            }
            if (checkResult != null) {
                checkResult.addSignal(PeriodTypeEnum.MONTH, "月K MACD金叉");
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
