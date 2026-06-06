package com.yh.bigdata.tts.spider.scheduler;

import java.util.*;

import javax.annotation.PostConstruct;

import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.dao.StockTargetMapper;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.spider.ws.MyWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockTarget;
import com.yh.bigdata.tts.spider.strategy.AbstractStrategy;
import com.yh.bigdata.tts.spider.response.CheckResult;

/**
 * @author duyp
 *
 * @date 2021/09/24
 *
 * @comment
 */

@Component
@EnableScheduling
@Slf4j
public class StockTargetScheduler {

    @Autowired
    private MyWebSocketHandler myWebSocketHandler;

    @Autowired
    private StockTargetMapper stockTargetMapper;

    @Autowired
    List<AbstractStrategy> strategies;

	@PostConstruct
	public void init() {

	}

	public void recommendSave() {
		try {

            // 取出昨天的推荐数据
            String lastDay = stockTargetMapper.selectLatestDay();
            Set<String> oldStockTargetList = new HashSet<>();
            if (Objects.nonNull(lastDay)) {
                List<StockTarget> stockTargets = stockTargetMapper.selectListByDay(lastDay);
                for (StockTarget stockTarget : stockTargets) {
                    oldStockTargetList.add(stockTarget.getCode());
                }
            }

            for (AbstractStrategy strategy : strategies) {

                if (strategy.getStrategy() != StrategyTypeEnum.TREND_NEW) {
                    continue;
                }

                for (StockBase stockBase : RealtimeStockCache.filterStockMap.values()) {

                    CheckResult checkResult = strategy.check(stockBase, null, null, QueryContextParam.empty());
                    if (!checkResult.isSuccess() || lastDay.equals(stockBase.getDay())) {
                        continue;
                    }

                    StockTarget stockTarget = buildStockTarget(stockBase, checkResult);
                    stockTarget.setNewFlag(!oldStockTargetList.contains(stockBase.getCode()));
                    stockTarget.setStrategy(strategy.getStrategy().getDesc());

                    if (stockTargetMapper.selectByPrimaryKey(stockBase.getCode(), stockBase.getDay(), strategy.getStrategy().getCode()) == null) {
                        stockTargetMapper.insert(stockTarget);
                    }else{
                        stockTargetMapper.update(stockTarget);
                    }
                }
            }


		} catch (Exception e) {
			log.error("recommendSave run exception...", e);
		}
	}

    private StockTarget buildStockTarget(StockBase stockBase, CheckResult checkResult) {
        StockTarget strategyStock = new StockTarget();
        strategyStock.setCode(stockBase.getCode());
        strategyStock.setName(stockBase.getName());
        strategyStock.setDay(stockBase.getDay());
        strategyStock.setClose(stockBase.getTrade());
        strategyStock.setTrendMessage(checkResult.getTrendMessage());
        strategyStock.setSignalMessage(checkResult.getSignalMessage());
        strategyStock.setChangeRate(checkResult.getChangeRate());
        return strategyStock;
    }

}
