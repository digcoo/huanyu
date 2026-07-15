package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.WaveBandStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.waveband.WaveBandEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.waveband.WaveBandScoreCalculator;
import com.yh.bigdata.tts.spider.strategy.tools.waveband.WaveBandTier;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class AbstractWaveBandStrategy extends AbstractStrategy {

    protected abstract WaveBandTier tier();

    protected abstract PeriodTypeEnum shapePeriodType();

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.DAY;
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        WaveBandTier bandTier = tier();
        try {
            WaveBandStrategyParams params = resolveParams(queryContextParam);
            WaveBandEvaluator.WaveBandEvaluation eval =
                    WaveBandEvaluator.evaluate(stockBase, checkResult, params, bandTier);
            if (!eval.isHit()) {
                return checkResult;
            }
            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(WaveBandScoreCalculator.computeScore());
            checkResult.setTrendPeriodType(shapePeriodType());
            checkResult.setOpPeriodType(PeriodTypeEnum.DAY);
            checkResult.addTrendPeriod(shapePeriodType(), WaveBandScoreCalculator.buildTrendMessage(bandTier));
            checkResult.addSignal(PeriodTypeEnum.DAY, WaveBandScoreCalculator.buildSignalMessage(bandTier));
        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private WaveBandStrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null) {
            return WaveBandStrategyParams.defaults();
        }
        if (tier() == WaveBandTier.MEDIUM) {
            return WaveBandStrategyParams.merge(queryContextParam.getWaveBandMedium());
        }
        return WaveBandStrategyParams.merge(queryContextParam.getWaveBandShort());
    }

    private void applyFallbackSortValue(StockBase stockBase, CheckResult checkResult) {
        if (checkResult.getSortValue() > 0 || !checkResult.isSuccess()) {
            return;
        }
        try {
            Trade dayTrade = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.DAY, 0);
            if (dayTrade != null && dayTrade.getChangeRate() != null) {
                checkResult.setSortValue(dayTrade.getChangeRate());
            } else if (stockBase.getChangeRate() != null) {
                checkResult.setSortValue(stockBase.getChangeRate());
            }
        } catch (Exception e2) {
            log.error("setSortValue error...{}", stockBase.getCode(), e2);
        }
    }
}
