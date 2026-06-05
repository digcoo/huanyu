//package com.yh.bigdata.tts.spider.strategy.group.test.daban;
//
//import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
//import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
//import com.yh.bigdata.tts.common.model.StockBase;
//import com.yh.bigdata.tts.common.param.QueryContextParam;
//import com.yh.bigdata.tts.spider.response.CheckResult;
//import com.yh.bigdata.tts.spider.strategy.AbstractStrategy;
//import com.yh.bigdata.tts.spider.strategy.tools.SignalToolsWrapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * 突破梯子Min
// */
//@Slf4j
//@Component
//public class DaBan1Strategy extends AbstractStrategy {
//
//
//    @Override
//    public StrategyTypeEnum getStrategy() {
//        return StrategyTypeEnum.DA_BAN_1;
//    }
//
//    @Override
//    public boolean checkNoRisk(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
//        return true;
//    }
//
//    @Override
//    public boolean checkPriority(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
//        return true;
//    }
//
//    @Override
//    public boolean checkTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
//        return true;
//    }
//
//    @Override
//    public boolean checkSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult, QueryContextParam queryContextParam) {
//        return SignalToolsWrapper.checkZhangTing1Signal(stockBase, opPeriodType, checkResult);
//    }
//
//    @Override
//    public boolean checkSubSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult, QueryContextParam queryContextParam) {
//        return false;
//    }
//
//}