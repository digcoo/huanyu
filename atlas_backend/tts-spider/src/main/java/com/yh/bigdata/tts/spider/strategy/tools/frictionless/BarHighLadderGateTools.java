package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;

/**
 * 高点递进门：最后一根 high &gt; 倒数第二根 high，或倒数第二根 high &gt; 倒数第三根 high；日/周/月须全部满足。
 */
public final class BarHighLadderGateTools {

    public static final String GATE_LABEL = "日周月高点递进门";

    private BarHighLadderGateTools() {
    }

    public static boolean passesPeriod(StockBase stock, PeriodTypeEnum period) {
        Trade last = RealtimeStockCache.getLastTrade(stock, period, 1);
        Trade prev = RealtimeStockCache.getLastTrade(stock, period, 2);
        Trade third = RealtimeStockCache.getLastTrade(stock, period, 3);
        if (last == null || prev == null || third == null
                || last.getHigh() == null || prev.getHigh() == null || third.getHigh() == null) {
            return false;
        }
        return last.getHigh() > prev.getHigh() || prev.getHigh() > third.getHigh();
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
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, dayOk ? "日高点递" : "日高点门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, weekOk ? "周高点递" : "周高点门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, monthOk ? "月高点递" : "月高点门未过");
        }
        return dayOk && weekOk && monthOk;
    }
}
