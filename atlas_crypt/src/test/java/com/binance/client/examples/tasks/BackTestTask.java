package com.binance.client.examples.tasks;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.examples.backtest.BackTestStat;
import com.binance.client.examples.constants.GlobalConstants;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.examples.constants.SymbolCacheData;
import com.binance.client.examples.strategy.CheckResult;
import com.binance.client.examples.strategy.LongChecker;
import com.binance.client.examples.strategy.ShortChecker;
import com.binance.client.model.market.Candlestick;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class BackTestTask extends Thread {

    String symbol;

    SyncRequestClient syncRequestClient = null;

    int KLINE_LIMIT = 1500;  //取5条k数据

    SymbolCacheData symbolData = new SymbolCacheData();


    public BackTestTask(String symbol) {
        this.symbol = symbol;
        this.syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                new RequestOptions());
    }

    @Override
    public void run() {
        log.info("开始执行回测");

        //加载及封装数据
        loadKLine();

        //执行回测
        doBackTest();

        log.info("执行回测结束");
    }

    private void loadKLine() {
        ConcurrentHashMap<PeriodTypeEnum, List<Candlestick>> periodTypeEnumListConcurrentHashMap = new ConcurrentHashMap<>();
        GlobalConstants.SPIDER_PERIODS.stream().forEach(periodTypeEnum -> {
            List<Candlestick> candlesticks = syncRequestClient.getCandlestick(symbol, periodTypeEnum.getInterval(), null, null, KLINE_LIMIT);
            periodTypeEnumListConcurrentHashMap.put(periodTypeEnum, candlesticks);
        });
        this.symbolData.setPeriodCandlestickMap(symbol, periodTypeEnumListConcurrentHashMap);
    }

    private BackTestStat doBackTest() {

        ConcurrentHashMap<PeriodTypeEnum, List<Candlestick>> data = symbolData.getData(symbol);

        List<Candlestick> candlesticks = data.get(PeriodTypeEnum.DAY1);
        for (int i = 1200; i < candlesticks.size() - 1; i++) {
            int offset = candlesticks.size() - i;

            //策略部分

            //下单部分

            //止盈止损委托

            //统计部分

            CheckResult checkResult = new CheckResult(symbol);
            new LongChecker(symbol, symbolData.getData(symbol), GlobalConstants.TREND_PERIODS, GlobalConstants.OP_PERIOD, offset).check(checkResult);
//            if (!checkResult.isSuccess()) {
//                checkResult = new CheckResult(symbol);
//                new ShortChecker(symbol, symbolData.getData(symbol), GlobalConstants.TREND_PERIODS, GlobalConstants.OP_PERIOD, offset).check(checkResult);
//            }

            if (checkResult.isSuccess()) {
                Candlestick succcessCandlestick = candlesticks.get(candlesticks.size() - offset - 2);
                log.info("策略命中， symbol:{}, side: {}, time: {}", symbol, checkResult.getSideType(), succcessCandlestick.getOpenTimeStr());
            }
        }
        return null;
    }

    public static void main(String[] args) {
        new BackTestTask("BTCUSDT").start();
    }

}
