package com.binance.client.utils.indicator;

import com.binance.client.model.market.Candlestick;
import com.binance.client.utils.DateUtil;
import com.binance.client.utils.MathUtil;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class  Ticker {
    private long timestamp;
    private double open;
    private double close;
    private double high;
    private double low;

    public String getTimestampStr() {
        return DateUtil.formatDate(this.timestamp, "yyyy-MM-dd HH:mm:ss");
    }

    public double getShiTiMax() {
        return MathUtil.max(open, close);
    }

    public double getShiTiMin() {
        return MathUtil.min(open, close);
    }

    public static List<Ticker> from(List<Candlestick> candlesticks) {
        return candlesticks.stream().map(x -> Ticker.builder()
                .close(x.getClose().doubleValue())
                .high(x.getHigh().doubleValue())
                .low(x.getLow().doubleValue())
                .open(x.getOpen().doubleValue())
                .timestamp(x.getOpenTime())
                .build()).collect(Collectors.toList());
    }
}