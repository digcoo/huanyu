package com.yh.bigdata.tts.spider.strategy.tools.mabearbreakma;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MaBearBreakMaStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.MinAvgAmountFilterTools;
import com.yh.bigdata.tts.spider.strategy.tools.ma3m.Ma3mCore;
import com.yh.bigdata.tts.spider.strategy.tools.mabreakma.MaBreakMaTools;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MA空头破MA：日线 3M 空头 + close&gt;max(MA5,MA10)；30 分 3M 多头 + 破 MAX。
 */
public final class MaBearBreakMaTools {

    private MaBearBreakMaTools() {
    }

    @Getter
    public static final class Hit {
        private final Trade dayBar;
        private final Ma3mCore.AlignKind dayBearAlign;
        private final MaBreakMaTools.Hit min30Hit;

        Hit(Trade dayBar, Ma3mCore.AlignKind dayBearAlign, MaBreakMaTools.Hit min30Hit) {
            this.dayBar = dayBar;
            this.dayBearAlign = dayBearAlign;
            this.min30Hit = min30Hit;
        }
    }

    public static Hit findHit(StockBase stock, MaBearBreakMaStrategyParams params) {
        MaBearBreakMaStrategyParams p = params != null ? params : MaBearBreakMaStrategyParams.defaults();
        if (stock == null) {
            return null;
        }
        List<Trade> dayBars = RealtimeStockCache.getLastTrades(
                stock, PeriodTypeEnum.DAY, Math.max(p.getLookbackDay(), 40));
        DayBear dayBear = findDayBear(dayBars);
        if (dayBear == null) {
            return null;
        }
        List<Trade> min30Bars = RealtimeStockCache.getLastTrades(
                stock, PeriodTypeEnum.MIN30, Math.max(p.getLookbackMin30(), 40));
        MaBreakMaTools.Hit min30Hit = MaBreakMaTools.findHitOnBars(min30Bars, PeriodTypeEnum.MIN30);
        if (min30Hit == null) {
            return null;
        }
        return new Hit(dayBear.bar, dayBear.align, min30Hit);
    }

    static DayBear findDayBear(List<Trade> dayBars) {
        if (CollectionUtils.isEmpty(dayBars)) {
            return null;
        }
        Trade last = dayBars.get(dayBars.size() - 1);
        if (!Ma3mCore.passesBearCloseAboveMa5Ma10(last)) {
            return null;
        }
        if (Ma3mCore.passesBearAlign(last, Ma3mCore.AlignKind.MA20)) {
            return new DayBear(last, Ma3mCore.AlignKind.MA20);
        }
        if (Ma3mCore.passesBearAlign(last, Ma3mCore.AlignKind.MA30)) {
            return new DayBear(last, Ma3mCore.AlignKind.MA30);
        }
        return null;
    }

    public static boolean passesOptionalGates(StockBase stock, CheckResult checkResult,
                                              MaBearBreakMaStrategyParams params) {
        MaBearBreakMaStrategyParams p = params != null ? params : MaBearBreakMaStrategyParams.defaults();
        if (p.isEnableMinAmountFilter()
                && !MinAvgAmountFilterTools.passWithMessage(stock, checkResult, p.getMinAvgAmount())) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(PeriodTypeEnum.DAY, "[MBBM]成交额不足");
            }
            return false;
        }
        return true;
    }

    public static String buildTrendMessage(Hit hit) {
        if (hit == null) {
            return "[MBBM]MA空头破MA";
        }
        MaBreakMaTools.Hit m = hit.getMin30Hit();
        return String.format("[MBBM]MA空头破MA|日%s空头+30分%s多头+%s破MAX|breakLine=%.2f",
                hit.getDayBearAlign() == Ma3mCore.AlignKind.MA30 ? "3M2" : "3M1",
                m.getAlignKind() == Ma3mCore.AlignKind.MA30 ? "3M2" : "3M1",
                m.getBreakKind() == Ma3mCore.BreakKind.OPEN ? "开盘" : "边沿",
                m.getBreakLine());
    }

    public static String buildSignalMessage(Hit hit) {
        if (hit == null || hit.getMin30Hit() == null) {
            return "";
        }
        MaBreakMaTools.Hit m = hit.getMin30Hit();
        Trade signalBar = m.getSignalBar();
        return String.format(
                "MA空头破MA,strategyTag=MBBM,period=min30,dayAlign=%s,align=%s,break=%s,sigDay=%s,sigClose=%.2f,breakLine=%.2f,day=%s",
                hit.getDayBearAlign() != null ? hit.getDayBearAlign().name() : "",
                m.getAlignKind() != null ? m.getAlignKind().name() : "",
                m.getBreakKind() != null ? m.getBreakKind().name() : "",
                signalBar != null && signalBar.getDay() != null ? signalBar.getDay() : "",
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                m.getBreakLine(),
                hit.getDayBar() != null && hit.getDayBar().getDay() != null ? hit.getDayBar().getDay() : "");
    }

    private static final class DayBear {
        private final Trade bar;
        private final Ma3mCore.AlignKind align;

        DayBear(Trade bar, Ma3mCore.AlignKind align) {
            this.bar = bar;
            this.align = align;
        }
    }
}
