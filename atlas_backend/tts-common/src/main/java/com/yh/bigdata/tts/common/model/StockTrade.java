package com.yh.bigdata.tts.common.model;

import java.util.Date;

public class StockTrade {
	
	private Long id;

	private String code;

	private String name;

	private Date buyTime;

	private Double buyPrice;
	
	private Long buyVolume;

	private Date sellTime;

	private Double sellPrice;
	
	private Long sellVolume;

	private Float rate;

	private Boolean isFinish;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Date getBuyTime() {
		return buyTime;
	}

	public void setBuyTime(Date buyTime) {
		this.buyTime = buyTime;
	}

	public Date getSellTime() {
		return sellTime;
	}

	public void setSellTime(Date sellTime) {
		this.sellTime = sellTime;
	}

	public Double getBuyPrice() {
		return buyPrice;
	}

	public void setBuyPrice(Double buyPrice) {
		this.buyPrice = buyPrice;
	}

	public Double getSellPrice() {
		return sellPrice;
	}

	public void setSellPrice(Double sellPrice) {
		this.sellPrice = sellPrice;
	}

	public Float getRate() {
		return rate;
	}

	public void setRate(Float rate) {
		this.rate = rate;
	}

	public Long getBuyVolume() {
		return buyVolume;
	}

	public void setBuyVolume(Long buyVolume) {
		this.buyVolume = buyVolume;
	}

	public Long getSellVolume() {
		return sellVolume;
	}

	public void setSellVolume(Long sellVolume) {
		this.sellVolume = sellVolume;
	}

	public Boolean getIsFinish() {
		return isFinish;
	}

	public void setIsFinish(Boolean isFinish) {
		this.isFinish = isFinish;
	}
	
}