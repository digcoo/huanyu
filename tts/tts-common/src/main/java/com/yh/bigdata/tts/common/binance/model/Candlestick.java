package com.yh.bigdata.tts.common.binance.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.utils.DateUtil;
import com.yh.bigdata.tts.common.utils.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Candlestick implements Serializable {
    private String symbol;
    private String name;
    private boolean st;
    private PeriodTypeEnum periodType;
    private long timestamp;
    private long openTime;
    private long closeTime;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;
    private BigDecimal amount;
    private int numberOfTrades;
    private BigDecimal takerBuyBaseAssetVolume;
    private BigDecimal takerBuyQuoteAssetVolume;
    private boolean isClosed;
    private BigDecimal lastClose;
    private BigDecimal changeRate;
    private BigDecimal shockRate;
    private BigDecimal turnoverRate;   //换手率
    private OrderBook orderBook;

    private BigDecimal ma5;
    private BigDecimal ma10;
    private BigDecimal ma20;
    private BigDecimal ma30;
    private BigDecimal ma60;

    private BigDecimal dea;
    private BigDecimal dif;
    private BigDecimal macd;

    @JsonIgnore
    public String getOpenTimeStr() {
        return DateFormatUtils.format(this.openTime, "yyyy-MM-dd HH:mm:ss");
    }

    public String getDay() {
        return getOpenTimeStr();
    }

    public void setDay(String day) throws ParseException {
        if (day.length() > 12) {
            this.setOpenTime(DateUtil.parseDate(day, DateUtil.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS).getTime());
        }else {
            this.setOpenTime(DateUtil.parseDate(day, DateUtil.DATE_FORMAT_YYYY_MM_DD).getTime());
        }
    }


    @JsonIgnore
    public BigDecimal getShiTiMax() {
        return MathUtil.max(this.open, this.close);
    }

    @JsonIgnore
    public BigDecimal getShiTiMin() {
        return MathUtil.min(this.open, this.close);
    }

}
