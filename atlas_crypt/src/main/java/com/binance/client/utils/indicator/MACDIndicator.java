package com.binance.client.utils.indicator;

import com.alibaba.fastjson.JSON;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.constant.PrivateConfig;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.market.Candlestick;
import com.binance.client.utils.DateUtil;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MACDIndicator {

    public static MACDPoint calculateCurrentMACD(List<Ticker> tickers) {
        List<MACDPoint> macdPoints = calculateMACD(tickers);
        return macdPoints.get(macdPoints.size() - 1);
    }

    public static List<MACDPoint> calculateMACD(List<Ticker> tickers) {
        List<Double> closes = tickers.stream().map(Ticker::getClose).collect(Collectors.toList());
        return calculateMACD(tickers, closes);
    }

    public static MACDPoint getLatestGoldMACDPoint(List<MACDPoint> MACDPoints) {
        MACDPoint latestGoldMACDPoint = null;
        for (int i = MACDPoints.size() - 1; i >= 0; i--) {
            if (MACDPoints.get(i).isIfGoldCross()) {
                latestGoldMACDPoint = MACDPoints.get(i);
                break;
            }
        }
        return latestGoldMACDPoint;
    }

    public static MACDPoint getLatestGreenGoldMACDPoint(List<MACDPoint> MACDPoints) {
        MACDPoint latestGoldMACDPoint = null;
        for (int i = MACDPoints.size() - 1; i >= 0; i--) {
            if (MACDPoints.get(i).isIfGreenGoldCross()) {
                latestGoldMACDPoint = MACDPoints.get(i);
                break;
            }
        }
        return latestGoldMACDPoint;
    }

    public static MACDPoint getLatestRedGoldMACDPoint(List<MACDPoint> MACDPoints) {
        MACDPoint latestGoldMACDPoint = null;
        for (int i = MACDPoints.size() - 1; i >= 0; i--) {
            if (MACDPoints.get(i).isIfRedGoldCross()) {
                latestGoldMACDPoint = MACDPoints.get(i);
                break;
            }
        }
        return latestGoldMACDPoint;
    }

    // 计算 EMA 指数移动平均线
    private static double calculateEMA(double previousEMA, double currentPrice, int period) {
        double multiplier = 2.0 / (period + 1);
        return (currentPrice - previousEMA) * multiplier + previousEMA;
    }

    // 计算 MACD 指标
    private static List<MACDPoint> calculateMACD(List<Ticker> tickers, List<Double> closes) {
        if (closes == null || closes.size() < 26) {
//            throw new IllegalArgumentException("数据点不足，至少需要26个数据点来计算 MACD");
            return Collections.emptyList();
        }

        List<MACDPoint> macdPoints = new ArrayList<>(closes.size());

        double ema12 = closes.get(0);
        double ema26 = closes.get(0);
        double dea = 0;

        for (int i = 0; i < closes.size(); i++) {
            double price = closes.get(i);

            // 计算 12 日 EMA 和 26 日 EMA
            if (i == 0) {
                ema12 = price;
                ema26 = price;
            } else {
                ema12 = calculateEMA(ema12, price, 12);
                ema26 = calculateEMA(ema26, price, 26);
            }

            double dif = ema12 - ema26; // 计算 DIF
            dea = calculateEMA(dea, dif, 9); // 计算 DEA
            double macd = 2 * (dif - dea); // 计算 MACD 柱状图


            MACDPoint.MACDPointBuilder macdPointBuilder = MACDPoint.builder()
                    .ticker(tickers.get(i))
                    .dif(dif)
                    .dea(dea)
                    .macd(macd)
                    .ifOver(false)
                    .ifGoldCross(false);
            ;
            if (macd > 0) {
                macdPointBuilder.ifOver(true);
            }

            if (i > 0) {
                if (macdPoints.get(i - 1).getMacd() < 0 && macd > 0) {
                    macdPointBuilder.ifGoldCross(true);
                    macdPointBuilder.ifGreenGoldCross(false);
                } else if (macdPoints.get(i - 1).getMacd() > 0 && macd < 0) {
                    macdPointBuilder.ifGoldCross(true);
                    macdPointBuilder.ifGreenGoldCross(true);
                }

            }

            macdPoints.add(macdPointBuilder.build());
        }

        return macdPoints;
    }



    @Data
    @Builder
    public static class MACDPoint {
        private Ticker ticker;
        private double dif;
        private double dea;
        private double macd;
        private boolean ifOver;
        private boolean ifGoldCross;
        private boolean ifGreenGoldCross;

        public boolean isIfGreenGoldCross() {
            return ifGoldCross && ifGreenGoldCross;
        }

        public boolean isIfRedGoldCross() {
            return ifGoldCross && !ifGreenGoldCross;
        }

        private String getTimestampStr() {
            return DateUtil.formatDate(this.ticker.getTimestamp(), "yyyy-MM-dd HH:mm:ss");
        }

    }

    public static void main(String[] args) throws IOException {


        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                new RequestOptions());
        String symbol = "BTCUSDT";
        int KLINE_LIMIT = 1500;
        List<Candlestick> candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MIN30.getInterval(), null, null, KLINE_LIMIT);

        List<MACDPoint> macdPoints = calculateMACD(Ticker.from(candlesticks));
        for (int i = 0; i < macdPoints.size(); i++) {
            if (macdPoints.get(i).ifGoldCross) {
                System.out.println("MACD 红色柱，发生在" + macdPoints.get(i).getTimestampStr() + "\t" + JSON.toJSONString(macdPoints.get(i).getTicker()));
            }
        }
    }
}
