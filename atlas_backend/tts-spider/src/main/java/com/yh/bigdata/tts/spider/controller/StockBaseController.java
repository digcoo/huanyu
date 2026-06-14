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
import com.yh.bigdata.tts.common.utils.StockQuoteUtils;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.scheduler.StockTargetScheduler;
import com.yh.bigdata.tts.spider.strategy.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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

    @Autowired
    StockTargetScheduler stockTargetScheduler;

    @Value("${atlas.strategy.rescan-enabled:true}")
    private boolean strategyRescanEnabled;

    String lastDay = null;
    final Set<String> oldStockTargetList = new HashSet<>();

    private static final long RECOMMEND_CACHE_TTL_MS = 10 * 60 * 1000L;
    private static final int DEFAULT_PAGE_SIZE = 12;

    private final Map<String, CachedRecommendations> recommendCache = new HashMap<>();

    private static final class CachedRecommendations {
        private final List<StockTarget> items;
        private final long cachedAt;

        private CachedRecommendations(List<StockTarget> items, long cachedAt) {
            this.items = items;
            this.cachedAt = cachedAt;
        }
    }

    @PostConstruct
    public void init() {
        strategyMap = new HashMap<StrategyTypeEnum, AbstractStrategy>();
        for (AbstractStrategy strategy : strategies) {
            strategyMap.put(strategy.getStrategy(), strategy);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadHistoricalTargets() {
        try {
            lastDay = stockTargetMapper.selectLatestDay();
            if (Objects.nonNull(lastDay)) {
                List<StockTarget> stockTargets = stockTargetMapper.selectListByDay(lastDay);
                for (StockTarget stockTarget : stockTargets) {
                    oldStockTargetList.add(stockTarget.getCode());
                }
            }
            log.info("StockBaseController loaded {} historical targets, lastDay={}", oldStockTargetList.size(), lastDay);
        } catch (Exception e) {
            log.warn("load historical stock_target skipped: {}", e.getMessage());
        }
    }
		
	private List<StockTarget> doQuery(StockPageQuery stockPageQuery) {
        List<StockTarget> stockTargets = new ArrayList<StockTarget>();

        List<StockTarget> newStockTargetsWithOldDay = new ArrayList<StockTarget>();

		Stopwatch stopwatch = Stopwatch.createStarted();

        AbstractStrategy abstractStrategy = stockPageQuery.getStrategyTypeEnum() == null
                ? null
                : strategyMap.get(stockPageQuery.getStrategyTypeEnum());
        if (abstractStrategy == null) {
            log.warn("strategy not registered: {}", stockPageQuery.getStrategy());
            return stockTargets;
        }

        List<CheckResult> checkResults = abstractStrategy.doQuery(stockPageQuery.getTrendPeriodTypesEnum(), stockPageQuery.getOpPeriodTypeEnum(), buildQueryContextParam(stockPageQuery));

		checkResults = checkResults.stream().filter(checkResult -> {
			
			StockBase stock = RealtimeStockCache.filterStockMap.get(checkResult.getCode());
			if (stock == null) {
			    return false;
			}
		
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
						, stockBase.getClose()
						, new  BigDecimal(stockBase.getChangeRate()).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_UP)
						);
				
			} else {
				log.info("【昨日已推荐-今日再推荐】【{}#{}】- 成交额【{}亿】- 趋势【{}】- 信号【{}】- 涨跌幅【{} - ({}%)】"
						, stockBase.getCode()
						, stockBase.getName()
						, MathUtil.formatMoney(stockBase.getAmount())
						, checkResult.getTrendMessage()
						, checkResult.getSignalMessage()
						, stockBase.getClose()
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
		log.info("------------->/stock/findMy 选股入口: {}", JSON.toJSONString(pageQuery));

        int page = pageQuery.getPage() == null || pageQuery.getPage() < 1 ? 1 : pageQuery.getPage();
        int size = pageQuery.getSize() == null || pageQuery.getSize() < 1 ? DEFAULT_PAGE_SIZE : pageQuery.getSize();

        List<StockTarget> allTargets = getOrQueryAll(pageQuery);
        PageResult<StockTarget> pageResult = new PageResult<>(page, size, (long) allTargets.size());

        int start = pageResult.getStartIndex();
        if (start >= allTargets.size()) {
            pageResult.setItems(Collections.emptyList());
        } else {
            int end = Math.min(start + size, allTargets.size());
            pageResult.setItems(new ArrayList<>(allTargets.subList(start, end)));
        }

        log.info("findMy page={}/{}, size={}, total={}", page, pageResult.getTotalPage(),
                pageResult.getItems().size(), pageResult.getTotalNum());

		return ResponseUtil.success(pageResult);
	}

    /**
     * 按当前请求参数重跑策略扫描并写入 stock_target（小程序「重跑策略」）
     */
    @PostMapping("/strategy/rescan")
    public Response<Map<String, Object>> rescanStrategy(StockPageQuery pageQuery) {
        if (!strategyRescanEnabled) {
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        if (pageQuery.getStrategyTypeEnum() != StrategyTypeEnum.TREND_NEW
                && pageQuery.getStrategyTypeEnum() != StrategyTypeEnum.PRE_GOLD_CROSS
                && pageQuery.getStrategyTypeEnum() != StrategyTypeEnum.PERIOD_RESONANCE
                && pageQuery.getStrategyTypeEnum() != StrategyTypeEnum.DEFAUL) {
            log.warn("rescan unsupported strategy: {}", pageQuery.getStrategy());
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        QueryContextParam contextParam = buildQueryContextParam(pageQuery);
        int saved = stockTargetScheduler.recommendSaveInternal(
                contextParam, true, pageQuery.getStrategyTypeEnum());
        clearRecommendCache();
        Map<String, Object> data = new HashMap<>();
        data.put("strategy", pageQuery.getStrategyTypeEnum().getCode());
        data.put("saved", saved);
        log.info("strategy rescan done, strategy={}, saved={}", pageQuery.getStrategy(), saved);
        return ResponseUtil.success(data);
    }

    private List<StockTarget> getOrQueryAll(StockPageQuery pageQuery) {
        String cacheKey = buildRecommendCacheKey(pageQuery);
        long now = System.currentTimeMillis();
        CachedRecommendations cached = recommendCache.get(cacheKey);
        if (cached != null && now - cached.cachedAt < RECOMMEND_CACHE_TTL_MS) {
            return cached.items;
        }
        List<StockTarget> items = doQuery(pageQuery);
        recommendCache.put(cacheKey, new CachedRecommendations(items, now));
        return items;
    }

    private String buildRecommendCacheKey(StockPageQuery pageQuery) {
        return String.join("|",
                String.valueOf(pageQuery.getStrategyTypeEnum()),
                String.valueOf(pageQuery.getTrendPeriodTypes()),
                String.valueOf(pageQuery.getOpPeriodType()),
                String.valueOf(pageQuery.isAll()),
                String.valueOf(pageQuery.getUDayLookback()),
                String.valueOf(pageQuery.getUStrongYangPct()),
                String.valueOf(pageQuery.getUWeekContextMin()),
                String.valueOf(pageQuery.getUMinAmountWan()),
                String.valueOf(pageQuery.getUEnableShort()),
                String.valueOf(pageQuery.getUEnableMedium()),
                String.valueOf(pageQuery.getUEnableLong()),
                String.valueOf(pageQuery.getUTierMin()),
                String.valueOf(pageQuery.getRMinAmountWan()),
                String.valueOf(pageQuery.getRCapitulationDayPct()),
                String.valueOf(pageQuery.getRCapitulationWeekPct()),
                String.valueOf(pageQuery.getRMinDrawdownPct()),
                String.valueOf(pageQuery.getRCapitulationLookbackDays()),
                String.valueOf(pageQuery.getRCapitulationLookbackWeeks()),
                String.valueOf(pageQuery.getREnableModeA()),
                String.valueOf(pageQuery.getREnableModeB()),
                String.valueOf(pageQuery.getREnableModeC()),
                String.valueOf(pageQuery.getRTierMin()),
                String.valueOf(pageQuery.getPMinAmountWan()),
                String.valueOf(pageQuery.getPEnableShort()),
                String.valueOf(pageQuery.getPEnableMedium()),
                String.valueOf(pageQuery.getPEnableLong()),
                String.valueOf(pageQuery.getPTierMin()),
                String.valueOf(pageQuery.getCMinAmountWan()),
                String.valueOf(pageQuery.getCEnableShort()),
                String.valueOf(pageQuery.getCEnableMedium()),
                String.valueOf(pageQuery.getCEnableLong()),
                String.valueOf(pageQuery.getCTierMin()));
    }

    public void clearRecommendCache() {
        recommendCache.clear();
        log.info("recommend cache cleared");
    }

    private StockTarget buildStockTarget(StockBase stockBase, StrategyTypeEnum strategyTypeEnum) {
        StockQuoteUtils.overlayLatestDayQuote(stockBase);
        StockTarget stockTarget = new StockTarget();
        stockTarget.setCode(stockBase.getCode());
        stockTarget.setDay(stockBase.getDay());
        stockTarget.setName(stockBase.getName());
        stockTarget.setStrategy(strategyTypeEnum.getCode());
        stockTarget.setClose(stockBase.getClose() != null ? stockBase.getClose() : 0D);
        stockTarget.setNewFlag(true);
        stockTarget.setTrendMessage(stockBase.getTrendMessage());
        stockTarget.setSignalMessage(stockBase.getSignalMessage());
        stockTarget.setChangeRate(stockBase.getChangeRate() != null ? stockBase.getChangeRate() : 0D);
        stockTarget.setMainBusiness(stockBase.getMainBusiness());
        return stockTarget;
    }

    private QueryContextParam buildQueryContextParam(StockPageQuery stockPageQuery) {
        return QueryContextParam.builder()
                .lianBanDays(Objects.nonNull(stockPageQuery.getLianBanDays())? stockPageQuery.getLianBanDays(): NumberUtils.INTEGER_ONE)
                .unilateral(stockPageQuery.toUnilateralParams())
                .preGolden(stockPageQuery.toPreGoldenParams())
                .resonance(stockPageQuery.toResonanceParams())
                .rebound(stockPageQuery.toReboundParams())
                .build();

    }

}
