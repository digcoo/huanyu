package com.yh.bigdata.tts.spider.strategy.tools.macdgcwhu;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdGcWaveHighLiftStrategyParams;
import com.yh.bigdata.tts.common.param.MacdPositiveGateParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdPositiveGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MACD金叉上移：日/周/月 MACD&gt;0 + 日 close&gt;前日 high + 前日收阳。
 */
public final class MacdGcWaveHighLiftTools {

    private static final double EPS = 1e-6;

    private MacdGcWaveHighLiftTools() {
    }

    @Getter
    public static final class Hit {
        private final Trade signalBar;
        private final Trade prevBar;

        Hit(Trade signalBar, Trade prevBar) {
            this.signalBar = signalBar;
            this.prevBar = prevBar;
        }
    }

    public static Hit resolveHit(StockBase stock, MacdGcWaveHighLiftStrategyParams params) {
        if (stock == null) {
            return null;
        }
        List<Trade> dayBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 5);
        if (CollectionUtils.isEmpty(dayBars) || dayBars.size() < 2) {
            return null;
        }
        return resolveHitOnBars(dayBars);
    }

    static Hit resolveHitOnBars(List<Trade> dayBars) {
        if (CollectionUtils.isEmpty(dayBars) || dayBars.size() < 2) {
            return null;
        }
        Trade signalBar = dayBars.get(dayBars.size() - 1);
        Trade prevBar = dayBars.get(dayBars.size() - 2);
        if (!passesCloseAbovePrevHigh(signalBar, prevBar)) {
            return null;
        }
        if (!isYangBar(prevBar)) {
            return null;
        }
        return new Hit(signalBar, prevBar);
    }

    public static boolean passesMultiPeriodMacdGate(StockBase stock, CheckResult checkResult,
                                                    MacdGcWaveHighLiftStrategyParams params) {
        MacdGcWaveHighLiftStrategyParams p = params != null
                ? params : MacdGcWaveHighLiftStrategyParams.defaults();
        if (!MacdPositiveGateTools.passGate(stock, checkResult, toMacdPositiveGate(p))) {
            return false;
        }
        if (!p.isRequireMin60Macd()) {
            return true;
        }
        if (!UnilateralMacdTools.isMacdPositive(stock, PeriodTypeEnum.MIN60)) {
            appendMacdMessage(checkResult, PeriodTypeEnum.MIN60, "Min60MACD≤0");
            return false;
        }
        appendMacdMessage(checkResult, PeriodTypeEnum.MIN60, "Min60MACD>0");
        return true;
    }

    public static MacdPositiveGateParams toMacdPositiveGate(MacdGcWaveHighLiftStrategyParams params) {
        MacdGcWaveHighLiftStrategyParams p = params != null
                ? params : MacdGcWaveHighLiftStrategyParams.defaults();
        return MacdPositiveGateParams.builder()
                .requireDayMacd(p.isRequireDayMacd())
                .requireWeekMacd(p.isRequireWeekMacd())
                .requireMonthMacd(p.isRequireMonthMacd())
                .build();
    }

    static boolean passesCloseAbovePrevHigh(Trade signalBar, Trade prevBar) {
        if (signalBar == null || prevBar == null) {
            return false;
        }
        Double signalClose = signalBar.getClose();
        Double prevHigh = prevBar.getHigh();
        if (signalClose == null || prevHigh == null) {
            return false;
        }
        return signalClose > prevHigh + EPS;
    }

    static boolean isYangBar(Trade bar) {
        if (bar == null || bar.getOpen() == null || bar.getClose() == null) {
            return false;
        }
        return bar.getClose() >= bar.getOpen() - EPS;
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MacdGcWaveHighLiftStrategyParams params,
                                              Trade signalBar, Trade prevBar) {
        MacdGcWaveHighLiftStrategyParams p = params != null ? params : MacdGcWaveHighLiftStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, "成交额不足");
            return false;
        }
        if (p.isEnableSignalRiseGate()) {
            double signalRise = BodyBarTierTools.risePct(signalBar, prevBar);
            if (Double.isNaN(signalRise) || signalRise <= p.getSignalRisePct() + EPS) {
                appendMessage(checkResult, "末K涨幅不足");
                return false;
            }
        }
        return true;
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade signalBar = hit.getSignalBar();
        Trade prevBar = hit.getPrevBar();
        double risePct = BodyBarTierTools.risePct(signalBar, prevBar);
        return String.format(
                "MACD金叉上移,strategyTag=MGCWHU,period=day,"
                        + "sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f,prevOpen=%.2f,prevHigh=%.2f,risePct=%.4f",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                dayOf(prevBar),
                prevBar != null && prevBar.getClose() != null ? prevBar.getClose() : 0,
                prevBar != null && prevBar.getOpen() != null ? prevBar.getOpen() : 0,
                prevBar != null && prevBar.getHigh() != null ? prevBar.getHigh() : 0,
                Double.isNaN(risePct) ? 0 : risePct);
    }

    public static String buildTrendMessage(MacdGcWaveHighLiftStrategyParams params) {
        MacdGcWaveHighLiftStrategyParams p = params != null
                ? params : MacdGcWaveHighLiftStrategyParams.defaults();
        StringBuilder macdPart = new StringBuilder();
        if (p.isRequireMin60Macd()) {
            macdPart.append("Min60MACD>0");
        }
        if (p.isRequireDayMacd()) {
            if (macdPart.length() > 0) {
                macdPart.append('/');
            }
            macdPart.append("日MACD>0");
        }
        if (p.isRequireWeekMacd()) {
            if (macdPart.length() > 0) {
                macdPart.append('/');
            }
            macdPart.append("周MACD>0");
        }
        if (p.isRequireMonthMacd()) {
            if (macdPart.length() > 0) {
                macdPart.append('/');
            }
            macdPart.append("月MACD>0");
        }
        String gate = macdPart.length() > 0 ? macdPart.toString() + ',' : "";
        return "[MGCWHU]MACD金叉上移|" + gate + "日收>前日high,前日收阳";
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, String msg) {
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "[MGCWHU]" + msg);
        }
    }

    private static void appendMacdMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[MGCWHU]" + msg);
        }
    }
}
