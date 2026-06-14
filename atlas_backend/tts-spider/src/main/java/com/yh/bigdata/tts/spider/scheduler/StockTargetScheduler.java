package com.yh.bigdata.tts.spider.scheduler;

import java.util.*;

import javax.annotation.PostConstruct;

import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.dao.StockTargetMapper;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.ReboundStrategyParams;
import com.yh.bigdata.tts.common.param.UnilateralStrategyParams;
import com.yh.bigdata.tts.spider.ws.MyWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockTarget;
import com.yh.bigdata.tts.common.utils.StockQuoteUtils;
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
            int saved = recommendSaveInternal();
            log.info("StockTargetScheduler recommendSave done, count={}", saved);
		} catch (Exception e) {
			log.error("recommendSave run exception...", e);
		}
	}

    /** @return 写入/更新的推荐条数（默认参数） */
    public int recommendSaveInternal() {
        return recommendSaveInternal(QueryContextParam.empty(), true);
    }

    /**
     * 按自定义上下文重跑策略并写入 stock_target
     *
     * @param contextParam 策略参数（如单边 u* 参数）
     * @param forceUpdate  true=同日强制 upsert
     */
    public int recommendSaveInternal(QueryContextParam contextParam, boolean forceUpdate) {
        return recommendSaveInternal(contextParam, forceUpdate, null);
    }

    /**
     * @param onlyStrategy 非空时仅扫描该策略（小程序 rescan）
     */
    public int recommendSaveInternal(QueryContextParam contextParam, boolean forceUpdate,
                                     StrategyTypeEnum onlyStrategy) {
            if (contextParam == null) {
                contextParam = QueryContextParam.empty();
            }
            if (contextParam.getUnilateral() == null) {
                contextParam.setUnilateral(UnilateralStrategyParams.defaults());
            }
            if (contextParam.getRebound() == null) {
                contextParam.setRebound(ReboundStrategyParams.defaults());
            }
            if (contextParam.getPreGolden() == null) {
                contextParam.setPreGolden(com.yh.bigdata.tts.common.param.PreGoldenStrategyParams.defaults());
            }
            if (contextParam.getResonance() == null) {
                contextParam.setResonance(com.yh.bigdata.tts.common.param.ResonanceStrategyParams.defaults());
            }
            int saved = 0;
            String lastDay = stockTargetMapper.selectLatestDay();
            Set<String> oldStockTargetList = new HashSet<>();
            if (Objects.nonNull(lastDay)) {
                List<StockTarget> stockTargets = stockTargetMapper.selectListByDay(lastDay);
                for (StockTarget stockTarget : stockTargets) {
                    oldStockTargetList.add(stockTarget.getCode());
                }
            }

            for (AbstractStrategy strategy : strategies) {

                StrategyTypeEnum type = strategy.getStrategy();
                if (onlyStrategy != null) {
                    if (type != onlyStrategy) {
                        continue;
                    }
                } else if (type != StrategyTypeEnum.TREND_NEW && type != StrategyTypeEnum.DEFAUL) {
                    continue;
                }

                for (StockBase stockBase : RealtimeStockCache.filterStockMap.values()) {

                    CheckResult checkResult = strategy.check(stockBase, null, null, contextParam);
                    if (!checkResult.isSuccess()) {
                        continue;
                    }
                    if (!forceUpdate && lastDay != null && lastDay.equals(stockBase.getDay())) {
                        continue;
                    }

                    StockTarget stockTarget = buildStockTarget(stockBase, checkResult);
                    stockTarget.setNewFlag(!oldStockTargetList.contains(stockBase.getCode()));
                    stockTarget.setStrategy(strategy.getStrategy().getCode());

                    if (stockTargetMapper.selectByPrimaryKey(stockBase.getCode(), stockBase.getDay(), strategy.getStrategy().getCode()) == null) {
                        stockTargetMapper.insert(stockTarget);
                    }else{
                        stockTargetMapper.update(stockTarget);
                    }
                    saved++;
                }
            }
            return saved;
    }

    private StockTarget buildStockTarget(StockBase stockBase, CheckResult checkResult) {
        StockQuoteUtils.overlayLatestDayQuote(stockBase);
        StockTarget strategyStock = new StockTarget();
        strategyStock.setCode(stockBase.getCode());
        strategyStock.setName(stockBase.getName());
        strategyStock.setDay(stockBase.getDay());
        strategyStock.setClose(stockBase.getClose() != null ? stockBase.getClose() : 0D);
        strategyStock.setTrendMessage(checkResult.getTrendMessage());
        strategyStock.setSignalMessage(checkResult.getSignalMessage());
        Double changeRate = stockBase.getChangeRate();
        strategyStock.setChangeRate(changeRate != null ? changeRate : checkResult.getChangeRate());
        return strategyStock;
    }

}
