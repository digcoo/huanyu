package com.yh.bigdata.tts.spider.strategy.tools.macrosspoint;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.macrosspoint.MaCrossPointEvaluator.MaCrossPointEvaluation;

import java.util.List;

/**
 * 策略3/4：边沿破金叉波段顶(且MA5≥MA10) / 死叉交叉点 / 金叉交叉点(且MA5≥MA10)，再按 MA10 vs MA60 分流。
 * 不要求父级均价之上。
 */
public final class MaTrendBreakEvaluator {

    private MaTrendBreakEvaluator() {
    }

    public static MaCrossPointEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                  MaCrossPointStrategyParams params, boolean bull) {
        MaCrossPointStrategyParams p = params != null ? params : MaCrossPointStrategyParams.defaults();
        PeriodTypeEnum period = MaCrossPointTools.resolvePeriod(p.getTier());
        String tag = bull ? "MTB" : "MBS";
        String miss = "未满足(破金叉波段顶且MA5>=MA10)/破死叉点/(破金叉交叉点且MA5>=MA10)";

        MaCrossPointTools.Hit hit = MaCrossPointTools.findTrendBreakHit(stock, p);
        if (hit == null) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[" + tag + "]" + miss);
            }
            return MaCrossPointEvaluation.miss();
        }

        List<Trade> signalBars = RealtimeStockCache.getLastTrades(
                stock, period, Math.max(MaCrossPointTools.resolveLookback(p), 60));
        if (bull) {
            if (!MaCrossPointCore.passesMa10GeMa60(signalBars)) {
                if (checkResult != null) {
                    checkResult.addTrendPeriod(period, "[" + tag + "]未满足MA10>=MA60");
                }
                return MaCrossPointEvaluation.miss();
            }
        } else if (!MaCrossPointCore.passesMa10LtMa60(signalBars)) {
            if (checkResult != null) {
                checkResult.addTrendPeriod(period, "[" + tag + "]未满足MA10<MA60");
            }
            return MaCrossPointEvaluation.miss();
        }

        if (!MaCrossPointTools.passesOptionalGates(stock, checkResult, p, hit, tag)) {
            return MaCrossPointEvaluation.miss();
        }

        if (checkResult != null) {
            checkResult.addTrendPeriod(period, MaCrossPointTools.buildTrendBreakTrendMessage(hit, bull));
            checkResult.addSignal(period, MaCrossPointTools.buildTrendBreakSignalMessage(hit, bull));
        }
        return MaCrossPointEvaluation.hit(period);
    }
}
