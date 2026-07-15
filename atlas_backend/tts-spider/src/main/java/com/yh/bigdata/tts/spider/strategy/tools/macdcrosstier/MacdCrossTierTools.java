package com.yh.bigdata.tts.spider.strategy.tools.macdcrosstier;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdCrossTierStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.macedge.MacdEdgeBreakoutTools;
import org.springframework.util.CollectionUtils;

import java.util.List;

public final class MacdCrossTierTools {

    private MacdCrossTierTools() {
    }

    public static MacdCrossTierHitPath resolveHitPath(StockBase stock, MacdCrossTierStrategyParams params) {
        MacdCrossTierStrategyParams p = params != null ? params : MacdCrossTierStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        int lookback = resolveLookback(p);
        if (stock == null || period == null || lookback < 1) {
            return null;
        }
        if (p.isEnableGoldenCross()) {
            MacdEdgeBreakoutTools.TierHit goldenHit = MacdEdgeBreakoutTools.findTierHit(
                    stock, period, lookback, true, false);
            if (goldenHit != null) {
                return MacdCrossTierHitPath.fromBreakout(
                        goldenHit, MacdCrossTierHitPath.Kind.GOLDEN_CROSS_BREAKOUT);
            }
        }
        if (p.isEnableDeathCross()) {
            MacdEdgeBreakoutTools.TierHit deathHit = MacdEdgeBreakoutTools.findTierHit(
                    stock, period, lookback, false, true);
            if (deathHit != null) {
                return MacdCrossTierHitPath.fromBreakout(
                        deathHit, MacdCrossTierHitPath.Kind.DEATH_CROSS_BREAKOUT);
            }
        }
        if (p.isEnableGoldenCrossRiseGate()
                && MacdCrossTierGoldenCrossRiseTools.passes(
                        stock, period, null, p.getSignalRisePct())) {
            List<Trade> bars = RealtimeStockCache.getLastTrades(stock, period, 2);
            if (!CollectionUtils.isEmpty(bars) && bars.size() >= 2) {
                return MacdCrossTierHitPath.fromRise(
                        period, bars.get(bars.size() - 1), bars.get(bars.size() - 2));
            }
        }
        return null;
    }

    /** @deprecated 使用 {@link #resolveHitPath} */
    @Deprecated
    public static MacdEdgeBreakoutTools.TierHit findTierHit(StockBase stock,
                                                             MacdCrossTierStrategyParams params) {
        MacdCrossTierHitPath path = resolveHitPath(stock, params);
        return path != null ? path.getTierHit() : null;
    }

    public static String buildPathLabel(MacdCrossTierHitPath path) {
        if (path == null || path.getKind() == null) {
            return "";
        }
        switch (path.getKind()) {
            case DEATH_CROSS_BREAKOUT:
                return "死叉K突破";
            case GOLDEN_CROSS_RISE:
                return "金叉上涨率";
            default:
                return "金叉K突破";
        }
    }

    public static PeriodTypeEnum resolvePeriod(MacdCrossTierStrategyParams.Tier tier) {
        if (tier == MacdCrossTierStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == MacdCrossTierStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    public static int resolveLookback(MacdCrossTierStrategyParams params) {
        MacdCrossTierStrategyParams p = params != null ? params : MacdCrossTierStrategyParams.defaults();
        if (p.getTier() == MacdCrossTierStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == MacdCrossTierStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        return p.getLookbackDay();
    }

    public static String buildTierLabel(MacdCrossTierStrategyParams params) {
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
}
