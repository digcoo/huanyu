package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;

/**
 * 全阳门：日、周、月、年最后一根 K 均为阳线（close &gt; open），须全部满足。
 */
public final class AllYangGateTools {

    public static final String GATE_LABEL = "日周月年全阳门";

    private static final double EPS = 1e-6;

    private AllYangGateTools() {
    }

    public static boolean passesPeriod(StockBase stock, PeriodTypeEnum period) {
        Trade last = RealtimeStockCache.getLastTrade(stock, period, 0);
        if (last == null || last.getOpen() == null || last.getClose() == null || last.getOpen() <= 0) {
            return false;
        }
        return last.getClose() > last.getOpen() + EPS;
    }

    public static boolean passesAll(StockBase stock) {
        return passesPeriod(stock, PeriodTypeEnum.DAY)
                && passesPeriod(stock, PeriodTypeEnum.WEEK)
                && passesPeriod(stock, PeriodTypeEnum.MONTH)
                && passesPeriod(stock, PeriodTypeEnum.YEAR);
    }

    public static boolean passesAll(StockBase stock, CheckResult checkResult) {
        boolean dayOk = passesPeriod(stock, PeriodTypeEnum.DAY);
        boolean weekOk = passesPeriod(stock, PeriodTypeEnum.WEEK);
        boolean monthOk = passesPeriod(stock, PeriodTypeEnum.MONTH);
        boolean yearOk = passesPeriod(stock, PeriodTypeEnum.YEAR);
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, dayOk ? "日全阳" : "日全阳门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, weekOk ? "周全阳" : "周全阳门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, monthOk ? "月全阳" : "月全阳门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.YEAR, yearOk ? "年全阳" : "年全阳门未过");
        }
        return dayOk && weekOk && monthOk && yearOk;
    }
}
