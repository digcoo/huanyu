package com.yh.bigdata.tts.spider.strategy.tools.macdgcwh;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdGcWaveHighStrategyParams;
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
 * MACD金叉波段High突破：最近 MACD 交叉须为金叉，定基准波段后同档边沿破波段 High。
 */
public final class MacdGcWaveHighTools {

    private static final double EPS = 1e-6;

    private MacdGcWaveHighTools() {
    }

    @Getter
    public static final class TierHit {
        private final MacdCrossStructureTools.CrossBar crossBar;
        private final YangBandTools.CompleteYangBand referenceBand;
        private final Trade signalBar;
        private final Trade prevBar;

        TierHit(MacdCrossStructureTools.CrossBar crossBar,
                YangBandTools.CompleteYangBand referenceBand,
                Trade signalBar, Trade prevBar) {
            this.crossBar = crossBar;
            this.referenceBand = referenceBand;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
        }
    }

    public static TierHit resolveHit(StockBase stock, MacdGcWaveHighStrategyParams params) {
        MacdGcWaveHighStrategyParams p = params != null ? params : MacdGcWaveHighStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        int lookback = resolveLookback(p);
        int fetchBars = lookback + 80;
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3) {
            return null;
        }
        List<MACDIndicatorUtils.MACDPoint> points = MACDIndicatorUtils.calculateMACD(Ticker.from(trades));
        return resolveHitOnBars(trades, points, p);
    }

    static TierHit resolveHitOnBars(List<Trade> trades, List<MACDIndicatorUtils.MACDPoint> points,
                                    MacdGcWaveHighStrategyParams params) {
        MacdGcWaveHighStrategyParams p = params != null ? params : MacdGcWaveHighStrategyParams.defaults();
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
        if (signalPoint != null && signalPoint.isIfRedGoldCross()) {
            return null;
        }

        MacdCrossStructureTools.CrossBar latestCross = MacdCrossStructureTools.findLatestCrossBar(
                trades, points, lookback, true, true);
        if (latestCross == null || latestCross.getKind() != MacdCrossStructureTools.CrossKind.GOLDEN) {
            return null;
        }
        if (sameBar(latestCross.getBar(), signalBar)) {
            return null;
        }

        YangBandTools.CompleteYangBand referenceBand = MacdGcWaveBandTools.resolveReferenceBand(
                trades, latestCross.getBar(), lookback + 40);
        if (referenceBand == null || Double.isNaN(referenceBand.getBandHigh())) {
            return null;
        }
        if (!passesBandHighEdge(prevBar, signalBar, referenceBand.getBandHigh())) {
            return null;
        }
        return new TierHit(latestCross, referenceBand, signalBar, prevBar);
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              PeriodTypeEnum period, MacdGcWaveHighStrategyParams params,
                                              Trade signalBar, Trade prevBar) {
        MacdGcWaveHighStrategyParams p = params != null ? params : MacdGcWaveHighStrategyParams.defaults();
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

    private static boolean passesBandHighEdge(Trade prevBar, Trade signalBar, double bandHigh) {
        if (prevBar == null || signalBar == null || Double.isNaN(bandHigh)) {
            return false;
        }
        Double prevClose = prevBar.getClose();
        Double signalClose = signalBar.getClose();
        if (prevClose == null || signalClose == null) {
            return false;
        }
        return prevClose <= bandHigh + EPS && signalClose > bandHigh + EPS;
    }

    public static String buildSignalMessage(PeriodTypeEnum period, TierHit hit) {
        if (hit == null) {
            return "";
        }
        Trade gcBar = hit.getCrossBar() != null ? hit.getCrossBar().getBar() : null;
        Trade signalBar = hit.getSignalBar();
        Trade prevBar = hit.getPrevBar();
        double bandHigh = hit.getReferenceBand() != null ? hit.getReferenceBand().getBandHigh() : 0;
        double risePct = BodyBarTierTools.risePct(signalBar, prevBar);
        return String.format(
                "MACD金叉波段High突破,strategyTag=MGCWH,signalTier=%s,gcDay=%s,gcClose=%.2f,"
                        + "bandHigh=%.2f,sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f,risePct=%.4f",
                period != null ? period.getCode() : "",
                dayOf(gcBar),
                gcBar != null && gcBar.getClose() != null ? gcBar.getClose() : 0,
                bandHigh,
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                dayOf(prevBar),
                prevBar != null && prevBar.getClose() != null ? prevBar.getClose() : 0,
                Double.isNaN(risePct) ? 0 : risePct);
    }

    public static PeriodTypeEnum resolvePeriod(MacdGcWaveHighStrategyParams.Tier tier) {
        if (tier == MacdGcWaveHighStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == MacdGcWaveHighStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(MacdGcWaveHighStrategyParams params) {
        MacdGcWaveHighStrategyParams p = params != null ? params : MacdGcWaveHighStrategyParams.defaults();
        if (p.getTier() == MacdGcWaveHighStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == MacdGcWaveHighStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        return p.getLookbackDay();
    }

    public static String buildTierLabel(MacdGcWaveHighStrategyParams params) {
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
            checkResult.addTrendPeriod(period, "[MGCWH]" + msg);
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
