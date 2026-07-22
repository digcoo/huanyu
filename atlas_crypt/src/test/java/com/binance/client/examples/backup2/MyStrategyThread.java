package com.binance.client.examples.backup2;//package com.binance.client.examples;
//
//
//import com.binance.client.RequestOptions;
//import com.binance.client.SyncRequestClient;
//import com.binance.client.enums.PeriodTypeEnum;
//import com.binance.client.enums.SideTypeEnum;
//import com.binance.client.examples.constants.PrivateConfig;
//import com.binance.client.model.market.Candlestick;
//import com.binance.client.model.market.ExchangeInformation;
//import com.binance.client.utils.CheckValidator;
//import com.binance.client.utils.IndicatorCaculater;
//import com.binance.client.utils.MathUtil;
//import com.binance.client.utils.MessageSenderUtil;
//import org.apache.commons.lang3.time.StopWatch;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.math.BigDecimal;
//import java.util.*;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//import java.util.stream.Collectors;
//
///**
// * 顺势策略
// *
// */
//public class MyStrategyThread extends Thread{
//
//    Logger log = LoggerFactory.getLogger(this.getClass().getClass());
//
//    public static final String API_KEY = "fAgJpcu289fMzPEP1YoeTz0Q4zOhly40d2vHoufpYu6oqiYxM2iUkCBeM0b2iVvG";
//    public static final String SECRET_KEY = "IQcZ7D1N0l44cP61Xcw2LxTlSIK4zSV5uSgtKjKZZuleNfIXbvuJEGzAoGTMzeCQ"; // Unnecessary if PRIVATE_KEY_PATH is used
//    private static final String link_pref = "https://www.binance.com/zh-CN/futures/";
//    Map<String, KLine> klineMap;
//
//    List<KLine> longKLines;
//    List<KLine> shortKLines;
//
//    Map<SideTypeEnum, List<String>> recMap = new HashMap<>();
//
//    MyStrategyThread(Map<String, KLine> klineMap, List<KLine>  longKLines, List<KLine>  shortKLines){
//        this.klineMap = klineMap;
//        this.longKLines = longKLines;
//        this.shortKLines = shortKLines;
//    }
//
//    //覆盖run方法
//    @Override
//    public void run() {
//        log.info("开始执行策略");
//        int limit = 150;  //取5条k数据
////        BigDecimal zhenfuRate = new BigDecimal(0.5);  //振幅
////        BigDecimal tradeAssetVolume = new BigDecimal(1_000_000);  //交易量
////        CandlestickInterval period = CandlestickInterval.HOURLY;     //周期
//
//        RequestOptions options = new RequestOptions();
//        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
//                options);
//
//        //获取所有合约交易对
//        ExchangeInformation exchangeInformation = syncRequestClient.getExchangeInformation();
//        List<String> symbols = exchangeInformation.getSymbols().stream().filter(x -> x.getSymbol().endsWith("USDT")).map(x -> x.getSymbol()).collect(Collectors.toList());
//
//        Lock lock = new ReentrantLock();
//        //定时拉取K线并判断
//        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
//        executorService.scheduleAtFixedRate(() -> {
//            lock.lock();
//            try {
//                StopWatch stopWatch = new StopWatch();
//                stopWatch.start();
//
//                for (String symbol : symbols) {
//
////                    if (!symbol.contains("BELUSDT")) {
////                        continue;
////                    }
//
//                    try {
////                        List<SymbolOrderBook> symbolOrderBookTicker = syncRequestClient.getSymbolOrderBookTicker(symbol);
//                        List<Candlestick> min30Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MIN30.getInterval(), null, null, limit);
//                        if (!checkBase(min30Candlesticks, new BigDecimal("0.005"), new BigDecimal(500_0000))) {
//                            continue;
//                        }
//                        Candlestick min30Candlestick0 = min30Candlesticks.get(min30Candlesticks.size() - 1);
//                        System.out.println(min30Candlestick0.getSymbol() + "\t" + min30Candlestick0.getClose());
//
////                        List<Candlestick> monthCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MONTH.getInterval(), null, null, limit);
//                        List<Candlestick> weekCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.WEEK.getInterval(), null, null, limit);
//                        List<Candlestick> dayCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.DAY.getInterval(), null, null, limit);
//                        List<Candlestick> hour4Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.HOUR4.getInterval(), null, null, limit);
////                        List<Candlestick> min5Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MIN5.getInterval(), null, null, limit);
////                        klineMap.put(symbol, new KLine(symbol, monthCandlesticks, weekCandlesticks, dayCandlesticks, hour4Candlesticks, min30Candlesticks, min5Candlesticks));
//
//                        KLine kline = new KLine(symbol, null, weekCandlesticks, dayCandlesticks, hour4Candlesticks, min30Candlesticks, null);
//                        klineMap.put(symbol, kline);
//
//                        checkBid(symbol, kline, longKLines, shortKLines, recMap);
//
//                    } catch (Exception ex) {
//                         log.error("symbol = {}", symbol, ex);
//                    }
//
//                }
//
//                stopWatch.stop();
//
//                log.warn("多头命中[{}/{}]", longKLines.size(), symbols.size());
//                log.error("空头命中[{}/{}]", shortKLines.size(), symbols.size());
//                log.info("总耗时[{}]秒\n\n", stopWatch.getTime() / 1000);
//
//
//                this.longKLines.clear();
//                this.shortKLines.clear();
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//
//            lock.unlock();
//        }, 0, 30, TimeUnit.SECONDS);
//    }
//
//
//    /**
//     * 校验成交量和振幅
//     * @param candlesticks
//     * @param zhenfuRate 振幅门槛值
//     * @param usdtAmountThreshold 成交额门槛值
//     * @return
//     */
//    public static boolean checkBase(List<Candlestick> candlesticks, BigDecimal zhenfuRate, BigDecimal usdtAmountThreshold) {
//        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
//        Candlestick candlestick1 = candlesticks.size() >= 2 ? candlesticks.get(candlesticks.size() - 2) : null;
////        Candlestick candlestick2 = candlesticks.size() >= 3 ? candlesticks.get(candlesticks.size() - 3) : null;
//        return
//                // 成交额
//                (candlestick0.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0
//                    || (candlestick1 == null || candlestick1.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0)
////                    || (candlestick2 == null || candlestick2.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0)
//                )
//
//                // 振幅
//                && (candlestick0.getZhenFuRate().compareTo(zhenfuRate) > 0
//                    || (candlestick1 != null && candlestick1.getZhenFuRate().compareTo(zhenfuRate) > 0)
////                    || (candlestick2 != null && candlestick2.getZhenFuRate().compareTo(zhenfuRate) > 0)
//                )
//                ;
//    }
//
//    public void checkBid(String symbol, KLine kline, List<KLine> longKLines, List<KLine> shortKLines, Map<SideTypeEnum, List<String>> recMap) {
//        try {
//
////            List<Candlestick> min5CandlestickList = kline.getM5Lines();
////            Candlestick min5Candlestick0 = min5CandlestickList.get(min5CandlestickList.size() - 1);
//
//            List<Candlestick> min30CandlestickList = kline.getM30Lines();
//            Candlestick min30Candlestick0 = min30CandlestickList.get(min30CandlestickList.size() - 1);
//            List<Candlestick> hour4CandlestickList = kline.getH4Lines();
//            Candlestick hour4Candlestick0 = hour4CandlestickList.get(hour4CandlestickList.size() - 1);
//            List<Candlestick> dayCandlestickList = kline.getDLines();
//            List<Candlestick> weekCandlestickList = kline.getWLines();
//
//            /**
//             * 策略说明：
//             * 1、MA20上：多空行情生命线
//             * 2、买点
//             *          买点1：深坑位：脱离、梯子、上移顺势、空间（排除箱顶）
//             *              止损位：跌破MA20
//             *              止盈位：箱顶
//             *          买点2：突破箱顶位：小深坑、吊颈、顺势、空间
//             *              止损位：跌破箱顶
//             *              止盈位：*****
//             * 3、止损：跌破MA20
//             * 4、非以上，等待
//             *
//             */
//
////            LongChecker longChecker = new LongChecker(dayCandlestickList, hour4CandlestickList, min30CandlestickList);
////            ShortChecker shortChecker = new ShortChecker(dayCandlestickList, hour4CandlestickList, min30CandlestickList);
//            LongChecker longChecker = new LongChecker(weekCandlestickList, dayCandlestickList, hour4CandlestickList);
//            ShortChecker shortChecker = new ShortChecker(weekCandlestickList, dayCandlestickList, dayCandlestickList);
//
//            //多军
//            if (min30Candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) > 0 && longChecker.check()) {
//                longKLines.add(kline);
//                log.warn("多头命中: {}", link_pref + symbol);
//                if (!recMap.getOrDefault(SideTypeEnum.LONG, new ArrayList<>()).contains(symbol)) {
//                    MessageSenderUtil.send(symbol + "开多：" + min30CandlestickList.get(min30CandlestickList.size() - 1).getClose());
//                    List<String> longRecList = recMap.getOrDefault(SideTypeEnum.LONG, new ArrayList<>());
//                    longRecList.add(symbol);
//                    recMap.put(SideTypeEnum.LONG, longRecList);
//                }
//            } else {
//                if (recMap.getOrDefault(SideTypeEnum.LONG, new ArrayList<>()).contains(symbol)) {
//                    recMap.get(SideTypeEnum.LONG).remove(symbol);
//                }
//            }
//
//            if (min30Candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0 && shortChecker.check()) {
//                shortKLines.add(kline);
//                log.warn("空头命中: {}", link_pref + symbol);
//                if (!recMap.getOrDefault(SideTypeEnum.SHORT, new ArrayList<>()).contains(symbol)) {
//                    MessageSenderUtil.send(symbol + "开空：" + min30CandlestickList.get(min30CandlestickList.size() - 1).getClose());
//                    List<String> shortRecList = recMap.getOrDefault(SideTypeEnum.SHORT, new ArrayList<>());
//                    shortRecList.add(symbol);
//                    recMap.put(SideTypeEnum.SHORT, shortRecList);
//                }
//            } else {
//                if (recMap.getOrDefault(SideTypeEnum.SHORT, new ArrayList<>()).contains(symbol)) {
//                    recMap.get(SideTypeEnum.SHORT).remove(symbol);
//                }
//            }
//        } catch (Exception ex) {
//            System.out.println("error symbol = " + symbol);
//            ex.printStackTrace();
//        }
//    }
//
//
//
//}
