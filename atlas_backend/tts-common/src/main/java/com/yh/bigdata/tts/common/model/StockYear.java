package com.yh.bigdata.tts.common.model;

import org.springframework.beans.BeanUtils;

import com.alibaba.fastjson.JSON;

public class StockYear extends Trade {

	private Integer sourceType;
	

	public Integer getSourceType() {
		return sourceType;
	}

	public void setSourceType(Integer sourceType) {
		this.sourceType = sourceType;
	}
//
//	@Override
//	public int compareTo(StockYear o) {
//		try {
//			return DateUtils.parseDate(this.getDay(), "yyyy-MM-dd").getTime() > DateUtils.parseDate(o.getDay(), "yyyy-MM-dd").getTime()? 1 : -1;
//		} catch (ParseException e) {
//			throw new IllegalArgumentException("stock year day format illegal");
//		}
//	}
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof StockDay) {
//			StockDay stockDay = (StockDay)obj;
//			return this.getDay().equals(stockDay.getDay()) && this.code.equals(stockDay.getCode());
//		}
//		return false;
//	}
	
	public Trade copy() {
		StockYear stockYear = new StockYear();
		BeanUtils.copyProperties(this, stockYear);
		return stockYear;
	}

	@Override
	public String toString() {
		return "StockYear : " + JSON.toJSONString(this);
	}	
	
	
}