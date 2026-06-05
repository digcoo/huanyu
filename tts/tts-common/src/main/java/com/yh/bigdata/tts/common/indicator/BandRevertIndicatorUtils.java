package com.yh.bigdata.tts.common.indicator;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

@Slf4j
public final class BandRevertIndicatorUtils {

    public static BandSegment getLastBandSegmentWithCurrent(PeriodTypeEnum trendPeriodType,List<Trade> trades, int lastN) {
        List<BandSegment> bandSegmentListWithCurrent = getBandSegmentListWithCurrent(trendPeriodType, trades);
        return bandSegmentListWithCurrent.size() > lastN? bandSegmentListWithCurrent.get(bandSegmentListWithCurrent.size() - lastN - 1): null;
    }

    public static List<BandSegment> getBandSegmentListWithCurrent(PeriodTypeEnum trendPeriodType,List<Trade> trades) {
        trades = trades.subList(0, trades.size() - 1);      //不包含当前trade
        if (trades.size() < 2) {
            return null;
        }

        List<BandSegment> bandSegmentList = getBandSegmentList(trendPeriodType, trades);
        List<BandSegment> targetList = bandSegmentList;


        targetList.removeIf(x -> MathUtil.min(x.getBandShiTiRate(), x.getBandChangeRate()) > -trendPeriodType.getBandShiTiRate());

        return targetList;
    }
//
//    public static BandSegment getLastBandSegmentWithoutCurrent(PeriodTypeEnum trendPeriodType,List<Trade> trades, int lastN) {
//        List<BandSegment> bandSegmentListWithoutCurrent = getBandSegmentListWithoutCurrent(trendPeriodType, trades);
//        return bandSegmentListWithoutCurrent.size() > lastN? bandSegmentListWithoutCurrent.get(bandSegmentListWithoutCurrent.size() - lastN - 1): null;
//    }
//
//    public static List<BandSegment> getBandSegmentListWithoutCurrent(PeriodTypeEnum trendPeriodType,List<Trade> trades) {
//        if (trades.size() < 2) {
//            return null;
//        }
//
//        List<BandSegment> targetList = new ArrayList<>();
//        Trade currentTrade0 = trades.get(trades.size() - 1);
//        Trade currentTrade1 = trades.get(trades.size() - 2);
//        List<BandSegment> bandSegmentList = getBandSegmentList(trendPeriodType, trades);
//        if (currentTrade1.isDecline()) {
//            targetList = bandSegmentList.subList(0, bandSegmentList.size() - 1);
//        }else {
//            if (currentTrade0.isDecline()) {
//                targetList = bandSegmentList.subList(0, bandSegmentList.size() - 1);
//            }else {
//                targetList = bandSegmentList;
//            }
//        }
//
//        targetList.removeIf(x -> MathUtil.min(x.getBandShiTiRate(), x.getBandChangeRate()) > -trendPeriodType.getTiZiShiTiRate());
//
//        return targetList;
//    }

    private static List<BandSegment> getBandSegmentList(PeriodTypeEnum trendPeriodType, List<Trade> trades) {
        List<BandSegment> bandSegments = new ArrayList<>();
        if (CollectionUtils.isEmpty(trades)) {
            return bandSegments;
        }

        int i = 1;
        while (i < trades.size()) {
            Trade current = trades.get(i);

            //跳过非上涨交易日
            if (!current.isDecline()) {
                i++;
                continue;
            }

            // 找到上涨起始日，初始化波段统计
            Trade bandStart = current;
            Trade bandEnd = current;
            Trade preBandStart = trades.get(i - 1);
            Double high = current.getHigh();
            Double low = current.getLow();

            //向后扩展，合并连续上涨的交易日
            int j = i + 1;

            Set<Trade> rangeTrades = new HashSet<>();
            rangeTrades.add(current);
            //上涨途中的调整条件：shiTi < 0 or changeRate < 0
            //下跌途中的反转条件：shiTi >= 0 or changeRate >= 0
//            while(j < trades.size() && ((trades.get(j-1).getChangeRate() > 0 && (trades.get(j).getShitiRate() >= 0 && trades.get(j).getChangeRate() > 0))
//                    || (trades.get(j-1).getChangeRate() <= 0 && (trades.get(j).getChangeRate() > 0 || trades.get(j).getShitiRate() >= 0)))) {

            while(j < trades.size() && ((trades.get(j-1).getChangeRate() < 0 && (trades.get(j).getShitiRate() <= 0 || trades.get(j).getChangeRate() < 0))
                    || (trades.get(j-1).getChangeRate() >= 0 && (trades.get(j).getShitiRate() <= 0)))) {

                Trade next = trades.get(j);
                bandEnd = next;

                if (next.getHigh() > high){
                    high = next.getHigh();
                }

                if (next.getLow() < low) {
                    low = next.getLow();
                }

                rangeTrades.add(next);
                j++;
            }

            // 过滤太小的波段
            BandSegment newBandSegment = new BandSegment(bandStart, bandEnd, high, low, new ArrayList<>(rangeTrades), preBandStart);            bandSegments.add(newBandSegment);

            // 从下一个未处理的交易日继续
            i = j;

        }
        
        return bandSegments;
    }

    @Data
    @AllArgsConstructor
    public static class BandSegment {

        private Trade firstTrade;
        private Trade lastTrade;

        private Double high;
        private Double low;
        private List<Trade> rangeTrades;
        private Trade preFirstTrade;
//
//        private PeriodTypeEnum periodType;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            BandSegment bandSegment = (BandSegment) o;
            return Objects.equals(bandSegment.getFirstTrade().getDay(), firstTrade.getDay()) && Objects.equals(bandSegment.getLastTrade().getDay(), lastTrade.getDay());
        }

        public double getBandChangeRate() {
            return (lastTrade.getClose() - firstTrade.getLastTrade() ) / firstTrade.getLastTrade();
        }

        public double getBandShiTiRate() {
            return (lastTrade.getClose() - firstTrade.getOpen() ) / firstTrade.getOpen();
        }

        public int hashCode() {
            return Objects.hash(firstTrade, lastTrade);
        }

    }


    public static void main(String[] args) throws IOException {
        PeriodTypeEnum trendPeriodType = PeriodTypeEnum.WEEK;

        List<Trade> dayTrades = XueQiuUtils.getXueQiuJson("sh600495", trendPeriodType.getCode());
        dayTrades = dayTrades.subList(dayTrades.size() - 60, dayTrades.size());

//        List<BandSegment> getBandSegmentList = BandIndicatorUtils.getBandSegmentList(dayTrades);
//        for (BandSegment bandSegment : getBandSegmentList) {
//            log.info("bandSegment: {} - {}", bandSegment.getFirstTrade().getDay(), bandSegment.getLastTrade().getDay());
//        }

        List<BandSegment> bandSegmentListWithoutCurrent = BandRevertIndicatorUtils.getBandSegmentListWithCurrent(trendPeriodType, dayTrades);
        for (BandSegment bandSegment : bandSegmentListWithoutCurrent) {
            log.info("bandSegmentListWithoutCurrent: {} - {}", bandSegment.getFirstTrade().getDay(), bandSegment.getLastTrade().getDay());
        }

//        BandSegment lastBandSegmentWithoutCurrent0 = BandRevertIndicatorUtils.getLastBandSegmentWithCurrent(trendPeriodType, dayTrades, 0);
//        log.info("lastBandSegmentWithoutCurrent0: {} - {}", lastBandSegmentWithoutCurrent0.getFirstTrade().getDay(), lastBandSegmentWithoutCurrent0.getLastTrade().getDay());
//
//        BandSegment lastBandSegmentWithoutCurrent1 = BandIndicatorUtils.getLastBandSegmentWithoutCurrent(trendPeriodType, dayTrades, 1);
//        log.info("lastBandSegmentWithoutCurrent1: {} - {}", lastBandSegmentWithoutCurrent1.getFirstTrade().getDay(), lastBandSegmentWithoutCurrent1.getLastTrade().getDay());

    }
}
