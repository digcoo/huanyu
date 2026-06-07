package com.yh.bigdata.tts.common.model;

import java.text.ParseException;

import org.apache.commons.lang3.time.DateUtils;

public class StockCapital implements Comparable<StockCapital>{
	
	protected String code;

	protected String name;

	protected String day;
	
	protected Double zhuli;
	
	protected Double close;
	
	protected Double changeRadio;

	protected Double zhuliRatio;

	protected Double extraLarge;

	protected Double extraLargeRatio;

	protected Double large;

	protected Double largeRatio;

	protected Double middle;

	protected Double middleRatio;

	protected Double small;

	protected Double smallRatio;
	
	
	@Override
	public int compareTo(StockCapital o) {
		try {
			if (DateUtils.parseDate(this.getDay(), "yyyy-MM-dd").getTime() > DateUtils.parseDate(o.getDay(), "yyyy-MM-dd").getTime()) {
				return 1;
			}else{
				return -1;
			}
		} catch (ParseException e) {
			throw new IllegalArgumentException("stock day day format illegal");
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StockCapital) {
			StockCapital stockDay = (StockCapital)obj;
			return this.getDay().equals(stockDay.getDay()) && this.code.equals(stockDay.getCode());
		}
		return false;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public Double getZhuli() {
		return zhuli;
	}

	public void setZhuli(Double zhuli) {
		this.zhuli = zhuli;
	}

	public Double getZhuliRatio() {
		return zhuliRatio;
	}

	public void setZhuliRatio(Double zhuliRatio) {
		this.zhuliRatio = zhuliRatio;
	}

	public Double getExtraLarge() {
		return extraLarge;
	}

	public void setExtraLarge(Double extraLarge) {
		this.extraLarge = extraLarge;
	}

	public Double getExtraLargeRatio() {
		return extraLargeRatio;
	}

	public void setExtraLargeRatio(Double extraLargeRatio) {
		this.extraLargeRatio = extraLargeRatio;
	}

	public Double getLarge() {
		return large;
	}

	public void setLarge(Double large) {
		this.large = large;
	}

	public Double getLargeRatio() {
		return largeRatio;
	}

	public void setLargeRatio(Double largeRatio) {
		this.largeRatio = largeRatio;
	}

	public Double getMiddle() {
		return middle;
	}

	public void setMiddle(Double middle) {
		this.middle = middle;
	}

	public Double getMiddleRatio() {
		return middleRatio;
	}

	public void setMiddleRatio(Double middleRatio) {
		this.middleRatio = middleRatio;
	}

	public Double getSmall() {
		return small;
	}

	public void setSmall(Double small) {
		this.small = small;
	}

	public Double getSmallRatio() {
		return smallRatio;
	}

	public void setSmallRatio(Double smallRatio) {
		this.smallRatio = smallRatio;
	}

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	@Deprecated
	public Double getTrade() {
		return close;
	}

	@Deprecated
	public void setTrade(Double trade) {
		this.close = trade;
	}

	public Double getChangeRadio() {
		return changeRadio;
	}

	public void setChangeRadio(Double changeRadio) {
		this.changeRadio = changeRadio;
	}
	
}