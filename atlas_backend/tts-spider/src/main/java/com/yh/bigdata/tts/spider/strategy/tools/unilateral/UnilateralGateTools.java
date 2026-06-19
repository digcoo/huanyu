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
 * 单边趋势 Gate · v1.2：成交额 + 月 MACD&gt;0 + 周/月未破 Low + 前周/前月非大阴线
 */
public final class UnilateralGateTools {

    /** 前一周 K 实体阴线率上限（绝对值），超过视为大阴线 */
    private static final double WEEK_PREV_MAX_BEAR_BODY_RATE = 0.05;
    /** 前一月 K 实体阴线率上限（绝对值），超过视为大阴线 */
    private static final double MONTH_PREV_MAX_BEAR_BODY_RATE = 0.10;

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

        if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MONTH)) {
            return false;
        }
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, "MACD>0");
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

        if (!checkPrevBarNotLargeBear(stock, PeriodTypeEnum.WEEK, WEEK_PREV_MAX_BEAR_BODY_RATE)) {
            return false;
        }
        if (checkResult != null) {
            checkResult.addRiskPeriod(PeriodTypeEnum.WEEK,
                    "前周非大阴线(实体阴线≤" + (int) (WEEK_PREV_MAX_BEAR_BODY_RATE * 100) + "%)");
        }

        if (!checkPrevBarNotLargeBear(stock, PeriodTypeEnum.MONTH, MONTH_PREV_MAX_BEAR_BODY_RATE)) {
            return false;
        }
        if (checkResult != null) {
            checkResult.addRiskPeriod(PeriodTypeEnum.MONTH,
                    "前月非大阴线(实体阴线≤" + (int) (MONTH_PREV_MAX_BEAR_BODY_RATE * 100) + "%)");
        }

        return true;
    }

    /**
     * 上一根完整 K：阳线/小阴线通过；大阴线（实体跌幅 &gt; maxBearBodyRate）不通过
     */
    private static boolean checkPrevBarNotLargeBear(StockBase stock, PeriodTypeEnum period,
                                                    double maxBearBodyRate) {
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
