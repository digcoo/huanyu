package com.yh.bigdata.tts.common.indicator;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.DateUtil;
import com.yh.bigdata.tts.common.utils.MathUtil;

import lombok.Data;

@Data
public class  Ticker {
    private long timestamp;
    private double open;
    private double close;
    private double high;
    private double low;
    private long volume;
    private double amount;
    private String day;

	public double getMid() {
		return (getHigh() + getLow()) / 2;
	}
	
	public double getShitiMax() {
		return MathUtil.max(getClose(), getOpen());
	}
	
	public double getShitiMin() {
		return MathUtil.min(getClose(), getOpen());
	}

    public double getShitiRate() {
        return (getClose() - getOpen()) / getOpen();
    }
	
	
	public String getTimestampStr() {
        return DateFormatUtils.format(this.timestamp, "yyyy-MM-dd");
    }


	public static List<Ticker> from(List<Trade> trades) {
        return trades.stream().map(x -> {
        	Ticker ticker = new Ticker();
        	ticker.setClose(x.getClose());
        	ticker.setHigh(x.getHigh());
        	ticker.setLow(x.getLow());
        	ticker.setOpen(x.getOpen());
            ticker.setVolume(x.getVolume());
            ticker.setAmount(x.getAmount());
        	ticker.setTimestamp(DateUtil.parseDate(x.getDay()).getTime());
        	ticker.setDay(x.getDay());
        	return ticker;
        }).collect(Collectors.toList());
        
    }

    public Trade toTrade() {
        Trade trade = new Trade();
        trade.setClose(this.close);
        trade.setHigh(this.high);
        trade.setLow(this.low);
        trade.setOpen(this.open);
        trade.setDay(this.day);
        trade.setVolume(this.volume);
        trade.setAmount(this.amount);
        return trade;
    }
}