package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.FrictionlessLadderStrategyParams;
import com.yh.bigdata.tts.common.param.LongStrategyParams;
import com.yh.bigdata.tts.common.param.MediumStrategyParams;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.TrendV2StrategyParams;
import com.yh.bigdata.tts.common.param.UltraShortStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.FrictionlessLadderEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.frictionless.FrictionlessLadderScoreCalculator;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.UltraShortGateTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 无阻力梯子（nrf）· 梯子策略 + 固定日/周/月 MACD 门
 */
@Slf4j
@Component
public class FrictionlessLadderStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.FRICTIONLESS_LADDER;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.DAY;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Arrays.asList(
                PeriodTypeEnum.MONTH,
                PeriodTypeEnum.WEEK,
                PeriodTypeEnum.DAY);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            FrictionlessLadderStrategyParams nrfParams = resolveNrfParams(queryContextParam);
            UltraShortStrategyParams ultraParams = resolveUltraParams(queryContextParam);
            TrendV2StrategyParams trendParams = resolveTrendParams(queryContextParam);
            MediumStrategyParams mediumParams = resolveMediumParams(queryContextParam);
            LongStrategyParams longParams = resolveLongParams(queryContextParam);

            FrictionlessLadderEvaluator.Evaluation eval = FrictionlessLadderEvaluator.evaluate(
                    stockBase,
                    checkResult,
                    nrfParams,
                    ultraParams,
                    trendParams,
                    mediumParams,
                    longParams);

            if (!eval.isHit()) {
                return checkResult;
            }

            appendUltraMessages(checkResult, stockBase, ultraParams);
            writeNrfMessages(checkResult, eval);

            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(FrictionlessLadderScoreCalculator.computeScore(eval));
            checkResult.setTrendPeriodType(trendPeriodForTier(nrfParams.getActiveTier()));
            checkResult.setOpPeriodType(opPeriodForTier(nrfParams.getActiveTier()));

        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private void writeNrfMessages(CheckResult checkResult, FrictionlessLadderEvaluator.Evaluation eval) {
        PeriodTypeEnum trendPeriod = trendPeriodForTier(eval.getActiveTier());
        PeriodTypeEnum signalPeriod = opPeriodForTier(eval.getActiveTier());
        checkResult.addTrendPeriod(trendPeriod, FrictionlessLadderScoreCalculator.buildTrendMessage(eval));
        checkResult.addSignal(signalPeriod, FrictionlessLadderScoreCalculator.buildSignalMessage(eval));
    }

    private void appendUltraMessages(CheckResult checkResult, StockBase stock,
                                     UltraShortStrategyParams ultraParams) {
        UltraShortGateTools.appendMessages(checkResult, stock, ultraParams);
    }

    private static PeriodTypeEnum trendPeriodForTier(FrictionlessLadderStrategyParams.ActiveTier tier) {
        if (tier == FrictionlessLadderStrategyParams.ActiveTier.LONG) {
            return PeriodTypeEnum.MONTH;
        }
        if (tier == FrictionlessLadderStrategyParams.ActiveTier.MEDIUM) {
            return PeriodTypeEnum.WEEK;
        }
        return PeriodTypeEnum.WEEK;
    }

    private static PeriodTypeEnum opPeriodForTier(FrictionlessLadderStrategyParams.ActiveTier tier) {
        if (tier == FrictionlessLadderStrategyParams.ActiveTier.LONG) {
            return PeriodTypeEnum.MONTH;
        }
        if (tier == FrictionlessLadderStrategyParams.ActiveTier.MEDIUM) {
            return PeriodTypeEnum.WEEK;
        }
        return PeriodTypeEnum.DAY;
    }

    private FrictionlessLadderStrategyParams resolveNrfParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getFrictionlessLadder() == null) {
            return FrictionlessLadderStrategyParams.defaults();
        }
        return FrictionlessLadderStrategyParams.merge(queryContextParam.getFrictionlessLadder());
    }

    private UltraShortStrategyParams resolveUltraParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getUltraShort() == null) {
            return UltraShortStrategyParams.defaults();
        }
        return UltraShortStrategyParams.merge(queryContextParam.getUltraShort());
    }

    private TrendV2StrategyParams resolveTrendParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getTrendV2() == null) {
            return TrendV2StrategyParams.defaults();
        }
        return TrendV2StrategyParams.merge(queryContextParam.getTrendV2());
    }

    private MediumStrategyParams resolveMediumParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getMedium() == null) {
            return MediumStrategyParams.defaults();
        }
        return MediumStrategyParams.merge(queryContextParam.getMedium());
    }

    private LongStrategyParams resolveLongParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getLongTerm() == null) {
            return LongStrategyParams.defaults();
        }
        return LongStrategyParams.merge(queryContextParam.getLongTerm());
    }

    private void applyFallbackSortValue(StockBase stockBase, CheckResult checkResult) {
        if (checkResult.getSortValue() > 0 || !checkResult.isSuccess()) {
            return;
        }
        try {
            Trade dayTrade = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.DAY, -1);
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
