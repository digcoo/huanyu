package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossBarResolver;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;

/**
 * MACD 死叉高门：MACD&gt;0，或 MACD≤0 且本档最后一根 K.close &gt; 最近 MACD 死叉 K.high；日/周/月须全部满足。
 */
public final class MacdDcHighGateTools {

    public static final String GATE_LABEL = "日周月MACD死叉高门";

    private static final double HIGH_EPS = 1e-6;

    private MacdDcHighGateTools() {
    }

    public static boolean passesPeriod(StockBase stock, PeriodTypeEnum period) {
        if (UnilateralMacdTools.isMacdPositive(stock, period)) {
            return true;
        }
        Trade current = RealtimeStockCache.getLastTrade(stock, period, -1);
        if (current == null || current.getClose() == null) {
            return false;
        }
        Trade dcBar = MacdCrossBarResolver.findLatestDeathCrossBar(stock, period);
        if (dcBar == null || dcBar.getHigh() == null) {
            return false;
        }
        return current.getClose() > dcBar.getHigh() + HIGH_EPS;
    }

    public static boolean passesAll(StockBase stock) {
        return passesPeriod(stock, PeriodTypeEnum.DAY)
                && passesPeriod(stock, PeriodTypeEnum.WEEK)
                && passesPeriod(stock, PeriodTypeEnum.MONTH);
    }

    public static boolean passesAll(StockBase stock, CheckResult checkResult) {
        boolean dayOk = passesPeriod(stock, PeriodTypeEnum.DAY);
        boolean weekOk = passesPeriod(stock, PeriodTypeEnum.WEEK);
        boolean monthOk = passesPeriod(stock, PeriodTypeEnum.MONTH);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, dayOk ? "日死叉高上" : "日MACD死叉高门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, weekOk ? "周死叉高上" : "周MACD死叉高门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, monthOk ? "月死叉高上" : "月MACD死叉高门未过");
        }
        return dayOk && weekOk && monthOk;
    }
}
