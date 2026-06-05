//package com.yh.bigdata.tts.spider.strategy.group.backcross;
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
// * 突破梯子Mid
// */
//@Slf4j
//@Component
//public class CrossTiZiMinOrLowStrategy extends AbstractStrategy {
//
//
//    @Override
//    public StrategyTypeEnum getStrategy() {
//        return StrategyTypeEnum.CROSS_TIZI_MIN;
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
//    public boolean checkNoRisk(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
//        return true;
//    }
//
//    /**
//     * 试盘信号 or 拉升信号
//     */
//    public boolean checkPriority(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
//        return true;
//    }
//
//    //Kline: 右侧（顶脱离、底支撑）、梯子（半山腰|脱离）、深坑、上移
//    public boolean checkTrend(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, CheckResult checkResult) {
//        return true;
//    }
//
//    @Override
//    public boolean checkSignal(StockBase stockBase, List<PeriodTypeEnum> trendPeriodTypes, PeriodTypeEnum opPeriodType, CheckResult checkResult, QueryContextParam queryContextParam) {
////        trendPeriodTypes = List.of(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH);
//        trendPeriodTypes = List.of(PeriodTypeEnum.WEEK);
//
//        return SignalToolsWrapper.checkCrossTiZiMinOrLowSignal(stockBase, trendPeriodTypes, opPeriodType, checkResult);
//    }
//
//}