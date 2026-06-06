package com.yh.bigdata.tts.common.model;

import org.springframework.beans.BeanUtils;

public class StockWeek extends Trade {

	
	private Integer sourceType;

	
	public Integer getSourceType() {
		return sourceType;
	}

	public void setSourceType(Integer sourceType) {
		this.sourceType = sourceType;
	}
	
//
//	@Override
//	public int compareTo(StockWeek o) {
//		try {
//			return DateUtils.parseDate(this.getDay(), "yyyy-MM-dd").getTime() > DateUtils.parseDate(o.getDay(), "yyyy-MM-dd").getTime()? 1 : -1;
//		} catch (ParseException e) {
//			throw new IllegalArgumentException("stock week day format illegal");
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
	
	public Trade copy() {
		StockWeek stockWeek = new StockWeek();
		BeanUtils.copyProperties(this, stockWeek);
		return stockWeek;
	}
}