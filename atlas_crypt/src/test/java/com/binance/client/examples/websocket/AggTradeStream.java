package com.binance.client.examples.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.client.websocket.UMWebsocketClientImpl;

import java.math.BigDecimal;

public final class AggTradeStream {
    private AggTradeStream() {
    }

    public static void main(String[] args) {
        UMWebsocketClientImpl client = new UMWebsocketClientImpl();
        int streamId1 = client.aggTradeStream("btcusdt", ((event) -> {
            JSONObject jsonObject = JSON.parseObject(event);
            Boolean isMakerOrder = jsonObject.getBoolean("m");
            BigDecimal price = jsonObject.getBigDecimal("p");
            BigDecimal qty = jsonObject.getBigDecimal("q");
//            if (price.multiply(qty).compareTo(new BigDecimal("1000000")) > 0) {
                System.out.println(event);
//            }
        }));
//        int streamId2 = client.aggTradeStream("ethusdt", ((event) -> {
//            System.out.println(event);
//        }));
//        client.closeConnection(streamId1);
//        client.closeConnection(streamId2);
    }
}
