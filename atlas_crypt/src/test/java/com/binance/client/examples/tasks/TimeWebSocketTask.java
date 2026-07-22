package com.binance.client.examples.tasks;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.client.examples.constants.SymbolCacheData;
import com.binance.client.websocket.UMWebsocketClientImpl;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TimeWebSocketTask extends Thread {


    SymbolCacheData symbolData;

    public TimeWebSocketTask(SymbolCacheData symbolData) {
        this.symbolData = symbolData;
    }

    @Override
    public void run() {
        UMWebsocketClientImpl client = new UMWebsocketClientImpl();

        Set socketSymbols = new HashSet();

        while (true) {
            for (String symbol: symbolData.getSymbols()) {
                if (!socketSymbols.contains(symbol)) {
                    int symbolStream = client.aggTradeStream(symbol, ((event) -> {
                        JSONObject jsonObject = JSON.parseObject(event);
//                        Boolean isMakerOrder = jsonObject.getBoolean("m");
//                        BigDecimal qty = jsonObject.getBigDecimal("q");
//                        String symbol = jsonObject.getString("s");
                        BigDecimal price = jsonObject.getBigDecimal("p");
                        symbolData.updateCandlestick(symbol, price);

//                        log.info("symbol: {}, price: {}", symbol, price);
                    }));

                    socketSymbols.add(symbol);
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
