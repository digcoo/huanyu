package com.yh.bigdata.tts.spider.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.yh.bigdata.tts.common.model.*;
import com.yh.bigdata.tts.common.param.TradeConvertHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.spider.service.StockService;

@Configuration
@Slf4j
public class MyApplicationLoader {

    @Autowired
    StockService stockService;

    @Autowired
    Environment env;

    @PostConstruct
    public void onApplicationEvent() {

        if (!"app".equals(env.getProperty("project"))) {
            return;
        }

        try {
            long start = System.currentTimeMillis();
            List<StockBase> stocks = stockService.findAllStocks();
            List<StockMin30> stockMin30s = stockService.findAllStockMin30s(null);
            List<StockDay> stockDays = stockService.findAllStockDays(null);
            List<StockWeek> stockWeeks = stockService.findAllStockWeeks(null);
            List<StockMonth> stockMonths = stockService.findAllStockMonths(null);
            List<StockYear> stockYears = stockService.findAllStockYears(null);
            List<StockQuarter> stockQuarters = stockService.findAllStockQuarters(null);

            RealtimeStockCache.min30Map = TradeConvertHelper.parseSortMapList(stockMin30s, PeriodTypeEnum.MIN30);
            RealtimeStockCache.dayMap = TradeConvertHelper.parseSortMapList(stockDays, PeriodTypeEnum.DAY);
            RealtimeStockCache.weekMap = TradeConvertHelper.parseSortMapList(stockWeeks, PeriodTypeEnum.WEEK);
            RealtimeStockCache.monthMap = TradeConvertHelper.parseSortMapList(stockMonths, PeriodTypeEnum.MONTH);
            RealtimeStockCache.yearMap = TradeConvertHelper.parseSortMapList(stockYears, PeriodTypeEnum.YEAR);
            RealtimeStockCache.quarterMap = TradeConvertHelper.parseSortMapList(stockQuarters, PeriodTypeEnum.QUARTER);

            //初步筛选
            List<StockBase> filterStocks = commonFilter(stocks, RealtimeStockCache.dayMap, RealtimeStockCache.weekMap, RealtimeStockCache.monthMap);
            RealtimeStockCache.filterStockMap = filterStocks.stream().collect(Collectors.toMap(StockBase::getCode, p -> {
                return p;
            }));

            //预加载当日分时数据
//			String nowTime = DateFormatUtils.format(Calendar.getInstance(), DateUtil.TIME_FORMAT_HH_MM);
//			if (nowTime.compareTo("09:30") > 0
//					&& nowTime.compareTo("15:00") < 0 && SinaHttpUtils.isTradeOfCurrentDay()) {
//				new PreLoadMinSpider().run();
//			}

            log.info("MyApplicationLoader strat cost = {}s", (System.currentTimeMillis() - start) / 1000);

        } catch (Exception e) {
            log.error("MyApplicationLoader onApplicationEvent exception....", e);
        }
    }

    private List<StockBase> commonFilter(List<StockBase> stocks
            , Map<String, List> stockDayMap
            , Map<String, List> stockWeekMap
            , Map<String, List> stockMonthMap) {
        List<StockBase> targetStocks = Lists.newArrayList();
        int notBidNum = 0;

        double minAvgAmount = 6000_0000;
        double minShockRate = 0.035;
        double minChangeRate = 0.025;

        for (StockBase stockBase : stocks) {
            List<StockDay> stockDays = stockDayMap.get(stockBase.getCode());
            try {

                //过滤(最近10日)平均交易额低于2000万(暂时无交易额字段)
                //过滤每日(最近20日)涨幅都不超过3%(有1日超过就可以)
                //过滤每日(最近10日)振幅都不超过3%(有1日超过就可以)
                boolean bid =
                        stockBase.getIsTrade()
                                && !stockBase.getCode().startsWith("sh688")
                                && stockDays != null
                                && stockDays.subList(stockDays.size() < 3 ? 0 : stockDays.size() - 3, stockDays.size()).stream().filter(tmp -> tmp.getAmount() != null && tmp.getAmount() > 0.1).mapToDouble(StockDay::getAmount).average().orElse(0) > minAvgAmount
                                && stockDays.subList(stockDays.size() < 20 ? 0 : stockDays.size() - 20, stockDays.size()).stream().mapToDouble(StockDay::getChangeRate).max().orElse(0) > minChangeRate
                                && stockDays.subList(stockDays.size() < 20 ? 0 : stockDays.size() - 20, stockDays.size()).stream().mapToDouble(StockDay::getHighLowRate).max().orElse(0) > minShockRate
                        ;

                if (bid) {
                    targetStocks.add(stockBase);
                } else {
                    notBidNum++;
                    log.info("not bid : code = {}, name = {}", stockBase.getCode(), stockBase.getName());
                }
            } catch (Exception e) {
                System.err.println("error parms : stock = " + JSON.toJSONString(stockBase)
                                + "days  = " + JSON.toJSONString(stockDays)
//				+ "weeks  = " + JSON.toJSONString(stockWeeks)
//				+ "months = " + JSON.toJSONString(stockMonths)
                );
            }
        }
        log.info("not bid number : {}", notBidNum);
        return targetStocks;
    }
}