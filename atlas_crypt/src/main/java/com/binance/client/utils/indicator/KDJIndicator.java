package com.binance.client.utils.indicator;

import com.binance.client.utils.DateUtil;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KDJIndicator {

    public static List<KDJPoint> calculateKDJ(List<Ticker> tickers) {
        List<Double> closes = tickers.stream().map(Ticker::getClose).collect(Collectors.toList());
        List<Double> highs = tickers.stream().map(Ticker::getHigh).collect(Collectors.toList());
        List<Double> lows = tickers.stream().map(Ticker::getLow).collect(Collectors.toList());
        return calculateKDJ(tickers, closes, highs, lows, 9, 3, 3);
    }

    // KDJ计算函数
    private static List<KDJPoint> calculateKDJ(List<Ticker> tickers, List<Double> closes, List<Double> highs, List<Double> lows, int n, int m1, int m2) {
        if (closes.size() < n || highs.size() < n || lows.size() < n) {
//            throw new IllegalArgumentException("数据长度不足");
            return Collections.emptyList();
        }

        List<KDJPoint> kdjPoints = new ArrayList<>(closes.size());

        // 初始值
        double k = 50.0;
        double d = 50.0;

        for (int i = n - 1; i < closes.size(); i++) {
            // 计算RSV
            double close = closes.get(i);
            double high = getMax(highs, i - n + 1, i);
            double low = getMin(lows, i - n + 1, i);
            double rsv = (close - low) / (high - low) * 100;

            // 计算K值
            k = (2.0 / 3) * k + (1.0 / 3) * rsv;

            // 计算D值
            d = (2.0 / 3) * d + (1.0 / 3) * k;

            // 计算J值
            double j = 3 * k - 2 * d;

            KDJPoint.KDJPointBuilder KDJPointBuilder = KDJPoint.builder()
                    .ticker(tickers.get(i))
                    .k(k)
                    .d(d)
                    .j(j)
                    .ifOver(false)
                    .ifGoldCross(false);
                    ;
            if (k > d) {
                KDJPointBuilder.ifOver(true);
            }

            if (i > n - 1) {
                if((kdjPoints.get(i - n).getK() < kdjPoints.get(i - n).getD() && k > d)
                        || (kdjPoints.get(i - n).getK() > kdjPoints.get(i - n).getD() && k < d)){
                    KDJPointBuilder.ifGoldCross(true);
                }
            }

            kdjPoints.add(KDJPointBuilder.build());
        }
        return kdjPoints;
    }

    // 获取最大值
    private static double getMax(List<Double> data, int start, int end) {
        double max = data.get(start);
        for (int i = start + 1; i <= end; i++) {
            if (data.get(i) > max) {
                max = data.get(i);
            }
        }
        return max;
    }

    // 获取最小值
    private static double getMin(List<Double> data, int start, int end) {
        double min = data.get(start);
        for (int i = start + 1; i <= end; i++) {
            if (data.get(i) < min) {
                min = data.get(i);
            }
        }
        return min;
    }

    public static boolean isRise(List<Ticker> tickers) {
        List<KDJPoint> kdjPoints = calculateKDJ(tickers);
        KDJPoint kdjPoint0 = kdjPoints.get(kdjPoints.size() - 1);
        KDJPoint kdjPoint1 = kdjPoints.get(kdjPoints.size() - 2);
        if (kdjPoint0.getJ() > kdjPoint1.getJ()) {
            return true;
        }
        return false;
    }

    @Data
    @Builder
    public static class KDJPoint {
        private Ticker ticker;
        private double k;
        private double d;
        private double j;
        private boolean ifOver;
        private boolean ifGoldCross;

        private String getTimestampStr() {
            return DateUtil.formatDate(this.ticker.getTimestamp(), "yyyy-MM-dd HH:mm:ss");
        }
    }

    public static void main(String[] args) throws IOException {

        List<Ticker> tickers = XueQiuUtils.getXueQiuJson();

        List<KDJPoint> kdjPoints = calculateKDJ(tickers);
        for (int i = 0; i < kdjPoints.size(); i++) {
            if (kdjPoints.get(i).ifGoldCross) {
                System.out.println("K值上穿D值，发生在" + kdjPoints.get(i).getTimestampStr());
            }
        }

    }


}
