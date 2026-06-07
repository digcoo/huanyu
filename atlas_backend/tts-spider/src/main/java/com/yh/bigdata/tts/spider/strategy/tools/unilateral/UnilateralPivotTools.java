package com.yh.bigdata.tts.spider.strategy.tools.unilateral;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.response.CheckResult;

/**
 * 单边趋势 v1.0 · 模式 A（月 K 拐点，补充路径）
 */
public final class UnilateralPivotTools {

    public static final String SIGNAL_MSG = "月K阳反阴&突破前第2月实体下沿";

    private UnilateralPivotTools() {
    }

    /**
     * K3=本月, K2=上月, K1=前第2根月K
     */
    public static boolean checkMonthPivot(StockBase stock, CheckResult checkResult) {
        if (stock == null) {
            return false;
        }
        PeriodTypeEnum period = PeriodTypeEnum.MONTH;
        Trade k3 = RealtimeStockCache.getLastTrade(stock, period, 0);
        Trade k2 = RealtimeStockCache.getLastTrade(stock, period, 1);
        Trade k1 = RealtimeStockCache.getLastTrade(stock, period, 2);
        if (k3 == null || k2 == null || k1 == null) {
            return false;
        }
        if (k3.getOpen() == null || k3.getClose() == null
                || k2.getOpen() == null || k2.getClose() == null) {
            return false;
        }
        Double k1BodyMin = k1.getShitiMin();
        if (k1BodyMin == null) {
            return false;
        }

        boolean yangAfterYin = k3.getClose() > k3.getOpen()
                && k2.getClose() <= k2.getOpen();
        boolean breakK1BodyMin = k3.getClose() > k1BodyMin;

        if (yangAfterYin && breakK1BodyMin) {
            if (checkResult != null) {
                checkResult.addSignal(period, SIGNAL_MSG);
            }
            return true;
        }
        return false;
    }
}
