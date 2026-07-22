package com.binance.client.examples.backup2;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.ExchangeInformation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
public class PeriodSpiderTask extends Thread {

    public static final String API_KEY = "fAgJpcu289fMzPEP1YoeTz0Q4zOhly40d2vHoufpYu6oqiYxM2iUkCBeM0b2iVvG";
    public static final String SECRET_KEY = "IQcZ7D1N0l44cP61Xcw2LxTlSIK4zSV5uSgtKjKZZuleNfIXbvuJEGzAoGTMzeCQ"; // Unnecessary if PRIVATE_KEY_PATH is used
    private static final String link_pref = "https://www.binance.com/zh-CN/futures/";

    SymbolCacheData symbolData;

    SyncRequestClient syncRequestClient = null;


    int KLINE_LIMIT = 100;  //取5条k数据

    int SYMBOL_TOP_N = 50;


    PeriodSpiderTask(SymbolCacheData symbolData) {
        this.symbolData = symbolData;
        this.syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                new RequestOptions());
    }

    @Override
    public void run() {

//        List<String> symbols = Arrays.asList(
//                "BTCUSDT"
//                , "ETHUSDT"
//                , "SOLUSDT");

        List<String> watchSymbols = getWatchSymbols();
        log.info("watchSymbols = {}", watchSymbols);

        Lock lock = new ReentrantLock();
        //定时拉取K线并判断
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            lock.lock();
            try {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                for (String symbol : watchSymbols) {

                    try {

                        ConcurrentHashMap<PeriodTypeEnum, List<Candlestick>> periodTypeEnumListConcurrentHashMap = new ConcurrentHashMap<>();
                        GlobalConstants.SPIDER_PERIODS.stream().forEach(periodTypeEnum -> {
                            List<Candlestick> candlesticks = syncRequestClient.getCandlestick(symbol, periodTypeEnum.getInterval(), null, null, KLINE_LIMIT);
                            periodTypeEnumListConcurrentHashMap.put(periodTypeEnum, candlesticks);
                        });
                        this.symbolData.setPeriodCandlestickMap(symbol, periodTypeEnumListConcurrentHashMap);


//                        List<CompletableFuture<List<Candlestick>>> futures = GlobalConstants.getSpiderAndCheckPeriodList().stream().map(periodTypeEnum -> {
//                            return CompletableFuture.supplyAsync(() ->
//                                    syncRequestClient.getCandlestick(symbol, periodTypeEnum.getInterval(), null, null, KLINE_LIMIT));
//                        }).collect(Collectors.toList());
//
//                        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
//                        periodTypeEnumListConcurrentHashMap.put(PeriodTypeEnum.MIN30, min30Future.get());


                    } catch (Exception ex) {
                        log.error("[PeriodSpiderTask] exception, symbol = {}", symbol, ex);
                    }

                }

                stopWatch.stop();

                log.info("[PeriodSpiderTask]总耗时[{}]秒, symbols[{}]", stopWatch.getTime() / 1000, watchSymbols.size());

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            lock.unlock();
        }, 0, 30, TimeUnit.SECONDS);
    }

    private List<String> getWatchSymbols() {

        ExchangeInformation exchangeInformation = this.syncRequestClient.getExchangeInformation();
        Set<String> symbols = exchangeInformation.getSymbols().stream()
                .filter(x -> x.getSymbol().endsWith("USDT"))
                .filter(x -> !x.getSymbol().equals("getSymbol"))
                .map(x -> x.getSymbol()).collect(Collectors.toSet());

        Map<String, BigDecimal> symbolToQuoteAssetVolumeMap = new HashMap<>();

        for (String symbol : symbols) {
            try {
                List<Candlestick> dayCandlesticks = syncRequestClient.getCandlestick(symbol, GlobalConstants.SPIDER_CHECK_PERIOD.getInterval(), null, null, KLINE_LIMIT);
                Candlestick dayCandlestick1 = dayCandlesticks.get(dayCandlesticks.size() - 2);
                if (dayCandlestick1.getQuoteAssetVolume().compareTo(new BigDecimal(1_0000_0000 * 1)) > 0) {
                    symbolToQuoteAssetVolumeMap.put(symbol, dayCandlestick1.getQuoteAssetVolume());
                }
            } catch (Exception ex) {
                log.error("getCandlestick error. symbol = {}", symbol, ex);
            }

        }

        List<Map.Entry<String, BigDecimal>> sortedList = new ArrayList<>(symbolToQuoteAssetVolumeMap.entrySet());
        sortedList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        List<Map.Entry<String, BigDecimal>> topEntries = sortedList.size() > SYMBOL_TOP_N ? sortedList.subList(0, SYMBOL_TOP_N) : sortedList;

        return topEntries.stream().map(x -> x.getKey()).collect(Collectors.toList());

    }

    public static void main(String[] args) {
//
//        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
//                new RequestOptions());
//        String symbol = "BTCUSDT";
//        int KLINE_LIMIT = 100;
//        List<Candlestick> candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.DAY1.getInterval(), null, null, KLINE_LIMIT);
//        log.info(String.valueOf(candlesticks.size()));
//
//        List<Double> closeList = candlesticks.stream().map(x -> x.getClose().doubleValue()).collect(Collectors.toList());
//
//        List<double[]> macdResults = MACDIndicator.calculateMACD(closeList);
//
//        for (int i = macdResults.size() - 30; i < macdResults.size(); i++) {
//            double[] values = macdResults.get(i);
//            System.out.printf("Day %d: DIF=%.4f, DEA=%.4f, MACD=%.4f%n", i + 1, values[0], values[1], values[2]);
//        }
//

    }
}
