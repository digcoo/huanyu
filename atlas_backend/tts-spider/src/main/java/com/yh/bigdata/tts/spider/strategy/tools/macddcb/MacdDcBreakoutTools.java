package com.yh.bigdata.tts.spider.strategy.tools.macddcb;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdDcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MACD死叉突破：MACD&lt;0、最近交叉为死叉、同档边沿突破死叉K前一根K high。
 */
public final class MacdDcBreakoutTools {

    private static final double EPS = 1e-6;

    private MacdDcBreakoutTools() {
    }

    @Getter
    public static final class TierHit {
        private final MacdCrossStructureTools.CrossBar crossBar;
        private final Trade referenceBar;
        private final Trade signalBar;
        private final Trade prevBar;
        private final double refHigh;

        TierHit(MacdCrossStructureTools.CrossBar crossBar, Trade referenceBar,
                Trade signalBar, Trade prevBar, double refHigh) {
            this.crossBar = crossBar;
            this.referenceBar = referenceBar;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.refHigh = refHigh;
        }
    }

    public static TierHit resolveHit(StockBase stock, MacdDcBreakoutStrategyParams params) {
        MacdDcBreakoutStrategyParams p = params != null ? params : MacdDcBreakoutStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        int lookback = resolveLookback(p);
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, lookback + 40);
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(trades));
        return resolveHitOnBars(trades, points, p);
    }

    static TierHit resolveHitOnBars(List<Trade> trades, List<MACDIndicatorUtils.MACDPoint> points,
                                    MacdDcBreakoutStrategyParams params) {
        MacdDcBreakoutStrategyParams p = params != null ? params : MacdDcBreakoutStrategyParams.defaults();
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }
        if (CollectionUtils.isEmpty(points) || points.size() != trades.size()) {
            return null;
        }
        int lookback = resolveLookback(p);
        Trade signalBar = trades.get(trades.size() - 1);
        Trade prevBar = trades.get(trades.size() - 2);
        MACDIndicatorUtils.MACDPoint signalPoint = points.get(points.size() - 1);
        if (signalPoint == null || signalPoint.getMacd() >= 0) {
            return null;
        }

        MacdCrossStructureTools.CrossBar latestCross = MacdCrossStructureTools.findLatestCrossBar(
                trades, points, lookback, false, true);
        if (latestCross == null || latestCross.getKind() != MacdCrossStructureTools.CrossKind.DEATH) {
            return null;
        }
        Trade dcBar = latestCross.getBar();
        int dcIdx = MacdCrossStructureTools.indexOfBar(trades, dcBar);
        if (dcIdx <= 0) {
            return null;
        }
        Trade referenceBar = trades.get(dcIdx - 1);
        if (referenceBar == null || referenceBar.getHigh() == null) {
            return null;
        }
        double refHigh = referenceBar.getHigh();
        int signalIdx = trades.size() - 1;
        if (signalIdx <= dcIdx) {
            return null;
        }
        if (sameBar(dcBar, signalBar) || sameBar(dcBar, prevBar)) {
            return null;
        }
        if (!passesRefHighEdge(signalBar, prevBar, refHigh)) {
            return null;
        }
        return new TierHit(latestCross, referenceBar, signalBar, prevBar, refHigh);
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              PeriodTypeEnum period, MacdDcBreakoutStrategyParams params,
                                              Trade signalBar, Trade prevBar) {
        MacdDcBreakoutStrategyParams p = params != null ? params : MacdDcBreakoutStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, period, "成交额不足");
            return false;
        }
        if (p.isEnableSignalRiseGate()) {
            double signalRise = BodyBarTierTools.risePct(signalBar, prevBar);
            if (Double.isNaN(signalRise) || signalRise <= p.getSignalRisePct() + EPS) {
                appendMessage(checkResult, period, periodLabel(period) + "末K涨幅不足");
                return false;
            }
        }
        return true;
    }

    static boolean passesRefHighEdge(Trade signalBar, Trade prevBar, double refHigh) {
        if (signalBar == null || prevBar == null || Double.isNaN(refHigh)) {
            return false;
        }
        Double signalClose = signalBar.getClose();
        Double prevClose = prevBar.getClose();
        if (signalClose == null || prevClose == null) {
            return false;
        }
        return prevClose <= refHigh + EPS && signalClose > refHigh + EPS;
    }

    public static String buildSignalMessage(PeriodTypeEnum period, TierHit hit) {
        if (hit == null) {
            return "";
        }
        Trade dcBar = hit.getCrossBar() != null ? hit.getCrossBar().getBar() : null;
        Trade referenceBar = hit.getReferenceBar();
        Trade signalBar = hit.getSignalBar();
        Trade prevBar = hit.getPrevBar();
        double risePct = BodyBarTierTools.risePct(signalBar, prevBar);
        return String.format(
                "MACD死叉突破,strategyTag=MDCB,signalTier=%s,dcDay=%s,dcClose=%.2f,"
                        + "refDay=%s,refHigh=%.2f,sigDay=%s,sigClose=%.2f,prevDay=%s,"
                        + "prevClose=%.2f,risePct=%.4f",
                period != null ? period.getCode() : "",
                dayOf(dcBar),
                dcBar != null && dcBar.getClose() != null ? dcBar.getClose() : 0,
                dayOf(referenceBar),
                hit.getRefHigh(),
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                dayOf(prevBar),
                prevBar != null && prevBar.getClose() != null ? prevBar.getClose() : 0,
                Double.isNaN(risePct) ? 0 : risePct);
    }

    public static PeriodTypeEnum resolvePeriod(MacdDcBreakoutStrategyParams.Tier tier) {
        if (tier == MacdDcBreakoutStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == MacdDcBreakoutStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(MacdDcBreakoutStrategyParams params) {
        MacdDcBreakoutStrategyParams p = params != null ? params : MacdDcBreakoutStrategyParams.defaults();
        if (p.getTier() == MacdDcBreakoutStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == MacdDcBreakoutStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        return p.getLookbackDay();
    }

    public static String buildTierLabel(MacdDcBreakoutStrategyParams params) {
        if (params == null || params.getTier() == null) {
            return "日";
        }
        switch (params.getTier()) {
            case WEEK:
                return "周";
            case MONTH:
                return "月";
            default:
                return "日";
        }
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[MDCB]" + msg);
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
