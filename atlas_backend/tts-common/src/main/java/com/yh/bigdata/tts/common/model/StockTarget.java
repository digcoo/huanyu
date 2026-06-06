package com.yh.bigdata.tts.common.model;

import java.text.ParseException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockTarget implements Comparable<StockTarget>{

	private String code;

	private String day;

	private String name;

	private String strategy;

	private double close;

	private boolean newFlag;

	private transient String trendMessage;

	private transient String signalMessage;

	private double changeRate;

    private String mainBusiness;

	@Override
	public int compareTo(StockTarget o) {
		try {
			return (int)(DateUtils.parseDate(this.day, "yyyy-MM-dd").getTime() - DateUtils.parseDate(o.getDay(), "yyyy-MM-dd").getTime());
		} catch (ParseException e) {
			throw new IllegalArgumentException("stock target day format illegal");
		}
	}
	
}