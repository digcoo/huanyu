package com.yh.bigdata.tts.spider.strategy.tools.rebound;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.indicator.MAIndicatorUtils;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.group.band.SignalBandTools;
import com.yh.bigdata.tts.spider.strategy.tools.IndicatorCommonUtils;
import com.yh.bigdata.tts.spider.strategy.tools.KlineCommonTools;

import java.util.List;

/**
 * 深坑反弹 v1.0 · 脱离 / 上移 / 日 K 确认
 */
public final class ReboundTriggerTools {

    private static final PeriodTypeEnum DAY = PeriodTypeEnum.DAY;
    private static final PeriodTypeEnum WEEK = PeriodTypeEnum.WEEK;
    private static final PeriodTypeEnum MONTH = PeriodTypeEnum.MONTH;

    private ReboundTriggerTools() {
    }

    public static TriggerSnapshot evaluate(StockBase stock) {
        TriggerSnapshot t = new TriggerSnapshot();
        t.breakRevertBandWeek = hit(SignalBandTools.checkRevertBandHighSignal(stock, WEEK, DAY));
        t.breakRevertBandMonth = hit(SignalBandTools.checkRevertBandHighSignal(stock, MONTH, DAY));
        t.lowShangYiWeek = hit(KlineCommonTools.checkLastLowShangYi(stock, WEEK));
        t.lowShangYiDay = hit(KlineCommonTools.checkLastLowShangYi(stock, DAY));
        t.macdCross = hit(IndicatorCommonUtils.checkCrossGoldMACD(stock, DAY))
                || hit(IndicatorCommonUtils.checkOverMACD(stock, DAY));
        t.yangAboveMa5 = checkYangAboveMa5(stock);
        return t;
    }

    public static boolean isModeA(TriggerSnapshot t) {
        return t != null && t.breakRevertBand();
    }

    public static boolean isModeB(TriggerSnapshot t) {
        if (t == null) {
            return false;
        }
        return t.lowShangYi() && (t.yangAboveMa5 || t.macdCross);
    }

    public static boolean isModeC(TriggerSnapshot t, boolean divergence) {
        if (t == null || !divergence) {
            return false;
        }
        return t.yangAboveMa5 || t.macdCross;
    }

    private static boolean checkYangAboveMa5(StockBase stock) {
        if (!hit(KlineCommonTools.checkRed(stock, DAY))) {
            return false;
        }
        Trade d0 = RealtimeStockCache.getLastTrade(stock, DAY, 0);
        if (d0 == null || d0.getClose() == null) {
            return false;
        }
        List<Trade> days = RealtimeStockCache.getLastTrades(stock, DAY, 30);
        if (days == null || days.size() < 6) {
            return false;
        }
        Double ma5 = MAIndicatorUtils.calLatestMA(days, 5);
        return ma5 != null && d0.getClose() > ma5;
    }

    private static boolean hit(CheckResponse resp) {
        return resp != null && resp.isSuccess();
    }

    public static final class TriggerSnapshot {
        public boolean breakRevertBandWeek;
        public boolean breakRevertBandMonth;
        public boolean lowShangYiWeek;
        public boolean lowShangYiDay;
        public boolean macdCross;
        public boolean yangAboveMa5;

        public boolean breakRevertBand() {
            return breakRevertBandWeek || breakRevertBandMonth;
        }

        public boolean lowShangYi() {
            return lowShangYiWeek || lowShangYiDay;
        }
    }
}
