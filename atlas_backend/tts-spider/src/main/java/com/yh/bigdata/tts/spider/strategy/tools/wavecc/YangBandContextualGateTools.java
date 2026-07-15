package com.yh.bigdata.tts.spider.strategy.tools.wavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 末波段形态门：凸波段现价&gt;首阳 low，凹波段现价&gt;末阳 high。
 */
public final class YangBandContextualGateTools {

    private static final double EPS = 1e-6;

    private YangBandContextualGateTools() {
    }

    public static boolean passesContextualGate(StockBase stock, CheckResult checkResult,
                                               PeriodTypeEnum contextPeriod, int lookback) {
        if (stock == null || contextPeriod == null || lookback < 3) {
            return false;
        }
        int fetchBars = Math.max(lookback + 2, lookback);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, contextPeriod, fetchBars);
        if (CollectionUtils.isEmpty(periodBars)) {
            return false;
        }
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(periodBars, lookback);
        if (bands.size() < 2) {
            return false;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        boolean convex = WaveShapeTools.isConvex(lastBand, prevBand);
        Double line;
        String shapeLabel;
        if (convex) {
            Trade first = lastBand.getFirstYang();
            line = first != null ? first.getLow() : null;
            shapeLabel = "凸末波首阳低";
        } else {
            Trade last = lastBand.getLastYang();
            line = last != null ? last.getHigh() : null;
            shapeLabel = "凹末波末阳高";
        }
        if (line == null || Double.isNaN(line)) {
            return false;
        }
        Double close = resolveCurrentClose(stock, contextPeriod, periodBars);
        if (close == null) {
            return false;
        }
        boolean pass = close > line + EPS;
        if (checkResult != null) {
            String periodLabel = periodLabel(contextPeriod);
            checkResult.addTrendPeriod(contextPeriod,
                    pass ? periodLabel + shapeLabel + "门" : periodLabel + shapeLabel + "门未过");
        }
        return pass;
    }

    /**
     * 父周期补充门：高周期末波段为凸时，现价须 &gt; 次波段顶 bandHigh。
     * 非凸形态时不施加此门。适用于日/周/月各档（父周期分别为周/月/年）。
     */
    public static boolean passesConvexPrevBandHighSupplement(StockBase stock, CheckResult checkResult,
                                                             PeriodTypeEnum contextPeriod, int lookback) {
        if (stock == null || contextPeriod == null || lookback < 3) {
            return false;
        }
        int fetchBars = Math.max(lookback + 2, lookback);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, contextPeriod, fetchBars);
        if (CollectionUtils.isEmpty(periodBars)) {
            return false;
        }
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(periodBars, lookback);
        if (bands.size() < 2) {
            return false;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        if (!WaveShapeTools.isConvex(lastBand, prevBand)) {
            return true;
        }
        double line = prevBand.getBandHigh();
        if (Double.isNaN(line)) {
            return false;
        }
        Double close = resolveCurrentClose(stock, contextPeriod, periodBars);
        if (close == null) {
            return false;
        }
        boolean pass = close > line + EPS;
        if (checkResult != null) {
            String periodLabel = periodLabel(contextPeriod);
            checkResult.addTrendPeriod(contextPeriod,
                    pass ? periodLabel + "凸次波段顶门" : periodLabel + "凸次波段顶门未过");
        }
        return pass;
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.YEAR) {
            return "年";
        }
        if (period == PeriodTypeEnum.MONTH) {
            return "月";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周";
        }
        return "日";
    }

    private static Double resolveCurrentClose(StockBase stock, PeriodTypeEnum period, List<Trade> periodBars) {
        Trade current = periodBars.get(periodBars.size() - 1);
        if (current != null && current.getClose() != null) {
            return current.getClose();
        }
        if (period == PeriodTypeEnum.DAY && stock != null && stock.getClose() != null) {
            return stock.getClose();
        }
        return null;
    }
}
