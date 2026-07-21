package com.yh.bigdata.tts.spider.strategy.tools.macdgcwhr;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdGcWaveHighRetestStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;
import com.yh.bigdata.tts.spider.strategy.tools.macdgcwave.MacdGcWaveBandTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MACD金叉波段High回踩：MACD&gt;0、收盘在基准波段 High 上、末 K low 贴近 bandHigh（差值幅度≤阈值）。
 */
public final class MacdGcWaveHighRetestTools {

    private static final double EPS = 1e-6;

    private MacdGcWaveHighRetestTools() {
    }

    @Getter
    public static final class TierHit {
        private final MacdCrossStructureTools.CrossBar crossBar;
        private final YangBandTools.CompleteYangBand referenceBand;
        private final Trade signalBar;

        TierHit(MacdCrossStructureTools.CrossBar crossBar,
                YangBandTools.CompleteYangBand referenceBand,
                Trade signalBar) {
            this.crossBar = crossBar;
            this.referenceBand = referenceBand;
            this.signalBar = signalBar;
        }
    }

    public static TierHit resolveHit(StockBase stock, MacdGcWaveHighRetestStrategyParams params) {
        MacdGcWaveHighRetestStrategyParams p = params != null ? params : MacdGcWaveHighRetestStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        int lookback = resolveLookback(p);
        int fetchBars = lookback + 80;
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(trades) || trades.size() < 2) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(trades));
        return resolveHitOnBars(trades, points, p);
    }

    static TierHit resolveHitOnBars(List<Trade> trades, List<MACDIndicatorUtils.MACDPoint> points,
                                     MacdGcWaveHighRetestStrategyParams params) {
        MacdGcWaveHighRetestStrategyParams p = params != null ? params : MacdGcWaveHighRetestStrategyParams.defaults();
        if (CollectionUtils.isEmpty(trades) || trades.size() < 2) {
            return null;
        }
        if (CollectionUtils.isEmpty(points) || points.size() != trades.size()) {
            return null;
        }
        int lookback = resolveLookback(p);
        Trade signalBar = trades.get(trades.size() - 1);
        MACDIndicatorUtils.MACDPoint signalPoint = points.get(points.size() - 1);
        if (signalPoint == null || signalPoint.getMacd() <= 0) {
            return null;
        }

        MacdCrossStructureTools.CrossBar latestCross = MacdCrossStructureTools.findLatestCrossBar(
                trades, points, lookback, true, true);
        if (latestCross == null || latestCross.getKind() != MacdCrossStructureTools.CrossKind.GOLDEN) {
            return null;
        }

        YangBandTools.CompleteYangBand referenceBand = MacdGcWaveBandTools.resolveReferenceBand(
                trades, latestCross.getBar(), lookback + 40);
        if (referenceBand == null || Double.isNaN(referenceBand.getBandHigh())) {
            return null;
        }
        double bandHigh = referenceBand.getBandHigh();
        if (!passesCloseAboveBandHigh(signalBar, bandHigh)) {
            return null;
        }
        if (!passesLowNearBandHigh(signalBar, bandHigh, p.getMaxBarRangePct())) {
            return null;
        }
        return new TierHit(latestCross, referenceBand, signalBar);
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              PeriodTypeEnum period, MacdGcWaveHighRetestStrategyParams params) {
        MacdGcWaveHighRetestStrategyParams p = params != null ? params : MacdGcWaveHighRetestStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, period, "成交额不足");
            return false;
        }
        return true;
    }

    static boolean passesCloseAboveBandHigh(Trade signalBar, double bandHigh) {
        if (signalBar == null || signalBar.getClose() == null || Double.isNaN(bandHigh)) {
            return false;
        }
        return signalBar.getClose() > bandHigh + EPS;
    }

    /** 末 K low 与基准 bandHigh 的相对差值：|low - bandHigh| / bandHigh ≤ maxGapPct。 */
    static boolean passesLowNearBandHigh(Trade signalBar, double bandHigh, double maxGapPct) {
        if (signalBar == null || signalBar.getLow() == null || Double.isNaN(bandHigh) || bandHigh <= 0) {
            return false;
        }
        double gapPct = Math.abs(signalBar.getLow() - bandHigh) / bandHigh;
        return gapPct <= maxGapPct + EPS;
    }

    public static String buildSignalMessage(PeriodTypeEnum period, TierHit hit) {
        if (hit == null) {
            return "";
        }
        Trade gcBar = hit.getCrossBar() != null ? hit.getCrossBar().getBar() : null;
        Trade signalBar = hit.getSignalBar();
        double bandHigh = hit.getReferenceBand() != null ? hit.getReferenceBand().getBandHigh() : 0;
        double lowBandHighGapPct = 0;
        if (signalBar != null && signalBar.getLow() != null && bandHigh > 0) {
            lowBandHighGapPct = Math.abs(signalBar.getLow() - bandHigh) / bandHigh;
        }
        return String.format(
                "MACD金叉波段High回踩,strategyTag=MGCWHR,signalTier=%s,gcDay=%s,gcClose=%.2f,"
                        + "bandHigh=%.2f,sigDay=%s,sigClose=%.2f,sigHigh=%.2f,sigLow=%.2f,lowBandHighGapPct=%.4f",
                period != null ? period.getCode() : "",
                dayOf(gcBar),
                gcBar != null && gcBar.getClose() != null ? gcBar.getClose() : 0,
                bandHigh,
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                signalBar != null && signalBar.getHigh() != null ? signalBar.getHigh() : 0,
                signalBar != null && signalBar.getLow() != null ? signalBar.getLow() : 0,
                lowBandHighGapPct);
    }

    public static PeriodTypeEnum resolvePeriod(MacdGcWaveHighRetestStrategyParams.Tier tier) {
        if (tier == MacdGcWaveHighRetestStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == MacdGcWaveHighRetestStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(MacdGcWaveHighRetestStrategyParams params) {
        MacdGcWaveHighRetestStrategyParams p = params != null ? params : MacdGcWaveHighRetestStrategyParams.defaults();
        if (p.getTier() == MacdGcWaveHighRetestStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == MacdGcWaveHighRetestStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        return p.getLookbackDay();
    }

    public static String buildTierLabel(MacdGcWaveHighRetestStrategyParams params) {
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

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[MGCWHR]" + msg);
        }
    }
}
