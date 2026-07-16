package com.yh.bigdata.tts.spider.strategy.tools.macdgcwhu;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdGcWaveHighLiftStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;
import com.yh.bigdata.tts.spider.strategy.tools.macdgcwave.MacdGcWaveBandTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MACD金叉波段High上移：MACD&gt;0、价在波段High（或未完成时用金叉K high）上、前K收&gt;波段High、首次上移前K high。
 */
public final class MacdGcWaveHighLiftTools {

    private static final double EPS = 1e-6;

    private MacdGcWaveHighLiftTools() {
    }

    @Getter
    public static final class TierHit {
        private final MacdCrossStructureTools.CrossBar crossBar;
        private final YangBandTools.CompleteYangBand referenceBand;
        private final Trade signalBar;
        private final Trade prevBar;
        private final Trade prevPrevBar;
        private final double priceFloor;
        private final boolean gcHighFallback;

        TierHit(MacdCrossStructureTools.CrossBar crossBar,
                YangBandTools.CompleteYangBand referenceBand,
                Trade signalBar, Trade prevBar, Trade prevPrevBar,
                double priceFloor, boolean gcHighFallback) {
            this.crossBar = crossBar;
            this.referenceBand = referenceBand;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.prevPrevBar = prevPrevBar;
            this.priceFloor = priceFloor;
            this.gcHighFallback = gcHighFallback;
        }
    }

    public static TierHit resolveHit(StockBase stock, MacdGcWaveHighLiftStrategyParams params) {
        MacdGcWaveHighLiftStrategyParams p = params != null ? params : MacdGcWaveHighLiftStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        int lookback = resolveLookback(p);
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, lookback + 80);
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(trades));
        return resolveHitOnBars(trades, points, p);
    }

    static TierHit resolveHitOnBars(List<Trade> trades, List<MACDIndicatorUtils.MACDPoint> points,
                                    MacdGcWaveHighLiftStrategyParams params) {
        MacdGcWaveHighLiftStrategyParams p = params != null ? params : MacdGcWaveHighLiftStrategyParams.defaults();
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }
        if (CollectionUtils.isEmpty(points) || points.size() != trades.size()) {
            return null;
        }
        int lookback = resolveLookback(p);
        Trade signalBar = trades.get(trades.size() - 1);
        Trade prevBar = trades.get(trades.size() - 2);
        Trade prevPrevBar = trades.get(trades.size() - 3);
        MACDIndicatorUtils.MACDPoint signalPoint = points.get(points.size() - 1);
        if (signalPoint == null || signalPoint.getMacd() <= 0) {
            return null;
        }

        MacdCrossStructureTools.CrossBar latestCross = MacdCrossStructureTools.findLatestCrossBar(
                trades, points, lookback, true, true);
        if (latestCross == null || latestCross.getKind() != MacdCrossStructureTools.CrossKind.GOLDEN) {
            return null;
        }
        Trade gcBar = latestCross.getBar();
        YangBandTools.CompleteYangBand referenceBand = MacdGcWaveBandTools.resolveReferenceBand(
                trades, gcBar, lookback + 40);
        PriceFloor floor = resolvePriceFloor(gcBar, referenceBand);
        if (floor == null || Double.isNaN(floor.value)) {
            return null;
        }
        if (!passesCloseAboveFloor(signalBar, floor.value)) {
            return null;
        }
        if (!passesCloseAboveFloor(prevBar, floor.value)) {
            return null;
        }
        if (!passesFirstPrevHighLift(signalBar, prevBar, prevPrevBar)) {
            return null;
        }
        return new TierHit(latestCross, referenceBand, signalBar, prevBar, prevPrevBar,
                floor.value, floor.gcHighFallback);
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              PeriodTypeEnum period, MacdGcWaveHighLiftStrategyParams params,
                                              Trade signalBar, Trade prevBar) {
        MacdGcWaveHighLiftStrategyParams p = params != null ? params : MacdGcWaveHighLiftStrategyParams.defaults();
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

    static PriceFloor resolvePriceFloor(Trade gcBar, YangBandTools.CompleteYangBand referenceBand) {
        if (referenceBand != null && !Double.isNaN(referenceBand.getBandHigh())) {
            return new PriceFloor(referenceBand.getBandHigh(), false);
        }
        if (YangBandTools.isStrictYang(gcBar) && gcBar.getHigh() != null) {
            return new PriceFloor(gcBar.getHigh(), true);
        }
        return null;
    }

    static boolean passesCloseAboveFloor(Trade signalBar, double floor) {
        if (signalBar == null || signalBar.getClose() == null || Double.isNaN(floor)) {
            return false;
        }
        return signalBar.getClose() > floor + EPS;
    }

    static boolean passesFirstPrevHighLift(Trade signalBar, Trade prevBar, Trade prevPrevBar) {
        if (signalBar == null || prevBar == null || prevPrevBar == null) {
            return false;
        }
        Double signalClose = signalBar.getClose();
        Double prevClose = prevBar.getClose();
        Double prevHigh = prevBar.getHigh();
        Double prevPrevHigh = prevPrevBar.getHigh();
        if (signalClose == null || prevClose == null || prevHigh == null || prevPrevHigh == null) {
            return false;
        }
        return signalClose > prevHigh + EPS && prevClose <= prevPrevHigh + EPS;
    }

    public static String buildSignalMessage(PeriodTypeEnum period, TierHit hit) {
        if (hit == null) {
            return "";
        }
        Trade gcBar = hit.getCrossBar() != null ? hit.getCrossBar().getBar() : null;
        Trade signalBar = hit.getSignalBar();
        Trade prevBar = hit.getPrevBar();
        double risePct = BodyBarTierTools.risePct(signalBar, prevBar);
        return String.format(
                "MACD金叉波段High上移,strategyTag=MGCWHU,signalTier=%s,gcDay=%s,gcClose=%.2f,"
                        + "priceFloor=%.2f,floorMode=%s,sigDay=%s,sigClose=%.2f,prevDay=%s,"
                        + "prevClose=%.2f,prevHigh=%.2f,prevPrevHigh=%.2f,risePct=%.4f",
                period != null ? period.getCode() : "",
                dayOf(gcBar),
                gcBar != null && gcBar.getClose() != null ? gcBar.getClose() : 0,
                hit.getPriceFloor(),
                hit.isGcHighFallback() ? "gcHigh" : "bandHigh",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                dayOf(prevBar),
                prevBar != null && prevBar.getClose() != null ? prevBar.getClose() : 0,
                prevBar != null && prevBar.getHigh() != null ? prevBar.getHigh() : 0,
                hit.getPrevPrevBar() != null && hit.getPrevPrevBar().getHigh() != null
                        ? hit.getPrevPrevBar().getHigh() : 0,
                Double.isNaN(risePct) ? 0 : risePct);
    }

    public static PeriodTypeEnum resolvePeriod(MacdGcWaveHighLiftStrategyParams.Tier tier) {
        if (tier == MacdGcWaveHighLiftStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == MacdGcWaveHighLiftStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(MacdGcWaveHighLiftStrategyParams params) {
        MacdGcWaveHighLiftStrategyParams p = params != null ? params : MacdGcWaveHighLiftStrategyParams.defaults();
        if (p.getTier() == MacdGcWaveHighLiftStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == MacdGcWaveHighLiftStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        return p.getLookbackDay();
    }

    public static String buildTierLabel(MacdGcWaveHighLiftStrategyParams params) {
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

    static final class PriceFloor {
        final double value;
        final boolean gcHighFallback;

        PriceFloor(double value, boolean gcHighFallback) {
            this.value = value;
            this.gcHighFallback = gcHighFallback;
        }
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[MGCWHU]" + msg);
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
