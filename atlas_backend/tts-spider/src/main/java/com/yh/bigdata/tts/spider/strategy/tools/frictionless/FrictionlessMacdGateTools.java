package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

/**
 * 无阻力 MACD 门：MACD&gt;0，或 MACD≤0 且当前 K.close &gt; 前 K.high
 * <p>策略评估前应调用 {@link StrategyGlobalGateTools#passGate(StockBase, CheckResult)} 做门控前置。</p>
 */
public final class FrictionlessMacdGateTools {

    /** 趋势消息中使用的 MACD 门控摘要 */
    public static final String GATE_LABEL = "日周月无阻力MACD门";

    private FrictionlessMacdGateTools() {
    }

    /**
     * @deprecated 请使用 {@link StrategyGlobalGateTools#passGate(StockBase, CheckResult)}
     */
    @Deprecated
    public static boolean passGate(StockBase stock, CheckResult checkResult) {
        return StrategyGlobalGateTools.passGate(stock, checkResult);
    }

    public static boolean passesPeriod(StockBase stock, PeriodTypeEnum period) {
        if (UnilateralMacdTools.isMacdPositive(stock, period)) {
            return true;
        }
        Trade current = RealtimeStockCache.getLastTrade(stock, period, -1);
        Trade prev = RealtimeStockCache.getLastTrade(stock, period, -2);
        if (current == null || prev == null || current.getClose() == null || prev.getHigh() == null) {
            return false;
        }
        return current.getClose() > prev.getHigh();
    }

    public static boolean passesMacdAll(StockBase stock) {
        return passesPeriod(stock, PeriodTypeEnum.DAY)
                && passesPeriod(stock, PeriodTypeEnum.WEEK)
                && passesPeriod(stock, PeriodTypeEnum.MONTH);
    }

    public static boolean passesMacdAll(StockBase stock, CheckResult checkResult) {
        boolean dayOk = passesPeriod(stock, PeriodTypeEnum.DAY);
        boolean weekOk = passesPeriod(stock, PeriodTypeEnum.WEEK);
        boolean monthOk = passesPeriod(stock, PeriodTypeEnum.MONTH);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, dayOk ? "日无阻力" : "日MACD门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, weekOk ? "周无阻力" : "周MACD门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, monthOk ? "月无阻力" : "月MACD门未过");
        }
        return dayOk && weekOk && monthOk;
    }

    /** @deprecated 请使用 {@link #passesMacdAll(StockBase)} */
    @Deprecated
    public static boolean passesAll(StockBase stock) {
        return passesMacdAll(stock);
    }

    /** @deprecated 请使用 {@link #passesMacdAll(StockBase, CheckResult)} */
    @Deprecated
    public static boolean passesAll(StockBase stock, CheckResult checkResult) {
        return passesMacdAll(stock, checkResult);
    }
}
