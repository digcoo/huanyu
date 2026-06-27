package com.yh.bigdata.tts.spider.strategy.tools;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 近 5 根日 K 平均成交额过滤（不含当前 K）
 */
public final class MinAvgAmountFilterTools {

    private MinAvgAmountFilterTools() {
    }

    public static boolean passes(StockBase stock, double minAmount) {
        if (stock == null || minAmount <= 0) {
            return minAmount <= 0;
        }
        List<Trade> lastTrades = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 10);
        if (CollectionUtils.isEmpty(lastTrades)) {
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

    public static boolean passWithMessage(StockBase stock, CheckResult checkResult, double minAmount) {
        if (minAmount <= 0) {
            return true;
        }
        if (!passes(stock, minAmount)) {
            return false;
        }
        if (checkResult != null) {
            checkResult.addRiskPeriod(PeriodTypeEnum.DAY,
                    "成交额≥" + (long) (minAmount / 10_000) + "万");
        }
        return true;
    }
}
