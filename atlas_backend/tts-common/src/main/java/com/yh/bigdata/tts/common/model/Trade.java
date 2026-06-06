package com.yh.bigdata.tts.common.model;

import java.text.ParseException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.alibaba.fastjson.JSON;
import com.yh.bigdata.tts.common.constants.MATypeEnum;
import com.yh.bigdata.tts.common.utils.DateUtil;

import lombok.Builder;

@Data
@Slf4j
public class Trade extends StockBase implements Comparable<Trade> {

	private Double ma_price5;

	private Double ma_price10;

	private Double ma_price20;

	private Double ma_price30;

	private Double close;

	public Trade() {
		
	}
	
	public Trade(Double open, Double close, Double high, Double low) {
		setOpen(open);
		setClose(close);
		setHigh(high);
		setLow(low);
	}

	public void setClose(Double close) {
        this.close = close;
		setTrade(close);
	}

    public Double getClose() {
		return getTrade();
	}

	public void setMa_price5(Double ma_price5) {
		this.ma_price5 = ma_price5;
		setMa5(ma_price5);
	}

	public void setMa_price10(Double ma_price10) {
		this.ma_price10 = ma_price10;
		setMa10(ma_price10);
	}

	public void setMa_price20(Double ma_price20) {
		this.ma_price20 = ma_price20;
		setMa20(ma_price20);
	}

    public void setMa_price30(Double ma_price30) {
		this.ma_price30 = ma_price30;
		setMa30(ma_price30);
	}
	
	public Long getStartTime() {
		try {
			String time = getDay();	
			if(time.length() >= 12) {
				return DateUtils.parseDate(time, "yyyy-MM-dd HH:mm:ss").getTime();
			}else {
				return DateUtils.parseDate(time, "yyyy-MM-dd").getTime();	
			}			
		} catch (ParseException ex) {
			log.error("parse date error...", ex);
		}	
		return null;
	}

	public Double getMA(MATypeEnum ma) {
		switch (ma) {
		case MA1:
			return getTrade();
		case MA5:
			return getMa5();
		case MA10:
			return getMa10();
		case MA20:
			return getMa20();
		case MA30:
			return getMa30();
		case MA60:
			return getMa60();
		case MA120:
			return getMa120();

		default:
			break;
		}
		return null;
	}
//
//	@Override
//	public String toString() {
//		return "trade : " + JSON.toJSONString(this);
//	}
	
	public void replace(Trade obj) {
		setCode(obj.getCode());
		setOpen(obj.getOpen());
		setHigh(obj.getHigh());
		setLow(obj.getLow());
		setTrade(obj.getTrade());
		setClose(obj.getClose());
		setMa5(obj.getMa5());
		setMa10(obj.getMa10());
		setMa20(obj.getMa20());
		setMa30(obj.getMa30());
		setMa60(obj.getMa60());
		setMa120(obj.getMa120());
		setAmount(obj.getAmount());
		setVolume(obj.getVolume());
		setLastTrade(obj.getLastTrade());
	}

	@Override
	public int compareTo(Trade o) {
		return this.getDay().compareTo(o.getDay());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Trade) {
			Trade stockDay = (Trade)obj;
			return this.getDay().equals(stockDay.getDay()) && this.getCode().equals(stockDay.getCode());
		}
		return false;
	}

}
