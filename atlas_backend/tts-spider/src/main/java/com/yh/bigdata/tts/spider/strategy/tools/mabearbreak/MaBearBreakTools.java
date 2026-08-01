package com.yh.bigdata.tts.spider.strategy.tools.mabearbreak;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MaBearBreakStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 均线空头突破：MA10&lt;MA20&lt;MA30，末 K close&gt;均线MAX，
 * 且（边沿突破均线MAX 或 开盘突破均线MAX）。
 */
public final class MaBearBreakTools {

    private static final double EPS = 1e-6;

    private MaBearBreakTools() {
    }

    public enum BreakoutMode {
        EDGE,
        OPEN
    }

    @Getter
    public static final class Hit {
        private final PeriodTypeEnum period;
        private final Trade signalBar;
        private final Trade prevBar;
        private final double signalMaxMa;
        private final BreakoutMode breakoutMode;

        Hit(PeriodTypeEnum period, Trade signalBar, Trade prevBar, double signalMaxMa,
            BreakoutMode breakoutMode) {
            this.period = period;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.signalMaxMa = signalMaxMa;
            this.breakoutMode = breakoutMode;
        }
    }

    public static Hit findHit(StockBase stock, MaBearBreakStrategyParams params) {
        MaBearBreakStrategyParams p = params != null ? params : MaBearBreakStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, 4);
        return findHitOnBars(trades, period);
    }

    static Hit findHitOnBars(List<Trade> trades, PeriodTypeEnum period) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 2 || period == null) {
            return null;
        }
        int lastIdx = trades.size() - 1;
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        if (prevBar == null) {
            return null;
        }
        if (!passesMaBearOrder(signalBar)) {
            return null;
        }
        Double signalMaxMa = signalBar.getMaxMA();
        if (signalMaxMa == null || !passesCloseAboveMaxMa(signalBar, signalMaxMa)) {
            return null;
        }
        BreakoutMode mode = resolveBreakoutMode(prevBar, signalBar);
        if (mode == null) {
            return null;
        }
        return new Hit(period, signalBar, prevBar, signalMaxMa, mode);
    }

    /** 边沿优先；否则开盘突破。 */
    static BreakoutMode resolveBreakoutMode(Trade prevBar, Trade signalBar) {
        if (passesMaxMaEdgeBreakout(prevBar, signalBar)) {
            return BreakoutMode.EDGE;
        }
        if (passesMaxMaOpenBreakout(signalBar)) {
            return BreakoutMode.OPEN;
        }
        return null;
    }

    static boolean passesMaBearOrder(Trade bar) {
        if (bar == null) {
            return false;
        }
        Double ma10 = bar.getMa10();
        Double ma20 = bar.getMa20();
        Double ma30 = bar.getMa30();
        if (ma10 == null || ma20 == null || ma30 == null) {
            return false;
        }
        return ma10 + EPS < ma20 && ma20 + EPS < ma30;
    }

    static boolean passesCloseAboveMaxMa(Trade bar, Double maxMa) {
        if (bar == null || bar.getClose() == null || maxMa == null) {
            return false;
        }
        return bar.getClose() > maxMa + EPS;
    }

    /** 前一根 close≤该根均线MAX，末 K close&gt;该根均线MAX */
    static boolean passesMaxMaEdgeBreakout(Trade prevBar, Trade signalBar) {
        if (prevBar == null || signalBar == null) {
            return false;
        }
        Double prevClose = prevBar.getClose();
        Double signalClose = signalBar.getClose();
        Double prevMaxMa = prevBar.getMaxMA();
        Double signalMaxMa = signalBar.getMaxMA();
        if (prevClose == null || signalClose == null || prevMaxMa == null || signalMaxMa == null) {
            return false;
        }
        return prevClose <= prevMaxMa + EPS && signalClose > signalMaxMa + EPS;
    }

    /** 末 K open≤该根均线MAX，末 K close&gt;该根均线MAX */
    static boolean passesMaxMaOpenBreakout(Trade signalBar) {
        if (signalBar == null) {
            return false;
        }
        Double open = signalBar.getOpen();
        Double close = signalBar.getClose();
        Double maxMa = signalBar.getMaxMA();
        if (open == null || close == null || maxMa == null) {
            return false;
        }
        return open <= maxMa + EPS && close > maxMa + EPS;
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MaBearBreakStrategyParams params) {
        MaBearBreakStrategyParams p = params != null ? params : MaBearBreakStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, "成交额不足");
            return false;
        }
        return true;
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[MBR]均线空头突破";
        }
        return String.format("[MBR]均线空头突破|%sMA10<MA20<MA30,末K>均线MAX,%s|maxMA=%.2f",
                periodLabel(hit.getPeriod()),
                breakoutLabel(hit.getBreakoutMode()),
                hit.getSignalMaxMa());
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        return String.format(
                "均线空头突破,strategyTag=MBR,period=%s,sigDay=%s,sigClose=%.2f,maxMA=%.2f,breakout=%s",
                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                hit.getSignalMaxMa(),
                hit.getBreakoutMode() != null ? hit.getBreakoutMode().name() : "");
    }

    public static PeriodTypeEnum resolvePeriod(MaBearBreakStrategyParams.Tier tier) {
        if (tier == MaBearBreakStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == MaBearBreakStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    private static String breakoutLabel(BreakoutMode mode) {
        if (mode == BreakoutMode.OPEN) {
            return "开盘突破均线MAX";
        }
        return "边沿突破均线MAX";
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
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

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[MBR]" + msg);
        }
    }
}
