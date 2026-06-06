package com.yh.bigdata.tts.common.model;

public class StockQuarter extends Trade {


	private Integer sourceType;

	public Integer getSourceType() {
		return sourceType;
	}

	public void setSourceType(Integer sourceType) {
		this.sourceType = sourceType;
	}
//	
//	
//	@Override
//	public int compareTo(StockQuarter o) {
//		try {
//			if (DateUtils.parseDate(this.getDay(), "yyyy-MM-dd").getTime() > DateUtils.parseDate(o.getDay(), "yyyy-MM-dd").getTime()) {
//				return 1;
//			}else{
//				return -1;
//			}
//		} catch (ParseException e) {
//			throw new IllegalArgumentException("stock quarter day format illegal");
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
}