package com.yh.bigdata.tts.spider.strategy.tools.unilateral;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.UnilateralStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.RiskTools;

import java.util.List;

/**
 * 单边趋势 v1.0 · Gate 硬门槛
 */
public final class UnilateralGateTools {

    private UnilateralGateTools() {
    }

    public static boolean passGate(StockBase stock, CheckResult checkResult, UnilateralStrategyParams params) {
        if (stock == null) {
            return false;
        }
        UnilateralStrategyParams p = params != null ? params : UnilateralStrategyParams.defaults();

        if (!checkMinAvgAmount(stock, p.getMinAvgAmount())) {
            return false;
        }
        if (checkResult != null) {
            checkResult.addRiskPeriod(PeriodTypeEnum.DAY, "成交额≥" + (long) (p.getMinAvgAmount() / 10_000) + "万");
        }

        boolean weekOk = RiskTools.checkNotUnderLowRisk(stock, PeriodTypeEnum.WEEK).isSuccess();
        boolean monthOk = RiskTools.checkNotUnderLowRisk(stock, PeriodTypeEnum.MONTH).isSuccess();
        if (!weekOk && !monthOk) {
            return false;
        }
        if (checkResult != null) {
            if (weekOk) {
                checkResult.addRiskPeriod(PeriodTypeEnum.WEEK, "未跌破支撑Low");
            }
            if (monthOk) {
                checkResult.addRiskPeriod(PeriodTypeEnum.MONTH, "未跌破支撑Low");
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
