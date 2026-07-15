package com.yh.bigdata.tts.spider.strategy.tools.macdgc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MacdGoldenCrossStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

public final class MacdGoldenCrossEvaluator {

    private MacdGoldenCrossEvaluator() {
    }

    public static MacdGoldenCrossEvaluation evaluate(StockBase stock, CheckResult checkResult,
                                                     MacdGoldenCrossStrategyParams params) {
        MacdGoldenCrossStrategyParams p = params != null ? params : MacdGoldenCrossStrategyParams.defaults();
        if (!MacdGoldenCrossTools.passesTierGate(stock, checkResult, p)) {
            return MacdGoldenCrossEvaluation.miss();
        }
        PeriodTypeEnum period = MacdGoldenCrossTools.resolvePeriod(p.getTier());
        if (checkResult != null) {
            String tierLabel = MacdGoldenCrossTools.buildTierLabel(p);
            checkResult.addTrendPeriod(period, "[MGC]MACD金叉|" + tierLabel + "档");
            List<Trade> bars = RealtimeStockCache.getLastTrades(stock, period, 2);
            if (!CollectionUtils.isEmpty(bars) && bars.size() >= 2) {
                checkResult.addSignal(period, MacdGoldenCrossTools.buildSignalMessage(
                        period, bars.get(bars.size() - 1), bars.get(bars.size() - 2)));
            }
        }
        return MacdGoldenCrossEvaluation.hit(period);
    }

    @Getter
    public static final class MacdGoldenCrossEvaluation {
        private final PeriodTypeEnum period;
        private final boolean hit;

        MacdGoldenCrossEvaluation(PeriodTypeEnum period, boolean hit) {
            this.period = period;
            this.hit = hit;
        }

        static MacdGoldenCrossEvaluation miss() {
            return new MacdGoldenCrossEvaluation(null, false);
        }

        static MacdGoldenCrossEvaluation hit(PeriodTypeEnum period) {
            return new MacdGoldenCrossEvaluation(period, true);
        }
    }
}
