package com.binance.client.channel.service;

import com.binance.client.channel.model.BinanceMessage;
import com.binance.client.utils.GsonUtil;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.internal.ws.RealWebSocket;
import okio.ByteString;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class BinanceDataService extends WebSocketListener {
    private static final String wsUrl = "wss://stream.binance.com:9443/ws";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .pingInterval(10, TimeUnit.SECONDS)
            .build();

    private final Map<String, Boolean> symbolSubscribeMap = Maps.newConcurrentMap();
    private final AtomicInteger requestId = new AtomicInteger(1);

    private boolean destroyed = false;
    private WebSocket rWebSocket;
    private boolean wsConnected;
    private long lastAliveTime = 0;

    public BinanceDataService() {
    }


    public void connectWebSocket() {
        Request request = new Request.Builder()
                .url(wsUrl)
                .build();
        rWebSocket = httpClient.newWebSocket(request, this);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        log.info("Binance WebSocket connection connected. response: {}", getWsResponse(response));
        rWebSocket = webSocket;
        wsConnected = true;
        lastAliveTime = System.currentTimeMillis();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        lastAliveTime = System.currentTimeMillis();
        JsonObject responseJson = JsonParser.parseString(text).getAsJsonObject();
        if (responseJson.has("error")) {
            log.warn("received error message from binance websocket: {}", text);
        } else if (responseJson.has("result")) {
            int id = responseJson.get("id").getAsInt();
            if (id == 0) {
                JsonArray resultJson = responseJson.getAsJsonArray("result");
                List<String> subscribedJson = GsonUtil.gson().fromJson(resultJson, new TypeToken<List<String>>() {
                }.getType());
                for (String subscribed : subscribedJson) {
                    String symbol = subscribed.substring(0, subscribed.indexOf("@"));
                    String subscribeType = subscribed.substring(subscribed.indexOf("@") + 1);
                    markSubscribed(symbol.toUpperCase(), subscribeType);
                }
            }
        } else if (responseJson.has("a") && responseJson.has("b")) {
            BinanceMessage.BookTicker bookTicker = GsonUtil.gson().fromJson(responseJson, BinanceMessage.BookTicker.class);
        } else if (responseJson.has("e") && responseJson.get("e").getAsString().equalsIgnoreCase("trade")) {
            BinanceMessage.Trade binanceTrade = GsonUtil.gson().fromJson(responseJson, BinanceMessage.Trade.class);
        } else {
            log.info("received unrecognized message from binance websocket. [{}]", text);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        lastAliveTime = System.currentTimeMillis();
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        log.warn("Binance WebSocket connection closing. code: {}, reason: {}", code, reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        log.warn("Binance WebSocket connection closed. code: {}, reason: {}", code, reason);
        releaseWsConnect();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        log.warn(String.format("Binance WebSocket connection failure. response: " + getWsResponse(response)), t);
        releaseWsConnect();
    }

    private String getWsResponse(Response response) {
        try {
            return response != null ? (", Response [ code: " + response.code() + (response.body() != null ? (", message: " + response.body().string() + "]") : "]")) : "";
        } catch (IOException e) {
            return "";
        }
    }

    public void releaseWsConnect() {
        if (rWebSocket != null) {
            rWebSocket.cancel();
            rWebSocket = null;
            wsConnected = false;
        }
        symbolSubscribeMap.keySet().forEach(this::markUnsubscribed);
    }

    public void ping() {
        if (wsConnected) {
            ((RealWebSocket) rWebSocket).onReadPing(ByteString.EMPTY);
        }
    }

    public void checkWsConnection() {
        if (destroyed) {
            return;
        }
        try {
            if (!wsConnected || System.currentTimeMillis() - lastAliveTime > 30000) {
                releaseWsConnect();
                Thread.sleep(2000);
                connectWebSocket();
            }
        } catch (Exception e) {

        }
    }

    public void checkSubscribe() {
        if (wsConnected) {
            List<String> unsubscribedSymbols = symbolSubscribeMap.keySet().stream()
                    .filter(item -> !symbolSubscribeMap.get(item))
                    .collect(Collectors.toList());
            if (!unsubscribedSymbols.isEmpty()) {
                try {
                    rWebSocket.send(BinanceMessage.subscribeTickers(unsubscribedSymbols, requestId.incrementAndGet()));
                    Thread.sleep(2000);
                    rWebSocket.send(BinanceMessage.listSubscription());
                } catch (Throwable t) {
                    log.warn("[checkSubscribe-Binance] error", t);
                }
            }
        }
    }

    public void subscribe(String... symbols) {
        for (String symbol : symbols) {
            symbolSubscribeMap.putIfAbsent(symbol, false);
        }
    }

    private void markSubscribed(String symbol, String subscribeType) {
        symbolSubscribeMap.computeIfPresent(symbol, (k, v) -> {
            log.info("BinanceMessage: {} {} subscribed successful", symbol, subscribeType);
            return true;
        });
    }

    private void markUnsubscribed(String symbol) {
        symbolSubscribeMap.computeIfPresent(symbol, (k, v) -> false);
    }
}
