package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.MathUtil;
import com.yh.bigdata.tts.spider.response.CheckResult;

/**
 * 双低支撑门：现价 &gt; max(前K.low, 前K2.low)，日/周/月须全部满足。
 */
public final class DualLowSupportGateTools {

    public static final String GATE_LABEL = "日周月双低支撑门";

    private DualLowSupportGateTools() {
    }

    public static boolean passesPeriod(StockBase stock, PeriodTypeEnum period) {
        Double price = resolveCurrentPrice(stock);
        Trade prev1 = RealtimeStockCache.getLastTrade(stock, period, 1);
        Trade prev2 = RealtimeStockCache.getLastTrade(stock, period, 2);
        if (price == null || prev1 == null || prev2 == null
                || prev1.getLow() == null || prev2.getLow() == null) {
            return false;
        }
        return price > MathUtil.max(prev1.getLow(), prev2.getLow());
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
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, dayOk ? "日双低上" : "日双低门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, weekOk ? "周双低上" : "周双低门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, monthOk ? "月双低上" : "月双低门未过");
        }
        return dayOk && weekOk && monthOk;
    }

    private static Double resolveCurrentPrice(StockBase stock) {
        if (stock == null) {
            return null;
        }
        if (stock.getClose() != null && stock.getClose() > 0) {
            return stock.getClose();
        }
        Trade dayBar = RealtimeStockCache.getLastTrade(stock, PeriodTypeEnum.DAY, 0);
        return dayBar != null ? dayBar.getClose() : null;
    }
}
