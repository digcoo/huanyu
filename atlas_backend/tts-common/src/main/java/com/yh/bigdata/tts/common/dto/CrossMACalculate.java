package com.yh.bigdata.tts.common.dto;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.javassist.CtBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.yh.bigdata.tts.common.constants.MATypeEnum;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.MACross;
import com.yh.bigdata.tts.common.model.MALine;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.DateUtil;


/**
 * 计算所有的MA交叉，包含MA5的交叉
 * @author junifer
 *
 */
@Slf4j
public class CrossMACalculate {

	private StockBase stockBase;
	private List<PeriodTradeDTO> periodTradeDTOs;
	
	private List<PeriodCrossMADTO> allCrosses = Lists.newArrayList();
	
	public CrossMACalculate(StockBase stockBase, List<PeriodTradeDTO> periodTradeDTOs) {
		this.stockBase = stockBase;
		this.periodTradeDTOs = periodTradeDTOs;
		calAllMaCross();
		
	}
	
	public boolean ifCrossMACross(List trades) {
//		
//		if("sh605033".equals(((Trade)(trades.get(0))).getCode())) {
//			System.out.println("======");
//		}

		for (Object obj : trades) {
			Trade trade = (Trade)obj;
			
			if(trade.getShitiRate() < 0 && trade.getChangeRate() < 0) {
				continue;
			}
					
			for (PeriodCrossMADTO basePeriodCrossMADTO : this.allCrosses) {
				
				if(basePeriodCrossMADTO.isAfter(trade.getStartTime())) {
					continue;
				}
				
				//时间内，并且价格高于交叉点TODO
//				if(basePeriodCrossMADTO.isBetween(trade.getStartTime())
//						&& basePeriodCrossMADTO.isOverMACross(trade.getClose())) {
//					return true;
//				}
				
				if (ifCrossMACross(trade, basePeriodCrossMADTO)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean ifCrossMACross(Trade trade, PeriodCrossMADTO periodCrossMADTO) {
		
		if(trade.getShitiRate() < 0 && trade.getChangeRate() < 0) {
			return false;
		}
		
		for (MACross baseCross : periodCrossMADTO.getCrossMAs()) {
			
			if(!baseCross.isContain(MATypeEnum.MA5)) {
				continue;
			}
			Double corssPrice = baseCross.getCrossPrice();
			
			if (trade.getPrevClose() < corssPrice
					&& trade.getClose() > corssPrice
					) {
				
				System.out.format("crossMACross : code[%s], MACrossPeriod[%s:%s:%s:%s], crossDay[%s:%s]", 
						stockBase.getCode(), 
						periodCrossMADTO.getPeriodTypeEnum(), periodCrossMADTO.getStartTimeStr(), periodCrossMADTO.getEndTimeStr(), corssPrice, 
						trade.getDay(), trade.getClose()).println();
				
				return true;
				
			}
			
		}
		return false;
	}

	private void calAllMaCross() {
//		if("sh605033".equals(stockBase.getCode())) {
//			System.out.println("======");
//		}
		for (PeriodTradeDTO periodTradeDTO : periodTradeDTOs) {
			PeriodTypeEnum periodTypeEnum = periodTradeDTO.getPeriodTypeEnum();
			List<Trade> allTrades = periodTradeDTO.getAllTrades();
			for (int i = 1; i < allTrades.size() ; i++) {
				Trade ct = allTrades.get(i);
				Trade lt = allTrades.get(i - 1);
				ct.setPreTrade(lt);
				
//				if(ct.getDay().compareTo("2023-08-17") > 0) {
//					System.out.println(ct.getCode() + "\t" + ct.getClose() + "\t" + ct.getDay());
//				}
//				System.out.println(ct.getCode() + "\t" + ct.getDay() + "\t" + ct.getClose() + 
//						"\t" + ct.getMa5() + "\t" + ct.getMa10() + "\t" + ct.getMa20() + "\t" + ct.getMa30());

				
				List<MACross> result = calMaCross(ct, lt);
				if (!CollectionUtils.isEmpty(result)) {
					Long startTime = lt.getStartTime();
					Long endTime = ct.getStartTime();
					allCrosses.add(new PeriodCrossMADTO(periodTypeEnum, ct, result, startTime, endTime));
				}
			}
		}
	}

	private List<MACross> calMaCross(Trade curTarde, Trade lastTrade) {
		if(curTarde == null || lastTrade == null) {
			return Lists.newArrayList();
		}
		
		List<MALine> MALines = Lists.newArrayList();
		if(curTarde.getMa5() != null && lastTrade.getMa5() != null) {
			MALines.add(new MALine(MATypeEnum.MA5, Pair.of(lastTrade.getMa5(), curTarde.getMa5())));
		}
		if(curTarde.getMa10() != null && lastTrade.getMa10() != null) {
			MALines.add(new MALine(MATypeEnum.MA10, Pair.of(lastTrade.getMa10(), curTarde.getMa10())));
		}
		if(curTarde.getMa20() != null && lastTrade.getMa20() != null) {
			MALines.add(new MALine(MATypeEnum.MA20, Pair.of(lastTrade.getMa20(), curTarde.getMa20())));
		}
		if(curTarde.getMa30() != null && lastTrade.getMa30() != null) {
			MALines.add(new MALine(MATypeEnum.MA30, Pair.of(lastTrade.getMa30(), curTarde.getMa30())));
		}
		return isPriceCrossed(MALines);
	}
	
	private List<MACross> isPriceCrossed(List<MALine> maLines) {
		List<MACross> maCrosses = Lists.newArrayList();
		for (int i = 0; i < maLines.size(); i++) {
			for (int j = i+1; j < maLines.size(); j++) {
				MALine maLine1 = maLines.get(i);
				MALine maLine2 = maLines.get(j);
				Pair<Boolean, MACross> priceCrossed = isPriceCrossed(maLine1, maLine2);
				if(priceCrossed.getLeft()) {
					maCrosses.add(priceCrossed.getRight());
				}
			}
		}
		return maCrosses;
	}
	
	private Pair<Boolean, MACross> isPriceCrossed(MALine maLineA, MALine maLineB) {
		
		//maLineB穿maLineA
		if(maLineB.getMALine().getLeft() <= maLineA.getMALine().getLeft() 
				&& maLineB.getMALine().getRight() >= maLineA.getMALine().getRight()) {
			return Pair.of(true, new MACross(maLineA, maLineB));
		} else if(maLineA.getMALine().getLeft() <= maLineB.getMALine().getLeft() 
				&& maLineA.getMALine().getRight() >= maLineB.getMALine().getRight()) {
			return Pair.of(true, new MACross(maLineB, maLineA));	//maLine1穿maLine2
		}
		return Pair.of(false, null);
	}
	
	public static class PeriodCrossMADTO {

		PeriodTypeEnum periodTypeEnum;
		
		Trade trade;
		
		List<MACross> crossMAs;

		Long startTime;
		
		Long endTime;

		public PeriodCrossMADTO(PeriodTypeEnum periodTypeEnum, Trade trade, List<MACross> crossMAs, Long startTime, Long endTime) {
			this.periodTypeEnum = periodTypeEnum;
//			this.trade = trade;
			this.crossMAs = crossMAs;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		public Trade getTrade() {
			return trade;
		}

		public String getStartTimeStr() {
			if (startTime != null) {
				return DateFormatUtils.format(startTime, "yyyy-MM-dd HH:mm:ss");
			}		
			return null;
		}

		public String getEndTimeStr() {
			if (endTime != null) {
				return DateFormatUtils.format(endTime, "yyyy-MM-dd HH:mm:ss");
			}		
			return null;
		}

		public List<MACross> getCrossMAs() {
			return crossMAs;
		}

		public PeriodTypeEnum getPeriodTypeEnum() {
			return periodTypeEnum;
		}
		
		public boolean isBetween(Long time) {
			return time >= startTime && (endTime == null || time < endTime);
		}

		public boolean isAfter(Long time) {
			return startTime > time;
		}
		
		

		//待优化 TODO
		public boolean isOverMACross(double price) {
			for (MACross maCross : crossMAs) {
				if(maCross.isContain(MATypeEnum.MA5) && price > maCross.getCrossPrice()) {
					return true;
				}
			}
			return false;
		}
		
	}
}
