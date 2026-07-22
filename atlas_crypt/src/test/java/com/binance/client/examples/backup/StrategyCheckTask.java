//package com.binance.client.examples.backup;
//
//
//import com.binance.client.enums.PeriodTypeEnum;
//import com.binance.client.enums.SideTypeEnum;
//import com.binance.client.model.market.Candlestick;
//import com.binance.client.utils.MessageSenderUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.time.StopWatch;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
///**
// * 顺势策略
// *
// */
//
//@Slf4j
//public class StrategyCheckTask extends Thread {
//
//    private static final String link_pref = "https://www.binance.com/zh-CN/futures/";
//
//    SymbolCacheData symbolData;
//
//    StrategyCheckTask(SymbolCacheData symbolData) {
//        this.symbolData = symbolData;
//    }
//
//    @Override
//    public void run() {
//        log.info("开始执行策略");
//        AtomicInteger atomicInteger = new AtomicInteger();
////        Lock lock = new ReentrantLock();
//        //定时拉取K线并判断
//        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
//        executorService.scheduleAtFixedRate(() -> {
//
//            try {
//
//                StopWatch stopWatch = new StopWatch();
//                stopWatch.start();
//
//
//                final List<CheckResult> checkResults = new ArrayList<>();
//
//
//                final Map<String, CheckResult> secondCheckResultMap = new HashMap<>();
//
//                for (String symbol : symbolData.getSymbols()) {
//                    try {
//
//                        ConcurrentHashMap<PeriodTypeEnum, List<Candlestick>> data = symbolData.getData(symbol);
//                        List<Candlestick> dayCandlesticks = data.get(GlobalConstants.FIRST_OP_PERIOD);
//
//                        if (dayCandlesticks == null || dayCandlesticks.size() < 2) {
//                            log.error("no day trade data, symbol = {}", symbol);
//                            continue;
//                        }
//
//                        CheckResult checkResult = new CheckResult(symbol, false);
//                        new LongChecker(symbol, GlobalConstants.FIRST_OP_PERIOD, symbolData.getData(symbol)).check(checkResult);
//                        new ShortChecker(symbol, GlobalConstants.FIRST_OP_PERIOD, symbolData.getData(symbol)).check(checkResult);
//
//
//                        if (GlobalConstants.SECOND_OP_PERIOD != null) {
//                            CheckResult secondCheckResult = new CheckResult(symbol, false);
//                            new LongChecker(symbol, GlobalConstants.SECOND_OP_PERIOD, symbolData.getData(symbol)).check(secondCheckResult);
//                            new ShortChecker(symbol, GlobalConstants.SECOND_OP_PERIOD, symbolData.getData(symbol)).check(secondCheckResult);
//
//                            secondCheckResultMap.put(symbol, secondCheckResult);
//                        }
//
//                        checkResults.add(checkResult);
//
//                        /**
//                         * 策略说明：
//                         * 1、机会：深坑、梯子、空间、脱离、上移、中枢突破
//                         * 2、危险：箱顶、斜侧顶、抛物顶
//                         * 3、MA20上：多空行情生命线
//                         *
//                         *
//                         * 2、买点
//                         *          买点1：深坑位：脱离、梯子、上移顺势、空间（排除箱顶）
//                         *              止损位：跌破MA20
//                         *              止盈位：箱顶
//                         *          买点2：突破箱顶位：小深坑、吊颈、顺势、空间
//                         *              止损位：跌破箱顶
//                         *              止盈位：*****
//                         * 3、止损：跌破MA20
//                         * 4、非以上，等待
//                         *
//                         */
//
//
//                    } catch (Exception ex) {
//                        log.error("[StrategyCheckTask] exception, symbol = {}", symbol, ex);
//                    }
//                }
//
//                //排序
//                List<CheckResult> sortCheckResults = checkResults.stream()
//                        .sorted((entry1, entry2) -> entry2.getChangeRate().abs().compareTo(entry1.getChangeRate().abs()))
//                        .collect(Collectors.toList());
//
//                AtomicInteger newBidCnt = new AtomicInteger(0);
//                for (CheckResult checkResult : sortCheckResults) {
//
//                    CheckResult secondCheckResult = secondCheckResultMap.get(checkResult.getSymbol());
//
//                    if (checkResult.isSuccess()
////                            && secondCheckResult.isSuccess()
////                            && secondCheckResult.getOpSideTypeEnum() == checkResult.getOpSideTypeEnum()
//                    ) {
//
//                        if (!symbolData.containsRecommend(checkResult.getOpPeriodTypeEnum(), checkResult.getOpSideTypeEnum(), checkResult.getSymbol())) {
//                            MessageSenderUtil.sendAsync(checkResult.getSymbol() + ":" + checkResult.getTrendMessage() + "  "+  checkResult.getSignalMessage() +"：" + checkResult.getClose());
//
//                            String changeRateMessage = checkResult.getChangeRate().multiply(new BigDecimal(100)).setScale(3, RoundingMode.HALF_DOWN) + "%";
//                            log.warn("【【{}-{}】】 -【趋势: {}】-【策略: {}】-【价格: {}-({})】 {}\n"
//                                    , checkResult.getSymbol()
//                                    , checkResult.getOpSideTypeEnum().getDesc()
//                                    , checkResult.getTrendMessage()
//                                    , checkResult.getSignalMessage()
//                                    , checkResult.getClose()
//                                    , changeRateMessage
//                                    , link_pref + checkResult.getSymbol());
//                            symbolData.addRecommend(checkResult.getOpPeriodTypeEnum(), checkResult.getOpSideTypeEnum(), checkResult.getSymbol());
//                            newBidCnt.incrementAndGet();
//                        }
//                    } else {
//                        if(symbolData.removeRecommend(checkResult.getOpPeriodTypeEnum(), SideTypeEnum.LONG, checkResult.getSymbol())) {
//                            log.warn("【【移除】】: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!{} ", checkResult.getSymbol());
//                        }
//                        if(symbolData.removeRecommend(checkResult.getOpPeriodTypeEnum(), SideTypeEnum.SHORT, checkResult.getSymbol())) {
//                            log.warn("【【移除】】: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!{} ", checkResult.getSymbol());
//                        }
//                    }
//                }
//
//                if (newBidCnt.get() > 0) {
//                      log.info("[StrategyCheckTask]总耗时[{}]秒, bid:{}", stopWatch.getTime() / 1000, newBidCnt.get());
//                }
//
//
//            } catch (Throwable ex) {
//                log.error("策略执行异常", ex);
//            }finally {
////                lock.unlock();
//            }
//
//        }, 0, 10, TimeUnit.SECONDS);
//
//    }
//
//
//    /**
//     * 校验成交量和振幅
//     *
//     * @param candlesticks
//     * @param zhenfuRate          振幅门槛值
//     * @param usdtAmountThreshold 成交额门槛值
//     * @return
//     */
//    public static boolean checkBase(List<Candlestick> candlesticks, BigDecimal zhenfuRate, BigDecimal usdtAmountThreshold) {
//        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
//        Candlestick candlestick1 = candlesticks.size() >= 2 ? candlesticks.get(candlesticks.size() - 2) : null;
//        return
//                // 成交额
//                (candlestick0.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0
//                        || (candlestick1 == null || candlestick1.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0)
//                )
//
//                        // 振幅
//                        && (candlestick0.getCrestShockRate().compareTo(zhenfuRate) > 0
//                        || (candlestick1 != null && candlestick1.getCrestShockRate().compareTo(zhenfuRate) > 0)
//                )
//                ;
//    }
//
//}
