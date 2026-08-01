package com.yh.bigdata.tts.spider.strategy.tools.trendma;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.TrendMaStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.AllYangGateTools;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import lombok.Getter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 趋势MA：日/周/月各档须 MACD&gt;0、close&gt;max(MA5~30)、收阳；至少 2 档满足。
 */
public final class TrendMaTools {

    private static final double EPS = 1e-6;
    private static final int MIN_TIER_COUNT = 2;

    private TrendMaTools() {
    }

    @Getter
    public static final class Hit {
        private final Set<PeriodTypeEnum> passedTiers;
        private final Trade dayBar;

        Hit(Set<PeriodTypeEnum> passedTiers, Trade dayBar) {
            this.passedTiers = passedTiers != null ? passedTiers : EnumSet.noneOf(PeriodTypeEnum.class);
            this.dayBar = dayBar;
        }
    }

    public static Hit findHit(StockBase stock) {
        if (stock == null) {
            return null;
        }
        Set<PeriodTypeEnum> passed = EnumSet.noneOf(PeriodTypeEnum.class);
        if (passesTier(stock, PeriodTypeEnum.DAY)) {
            passed.add(PeriodTypeEnum.DAY);
        }
        if (passesTier(stock, PeriodTypeEnum.WEEK)) {
            passed.add(PeriodTypeEnum.WEEK);
        }
        if (passesTier(stock, PeriodTypeEnum.MONTH)) {
            passed.add(PeriodTypeEnum.MONTH);
        }
        if (passed.size() < MIN_TIER_COUNT) {
            return null;
        }
        Trade dayBar = RealtimeStockCache.getLastTrade(stock, PeriodTypeEnum.DAY, 0);
        return new Hit(passed, dayBar);
    }

    static boolean passesTier(StockBase stock, PeriodTypeEnum period) {
        if (stock == null || period == null) {
            return false;
        }
        if (!UnilateralMacdTools.isMacdPositive(stock, period)) {
            return false;
        }
        if (!AllYangGateTools.passesPeriod(stock, period)) {
            return false;
        }
        Trade last = RealtimeStockCache.getLastTrade(stock, period, 0);
        return passesCloseAboveMaxMa(last);
    }

    static boolean passesCloseAboveMaxMa(Trade last) {
        if (last == null || last.getClose() == null) {
            return false;
        }
        Double maxMa = last.getMaxMA();
        if (maxMa == null) {
            return false;
        }
        return last.getClose() > maxMa + EPS;
    }

    static int countPassedTiers(boolean dayOk, boolean weekOk, boolean monthOk) {
        int n = 0;
        if (dayOk) {
            n++;
        }
        if (weekOk) {
            n++;
        }
        if (monthOk) {
            n++;
        }
        return n;
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              TrendMaStrategyParams params) {
        TrendMaStrategyParams p = params != null ? params : TrendMaStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            appendMessage(checkResult, "成交额不足");
            return false;
        }
        return true;
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null || hit.getPassedTiers().isEmpty()) {
            return "[TMA]趋势MA";
        }
        return String.format("[TMA]趋势MA|满足=%s", tierLabels(hit.getPassedTiers()));
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null) {
            return "";
        }
        Trade dayBar = hit.getDayBar();
        return String.format(
                "趋势MA,strategyTag=TMA,period=day,tiers=%s,sigDay=%s,sigClose=%.2f",
                tierCodes(hit.getPassedTiers()),
                dayOf(dayBar),
                dayBar != null && dayBar.getClose() != null ? dayBar.getClose() : 0);
    }

    private static String tierLabels(Set<PeriodTypeEnum> tiers) {
        List<String> labels = new ArrayList<>();
        if (tiers.contains(PeriodTypeEnum.DAY)) {
            labels.add("日");
        }
        if (tiers.contains(PeriodTypeEnum.WEEK)) {
            labels.add("周");
        }
        if (tiers.contains(PeriodTypeEnum.MONTH)) {
            labels.add("月");
        }
        return String.join("/", labels);
    }

    private static String tierCodes(Set<PeriodTypeEnum> tiers) {
        List<String> codes = new ArrayList<>();
        if (tiers.contains(PeriodTypeEnum.DAY)) {
            codes.add("day");
        }
        if (tiers.contains(PeriodTypeEnum.WEEK)) {
            codes.add("week");
        }
        if (tiers.contains(PeriodTypeEnum.MONTH)) {
            codes.add("month");
        }
        return String.join(",", codes);
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, String msg) {
        if (checkResult != null) {
            checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "[TMA]" + msg);
        }
    }
}
