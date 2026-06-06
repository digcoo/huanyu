package com.yh.bigdata.tts.common.model;

import org.springframework.beans.BeanUtils;

public class StockDay extends Trade {

	private int dayRank;
	
	public int getDayRank() {
		return dayRank;
	}

	public void setDayRank(int dayRank) {
		this.dayRank = dayRank;
	}
//
//	public Double getZhenfuRate(){
//		return (high - low) / low;
//	}
//	
//	public Double getZhangfuRate(){
//		return lastTrade == null? 0 : (trade - lastTrade) / lastTrade;
//	}
//	
//	public Double getZhangfuRateAbs() {
//		return lastTrade == null? 0 : Math.abs(trade - lastTrade) / lastTrade;
//	}
//	
//	public Double getShitiRateAbs() {
//		return Math.abs(trade - open) / open;
//	}

//	@Override
//	public int compareTo(StockDay o) {
//		try {
//			return DateUtils.parseDate(this.getDay(), "yyyy-MM-dd").getTime() > DateUtils.parseDate(o.getDay(), "yyyy-MM-dd").getTime()? 1 : -1;
//		} catch (ParseException e) {
//			throw new IllegalArgumentException("stock day day format illegal");
//		}
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof StockDay) {
//			StockDay stockDay = (StockDay)obj;
//			return this.getDay().equals(stockDay.getDay()) && this.code.equals(stockDay.getCode());
//		}
//		return false;
//	}
//	
	
	public Trade copy() {
		StockDay stockDay = new StockDay();
		BeanUtils.copyProperties(this, stockDay);
		return stockDay;
	}
}