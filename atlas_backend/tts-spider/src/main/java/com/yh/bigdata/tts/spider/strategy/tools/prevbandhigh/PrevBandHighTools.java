package com.yh.bigdata.tts.spider.strategy.tools.prevbandhigh;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.macrosspoint.MaCrossPointCore;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 突破前波段High：同档边沿突破末一个完整阳波段（末波段）bandHigh。
 */
public final class PrevBandHighTools {

    private PrevBandHighTools() {
    }

    @Getter
    public static final class Hit {
        private final PeriodTypeEnum period;
        private final Trade signalBar;
        private final Trade prevBar;
        private final YangBandTools.CompleteYangBand lastBand;
        private final double breakLine;

        Hit(PeriodTypeEnum period, Trade signalBar, Trade prevBar,
            YangBandTools.CompleteYangBand lastBand, double breakLine) {
            this.period = period;
            this.signalBar = signalBar;
            this.prevBar = prevBar;
            this.lastBand = lastBand;
            this.breakLine = breakLine;
        }
    }

    public static Hit findHit(StockBase stock, MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return null;
        }
        int lookback = resolveLookback(p);
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, period, Math.max(lookback, 40));
        return findHitOnBars(trades, period, lookback);
    }

    static Hit findHitOnBars(List<Trade> trades, PeriodTypeEnum period, int lookback) {
        if (CollectionUtils.isEmpty(trades) || trades.size() < 3 || period == null || lookback < 3) {
            return null;
        }
        int lastIdx = trades.size() - 1;
        Trade signalBar = trades.get(lastIdx);
        Trade prevBar = trades.get(lastIdx - 1);
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(trades, lookback);
        if (bands.isEmpty()) {
            return null;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        double breakLine = lastBand.getBandHigh();
        if (Double.isNaN(breakLine)) {
            return null;
        }
        if (!MaCrossPointCore.passesEdgeBreak(prevBar, signalBar, breakLine)) {
            return null;
        }
        return new Hit(period, signalBar, prevBar, lastBand, breakLine);
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MaCrossPointStrategyParams params, Hit hit) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, PeriodTypeEnum.DAY, "成交额不足");
            return false;
        }
        if (p.isEnableRightTrend()) {
            Trade signal = hit != null ? hit.getSignalBar() : null;
            if (!MaCrossPointCore.passesRightTrend(signal)) {
                appendMessage(checkResult, hit != null ? hit.getPeriod() : PeriodTypeEnum.DAY,
                        "未满足右侧趋势MA5>MA60");
                return false;
            }
        }
        return true;
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        return String.format("[PBH]突破前波段High|%s边沿破末波段顶|breakLine=%.2f",
                periodLabel(hit.getPeriod()), hit.getBreakLine());
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        return String.format(
                "突破前波段High,strategyTag=PBH,period=%s,sigDay=%s,sigClose=%.2f,breakLine=%.2f,bandHighDay=%s",
                hit.getPeriod() != null ? hit.getPeriod().getCode() : "day",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                hit.getBreakLine(),
                dayOf(hit.getLastBand() != null ? hit.getLastBand().getBandHighBar() : null));
    }

    public static PeriodTypeEnum resolvePeriod(MaCrossPointStrategyParams.Tier tier) {
        if (tier == MaCrossPointStrategyParams.Tier.MIN30) {
            return PeriodTypeEnum.MIN30;
        }
        if (tier == MaCrossPointStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == MaCrossPointStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(MaCrossPointStrategyParams params) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        if (p.getTier() == MaCrossPointStrategyParams.Tier.MIN30) {
            return p.getLookbackMin30();
        }
        if (p.getTier() == MaCrossPointStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == MaCrossPointStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        return p.getLookbackDay();
    }

    private static String periodLabel(PeriodTypeEnum period) {
        if (period == PeriodTypeEnum.MIN30) {
            return "30分";
        }
        if (period == PeriodTypeEnum.MONTH) {
            return "月";
        }
        if (period == PeriodTypeEnum.WEEK) {
            return "周";
        }
        return "日";
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[PBH]" + msg);
        }
    }
}
