package com.binance.client.channel.model;

import com.binance.client.utils.GsonUtil;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class BinanceMessage {
    public static String subscribeTickers(List<String> symbols, int requestId) {
        String symbolsArrayString = GsonUtil.gson().toJson(symbols.stream().map(item -> item.toLowerCase() + "@bookTicker").collect(Collectors.toList()));
        return "{\"method\":\"SUBSCRIBE\",\"params\":" + symbolsArrayString + ", \"id\":" + requestId + "}";
    }

    public static String subscribeTrades(List<String> symbols, int requestId) {
        String symbolsArrayString = GsonUtil.gson().toJson(symbols.stream().map(item -> item.toLowerCase() + "@trade").collect(Collectors.toList()));
        return "{\"method\":\"SUBSCRIBE\",\"params\":" + symbolsArrayString + ", \"id\":" + requestId + "}";
    }

    public static String subscribe(List<String> symbols, int requestId) {
        List<String> tradeSubscribes = new java.util.ArrayList<>(symbols.stream().map(item -> item.toLowerCase() + "@trade").collect(Collectors.toList()));
        List<String> tickerSubscribes = symbols.stream().map(item -> item.toLowerCase() + "@bookTicker").collect(Collectors.toList());
        tradeSubscribes.addAll(tickerSubscribes);
        String symbolsArrayString = GsonUtil.gson().toJson(tradeSubscribes);
        return "{\"method\":\"SUBSCRIBE\",\"params\":" + symbolsArrayString + ", \"id\":" + requestId + "}";
    }

    public static String listSubscription() {
        return "{\"method\":\"LIST_SUBSCRIPTIONS\",\"id\": 0}";
    }

    public static String ping() {
        return "ping";
    }

    @Data
    public static class Trade {

        @SerializedName("s")
        private String symbol;
        @SerializedName("p")
        private BigDecimal price;
        @SerializedName("T")
        private Long time;

    }

    @Data
    public static class BookTicker {

        @SerializedName("s")
        private String symbol;
        @SerializedName("a")
        private BigDecimal bestAsk;
        @SerializedName("b")
        private BigDecimal bestBid;

    }

    @Data
    public static class Symbol {

        private String symbol;
        private String status;

    }
}
