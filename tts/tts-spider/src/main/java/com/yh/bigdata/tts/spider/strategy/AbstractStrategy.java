package com.yh.bigdata.tts.spider.strategy;

import java.util.*;
import java.util.stream.Collectors;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.group.min30.SignalMin30ToolsWrapper;
import com.yh.bigdata.tts.spider.strategy.tools.*;

import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;

import lombok.extern.slf4j.Slf4j;


//趋势： 右侧（顶脱离、中位数）、梯子（中位数|脱离）、深坑、上移
@Slf4j
public abstract class AbstractStrategy {

    protected static final Map<PeriodTypeEnum, List<PeriodTypeEnum>> COM_PERIOD_AMP = Map.of(
            PeriodTypeEnum.MONTH, List.of(PeriodTypeEnum.YEAR),
            PeriodTypeEnum.WEEK, List.of(PeriodTypeEnum.MONTH, PeriodTypeEnum.QUARTER),
            PeriodTypeEnum.DAY, List.of(PeriodTypeEnum.WEEK)
    );

	public abstract StrategyTypeEnum getStrategy();

    protected PeriodTypeEnum getOpPeriodType() {
        return PeriodTypeEnum.DAY;
    }

    protected List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Arrays.asList(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH);
    }

    //============================================================NoRisk=======================================================================================

    protected boolean checkSubNoRisk(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        return true;
    }
    public boolean checkNoRisk(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        return checkSubNoRisk(stockBase, trendPeriodTypes, checkResult)

                // 风险1: 跌破连续支撑（Low）
                && RiskToolsWrapper.checkNotUnderLowRisk(stockBase, Arrays.asList(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH), checkResult)

//                RiskToolsWrapper.checkNoXieSanJiaoRisk(stockBase, List.of(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH), checkResult)
                ;
    }


    //============================================================Priority=======================================================================================

    /**
     * 试盘信号 or 拉升信号
     */

    protected boolean checkSubPriority(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        return true;
    }
    public boolean checkPriority(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        return checkSubPriority(stockBase, trendPeriodTypes, checkResult)

                //偏好2：日平均成交额~
                &&  PriorityToolsWrapper.checkBaseAmountPriority(stockBase, checkResult)
                ;
    }



    //============================================================Trend=======================================================================================
    protected boolean checkSubTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        return true;
    }
    //Kline: 右侧（顶脱离、底支撑）、梯子（半山腰|脱离）、深坑、上移
    public boolean checkTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
        List<PeriodTypeEnum> allTrendPeriodTypes = List.of(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH);
        return checkSubTrend(stockBase, trendPeriodTypes, checkResult)
//                //or
//                && TrendToolsWrapper.checkOverMACDTrend(stockBase, allTrendPeriodTypes, checkResult)
//
//                //and
//                && TrendToolsWrapper.checkNoPressureTrend(stockBase, allTrendPeriodTypes, checkResult, false)
//
//                //and
//                && TrendToolsWrapper.checkOverMA20Trend(stockBase, allTrendPeriodTypes, checkResult)
//
//                && checkSubTrend(stockBase, trendPeriodTypes, checkResult)
//
//                // 前K的Low之上
//                && TrendToolsWrapper.checkOverLastLowTrend(stockBase, allTrendPeriodTypes, checkResult)

                // 趋势波段之上
                && TrendToolsWrapper.checkOverBandLowTrend(stockBase, trendPeriodTypes, checkResult)

                // 反转波段之上
                && TrendToolsWrapper.checkOverRevertBandLowTrend(stockBase, trendPeriodTypes, checkResult)
        ;

    }



    //============================================================Signal=======================================================================================
    protected boolean checkSubSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult, QueryContextParam queryContextParam) {
        return true;
    }
    public boolean checkSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult, QueryContextParam queryContextParam) {
        return checkSubSignal(stockBase, trendPeriodTypes, opPeriodType, checkResult, queryContextParam)
//                && TrendToolsWrapper.checkNoPressureTrend(stockBase, List.of(PeriodTypeEnum.DAY), checkResult, false)
//                && TrendToolsWrapper.checkMA5OverMA20Trend(stockBase, List.of(PeriodTypeEnum.DAY), checkResult)
//                && TrendToolsWrapper.checkRedTrend(stockBase, List.of(PeriodTypeEnum.DAY), checkResult)

                && SignalMin30ToolsWrapper.checkCrossZaoPanMin30HighSignal(stockBase, trendPeriodTypes, checkResult)

                ;
    }



	public List<CheckResult> doQuery(List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {
        return RealtimeStockCache.filterStockMap.values().stream()
                .map(x -> check(x, trendPeriodTypes, opPeriodType, queryContextParam))
                .filter(CheckResult::isSuccess)
                .collect(Collectors.toList())
                ;
    }
	
	public CheckResult check(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, QueryContextParam queryContextParam) {

        CheckResult checkResult = new CheckResult(stockBase.getCode(), stockBase.getChangeRate());

        try {
//            List<PeriodTypeEnum> finalTrendPeriodTypes = !CollectionUtils.isEmpty(trendPeriodTypes)? trendPeriodTypes : getTrendPeriodTypes();
            List<PeriodTypeEnum> finalTrendPeriodTypes = getTrendPeriodTypes();
            PeriodTypeEnum finalOpPeriodType = getOpPeriodType();
            if (Objects.nonNull(opPeriodType) && opPeriodType.ordinal() > finalOpPeriodType.ordinal()) {
                finalOpPeriodType = opPeriodType;
            }

            //风险过滤
            boolean checkNoRisk = checkNoRisk(stockBase, finalTrendPeriodTypes, checkResult);
            if (!checkNoRisk) {
                return checkResult;
            }

            // 策略偏好
            boolean checkPriority = checkPriority(stockBase, finalTrendPeriodTypes, checkResult);
            if (!checkPriority) {
                return checkResult;
            }

            //趋势： MACD上
            //右侧（顶脱离、底支撑）、梯子（半山腰|脱离）、深坑、上移
            boolean checkTrend = checkTrend(stockBase, finalTrendPeriodTypes, checkResult);
            if (!checkTrend) {
                return checkResult;
            }
            checkResult.setHasTrend(true);

            //信号：上移、MACD金叉
            boolean checkSignal = checkSignal(stockBase, finalTrendPeriodTypes, finalOpPeriodType, checkResult, queryContextParam);
            if (!checkSignal) {
                return checkResult;
            }
            checkResult.setHasSignal(true);

            if (checkResult.isSuccess()) {
                checkResult.setTrendPeriodType(trendPeriodTypes.get(0));
                checkResult.setOpPeriodType(finalOpPeriodType);
            }

        } catch (Exception ex) {
            log.error("{} - check exception : stock = {}", this.getClass().getName(), stockBase.getCode(), ex);
        } finally {
            try {
                Trade monthTrade1 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.MONTH, -1);
                Trade weekTrade1 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.WEEK, -1);
                Trade dayTrade1 = RealtimeStockCache.getLastTrade(stockBase, PeriodTypeEnum.DAY, -1);
                if(monthTrade1 != null) {
                    checkResult.setSortValue(monthTrade1.getChangeRate());
                } else if(weekTrade1 != null) {
                    checkResult.setSortValue(weekTrade1.getChangeRate());
                }else {
                    checkResult.setSortValue(dayTrade1 == null? 0: dayTrade1.getChangeRate());
                }
            } catch (Exception e2) {
                log.error("setSortValue error...{}", stockBase.getCode(), e2);
            }

        }

        return checkResult;

    }

}