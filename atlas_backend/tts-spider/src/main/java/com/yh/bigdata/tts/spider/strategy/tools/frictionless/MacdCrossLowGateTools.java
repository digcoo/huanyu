package com.yh.bigdata.tts.spider.strategy.tools.frictionless;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossBarResolver;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;

/**
 * MACD 交叉 low 门：现价（收盘）&gt; 最近一根 MACD 交叉 K 的 low，日/周/月须全部满足。
 * <p>交叉 K 定义与 {@link MacdCrossStructureTools#findLatestCrossBar} 一致（红柱金叉或绿柱死叉，不含当前 K）。</p>
 */
public final class MacdCrossLowGateTools {

    public static final String GATE_LABEL = "日周月MACD交叉low门";

    private static final double LOW_EPS = 1e-6;

    private MacdCrossLowGateTools() {
    }

    public static boolean passesPeriod(StockBase stock, PeriodTypeEnum period) {
        Double price = resolveCurrentPrice(stock);
        if (price == null || period == null) {
            return false;
        }
        Trade crossBar = findLatestCrossBar(stock, period);
        if (crossBar == null || crossBar.getLow() == null) {
            return false;
        }
        return price > crossBar.getLow() + LOW_EPS;
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
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, dayOk ? "日交叉low上" : "日MACD交叉low门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.WEEK, weekOk ? "周交叉low上" : "周MACD交叉low门未过");
            checkResult.addTrendPeriod(PeriodTypeEnum.MONTH, monthOk ? "月交叉low上" : "月MACD交叉low门未过");
        }
        return dayOk && weekOk && monthOk;
    }

    static Trade findLatestCrossBar(StockBase stock, PeriodTypeEnum period) {
        MacdCrossStructureTools.CrossBar cross = MacdCrossBarResolver.findLatestCrossBar(
                stock, period, MacdCrossBarResolver.gateLookbackFor(period));
        return cross != null ? cross.getBar() : null;
    }

    private static Double resolveCurrentPrice(StockBase stock) {
        if (stock == null) {
            return null;
        }
        if (stock.getClose() != null && stock.getClose() > 0) {
            return stock.getClose();
        }
        Trade dayBar = RealtimeStockCache.getLastTrade(stock, PeriodTypeEnum.DAY, -1);
        return dayBar != null ? dayBar.getClose() : null;
    }
}
