package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockTarget;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.service.StrategyService;
import com.yh.bigdata.tts.spider.strategy.AbstractStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@Slf4j
public class StrategyServiceImpl implements StrategyService {

    @Autowired
    List<AbstractStrategy> strategies;
    Map<StrategyTypeEnum, AbstractStrategy> strategyMap;

    @PostConstruct
    public void init() {
        strategyMap = new HashMap<StrategyTypeEnum, AbstractStrategy>();
        for (AbstractStrategy strategy : strategies) {
            strategyMap.put(strategy.getStrategy(), strategy);
        }
    }

    @Override
    public List<StockTarget> getAndUpdateTriggerStockTargets(StrategyTypeEnum strategyType) {

        List<StockTarget> stockTargetList = new ArrayList<>();

        long currentWindow = RealtimeStockCache.getCurrentWindow();

        AbstractStrategy strategy = strategyMap.get(strategyType);

        for (StockBase stockBase : RealtimeStockCache.filterStockMap.values()) {

            CheckResult checkResult = strategy.check(stockBase, Arrays.asList(PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH, PeriodTypeEnum.QUARTER, PeriodTypeEnum.YEAR), PeriodTypeEnum.DAY, QueryContextParam.empty());

            if (checkResult.isSuccess()) {
                String stockCode = stockBase.getCode();
                Long lastWindow = RealtimeStockCache.lastSentTimeMap.get(stockCode);

                // 如果当前时间窗口未发送过
                if (lastWindow == null || lastWindow < currentWindow) {

                    RealtimeStockCache.lastSentTimeMap.put(stockCode, currentWindow); // 记录当前时间窗口
                    RealtimeStockCache.windowToCodeMap.putIfAbsent(currentWindow, new HashSet<StockBase>());
                    RealtimeStockCache.windowToCodeMap.get(currentWindow).add(stockBase);

                    stockTargetList.add(StockTarget.builder().code(stockCode).name(stockBase.getName()).build());
                }
            }
        }

        return stockTargetList;
    }
}
