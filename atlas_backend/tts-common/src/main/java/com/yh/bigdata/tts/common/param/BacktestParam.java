package com.yh.bigdata.tts.common.param;

import lombok.Data;

/**
 * @author duyp
 * 
 * @date 2019/01/15
 * 
 * @comment
 */
@Data
public class BacktestParam {
	
	private Integer days;
	
	private String strategy;

	public Integer getDays() {
		return days;
	}

	public void setDays(Integer days) {
		this.days = days;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	} 
	
	

}