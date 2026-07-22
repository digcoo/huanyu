package com.binance.client.examples;

import com.binance.client.enums.SideTypeEnum;
import com.binance.client.examples.constants.NetworkConfig;
import com.binance.client.examples.constants.SymbolCacheData;
import com.binance.client.examples.tasks.PeriodSpiderTask;
import com.binance.client.examples.tasks.StrategyCheckTask;
import com.binance.client.examples.tasks.TimeWebSocketTask;
import com.binance.client.utils.NetworkProxySupport;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
public class BootServer {

    static Map<String, KLine> klineMap = new ConcurrentHashMap<>();
    static List<KLine> longKLineList = new CopyOnWriteArrayList<>();
    static List<KLine> shortKLineList = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws Exception {
        NetworkProxySupport.configure(NetworkConfig.HTTP_PROXY, NetworkConfig.USE_HTTP_PROXY);

        // 创建HttpServer实例，监听9080端口
        HttpServer server = HttpServer.create(new InetSocketAddress(9081), 0);

        Map<SideTypeEnum, Set<String>> recMap = new HashMap<>();
        SymbolCacheData symbolData = new SymbolCacheData();
//
//        // 创建处理请求的上下文
//        server.createContext("/future/klines", new KlineHandler(symbolData));
//
//        // 创建处理请求的上下文(多头)
//        server.createContext("/future/recommendLong", new LongRecommendHandler(symbolData));
//        // 创建处理请求的上下文(空头)
//        server.createContext("/future/recommendShort", new ShortRecommendHandler(symbolData));

        // 设置服务器的线程池数量
        server.setExecutor(null); // 使用默认的线程池

        // 启动服务器
        server.start();

        System.out.println("Server started on port 9090");


        PeriodSpiderTask periodSpiderTask = new PeriodSpiderTask(symbolData);
        TimeWebSocketTask timeWebSocketTask = new TimeWebSocketTask(symbolData);
        StrategyCheckTask strategyCheckTask = new StrategyCheckTask(symbolData);
        periodSpiderTask.start();
        timeWebSocketTask.start();
        strategyCheckTask.start();

    }
 }
