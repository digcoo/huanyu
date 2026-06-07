package com.yh.bigdata.tts.common.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yh.bigdata.tts.common.constants.QushiLocationTypeEnum;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.Box;
import com.yh.bigdata.tts.common.dto.SumaryPrice;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;

public final class PriceUtil {
	
	
	public static boolean isSameSide(@SuppressWarnings("rawtypes") List trades, Boolean rise) {
		if (rise == null) {
			Trade baseTrade = (Trade)(trades.get(0));
			rise = baseTrade.getClose() > baseTrade.getOpen();
		}
		
		
		for (int i = 0; i < trades.size(); i++) {
			Trade trade = (Trade)(trades.get(i));
			if (rise != trade.getClose() > trade.getOpen()) {
				return false;
			}
		}
		return true;
		
	}
	

	/**
	 * 上涨区间段：
	 * 1、下一个区间段未收盘价未突破上一个高点，则放弃
	 * @param trades
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List<Box> getBoxSegments(List trades, PeriodTypeEnum qushiPeriodTypeEnum) {
		return getBoxSegments(trades, qushiPeriodTypeEnum, true);
	}

	/**
	 * 上涨区间段：
	 * 1、下一个区间段未收盘价未突破上一个高点，则放弃
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List<Box> getBoxSegments(List trades, PeriodTypeEnum qushiPeriodTypeEnum, boolean filterSmall) {
		
		List<Box> targetSegments = Lists.newArrayList();
		

		List<Box> segmentList = Lists.newArrayList();
		
//			System.out.println("============================================" + ((Trade)trades.get(0)).getCode() + "\t" + ((Trade)trades.get(0)).getDay());
		
		int startIndex = 0;
		
		@SuppressWarnings("unchecked")
		List<Trade> newTrades = trades.subList(startIndex, trades.size());
		
		String code = newTrades.get(0).getCode();
		
		
		int head = -1;
		int tail = -1;
		int i = 0;
		//			while(i < trades.size() -2 ) {
		while(i < trades.size()) {
		
			Trade trade = (Trade)(trades.get(i));
			
			if (trade.getChangeRate() >= 0) {
				if(head == -1) {		//定位区间开始位
					head = i;
				}
			} else {
					
				if(head == -1) {		//区间起始位还未确定
					if(trade.getShitiRate() >= 0) {			//定位区间开始位
						head = i;
					}
					i++;
					continue;
				}
				
				Trade headTrade = (Trade)(trades.get(head));
				
				Trade last1Trade = (Trade)(trades.get(i-1));
				Trade next1Trade = i+1 > trades.size()-1 ? null : (Trade)(trades.get(i+1));
				Trade next2Trade = i+2 > trades.size()-1 ? null : (Trade)(trades.get(i+2));
				
				if(trade.getClose() < headTrade.getOpen()  //跌破区间底部，则区间结束
						|| (next1Trade != null && (next1Trade.getClose() < last1Trade.getClose() || next1Trade.getClose() < headTrade.getOpen() )  //下跌之后，后续2个周期价格还未拉回，则区间结束
								)
						) {
					
					tail = i - 1;
					Trade tailTrade = (Trade)(trades.get(tail));
					
		//						System.out.println(headTrade.getDay()+ "\t" + tailTrade.getDay()+ "\t" + (tail - head + 1));
					
					segmentList .add(new Box(code, head, tail, newTrades));
					
					head = -1;
					tail = -1;
				}
				
				if(head == -1) {		//区间起始位还未确定
					if(trade.getShitiRate() >= 0) {			//定位区间开始位
						head = i;
					}
				}
			}
			i++;
		}
		
		if (head > 0) {	//结束之后趋势还未结束：统计最后一个周期，涨则加上，跌则去掉
			int lastIndex = trades.size() - 1;
			Trade trade = (Trade)(trades.get(lastIndex));
			
			if (trade.getChangeRate() >= 0) {
				tail = lastIndex;
			} else {
				tail = lastIndex - 1 < head? head : lastIndex - 1;
			}
			Trade headTrade = (Trade)(trades.get(head));
			Trade tailTrade = (Trade)(trades.get(tail));
			
//				System.out.println(headTrade.getDay()+ "\t" + tailTrade.getDay()+ "\t" + (tail - head + 1));
			
			segmentList.add(new Box(code, head, tail, newTrades));
			
		}
		
		targetSegments = segmentList;
		
		//			for (Segment segment : targetSegments) {
		//				System.out.println(segment.getHeadDay() + "\t" + segment.getTailDay() + "\t" + segment.length());
		//			}
		
		//去除小周期
		//			for (int j = segmentList.size() - 1; j > 0; j--) {
		//				Segment segment = segmentList.get(j);
		//				Segment preSegment = segmentList.get(j-1);
		//				if (segment.getTrade() < preSegment.getTrade() 
		//						&& segment.getOpen() > preSegment.getOpen()
		//						) {
		////					System.out.println("ignore : " + segment.getHeadDay() + "\t" + segment.getTailDay());
		//					continue;
		//				}else {
		//					targetSegments.add(segment);
		//				}
		//			}
		//			Collections.reverse(targetSegments);
			
		
//		return targetSegments.subList(targetSegments .size() <= latestN? 0: (targetSegments .size()-latestN), targetSegments .size());
		return targetSegments;
		
	}
	
	//去除当前区间
	public static List<Box> getBoxSegmentsWioutCurrentSegment(List<Box> segments, @SuppressWarnings("rawtypes") List trades, PeriodTypeEnum qushiPeriodTypeEnum, int latestN) {
		
		List<Box> targetSegments = segments;
		if (!CollectionUtils.isEmpty(segments)) {

			Box lastSegment = segments.get(segments.size() - 1);
			
			Trade lastTrade1 = (Trade)trades.get(trades.size() - 1);
			Trade lastTrade2 = (Trade)trades.get(trades.size() - 2);
			
			
			if(DateUtil.isIn(lastSegment.getHeadDay(), lastSegment.getTailDay(), lastTrade1.getDay())
					|| DateUtil.isIn(lastSegment.getHeadDay(), lastSegment.getTailDay(), lastTrade2.getDay())
					) {
				targetSegments = segments.subList(0, segments.size() - 1);
			}
		}
		
		return targetSegments.subList(targetSegments .size() <= latestN? 0: (targetSegments .size()-latestN), targetSegments .size());
		
	}

}
