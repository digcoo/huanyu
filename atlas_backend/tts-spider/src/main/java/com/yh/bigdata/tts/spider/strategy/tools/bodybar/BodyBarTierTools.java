package com.yh.bigdata.tts.spider.strategy.tools.bodybar;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.BodyBarTierStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 柱子策略：近 N 根（不含信号 K）存在强实体柱，且信号 K 上涨率与破前 K high。
 */
public final class BodyBarTierTools {

    private static final double EPS = 1e-6;

    private BodyBarTierTools() {
    }

    public static boolean passesTierGate(StockBase stock, CheckResult checkResult,
                                         BodyBarTierStrategyParams params) {
        BodyBarTierStrategyParams p = params != null ? params : BodyBarTierStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null || p.getLookbackBars() < 3) {
            return false;
        }
        int fetchBars = p.getLookbackBars() + 1;
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        return passesTierGateOnBars(periodBars, stock, checkResult, period, p);
    }

    static boolean passesTierGateOnBars(List<Trade> periodBars, StockBase stock, CheckResult checkResult,
                                        PeriodTypeEnum period, BodyBarTierStrategyParams params) {
        BodyBarTierStrategyParams p = params != null ? params : BodyBarTierStrategyParams.defaults();
        int lookback = p.getLookbackBars();
        if (CollectionUtils.isEmpty(periodBars) || periodBars.size() < lookback + 1) {
            appendMessage(checkResult, period, periodLabel(period) + "K不足");
            return false;
        }
        Trade signalBar = periodBars.get(periodBars.size() - 1);
        Trade prevBar = periodBars.get(periodBars.size() - 2);
        double historyThreshold = p.resolveHistoryBodyPct();
        double signalThreshold = p.resolveSignalRisePct();

        boolean hasHistoryStrongBody = false;
        int historyStart = periodBars.size() - lookback - 1;
        int historyEnd = periodBars.size() - 2;
        for (int i = historyStart; i <= historyEnd; i++) {
            double bodyPct = bodyPct(periodBars.get(i));
            if (!Double.isNaN(bodyPct) && bodyPct > historyThreshold + EPS) {
                hasHistoryStrongBody = true;
                break;
            }
        }
        if (!hasHistoryStrongBody) {
            appendMessage(checkResult, period, periodLabel(period) + "无历史强柱");
            return false;
        }

        double signalRisePct = risePct(signalBar, prevBar);
        if (Double.isNaN(signalRisePct) || signalRisePct <= signalThreshold + EPS) {
            appendMessage(checkResult, period, periodLabel(period) + "信号上涨率不足");
            return false;
        }

        Double close = resolveClose(stock, period, signalBar);
        Double prevHigh = prevBar != null ? prevBar.getHigh() : null;
        if (close == null || prevHigh == null || close <= prevHigh + EPS) {
            appendMessage(checkResult, period, periodLabel(period) + "未破前K高");
            return false;
        }

        appendMessage(checkResult, period, periodLabel(period) + "柱子突破");
        return true;
    }

    /** 实体柱：(close - open) / open */
    static double bodyPct(Trade bar) {
        if (bar == null || bar.getOpen() == null || bar.getClose() == null || bar.getOpen() <= 0) {
            return Double.NaN;
        }
        return (bar.getClose() - bar.getOpen()) / bar.getOpen();
    }

    /** 上涨率：(close - 前收) / 前收 */
    public static double risePct(Trade bar, Trade prevBar) {
        if (bar == null || bar.getClose() == null) {
            return Double.NaN;
        }
        Double prevClose = bar.getPrevClose();
        if (prevClose == null && prevBar != null) {
            prevClose = prevBar.getClose();
        }
        if (prevClose == null || prevClose <= 0) {
            return Double.NaN;
        }
        return (bar.getClose() - prevClose) / prevClose;
    }

    public static PeriodTypeEnum resolvePeriod(BodyBarTierStrategyParams.Tier tier) {
        if (tier == BodyBarTierStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == BodyBarTierStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    private static Double resolveClose(StockBase stock, PeriodTypeEnum period, Trade signalBar) {
        if (signalBar != null && signalBar.getClose() != null) {
            return signalBar.getClose();
        }
        if (period == PeriodTypeEnum.DAY && stock != null && stock.getClose() != null) {
            return stock.getClose();
        }
        return null;
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[BBT]" + msg);
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
