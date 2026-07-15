package com.yh.bigdata.tts.spider.strategy.tools.macdgc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdGoldenCrossStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MACD金叉：末 K MACD 金叉 + 可选成交额 / 末 K 上涨率 / 近 N 根历史上涨率门。
 */
public final class MacdGoldenCrossTools {

    private static final double EPS = 1e-6;

    private MacdGoldenCrossTools() {
    }

    public static boolean passesTierGate(StockBase stock, CheckResult checkResult,
                                         MacdGoldenCrossStrategyParams params) {
        MacdGoldenCrossStrategyParams p = params != null ? params : MacdGoldenCrossStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return false;
        }
        if (!UnilateralMacdTools.isGoldenCross(stock, period)) {
            appendMessage(checkResult, period, periodLabel(period) + "非MACD金叉");
            return false;
        }
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, period, "成交额不足");
            return false;
        }
        int lookback = Math.max(p.getHistoryLookbackBars(), 1);
        int fetchBars = Math.max(lookback + 1, 2);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        return passesTierGateOnBars(periodBars, checkResult, period, p);
    }

    static boolean passesTierGateOnBars(List<Trade> periodBars, CheckResult checkResult,
                                        PeriodTypeEnum period, MacdGoldenCrossStrategyParams params) {
        MacdGoldenCrossStrategyParams p = params != null ? params : MacdGoldenCrossStrategyParams.defaults();
        int lookback = Math.max(p.getHistoryLookbackBars(), 1);
        if (CollectionUtils.isEmpty(periodBars) || periodBars.size() < 2) {
            appendMessage(checkResult, period, periodLabel(period) + "K不足");
            return false;
        }
        Trade signalBar = periodBars.get(periodBars.size() - 1);
        Trade prevBar = periodBars.get(periodBars.size() - 2);

        if (p.isEnableSignalRiseGate()) {
            double signalRise = BodyBarTierTools.risePct(signalBar, prevBar);
            if (Double.isNaN(signalRise) || signalRise <= p.getSignalRisePct() + EPS) {
                appendMessage(checkResult, period, periodLabel(period) + "末K涨幅不足");
                return false;
            }
        }

        if (p.isEnableHistoryRiseGate()) {
            if (periodBars.size() < lookback + 1) {
                appendMessage(checkResult, period, periodLabel(period) + "历史K不足");
                return false;
            }
            boolean hasHistoryRise = false;
            int historyStart = periodBars.size() - lookback - 1;
            int historyEnd = periodBars.size() - 2;
            for (int i = historyStart; i <= historyEnd; i++) {
                Trade bar = periodBars.get(i);
                Trade prev = periodBars.get(i - 1);
                double rise = BodyBarTierTools.risePct(bar, prev);
                if (!Double.isNaN(rise) && rise > p.getHistoryRisePct() + EPS) {
                    hasHistoryRise = true;
                    break;
                }
            }
            if (!hasHistoryRise) {
                appendMessage(checkResult, period, periodLabel(period) + "近" + lookback + "根无达标涨幅");
                return false;
            }
        }

        appendMessage(checkResult, period, periodLabel(period) + "MACD金叉");
        return true;
    }

    public static String buildSignalMessage(PeriodTypeEnum period, Trade signalBar, Trade prevBar) {
        double risePct = BodyBarTierTools.risePct(signalBar, prevBar);
        return String.format(
                "MACD金叉,strategyTag=MGC,signalTier=%s,sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f,risePct=%.4f",
                period != null ? period.getCode() : "",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                dayOf(prevBar),
                prevBar != null && prevBar.getClose() != null ? prevBar.getClose() : 0,
                Double.isNaN(risePct) ? 0 : risePct);
    }

    public static PeriodTypeEnum resolvePeriod(MacdGoldenCrossStrategyParams.Tier tier) {
        if (tier == MacdGoldenCrossStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == MacdGoldenCrossStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static String buildTierLabel(MacdGoldenCrossStrategyParams params) {
        if (params == null || params.getTier() == null) {
            return "日";
        }
        switch (params.getTier()) {
            case WEEK:
                return "周";
            case MONTH:
                return "月";
            default:
                return "日";
        }
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[MGC]" + msg);
        }
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MONTH) {
            return "月";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周";
        }
        return "日";
    }
}
