package com.yh.bigdata.tts.spider.strategy.tools.rebound;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.ReboundStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.IndicatorCommonUtils;

import java.util.List;

/**
 * 深坑反弹 v1.0 · Gate
 */
public final class ReboundGateTools {

    private static final double CHASE_52W_RATIO = 0.95;

    private ReboundGateTools() {
    }

    public static boolean passGate(StockBase stock, CheckResult checkResult, ReboundStrategyParams params) {
        if (stock == null) {
            return false;
        }
        ReboundStrategyParams p = params != null ? params : ReboundStrategyParams.defaults();

        if (!checkMinAvgAmount(stock, p.getMinAvgAmount())) {
            return false;
        }
        if (checkResult != null) {
            checkResult.addRiskPeriod(PeriodTypeEnum.DAY,
                    "成交额≥" + (long) (p.getMinAvgAmount() / 10_000) + "万");
        }

        if (!checkNotChasing(stock)) {
            return false;
        }
        if (checkResult != null) {
            checkResult.addRiskPeriod(PeriodTypeEnum.DAY, "不追高");
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

    /** 不追高位：远离 52 周高，或仍在大周期 MA20 下方 */
    private static boolean checkNotChasing(StockBase stock) {
        Trade day = RealtimeStockCache.getLastTrade(stock, PeriodTypeEnum.DAY, 0);
        if (day == null || day.getClose() == null) {
            return false;
        }
        double close = day.getClose();

        if (stock.getHigh52w() != null && stock.getHigh52w() > 0) {
            return close < stock.getHigh52w() * CHASE_52W_RATIO;
        }

        boolean weekUnderMa20 = !IndicatorCommonUtils.checkOverMA20(stock, PeriodTypeEnum.WEEK).isSuccess();
        boolean monthUnderMa20 = !IndicatorCommonUtils.checkOverMA20(stock, PeriodTypeEnum.MONTH).isSuccess();
        return weekUnderMa20 || monthUnderMa20;
    }
}
