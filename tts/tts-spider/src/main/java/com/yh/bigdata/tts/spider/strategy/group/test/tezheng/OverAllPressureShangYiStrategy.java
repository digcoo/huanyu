//package com.yh.bigdata.tts.spider.strategy.group.test.tezheng;
//
//import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
//import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
//import com.yh.bigdata.tts.common.model.StockBase;
//import com.yh.bigdata.tts.common.param.QueryContextParam;
//import com.yh.bigdata.tts.spider.response.CheckResult;
//import com.yh.bigdata.tts.spider.strategy.AbstractStrategy;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * 梯子之上： macd上移
// */
//@Slf4j
//@Component
//public class OverAllPressureShangYiStrategy extends AbstractStrategy {
//
//
//    @Override
//    public StrategyTypeEnum getStrategy() {
//        return StrategyTypeEnum.OVER_ALL_PRESSURE;
//    }
//
//    @Override
//    public PeriodTypeEnum getOpPeriodType() {
//        return PeriodTypeEnum.DAY;
//    }
//
//    @Override
//    public List<PeriodTypeEnum> getTrendPeriodTypes() {
//        return List.of(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH, PeriodTypeEnum.QUARTER, PeriodTypeEnum.YEAR);
//    }
//
//    @Override
//    public boolean checkSubSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult, QueryContextParam queryContextParam) {
//        trendPeriodTypes = List.of(PeriodTypeEnum.DAY);
//
//        return true;
//    }
//
//}