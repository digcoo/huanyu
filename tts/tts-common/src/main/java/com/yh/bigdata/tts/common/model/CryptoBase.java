package com.yh.bigdata.tts.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.utils.MathUtil;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class CryptoBase {

	private String symbol;

    private BigDecimal close;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal volume;

    private BigDecimal amount;

    private BigDecimal lastClose;

    private String day;

	private String trendMessage;

	private String signalMessage;


	public CryptoBase() {
	}

	public CryptoBase(String symbol) {
		this.symbol = symbol;
	}

	public String getDay() {
        if (this.day != null && day.contains(".")) {
            this.day = this.day.substring(0, this.day.indexOf("."));
        }
        return day;
    }

	@JSONField(serialize = false)
	@JsonIgnore
	public double getShitiMax() {
		return MathUtil.max(this.close, this.open).doubleValue();
	}

    @JSONField(serialize = false)
    @JsonIgnore
    public double getShitiMin() {
        return MathUtil.min(this.close, this.open).doubleValue();
    }

    @JSONField(serialize = false)
	@JsonIgnore
	public double getShitiMid() {
		return (getShitiMax() + getShitiMin()) / 2;
	}

    @JsonIgnore
    @JSONField(serialize=false, deserialize = false)
    public double getShitiRate(){
        return (close.doubleValue() - open.doubleValue()) / open.doubleValue();
    }

	@JSONField(serialize = false)
	@JsonIgnore
	public double getRealtimeAvg() {
		try {
			return this.amount.doubleValue()/this.volume.doubleValue();
		} catch (Exception e) {
			return Double.MAX_VALUE;
		}
	}

	@JsonIgnore
	@JSONField(serialize=false, deserialize = false)
	public double getChangeRate(){
		return lastClose == null ? 0 : (close.doubleValue() - lastClose.doubleValue()) / lastClose.doubleValue();
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CryptoBase other = (CryptoBase) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}

}