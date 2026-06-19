package com.yh.bigdata.tts.spider.strategy.tools.trend;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.TrendV2StrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

import java.util.List;

/**
 * 短线策略 · 成交额 + 可选 MACD/周金叉 + 前周/前月非大阴线
 */
public final class TrendV2FilterTools {

    private TrendV2FilterTools() {
    }

    public static boolean passFilters(StockBase stock, CheckResult checkResult, TrendV2StrategyParams params) {
        if (stock == null) {
            return false;
        }
        TrendV2StrategyParams p = params != null ? params : TrendV2StrategyParams.defaults();

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

        if (p.isRequireWeekMacd()) {
            if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.WEEK)) {
                return false;
            }
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, "周MACD>0");
            }
        }

        if (p.isRequireDayMacd()) {
            if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.DAY)) {
                return false;
            }
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "日MACD>0");
            }
        }

        if (p.isRequireWeekGoldenCross()) {
            if (!UnilateralMacdTools.isGoldenCross(stock, PeriodTypeEnum.WEEK)) {
                return false;
            }
            if (checkResult != null) {
                checkResult.addSignal(PeriodTypeEnum.WEEK, "周K MACD金叉");
            }
        }

        if (!isPrevBarNotLargeBear(stock, PeriodTypeEnum.WEEK, p.getWeekPrevMaxBearPct())) {
            return false;
        }
        if (checkResult != null) {
            checkResult.addRiskPeriod(PeriodTypeEnum.WEEK,
                    "前周非大阴线(实体阴线≤" + (int) (p.getWeekPrevMaxBearPct() * 100) + "%)");
        }

        if (!isPrevBarNotLargeBear(stock, PeriodTypeEnum.MONTH, p.getMonthPrevMaxBearPct())) {
            return false;
        }
        if (checkResult != null) {
            checkResult.addRiskPeriod(PeriodTypeEnum.MONTH,
                    "前月非大阴线(实体阴线≤" + (int) (p.getMonthPrevMaxBearPct() * 100) + "%)");
        }

        return true;
    }

    static boolean isPrevBarNotLargeBear(StockBase stock, PeriodTypeEnum period, double maxBearBodyRate) {
        Trade prev = RealtimeStockCache.getLastTrade(stock, period, 1);
        if (prev == null || prev.getOpen() == null || prev.getOpen() <= 0 || prev.getClose() == null) {
            return false;
        }
        Double shitiRate = prev.getShitiRate();
        if (shitiRate == null) {
            return false;
        }
        if (shitiRate >= 0) {
            return true;
        }
        return shitiRate >= -maxBearBodyRate;
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
