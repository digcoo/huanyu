package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.MaCrossPointStrategyParams;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.prevbandhigh.PrevBandHighEvaluator;
import com.yh.bigdata.tts.spider.strategy.tools.prevbandhigh.PrevBandHighTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class PrevBandHighStrategy extends AbstractStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.PREV_BAND_HIGH;
    }

    @Override
    public PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.DAY;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Collections.singletonList(PeriodTypeEnum.DAY);
    }

    @Override
    public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes,
                             PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());
        try {
            MaCrossPointStrategyParams params = resolveParams(queryContextParam);
            PrevBandHighEvaluator.PrevBandHighEvaluation eval =
                    PrevBandHighEvaluator.evaluate(stockBase, checkResult, params);
            if (!eval.isHit()) {
                return checkResult;
            }
            PeriodTypeEnum primary = eval.getPeriod() != null
                    ? eval.getPeriod()
                    : PrevBandHighTools.resolvePeriod(params.getTier());
            checkResult.setHasTrend(true);
            checkResult.setHasSignal(true);
            checkResult.setSortValue(50);
            checkResult.setTrendPeriodType(primary);
            checkResult.setOpPeriodType(primary);
        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", getClass().getName(), stockBase.getCode(), ex);
        } finally {
            applyFallbackSortValue(stockBase, checkResult);
        }
        return checkResult;
    }

    private MaCrossPointStrategyParams resolveParams(QueryContextParam queryContextParam) {
        if (queryContextParam == null || queryContextParam.getPrevBandHigh() == null) {
            return MaCrossPointStrategyParams.defaults();
        }
        return MaCrossPointStrategyParams.merge(queryContextParam.getPrevBandHigh());
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
