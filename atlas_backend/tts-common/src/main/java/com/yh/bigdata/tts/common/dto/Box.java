package com.yh.bigdata.tts.common.dto;

import java.util.Comparator;
import java.util.List;

import com.yh.bigdata.tts.common.model.Trade;

public class Box {
	
	private String code;

	//上涨区间头
	private int head;
	

	//上涨区间尾
	private int tail;
	
	
	//总区间段
	private List<Trade> allTrades;
	
	public Box() {
		
	}

	public Box(String code, int head, int tail, List<Trade> allTrades) {
		this.code = code;
		this.head = head;
		this.tail = tail;
		this.allTrades = allTrades;
	}
	
	public String getCode() {
		return code;
	}

	public int length() {
		return tail - head + 1;
	}
	
	public Double getHigh() {
//		return allTrades.get(tail).getHigh();
		return getTrades().stream().map(x -> x.getHigh()).max(Comparator.comparing(Double::doubleValue)).get();				
	}

	
	public Double getLow() {
//		return allTrades.get(head).getLow();
//		Double double1 = getTrades().stream().map(x -> x.getLow()).min(Comparator.comparing(Double::doubleValue)).get();
//		return new BigDecimal(double1, 2);
		return getTrades().stream().map(x -> x.getLow()).min(Comparator.comparing(Double::doubleValue)).get();
	}
	

	public Double getOpen() {
//		return allTrades.get(head).getOpen();
		return getTrades().stream().map(x -> x.getOpen()).min(Comparator.comparing(Double::doubleValue)).get();				
	}
	

	public Double getClose() {
		return getTrades().stream().map(x -> x.getClose()).max(Comparator.comparing(Double::doubleValue)).get();
	}

	@Deprecated
	public Double getTrade() {
		return getClose();
	}
	
	public Double getShitiMax() {
		return getTrades().stream().map(x -> x.getClose()).max(Comparator.comparing(Double::doubleValue)).get();
	}

	public Double getShitiRate() {
		return (getClose() - getOpen()) / getOpen();
	}

	public Double getZhenfuRate() {
		return (getHigh() - getLow()) / getLow();
	}
	
	public String getHeadDay() {
		return allTrades.get(head).getDay();
	}

	public String getTailDay() {
		return allTrades.get(tail).getDay();
	}
	
	public List<Trade> getTrades() {
		return allTrades.subList(head, tail + 1);
	}
}
