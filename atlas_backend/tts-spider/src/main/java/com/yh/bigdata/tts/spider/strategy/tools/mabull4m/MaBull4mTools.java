package com.yh.bigdata.tts.spider.strategy.tools.mabull4m;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MaBull4mStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.ma3m.Ma3mCore;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MA多头4M排列：MA5≥MA10≥MA20≥MA30，收阳，末K close &gt; 前一根 Low。
 */
public final class MaBull4mTools {

    private static final double EPS = 1e-6;

    private MaBull4mTools() {
    }

    @Getter
    public static final class Hit {
        private final PeriodTypeEnum period;
        private final Trade signalBar;
        private final Trade prevBar;
        private final double prevLow;

        Hit(PeriodTypeEnum period, Trade signalBar, Trade prevBar, double prevLow) {
            this.period = period;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.prevLow = prevLow;
        }
    }

    public static Hit findHit(StockBase stock, MaBull4mStrategyParams params) {
        MaBull4mStrategyParams p = params != null ? params : MaBull4mStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, Math.max(resolveLookback(p), 40));
        return findHitOnBars(trades, period);
    }

    static Hit findHitOnBars(List<Trade> trades, PeriodTypeEnum period) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 2 || period == null) {
            return null;
        }
        int lastIdx = trades.size() - 1;
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        if (!Ma3mCore.passes4mBullAlign(signalBar)) {
            return null;
        }
        if (!Ma3mCore.isYang(signalBar)) {
            return null;
        }
        if (signalBar.getClose() == null || prevBar == null || prevBar.getLow() == null) {
            return null;
        }
        if (signalBar.getClose() <= prevBar.getLow() + EPS) {
            return null;
        }
        return new Hit(period, signalBar, prevBar, prevBar.getLow());
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MaBull4mStrategyParams params) {
        MaBull4mStrategyParams p = params != null ? params : MaBull4mStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "[M4M]成交额不足");
            }
            return false;
        }
        return true;
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[M4M]MA多头4M排列";
        }
        return String.format("[M4M]MA多头4M排列|%s多头+收阳+close>前Low|prevLow=%.2f",
                periodLabel(hit.getPeriod()), hit.getPrevLow());
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        return String.format(
                "MA多头4M排列,strategyTag=M4M,period=%s,sigDay=%s,sigClose=%.2f,prevLow=%.2f",
                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",
                signalBar != null && signalBar.getDay() != null ? signalBar.getDay() : "",
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                hit.getPrevLow());
    }

    public static PeriodTypeEnum resolvePeriod(MaBull4mStrategyParams.Tier tier) {
        if (tier == MaBull4mStrategyParams.Tier.MIN30) {
            return PeriodTypeEnum.MIN30;
        }
        if (tier == MaBull4mStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == MaBull4mStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(MaBull4mStrategyParams params) {
        MaBull4mStrategyParams p = params != null ? params : MaBull4mStrategyParams.defaults();
        if (p.getTier() == MaBull4mStrategyParams.Tier.MIN30) {
            return p.getLookbackMin30();
        }
        if (p.getTier() == MaBull4mStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == MaBull4mStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        return p.getLookbackDay();
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MIN30) {
            return "30分";
        }
        if (period == PeriodTypeEnum.MONTH) {
            return "月";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周";
        }
        return "日";
    }
}
