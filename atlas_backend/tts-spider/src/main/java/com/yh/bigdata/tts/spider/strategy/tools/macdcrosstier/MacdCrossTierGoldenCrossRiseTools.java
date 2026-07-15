package com.yh.bigdata.tts.spider.strategy.tools.macdcrosstier;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.unilateral.UnilateralMacdTools;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 当前 MACD 金叉且信号 K 上涨率达标。
 */
public final class MacdCrossTierGoldenCrossRiseTools {

    private static final double EPS = 1e-6;

    private MacdCrossTierGoldenCrossRiseTools() {
    }

    public static boolean passes(StockBase stock, PeriodTypeEnum period, CheckResult checkResult,
                                 double minRisePct) {
        if (stock == null || period == null || !UnilateralMacdTools.isGoldenCross(stock, period)) {
            appendMessage(checkResult, period, periodLabel(period) + "非MACD金叉");
            return false;
        }
        List<Trade> bars = RealtimeStockCache.getLastTrades(stock, period, 2);
        if (CollectionUtils.isEmpty(bars) || bars.size() < 2) {
            appendMessage(checkResult, period, periodLabel(period) + "无K");
            return false;
        }
        Trade prevBar = bars.get(bars.size() - 2);
        Trade signalBar = bars.get(bars.size() - 1);
        if (!passesRiseOnBars(signalBar, prevBar, minRisePct)) {
            appendMessage(checkResult, period, periodLabel(period) + "上涨率不足");
            return false;
        }
        appendMessage(checkResult, period, periodLabel(period) + "金叉上涨率");
        return true;
    }

    static boolean passesRiseOnBars(Trade signalBar, Trade prevBar, double minRisePct) {
        double risePct = BodyBarTierTools.risePct(signalBar, prevBar);
        return !Double.isNaN(risePct) && risePct > minRisePct + EPS;
    }

    static String buildSignalMessage(PeriodTypeEnum period, Trade signalBar, Trade prevBar) {
        double risePct = BodyBarTierTools.risePct(signalBar, prevBar);
        return String.format(
                "MACD金叉上涨率,signalTier=%s,sigDay=%s,sigClose=%.2f,prevDay=%s,prevClose=%.2f,risePct=%.4f",
                period != null ? period.getCode() : "",
                dayOf(signalBar),
                signalBar != null && signalBar.getClose() != null ? signalBar.getClose() : 0,
                dayOf(prevBar),
                prevBar != null && prevBar.getClose() != null ? prevBar.getClose() : 0,
                Double.isNaN(risePct) ? 0 : risePct);
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }

    private static void appendMessage(CheckResult checkResult, PeriodTypeEnum period, String msg) {
        if (checkResult != null && period != null) {
            checkResult.addTrendPeriod(period, "[MCT]" + msg);
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
