package com.binance.client.examples;

import com.binance.client.examples.constants.NetworkConfig;
import com.binance.client.examples.constants.SymbolCacheData;
import com.binance.client.examples.tasks.PeriodSpiderTask;
import com.binance.client.examples.tasks.StrategyCheckTask;
import com.binance.client.examples.tasks.TimeWebSocketTask;
import com.binance.client.utils.NetworkProxySupport;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;


@Slf4j
public class BootServer {

    public static void main(String[] args) throws Exception {
        NetworkProxySupport.configure(NetworkConfig.HTTP_PROXY, NetworkConfig.USE_HTTP_PROXY);

        HttpServer server = HttpServer.create(new InetSocketAddress(9081), 0);
        server.setExecutor(null);
        server.start();

        log.info("Server started on port 9081");

        SymbolCacheData symbolData = new SymbolCacheData();
        PeriodSpiderTask periodSpiderTask = new PeriodSpiderTask(symbolData);
        TimeWebSocketTask timeWebSocketTask = new TimeWebSocketTask(symbolData);
        StrategyCheckTask strategyCheckTask = new StrategyCheckTask(symbolData);
        periodSpiderTask.start();
        timeWebSocketTask.start();
        strategyCheckTask.start();
    }
}
