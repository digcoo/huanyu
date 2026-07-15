package com.yh.bigdata.tts.spider.strategy.tools.waveperiodgate;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.WavePeriodGateStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 形态门：日/周/月档 close &gt; min(末/次波段 low) + 可选 max low / 上级周期 min low / 凸凹突破。
 */
public final class WavePeriodGateTools {

    private static final double EPS = 1e-6;

    static final class UpperPeriodContext {
        private final PeriodTypeEnum period;
        private final int lookback;

        UpperPeriodContext(PeriodTypeEnum period, int lookback) {
            this.period = period;
            this.lookback = lookback;
        }

        PeriodTypeEnum getPeriod() {
            return period;
        }

        int getLookback() {
            return lookback;
        }
    }

    private WavePeriodGateTools() {
    }

    public static boolean passesUpperPeriodGate(StockBase stock, CheckResult checkResult,
                                                WavePeriodGateStrategyParams params) {
        WavePeriodGateStrategyParams p = params != null ? params : WavePeriodGateStrategyParams.defaults();
        UpperPeriodContext ctx = resolveUpperPeriod(p);
        if (ctx == null || stock == null || ctx.getLookback() < 3) {
            return true;
        }
        int fetchBars = Math.max(ctx.getLookback() + 2, ctx.getLookback());
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, ctx.getPeriod(), fetchBars);
        return passesUpperPeriodGateOnBars(periodBars, checkResult, ctx);
    }

    static boolean passesUpperPeriodGateOnBars(List<Trade> periodBars, CheckResult checkResult,
                                               UpperPeriodContext ctx) {
        if (ctx == null || ctx.getLookback() < 3) {
            return true;
        }
        PeriodTypeEnum period = ctx.getPeriod();
        if (CollectionUtils.isEmpty(periodBars)) {
            appendMessage(checkResult, period, periodLabel(period) + "上级无K");
            return false;
        }
        Trade lastBar = periodBars.get(periodBars.size() - 1);
        Double close = lastBar.getClose();
        if (close == null) {
            return false;
        }
        List<YangBandTools.CompleteYangBand> bands =
                YangBandTools.findCompleteBands(periodBars, ctx.getLookback());
        if (bands.size() >= 2) {
            YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
            YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
            double minLow = Math.min(lastBand.getBandLow(), prevBand.getBandLow());
            if (Double.isNaN(minLow) || close <= minLow + EPS) {
                appendMessage(checkResult, period, periodLabel(period) + "未过min波段low");
                return false;
            }
            appendMessage(checkResult, period, periodLabel(period) + "上级min low");
            return true;
        }
        if (bands.size() == 1) {
            double bandLow = bands.get(0).getBandLow();
            if (Double.isNaN(bandLow) || close <= bandLow + EPS) {
                appendMessage(checkResult, period, periodLabel(period) + "未过1波段low");
                return false;
            }
            appendMessage(checkResult, period, periodLabel(period) + "上级1波段low");
            return true;
        }
        if (YangBandTools.isStrictYang(lastBar)) {
            appendMessage(checkResult, period, periodLabel(period) + "上级末K阳");
            return true;
        }
        appendMessage(checkResult, period, periodLabel(period) + "无波段末K非阳");
        return false;
    }

    static UpperPeriodContext resolveUpperPeriod(WavePeriodGateStrategyParams params) {
        WavePeriodGateStrategyParams p = params != null ? params : WavePeriodGateStrategyParams.defaults();
        if (p.getTier() == WavePeriodGateStrategyParams.Tier.WEEK) {
            return new UpperPeriodContext(PeriodTypeEnum.MONTH, p.getLookbackMonth());
        }
        if (p.getTier() == WavePeriodGateStrategyParams.Tier.MONTH) {
            return new UpperPeriodContext(PeriodTypeEnum.YEAR, p.getLookbackYear());
        }
        if (p.getTier() == WavePeriodGateStrategyParams.Tier.DAY) {
            return new UpperPeriodContext(PeriodTypeEnum.WEEK, p.getLookbackWeek());
        }
        return null;
    }

    public static boolean passesBreakoutGate(StockBase stock, CheckResult checkResult,
                                             WavePeriodGateStrategyParams params) {
        WavePeriodGateStrategyParams p = params != null ? params : WavePeriodGateStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        int lookback = resolveLookback(p);
        if (stock == null || period == null || lookback < 3) {
            return false;
        }
        int fetchBars = Math.max(lookback + 2, lookback);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        return passesBreakoutGateOnBars(periodBars, stock, checkResult, period, lookback, p);
    }

    static boolean passesBreakoutGateOnBars(List<Trade> periodBars, StockBase stock, CheckResult checkResult,
                                            PeriodTypeEnum period, int lookback,
                                            WavePeriodGateStrategyParams params) {
        WavePeriodGateStrategyParams p = params != null ? params : WavePeriodGateStrategyParams.defaults();
        if (CollectionUtils.isEmpty(periodBars) || periodBars.size() < 2) {
            return false;
        }
        if (!p.isEnableConcaveBreakout() && !p.isEnableConvexBreakout()) {
            appendMessage(checkResult, period, periodLabel(period) + "突破未启用");
            return false;
        }
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(periodBars, lookback);
        if (bands.size() < 2) {
            appendMessage(checkResult, period, periodLabel(period) + "波段不足");
            return false;
        }
        Trade current = periodBars.get(periodBars.size() - 1);
        Trade prevBar = periodBars.get(periodBars.size() - 2);
        Trade prevPrevBar = periodBars.size() >= 3 ? periodBars.get(periodBars.size() - 3) : null;
        Double close = resolveClose(stock, period, current);
        if (close == null) {
            return false;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        boolean convex = WaveShapeTools.isConvex(lastBand, prevBand);
        boolean pass;
        if (convex) {
            if (!p.isEnableConvexBreakout()) {
                appendMessage(checkResult, period, periodLabel(period) + "凸波段未启用");
                return false;
            }
            pass = passesConvexBreakout(prevBar, prevPrevBar, close);
        } else {
            if (!p.isEnableConcaveBreakout()) {
                appendMessage(checkResult, period, periodLabel(period) + "凹波段未启用");
                return false;
            }
            pass = passesConcaveBreakout(prevBar, close, lastBand);
        }
        if (checkResult != null) {
            String shape = convex ? "凸" : "凹";
            appendMessage(checkResult, period,
                    pass ? periodLabel(period) + shape + "边沿突破" : periodLabel(period) + shape + "突破未过");
        }
        return pass;
    }

    public static boolean passesTierMacdPositiveGate(StockBase stock, CheckResult checkResult,
                                                    WavePeriodGateStrategyParams params) {
        WavePeriodGateStrategyParams p = params != null ? params : WavePeriodGateStrategyParams.defaults();
        if (!p.isEnableTierMacdPositiveGate()) {
            return true;
        }
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        if (stock == null || period == null) {
            return false;
        }
        boolean pass = UnilateralMacdTools.isMacdPositive(stock, period);
        if (checkResult != null) {
            appendMessage(checkResult, period,
                    pass ? periodLabel(period) + "MACD>0" : periodLabel(period) + "MACD未>0");
        }
        return pass;
    }

    public static boolean passesTierGate(StockBase stock, CheckResult checkResult,
                                         WavePeriodGateStrategyParams params) {
        WavePeriodGateStrategyParams p = params != null ? params : WavePeriodGateStrategyParams.defaults();
        PeriodTypeEnum period = resolvePeriod(p.getTier());
        int lookback = resolveLookback(p);
        if (stock == null || period == null || lookback < 3) {
            return false;
        }
        int fetchBars = Math.max(lookback + 2, lookback);
        List<Trade> periodBars = RealtimeStockCache.getLastTrades(stock, period, fetchBars);
        return passesTierGateOnBars(periodBars, stock, checkResult, period, lookback, p);
    }

    static boolean passesTierGateOnBars(List<Trade> periodBars, StockBase stock, CheckResult checkResult,
                                        PeriodTypeEnum period, int lookback,
                                        WavePeriodGateStrategyParams params) {
        WavePeriodGateStrategyParams p = params != null ? params : WavePeriodGateStrategyParams.defaults();
        if (CollectionUtils.isEmpty(periodBars) || periodBars.size() < 2) {
            return false;
        }
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(periodBars, lookback);
        if (bands.size() < 2) {
            appendMessage(checkResult, period, periodLabel(period) + "波段不足");
            return false;
        }
        Trade current = periodBars.get(periodBars.size() - 1);
        Trade prevBar = periodBars.get(periodBars.size() - 2);
        Trade prevPrevBar = periodBars.size() >= 3 ? periodBars.get(periodBars.size() - 3) : null;
        Double close = resolveClose(stock, period, current);
        if (close == null || !passesSignalBarNonYin(current, close)) {
            if (close != null) {
                appendMessage(checkResult, period, periodLabel(period) + "信号K收阴");
            }
            return false;
        }
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        double lastLow = lastBand.getBandLow();
        double prevLow = prevBand.getBandLow();
        if (Double.isNaN(lastLow) || Double.isNaN(prevLow)) {
            return false;
        }
        double minLow = Math.min(lastLow, prevLow);
        if (close <= minLow + EPS) {
            appendMessage(checkResult, period, periodLabel(period) + "未过min波段low");
            return false;
        }
        if (p.isEnableMaxBandLowGate()) {
            double maxLow = Math.max(lastLow, prevLow);
            if (close <= maxLow + EPS) {
                appendMessage(checkResult, period, periodLabel(period) + "未过max波段low");
                return false;
            }
        }
        if (!p.isEnableConcaveBreakout() && !p.isEnableConvexBreakout()) {
            appendMessage(checkResult, period, periodLabel(period) + "形态门");
            return true;
        }
        boolean convex = WaveShapeTools.isConvex(lastBand, prevBand);
        boolean pass;
        if (convex) {
            if (!p.isEnableConvexBreakout()) {
                appendMessage(checkResult, period, periodLabel(period) + "凸波段未启用");
                return false;
            }
            pass = passesConvexBreakout(prevBar, prevPrevBar, close);
        } else {
            if (!p.isEnableConcaveBreakout()) {
                appendMessage(checkResult, period, periodLabel(period) + "凹波段未启用");
                return false;
            }
            pass = passesConcaveBreakout(prevBar, close, lastBand);
        }
        if (checkResult != null) {
            String shape = convex ? "凸" : "凹";
            appendMessage(checkResult, period,
                    pass ? periodLabel(period) + shape + "突破" : periodLabel(period) + shape + "突破未过");
        }
        return pass;
    }

    /**
     * 凸波段首次突破前 K high：前 K 收盘 ≤ 前 K high 且信号 K 收盘 &gt; 前 K high；
     * 且前 K 收盘不得已站上再前 K high（否则突破已在上一根发生）。
     */
    private static boolean passesConvexBreakout(Trade prevBar, Trade prevPrevBar, double signalClose) {
        if (prevBar == null || prevBar.getHigh() == null || prevBar.getClose() == null) {
            return false;
        }
        double line = prevBar.getHigh();
        if (!passesEdgeBreakout(prevBar.getClose(), signalClose, line)) {
            return false;
        }
        if (prevPrevBar != null && prevPrevBar.getHigh() != null && prevPrevBar.getClose() != null) {
            if (prevBar.getClose() > prevPrevBar.getHigh() + EPS) {
                return false;
            }
        }
        return true;
    }

    /** 凹波段：前 K 收盘 ≤ 末波段 high，且信号 K 收盘 &gt; 末波段 high（日/周/月同义）。 */
    private static boolean passesConcaveBreakout(Trade prevBar, double signalClose,
                                                 YangBandTools.CompleteYangBand lastBand) {
        if (lastBand == null || Double.isNaN(lastBand.getBandHigh()) || prevBar == null) {
            return false;
        }
        return passesEdgeBreakout(prevBar.getClose(), signalClose, lastBand.getBandHigh());
    }

    /** 同档最后一根 K（信号 K）须 close ≥ open（允许平盘）。 */
    private static boolean passesSignalBarNonYin(Trade signalBar, double signalClose) {
        if (signalBar == null || signalBar.getOpen() == null) {
            return false;
        }
        return signalClose >= signalBar.getOpen() - EPS;
    }

    private static boolean passesEdgeBreakout(Double prevClose, double signalClose, double line) {
        if (prevClose == null || Double.isNaN(line)) {
            return false;
        }
        return prevClose <= line + EPS && signalClose > line + EPS;
    }

    public static PeriodTypeEnum resolvePeriod(WavePeriodGateStrategyParams.Tier tier) {
        if (tier == WavePeriodGateStrategyParams.Tier.WEEK) {
            return PeriodTypeEnum.WEEK;
        }
        if (tier == WavePeriodGateStrategyParams.Tier.MONTH) {
            return PeriodTypeEnum.MONTH;
        }
        return PeriodTypeEnum.DAY;
    }

    static int resolveLookback(WavePeriodGateStrategyParams params) {
        WavePeriodGateStrategyParams p = params != null ? params : WavePeriodGateStrategyParams.defaults();
        if (p.getTier() == WavePeriodGateStrategyParams.Tier.WEEK) {
            return p.getLookbackWeek();
        }
        if (p.getTier() == WavePeriodGateStrategyParams.Tier.MONTH) {
            return p.getLookbackMonth();
        }
        return p.getLookbackDay();
    }

    private static Double resolveClose(StockBase stock, PeriodTypeEnum period, Trade current) {
        if (current != null && current.getClose() != null) {
            return current.getClose();
        }
        if (period == PeriodTypeEnum.DAY && stock != null && stock.getClose() != null) {
            return stock.getClose();
        }
        return null;
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null) {
            checkResult.addTrendPeriod(period, msg);
        }
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
}
