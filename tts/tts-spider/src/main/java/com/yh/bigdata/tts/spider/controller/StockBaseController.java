package com.yh.bigdata.tts.spider.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.dao.StockTargetMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockTarget;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.common.param.base.PageResult;
import com.yh.bigdata.tts.common.param.base.Response;
import com.yh.bigdata.tts.common.param.base.ResponseUtil;
import com.yh.bigdata.tts.common.utils.MathUtil;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by zhou1 on 2019/1/8.
 */


/**
 * 
 * 
 * 
 * 侧重点*******************************
 * 
 * 1、大周期脱离（信号）
 *      深坑 + 脱离 + 上移
 *      底部宽幅震荡 + 半山腰
 *
 *      突破脱离位 、 回踩脱离位
 *
 *
 * 2、大周期梯子（信号）：
 *      深坑 + 脱离 + 上移
 *      底部宽幅震荡 + 半山腰
 *
 *      突破脱离位 、 回踩脱离位
 *
 * ***********************************
 *
 *
 * @author junifer
 *
 */
@RestController
@RequestMapping({ "/stock" })
@Slf4j
public class StockBaseController {

    @Autowired
    StockTargetMapper stockTargetMapper;

    @Autowired
    List<AbstractStrategy> strategies;
    Map<StrategyTypeEnum, AbstractStrategy> strategyMap;

    String lastDay = null;
    final Set<String> oldStockTargetList = new HashSet<>();

    @PostConstruct
    public void init() {
        strategyMap = new HashMap<StrategyTypeEnum, AbstractStrategy>();
        for (AbstractStrategy strategy : strategies) {
            strategyMap.put(strategy.getStrategy(), strategy);
        }

        //取出昨天的推荐
        lastDay = stockTargetMapper.selectLatestDay();
        if (Objects.nonNull(lastDay)) {
            List<StockTarget> stockTargets = stockTargetMapper.selectListByDay(lastDay);
            for (StockTarget stockTarget : stockTargets) {
                oldStockTargetList.add(stockTarget.getCode());
            }
        }
    }
		
	private List<StockTarget> doQuery(StockPageQuery stockPageQuery) {
        List<StockTarget> stockTargets = new ArrayList<StockTarget>();

        List<StockTarget> newStockTargetsWithOldDay = new ArrayList<StockTarget>();

		Stopwatch stopwatch = Stopwatch.createStarted();

        AbstractStrategy abstractStrategy = strategyMap.get(stockPageQuery.getStrategyTypeEnum());

        List<CheckResult> checkResults = abstractStrategy.doQuery(stockPageQuery.getTrendPeriodTypesEnum(), stockPageQuery.getOpPeriodTypeEnum(), buildQueryContextParam(stockPageQuery));

		checkResults = checkResults.stream().filter(checkResult -> {
			
			StockBase stock = RealtimeStockCache.filterStockMap.get(checkResult.getCode());
		
				return stock.getIsTrade()
	//				&& !stock.getCode().startsWith("sz3") 
					&& !stock.getCode().startsWith("sz1")
					&& !stock.getCode().startsWith("sh688")
					&& !stock.getCode().contains("bj")
					&& !stock.getName().contains("退")
					&& !stock.getName().contains("ST")
					&& !stock.getName().contains("债")
	//				&& !(stock.getLastMinAmount() < 100_0000)
					
					;
		}).collect(Collectors.toList());
		
		checkResults.sort(Comparator.comparing(CheckResult::getSortValue).reversed());		
		
		for (CheckResult checkResult : checkResults) {
			StockBase stockBase = RealtimeStockCache.filterStockMap.get(checkResult.getCode());
			
            StockTarget stockTarget = buildStockTarget(stockBase, stockPageQuery.getStrategyTypeEnum());
            stockTarget.setTrendMessage(checkResult.getTrendMessage());
            stockTarget.setSignalMessage(checkResult.getSignalMessage());
            stockTarget.setNewFlag(!oldStockTargetList.contains(stockBase.getCode()));

            stockTargets.add(stockTarget);

			if (stockTarget.isNewFlag()) {

                newStockTargetsWithOldDay.add(stockTarget);
				
				log.info("【今日新推荐】【{}#{}】- 成交额【{}亿】- 趋势【{}】- 信号【{}】- 涨跌幅【{} - ({}%)】"
						, stockBase.getCode()
						, stockBase.getName()
						, MathUtil.formatMoney(stockBase.getAmount())
						, checkResult.getTrendMessage()
						, checkResult.getSignalMessage()
						, stockBase.getTrade()
						, new  BigDecimal(stockBase.getChangeRate()).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_UP)
						);
				
			} else {
				log.info("【昨日已推荐-今日再推荐】【{}#{}】- 成交额【{}亿】- 趋势【{}】- 信号【{}】- 涨跌幅【{} - ({}%)】"
						, stockBase.getCode()
						, stockBase.getName()
						, MathUtil.formatMoney(stockBase.getAmount())
						, checkResult.getTrendMessage()
						, checkResult.getSignalMessage()
						, stockBase.getTrade()
						, new  BigDecimal(stockBase.getChangeRate()).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_UP)
						);
			}
			
		}
		
		log.info("\n\n strategy={}, trendPeriodType={}, opPeriodType={}, oldSize={}, newSize={}, cost={}", abstractStrategy.getStrategy(), stockPageQuery.getTrendPeriodTypeEnum(), stockPageQuery.getOpPeriodTypeEnum(), stockTargets.size() - newStockTargetsWithOldDay.size(),  newStockTargetsWithOldDay.size(),  stopwatch.stop().elapsed(TimeUnit.SECONDS));

        if (stockPageQuery.isAll()) {
			return stockTargets;
		}else {
			return newStockTargetsWithOldDay;
		}
		
	}
	
	/**
	 * 短线符合预期
	 * 短线突破左侧支撑位(横向)
	 * @param pageQuery
	 * @return
	 */
	@RequestMapping(value = { "/findMy" }, method = { RequestMethod.GET })
	public Response<PageResult<StockTarget>> query(StockPageQuery pageQuery) {
//		Stopwatch stopwatch = Stopwatch.createStarted();

		log.info("------------->/stock/findMy 选股入口: {}", JSON.toJSONString(pageQuery));
		
		PageResult<StockTarget> pageResult = new PageResult<StockTarget>();
		List<StockTarget> targetStocks = doQuery(pageQuery);
		pageResult.setItems(targetStocks);
		
		pageResult.setTotalPage(1);
				
		return ResponseUtil.success(pageResult);
		
	}

    private StockTarget buildStockTarget(StockBase stockBase, StrategyTypeEnum strategyTypeEnum) {
        StockTarget stockTarget = new StockTarget();
        stockTarget.setCode(stockBase.getCode());
        stockTarget.setDay(stockBase.getDay());
        stockTarget.setName(stockBase.getName());
        stockTarget.setStrategy(strategyTypeEnum.getCode());
        stockTarget.setClose(stockBase.getTrade());
        stockTarget.setNewFlag(true);
        stockTarget.setTrendMessage(stockBase.getTrendMessage());
        stockTarget.setSignalMessage(stockBase.getSignalMessage());
        stockTarget.setChangeRate(stockBase.getChangeRate());
        stockTarget.setMainBusiness(stockBase.getMainBusiness());
        return stockTarget;
    }

    private QueryContextParam buildQueryContextParam(StockPageQuery stockPageQuery) {
        return QueryContextParam.builder()
                .lianBanDays(Objects.nonNull(stockPageQuery.getLianBanDays())? stockPageQuery.getLianBanDays(): NumberUtils.INTEGER_ONE)
                .build();

    }

}
