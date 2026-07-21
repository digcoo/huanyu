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
        pageResult.getItems().forEach(this::slimTargetForList);

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
        StrategyTypeEnum type = pageQuery.getStrategyTypeEnum();
        if (type == null || !type.isActive()) {
            log.warn("rescan unsupported strategy: {}", pageQuery.getStrategy());
            return ResponseUtil.fail(ResponseUtil.OPERATE_FAILED);
        }
        QueryContextParam contextParam = buildQueryContextParam(pageQuery);
        int saved = stockTargetScheduler.recommendSaveInternal(
                contextParam, true, type);
        warmRecommendCache(pageQuery);
        Map<String, Object> data = new HashMap<>();
        data.put("strategy", type.getCode());
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
                String.valueOf(pageQuery.getREnableShort()),
                String.valueOf(pageQuery.getREnableMedium()),
                String.valueOf(pageQuery.getREnableLong()),
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
                String.valueOf(pageQuery.getCTierMin()),
                String.valueOf(pageQuery.getLMinAmountWan()),
                String.valueOf(pageQuery.getLTierMin()),
                String.valueOf(pageQuery.getLMin30BodyPct()),
                String.valueOf(pageQuery.getLMin30PrevDays()),
                String.valueOf(pageQuery.getLMin30BarsPerDay()),
                String.valueOf(pageQuery.getTMinAmountWan()),
                String.valueOf(pageQuery.getTTierMin()),
                String.valueOf(pageQuery.getTEnableBear()),
                String.valueOf(pageQuery.getTEnableBull()),
                String.valueOf(pageQuery.getTEnableUltra()),
                String.valueOf(pageQuery.getTEnableShort()),
                String.valueOf(pageQuery.getTEnableMedium()),
                String.valueOf(pageQuery.getTEnableLong()),
                String.valueOf(pageQuery.getG2MinAmountWan()),
                String.valueOf(pageQuery.getG2TierMin()),
                String.valueOf(pageQuery.getG2EnableShort()),
                String.valueOf(pageQuery.getG2EnableMedium()),
                String.valueOf(pageQuery.getG2EnableLong()),
                String.valueOf(pageQuery.getG2LookbackShort()),
                String.valueOf(pageQuery.getG2LookbackMedium()),
                String.valueOf(pageQuery.getG2LookbackLong()),
                String.valueOf(pageQuery.getD2MinAmountWan()),
                String.valueOf(pageQuery.getD2TierMin()),
                String.valueOf(pageQuery.getD2EnableShort()),
                String.valueOf(pageQuery.getD2EnableLong()),
                String.valueOf(pageQuery.getD2LookbackShort()),
                String.valueOf(pageQuery.getD2LookbackLong()),
                String.valueOf(pageQuery.getUlMinAmountWan()),
                String.valueOf(pageQuery.getUlRequireMonthMacd()),
                String.valueOf(pageQuery.getUlRequireWeekMacd()),
                String.valueOf(pageQuery.getUlRequireDayMacd()),
                String.valueOf(pageQuery.getUlRequireDayMacdNegative()),
                String.valueOf(pageQuery.getUlRequireWeekMacdNegative()),
                String.valueOf(pageQuery.getUlRequireMonthMacdNegative()),
                String.valueOf(pageQuery.getUlRequireCurrentBreakout()),
                String.valueOf(pageQuery.getTrMinAmountWan()),
                String.valueOf(pageQuery.getTrPrevWeeks()),
                String.valueOf(pageQuery.getTrRequireCurrentBreakout()),
                String.valueOf(pageQuery.getTrRequireMonthMacd()),
                String.valueOf(pageQuery.getTrRequireWeekMacd()),
                String.valueOf(pageQuery.getTrRequireDayMacd()),
                String.valueOf(pageQuery.getTrRequireWeekGoldenCross()),
                String.valueOf(pageQuery.getTrRequireUltra()),
                String.valueOf(pageQuery.getMdMinAmountWan()),
                String.valueOf(pageQuery.getMdPrevMonths()),
                String.valueOf(pageQuery.getMdRequireCurrentBreakout()),
                String.valueOf(pageQuery.getMdRequireMonthMacd()),
                String.valueOf(pageQuery.getMdRequireYearMacd()),
                String.valueOf(pageQuery.getMdRequireMonthGoldenCross()),
                String.valueOf(pageQuery.getMdRequireUltra()),
                String.valueOf(pageQuery.getLgMinAmountWan()),
                String.valueOf(pageQuery.getLgPrevYears()),
                String.valueOf(pageQuery.getLgRequireCurrentBreakout()),
                String.valueOf(pageQuery.getLgRequireYearMacd()),
                String.valueOf(pageQuery.getLgRequireMonthMacd()),
                String.valueOf(pageQuery.getLgRequireYearGoldenCross()),
                String.valueOf(pageQuery.getLgRequireUltra()),
                String.valueOf(pageQuery.getNrfActiveTier()),
                String.valueOf(pageQuery.getCaEnableDualLowGate()),
                String.valueOf(pageQuery.getCaEnableMacdGate()),
                String.valueOf(pageQuery.getCaEnableMacdDcHighGate()),
                String.valueOf(pageQuery.getCaEnableCrossLowGate()),
                String.valueOf(pageQuery.getCaEnableDay()),
                String.valueOf(pageQuery.getCaEnableWeek()),
                String.valueOf(pageQuery.getCaEnableMonth()),
                String.valueOf(pageQuery.getCaLookbackDay()),
                String.valueOf(pageQuery.getCaLookbackWeek()),
                String.valueOf(pageQuery.getCaLookbackMonth()),
                String.valueOf(pageQuery.getCaRequireUltra()),
                String.valueOf(pageQuery.getCaMinAmountWan()),
                String.valueOf(pageQuery.getLdEnableDualLowGate()),
                String.valueOf(pageQuery.getLdEnableMacdGate()),
                String.valueOf(pageQuery.getLdEnableMacdDcHighGate()),
                String.valueOf(pageQuery.getLdEnableCrossLowGate()),
                String.valueOf(pageQuery.getLdEnableBarHighGate()),
                String.valueOf(pageQuery.getLdEnableDay()),
                String.valueOf(pageQuery.getLdEnableWeek()),
                String.valueOf(pageQuery.getLdEnableMonth()),
                String.valueOf(pageQuery.getLdLookbackDay()),
                String.valueOf(pageQuery.getLdLookbackWeek()),
                String.valueOf(pageQuery.getLdLookbackMonth()),
                String.valueOf(pageQuery.getLdRequireUltra()),
                String.valueOf(pageQuery.getLdMinAmountWan()),
                String.valueOf(pageQuery.getMeEnableDualLowGate()),
                String.valueOf(pageQuery.getMeEnableMacdGate()),
                String.valueOf(pageQuery.getMeEnableMacdDcHighGate()),
                String.valueOf(pageQuery.getMeEnableCrossLowGate()),
                String.valueOf(pageQuery.getMeEnableBarHighGate()),
                String.valueOf(pageQuery.getMeEnableAllYangGate()),
                String.valueOf(pageQuery.getMeEnableMin30()),
                String.valueOf(pageQuery.getMeEnableDay()),
                String.valueOf(pageQuery.getMeEnableWeek()),
                String.valueOf(pageQuery.getMeEnableMonth()),
                String.valueOf(pageQuery.getMeEnableYear()),
                String.valueOf(pageQuery.getMeLookbackMin30()),
                String.valueOf(pageQuery.getMeLookbackDay()),
                String.valueOf(pageQuery.getMeLookbackWeek()),
                String.valueOf(pageQuery.getMeLookbackMonth()),
                String.valueOf(pageQuery.getMeLookbackYear()),
                String.valueOf(pageQuery.getMeMinAmountWan()),
                String.valueOf(pageQuery.getWcvEnableAllYangGate()),
                String.valueOf(pageQuery.getWcvEnableMin30Gate()),
                String.valueOf(pageQuery.getWcvEnableLastHighBreak()),
                String.valueOf(pageQuery.getWcvEnableLastMedianBreak()),
                String.valueOf(pageQuery.getWcvEnableLastLowBreak()),
                String.valueOf(pageQuery.getWcvEnableDay()),
                String.valueOf(pageQuery.getWcvEnableWeek()),
                String.valueOf(pageQuery.getWcvEnableMonth()),
                String.valueOf(pageQuery.getWcvLookbackDay()),
                String.valueOf(pageQuery.getWcvLookbackWeek()),
                String.valueOf(pageQuery.getWcvLookbackMonth()),
                String.valueOf(pageQuery.getWcvMinAmountWan()),
                String.valueOf(pageQuery.getWccEnableAllYangGate()),
                String.valueOf(pageQuery.getWccEnableMin30Gate()),
                String.valueOf(pageQuery.getWccEnableLastHighBreak()),
                String.valueOf(pageQuery.getWccEnableLastMedianBreak()),
                String.valueOf(pageQuery.getWccEnableLastLowBreak()),
                String.valueOf(pageQuery.getWccEnableDay()),
                String.valueOf(pageQuery.getWccEnableWeek()),
                String.valueOf(pageQuery.getWccEnableMonth()),
                String.valueOf(pageQuery.getWccLookbackDay()),
                String.valueOf(pageQuery.getWccLookbackWeek()),
                String.valueOf(pageQuery.getWccLookbackMonth()),
                String.valueOf(pageQuery.getWccMinAmountWan()),
                String.valueOf(pageQuery.getWcvdEnableAllYangGate()),
                String.valueOf(pageQuery.getWcvdEnableMin30Gate()),
                String.valueOf(pageQuery.getWcvdEnableLastHighBreak()),
                String.valueOf(pageQuery.getWcvdEnableLastMedianBreak()),
                String.valueOf(pageQuery.getWcvdEnableLastLowBreak()),
                String.valueOf(pageQuery.getWcvdEnableDay()),
                String.valueOf(pageQuery.getWcvdEnableWeek()),
                String.valueOf(pageQuery.getWcvdEnableMonth()),
                String.valueOf(pageQuery.getWcvdLookbackDay()),
                String.valueOf(pageQuery.getWcvdLookbackWeek()),
                String.valueOf(pageQuery.getWcvdLookbackMonth()),
                String.valueOf(pageQuery.getWcvdMinAmountWan()),
                String.valueOf(pageQuery.getWccdEnableAllYangGate()),
                String.valueOf(pageQuery.getWccdEnableMin30Gate()),
                String.valueOf(pageQuery.getWccdEnableLastHighBreak()),
                String.valueOf(pageQuery.getWccdEnableLastMedianBreak()),
                String.valueOf(pageQuery.getWccdEnableLastLowBreak()),
                String.valueOf(pageQuery.getWccdEnableDay()),
                String.valueOf(pageQuery.getWccdEnableWeek()),
                String.valueOf(pageQuery.getWccdEnableMonth()),
                String.valueOf(pageQuery.getWccdLookbackDay()),
                String.valueOf(pageQuery.getWccdLookbackWeek()),
                String.valueOf(pageQuery.getWccdLookbackMonth()),
                String.valueOf(pageQuery.getWccdMinAmountWan()),
                String.valueOf(pageQuery.getCwcvEnableAllYangGate()),
                String.valueOf(pageQuery.getCwcvEnableMin30Gate()),
                String.valueOf(pageQuery.getCwcvEnableBandLastYangLowGate()),
                String.valueOf(pageQuery.getCwcvEnableYangBandTrendGate()),
                String.valueOf(pageQuery.getCwcvEnablePrevBandBreak()),
                String.valueOf(pageQuery.getCwcvEnableDay()),
                String.valueOf(pageQuery.getCwcvEnableWeek()),
                String.valueOf(pageQuery.getCwcvEnableMonth()),
                String.valueOf(pageQuery.getCwcvLookbackDay()),
                String.valueOf(pageQuery.getCwcvLookbackWeek()),
                String.valueOf(pageQuery.getCwcvLookbackMonth()),
                String.valueOf(pageQuery.getCwcvLookbackYear()),
                String.valueOf(pageQuery.getCwcvMinAmountWan()),
                String.valueOf(pageQuery.getCwcavEnableAllYangGate()),
                String.valueOf(pageQuery.getCwcavEnableMin30Gate()),
                String.valueOf(pageQuery.getCwcavEnableBandLastYangLowGate()),
                String.valueOf(pageQuery.getCwcavEnableYangBandTrendGate()),
                String.valueOf(pageQuery.getCwcavEnablePrevBandBreak()),
                String.valueOf(pageQuery.getCwcavEnableDay()),
                String.valueOf(pageQuery.getCwcavEnableWeek()),
                String.valueOf(pageQuery.getCwcavEnableMonth()),
                String.valueOf(pageQuery.getCwcavLookbackDay()),
                String.valueOf(pageQuery.getCwcavLookbackWeek()),
                String.valueOf(pageQuery.getCwcavLookbackMonth()),
                String.valueOf(pageQuery.getCwcavLookbackYear()),
                String.valueOf(pageQuery.getCwcavMinAmountWan()),
                String.valueOf(pageQuery.getCwcvdEnableAllYangGate()),
                String.valueOf(pageQuery.getCwcvdEnableMin30Gate()),
                String.valueOf(pageQuery.getCwcvdEnableBandLastYangLowGate()),
                String.valueOf(pageQuery.getCwcvdEnableYangBandTrendGate()),
                String.valueOf(pageQuery.getCwcvdEnablePrevBandBreak()),
                String.valueOf(pageQuery.getCwcvdEnableDay()),
                String.valueOf(pageQuery.getCwcvdEnableWeek()),
                String.valueOf(pageQuery.getCwcvdEnableMonth()),
                String.valueOf(pageQuery.getCwcvdLookbackDay()),
                String.valueOf(pageQuery.getCwcvdLookbackWeek()),
                String.valueOf(pageQuery.getCwcvdLookbackMonth()),
                String.valueOf(pageQuery.getCwcvdLookbackYear()),
                String.valueOf(pageQuery.getCwcvdMinAmountWan()),
                String.valueOf(pageQuery.getCwcadEnableAllYangGate()),
                String.valueOf(pageQuery.getCwcadEnableMin30Gate()),
                String.valueOf(pageQuery.getCwcadEnableBandLastYangLowGate()),
                String.valueOf(pageQuery.getCwcadEnableYangBandTrendGate()),
                String.valueOf(pageQuery.getCwcadEnablePrevBandBreak()),
                String.valueOf(pageQuery.getCwcadEnableDay()),
                String.valueOf(pageQuery.getCwcadEnableWeek()),
                String.valueOf(pageQuery.getCwcadEnableMonth()),
                String.valueOf(pageQuery.getCwcadLookbackDay()),
                String.valueOf(pageQuery.getCwcadLookbackWeek()),
                String.valueOf(pageQuery.getCwcadLookbackMonth()),
                String.valueOf(pageQuery.getCwcadLookbackYear()),
                String.valueOf(pageQuery.getCwcadMinAmountWan()),
                String.valueOf(pageQuery.getWbLookbackDay()),
                String.valueOf(pageQuery.getWbLookbackWeek()),
                String.valueOf(pageQuery.getWbLookbackMonth()),
                String.valueOf(pageQuery.getWbMinAmountWan()),
                String.valueOf(pageQuery.getWbsLookbackDay()),
                String.valueOf(pageQuery.getWbsLookbackWeek()),
                String.valueOf(pageQuery.getWbsMinAmountWan()),
                String.valueOf(pageQuery.getWbmLookbackDay()),
                String.valueOf(pageQuery.getWbmLookbackMonth()),
                String.valueOf(pageQuery.getWbmMinAmountWan()),
                String.valueOf(pageQuery.getWpgTier()),
                String.valueOf(pageQuery.getWpgLookbackDay()),
                String.valueOf(pageQuery.getWpgLookbackWeek()),
                String.valueOf(pageQuery.getWpgLookbackMonth()),
                String.valueOf(pageQuery.getWpgEnableMinAmountFilter()),
                String.valueOf(pageQuery.getWpgEnableMaxBandLowGate()),
                String.valueOf(pageQuery.getWpgEnableConcaveBreakout()),
                String.valueOf(pageQuery.getWpgEnableConvexBreakout()),
                String.valueOf(pageQuery.getWpgMinAmountWan()),
                String.valueOf(pageQuery.getWpgEnableUpperPeriodMinBandLowGate()),
                String.valueOf(pageQuery.getWpgEnableTierMacdPositive()),
                String.valueOf(pageQuery.getWpgEnableWeekMonthBandShapeGate()),
                String.valueOf(pageQuery.getWpgEnableWeekMonthBandLowGate()),
                String.valueOf(pageQuery.getWpgEnableYearWeekMonthYangGate()),
                String.valueOf(pageQuery.getWpgLookbackYear()),
                String.valueOf(pageQuery.getMctTier()),
                String.valueOf(pageQuery.getMctLookbackDay()),
                String.valueOf(pageQuery.getMctLookbackWeek()),
                String.valueOf(pageQuery.getMctLookbackMonth()),
                String.valueOf(pageQuery.getMctEnableGoldenCross()),
                String.valueOf(pageQuery.getMctEnableDeathCross()),
                String.valueOf(pageQuery.getMctEnableGoldenCrossRiseGate()),
                String.valueOf(pageQuery.getCltTier()),
                String.valueOf(pageQuery.getCltLookbackDay()),
                String.valueOf(pageQuery.getBbtTier()),
                String.valueOf(pageQuery.getBbtLookbackBars()),
                String.valueOf(pageQuery.getMgcTier()),
                String.valueOf(pageQuery.getMgcEnableMinAmountFilter()),
                String.valueOf(pageQuery.getMgcMinAmountWan()),
                String.valueOf(pageQuery.getMgcEnableSignalRiseGate()),
                String.valueOf(pageQuery.getMgcSignalRisePct()),
                String.valueOf(pageQuery.getMgcEnableHistoryRiseGate()),
                String.valueOf(pageQuery.getMgcHistoryLookbackBars()),
                String.valueOf(pageQuery.getMgcHistoryRisePct()),
                String.valueOf(pageQuery.getMgcwhTier()),
                String.valueOf(pageQuery.getMgcwhLookbackDay()),
                String.valueOf(pageQuery.getMgcwhLookbackWeek()),
                String.valueOf(pageQuery.getMgcwhLookbackMonth()),
                String.valueOf(pageQuery.getMgcwhEnableMinAmountFilter()),
                String.valueOf(pageQuery.getMgcwhMinAmountWan()),
                String.valueOf(pageQuery.getMgcwhEnableSignalRiseGate()),
                String.valueOf(pageQuery.getMgcwhSignalRisePct()),
                String.valueOf(pageQuery.getMgcwhrTier()),
                String.valueOf(pageQuery.getMgcwhrLookbackDay()),
                String.valueOf(pageQuery.getMgcwhrLookbackWeek()),
                String.valueOf(pageQuery.getMgcwhrLookbackMonth()),
                String.valueOf(pageQuery.getMgcwhrEnableMinAmountFilter()),
                String.valueOf(pageQuery.getMgcwhrMinAmountWan()),
                String.valueOf(pageQuery.getMgcwhuTier()),
                String.valueOf(pageQuery.getMgcwhuLookbackDay()),
                String.valueOf(pageQuery.getMgcwhuLookbackWeek()),
                String.valueOf(pageQuery.getMgcwhuLookbackMonth()),
                String.valueOf(pageQuery.getMgcwhuEnableMinAmountFilter()),
                String.valueOf(pageQuery.getMgcwhuMinAmountWan()),
                String.valueOf(pageQuery.getMgcwhuEnableSignalRiseGate()),
                String.valueOf(pageQuery.getMgcwhuSignalRisePct()),
                String.valueOf(pageQuery.getMdcbTier()),
                String.valueOf(pageQuery.getMdcbLookbackDay()),
                String.valueOf(pageQuery.getMdcbLookbackWeek()),
                String.valueOf(pageQuery.getMdcbLookbackMonth()),
                String.valueOf(pageQuery.getMdcbEnableMinAmountFilter()),
                String.valueOf(pageQuery.getMdcbMinAmountWan()),
                String.valueOf(pageQuery.getMdcbEnableSignalRiseGate()),
                String.valueOf(pageQuery.getMdcbSignalRisePct()),
                String.valueOf(pageQuery.getWccbTier()),
                String.valueOf(pageQuery.getWccbLookbackDay()),
                String.valueOf(pageQuery.getWccbLookbackWeek()),
                String.valueOf(pageQuery.getWccbLookbackMonth()),
                String.valueOf(pageQuery.getWccbEnableMinAmountFilter()),
                String.valueOf(pageQuery.getWccbMinAmountWan()),
                String.valueOf(pageQuery.getWccbEnableSignalRiseGate()),
                String.valueOf(pageQuery.getWccbSignalRisePct()),
                String.valueOf(pageQuery.getMgRequireDayMacd()),
                String.valueOf(pageQuery.getMgRequireWeekMacd()),
                String.valueOf(pageQuery.getMgRequireMonthMacd()),
                String.valueOf(pageQuery.getUlgcPrevDays()),
                String.valueOf(pageQuery.getUlgcMaxBarsPerDay()),
                String.valueOf(pageQuery.getUlgcGcLookbackBars()),
                String.valueOf(pageQuery.getUlgcSignalRisePct()),
                String.valueOf(pageQuery.getDm60PrevDays()),
                String.valueOf(pageQuery.getDm60MaxBarsPerDay()),
                String.valueOf(pageQuery.getDm60GcLookbackBars()),
                String.valueOf(pageQuery.getDm60EnableMinAmountFilter()),
                String.valueOf(pageQuery.getDm60MinAmountWan()),
                String.valueOf(pageQuery.getDm60EnableSignalRiseGate()),
                String.valueOf(pageQuery.getDm60SignalRisePct()));
    }

    /** 重跑后预热 findMy 缓存，避免小程序二次全市场扫描超时 */
    private void warmRecommendCache(StockPageQuery pageQuery) {
        List<StockTarget> items = doQuery(pageQuery);
        recommendCache.put(buildRecommendCacheKey(pageQuery),
                new CachedRecommendations(items, System.currentTimeMillis()));
        log.info("recommend cache warmed, strategy={}, size={}", pageQuery.getStrategy(), items.size());
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

    /** 列表 API 精简 payload：主营摘要在详情页加载 */
    private void slimTargetForList(StockTarget target) {
        if (target == null) {
            return;
        }
        target.setMainBusiness(null);
        target.setTrendMessage(truncateForList(target.getTrendMessage(), 200));
        target.setSignalMessage(truncateForList(target.getSignalMessage(), 200));
    }

    private static String truncateForList(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "…";
    }

    private QueryContextParam buildQueryContextParam(StockPageQuery stockPageQuery) {
        return QueryContextParam.builder()
                .lianBanDays(Objects.nonNull(stockPageQuery.getLianBanDays())? stockPageQuery.getLianBanDays(): NumberUtils.INTEGER_ONE)
                .unilateral(stockPageQuery.toUnilateralParams())
                .preGolden(stockPageQuery.toPreGoldenParams())
                .resonance(stockPageQuery.toResonanceParams())
                .rebound(stockPageQuery.toReboundParams())
                .ultraLow(stockPageQuery.toUltraLowParams())
                .retest(stockPageQuery.toRetestParams())
                .gc2(stockPageQuery.toGc2Params())
                .dc2(stockPageQuery.toDc2Params())
                .ultraShort(stockPageQuery.toUltraShortParams())
                .trendV2(stockPageQuery.toTrendV2Params())
                .medium(stockPageQuery.toMediumParams())
                .longTerm(stockPageQuery.toLongParams())
                .frictionlessLadder(stockPageQuery.toFrictionlessParams())
                .pillar(stockPageQuery.toPillarParams())
                .cascade(stockPageQuery.toCascadeParams())
                .ladderDip(stockPageQuery.toLadderDipParams())
                .macdEdge(stockPageQuery.toMacdEdgeParams())
                .waveConvex(stockPageQuery.toWaveConvexParams())
                .waveConcave(stockPageQuery.toWaveConcaveParams())
                .waveConvexDay(stockPageQuery.toWaveConvexDayParams())
                .waveConcaveDay(stockPageQuery.toWaveConcaveDayParams())
                .cascadeWaveConvex(stockPageQuery.toCascadeWaveConvexParams())
                .cascadeWaveConcave(stockPageQuery.toCascadeWaveConcaveParams())
                .cascadeWaveConvexDay(stockPageQuery.toCascadeWaveConvexDayParams())
                .cascadeWaveConcaveDay(stockPageQuery.toCascadeWaveConcaveDayParams())
                .waveBand(stockPageQuery.toWaveBandParams())
                .waveBandShort(stockPageQuery.toWaveBandShortParams())
                .waveBandMedium(stockPageQuery.toWaveBandMediumParams())
                .wavePeriodGate(stockPageQuery.toWavePeriodGateParams())
                .macdCrossTier(stockPageQuery.toMacdCrossTierParams())
                .convexLiftTier(stockPageQuery.toConvexLiftTierParams())
                .bodyBarTier(stockPageQuery.toBodyBarTierParams())
                .macdGoldenCross(stockPageQuery.toMacdGoldenCrossParams())
                .macdGcWaveHigh(stockPageQuery.toMacdGcWaveHighParams())
                .macdGcWaveHighRetest(stockPageQuery.toMacdGcWaveHighRetestParams())
                .macdGcWaveHighLift(stockPageQuery.toMacdGcWaveHighLiftParams())
                .macdDcBreakout(stockPageQuery.toMacdDcBreakoutParams())
                .macdPositiveGate(stockPageQuery.toMacdPositiveGateParams())
                .ultraGcBreakout(stockPageQuery.toUltraGcBreakoutParams())
                .waveCcBreakout(stockPageQuery.toWaveCcBreakoutParams())
                .dayMin60Combo(stockPageQuery.toDayMin60ComboParams())
                .weekMin60Combo(stockPageQuery.toWeekMin60ComboParams())
                .dayWeekCombo(stockPageQuery.toDayWeekComboParams())
                .dayMonthCombo(stockPageQuery.toDayMonthComboParams())
                .min60WaveCcBreakout(stockPageQuery.toMin60WaveCcBreakoutParams())
                .dayWaveCcBreakout(stockPageQuery.toDayWaveCcBreakoutParams())
                .weekWaveCcBreakout(stockPageQuery.toWeekWaveCcBreakoutParams())
                .monthWaveCcBreakout(stockPageQuery.toMonthWaveCcBreakoutParams())
                .build();

    }

}
