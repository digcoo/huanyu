package com.yh.bigdata.tts.common.indicator;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.yh.bigdata.tts.common.utils.DateUtil.DATE_FORMAT_YYYY_MM_DD;

public final class RSIIndicatorUtils {
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_YYYY_MM_DD);

    public static List<RSIPoint> calculateRSI(List<Ticker> tickers) {
        if (!CollectionUtils.isEmpty(tickers) || tickers.size() > 24) {
            List<RSIPoint> rsiPoints = new ArrayList<>();
            for (int i = 24; i < tickers.size(); i++) {
                BarSeries series = new BaseBarSeriesBuilder().withName("calculateRSI").build();
                List<Ticker> tmpTickers = tickers.subList(0, i);
                for (Ticker ticker: tmpTickers) {
                    series.addBar(Duration.ofDays(1),
                            LocalDate.parse(ticker.getDay(), formatter).atStartOfDay(ZoneId.systemDefault()),
                            ticker.getOpen(), ticker.getHigh(), ticker.getLow(), ticker.getOpen(), ticker.getVolume());
                }
                ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
                RSIIndicator rsi1 = new RSIIndicator(closePrice, 6);
                RSIIndicator rsi2 = new RSIIndicator(closePrice, 12);
                RSIIndicator rsi3 = new RSIIndicator(closePrice, 24);
                rsiPoints.add(RSIPoint.builder()
                        .rsi1(rsi1.getValue(rsi1.getBarSeries().getEndIndex()).doubleValue())
                        .rsi2(rsi2.getValue(rsi2.getBarSeries().getEndIndex()).doubleValue())
                        .rsi3(rsi3.getValue(rsi3.getBarSeries().getEndIndex()).doubleValue())
                        .build());
            }
            return rsiPoints;
        }
        return null;
    }


    @Data
    @Builder(toBuilder = true)
    public static class RSIPoint {
        private double rsi1;
        private double rsi2;
        private double rsi3;
        private Ticker ticker;
        private Ticker preTicker;

        private String getTimestampStr() {
            return DateFormatUtils.format(this.ticker.getTimestamp(), "yyyy-MM-dd");
        }
    }
}
