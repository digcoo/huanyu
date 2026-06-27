package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.backtest.BacktestSnapshotContext;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.dto.atlas.BacktestResultVo;
import com.yh.bigdata.tts.common.dto.atlas.BacktestStrategyOptionVo;
import com.yh.bigdata.tts.common.dto.atlas.BacktestSummaryVo;
import com.yh.bigdata.tts.common.dto.atlas.BacktestTradeVo;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.BacktestParam;
import com.yh.bigdata.tts.common.param.QueryContextParam;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.service.BacktestService;
import com.yh.bigdata.tts.spider.strategy.AbstractStrategy;
import com.yh.bigdata.tts.spider.strategy.tools.StockEvaluationScratchpad;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BacktestServiceImpl implements BacktestService {

    private static final Set<StrategyTypeEnum> BACKTEST_STRATEGIES = EnumSet.of(
            StrategyTypeEnum.ULTRA_SHORT
    );

    private static final Pattern TIER_PATTERN = Pattern.compile("\\[([SAB])\\]");

    @Autowired
    private List<AbstractStrategy> strategies;

    private Map<StrategyTypeEnum, AbstractStrategy> strategyMap;

    @PostConstruct
    public void init() {
        strategyMap = new HashMap<>();
        for (AbstractStrategy strategy : strategies) {
            strategyMap.put(strategy.getStrategy(), strategy);
        }
    }

    @Override
    public boolean isCacheReady() {
        return RealtimeStockCache.filterStockMap != null
                && !RealtimeStockCache.filterStockMap.isEmpty()
                && RealtimeStockCache.dayMap != null
                && !RealtimeStockCache.dayMap.isEmpty();
    }

    @Override
    public List<BacktestStrategyOptionVo> listStrategies() {
        return BACKTEST_STRATEGIES.stream()
                .sorted(Comparator.comparingInt(StrategyTypeEnum::getGroupOrder))
                .map(type -> BacktestStrategyOptionVo.builder()
                        .code(type.getCode())
                        .name(type.getDesc())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public BacktestResultVo run(BacktestParam param) {
        if (!isCacheReady()) {
            throw new IllegalStateException("K线缓存未就绪，请确认后端已加载 RealtimeStockCache");
        }

        long startMs = System.currentTimeMillis();
        BacktestParam safe = param != null ? param : new BacktestParam();
        safe.setStrategy(StringUtils.defaultIfBlank(safe.getStrategy(), "ultra"));
        StrategyTypeEnum strategyType = StrategyTypeEnum.getByCode(safe.getStrategy());
        if (!BACKTEST_STRATEGIES.contains(strategyType)) {
            throw new IllegalArgumentException("不支持的回测策略: " + safe.getStrategy());
        }

        AbstractStrategy strategy = strategyMap.get(strategyType);
        if (strategy == null) {
            throw new IllegalArgumentException("策略未注册: " + strategyType.getCode());
        }

        int scanDays = clamp(safe.getDays(), 30, 730, 365);
        int holdDays = clamp(safe.getHoldDays(), 1, 60, 10);
        int maxStocks = clamp(safe.getMaxStocks(), 1, 2000, 200);
        int cooldown = safe.effectiveCooldown();
        double winThreshold = safe.effectiveWinThreshold();
        QueryContextParam ctx = mergeContext(safe.getParams());

        List<StockBase> stocks = pickStocks(safe.getCodes(), maxStocks);
        List<BacktestTradeVo> trades = new ArrayList<>();

        for (StockBase stock : stocks) {
            scanStock(stock, strategy, ctx, scanDays, holdDays, cooldown, winThreshold, trades);
        }

        trades.sort(Comparator.comparing(BacktestTradeVo::getSignalDay).reversed());

        BacktestSummaryVo summary = buildSummary(strategyType, scanDays, holdDays, stocks.size(),
                trades, System.currentTimeMillis() - startMs);

        return BacktestResultVo.builder()
                .summary(summary)
                .trades(trades)
                .build();
    }

    private void scanStock(StockBase stock, AbstractStrategy strategy, QueryContextParam ctx,
                           int scanDays, int holdDays, int cooldown, double winThreshold,
                           List<BacktestTradeVo> out) {
        int fetchBars = scanDays + holdDays + 120;
        List<Trade> dayBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, fetchBars);
        if (dayBars == null || dayBars.size() <= holdDays + 5) {
            return;
        }

        int scanStart = Math.max(0, dayBars.size() - scanDays - holdDays);
        int lastSignalIdx = -cooldown;

        for (int i = scanStart; i < dayBars.size() - holdDays; i++) {
            if (i - lastSignalIdx < cooldown) {
                continue;
            }

            Trade signalBar = dayBars.get(i);
            if (signalBar.getClose() == null || signalBar.getClose() <= 0) {
                continue;
            }

            StockBase asOfStock = cloneAtBar(stock, signalBar);
            CheckResult checkResult = BacktestSnapshotContext.runWithSnapshot(
                    stock.getCode(),
                    signalBar.getDay(),
                    () -> StockEvaluationScratchpad.runWithScratchpad(
                            () -> strategy.check(asOfStock, null, null, ctx)));

            if (checkResult == null || !checkResult.isSuccess()) {
                continue;
            }

            Trade exitBar = dayBars.get(i + holdDays);
            if (exitBar.getClose() == null || exitBar.getClose() <= 0) {
                continue;
            }

            double entry = signalBar.getClose();
            double exit = exitBar.getClose();
            double pnlPct = (exit - entry) / entry * 100.0;
            boolean win = pnlPct > winThreshold;

            out.add(BacktestTradeVo.builder()
                    .code(stock.getCode())
                    .name(stock.getName())
                    .signalDay(signalBar.getDay())
                    .exitDay(exitBar.getDay())
                    .entryPrice(round2(entry))
                    .exitPrice(round2(exit))
                    .pnlPct(round2(pnlPct))
                    .win(win)
                    .tier(extractTier(checkResult.getTrendMessage()))
                    .trendMessage(checkResult.getTrendMessage())
                    .signalMessage(checkResult.getSignalMessage())
                    .build());

            lastSignalIdx = i;
        }
    }

    private List<StockBase> pickStocks(String codes, int maxStocks) {
        if (StringUtils.isNotBlank(codes)) {
            return Arrays.stream(codes.split("[,，\\s]+"))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(StockCodeUtil::normalizeCnCode)
                    .distinct()
                    .map(RealtimeStockCache::getStockBase)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return RealtimeStockCache.filterStockMap.values().stream()
                .limit(maxStocks)
                .collect(Collectors.toList());
    }

    private static StockBase cloneAtBar(StockBase source, Trade bar) {
        StockBase copy = new StockBase();
        copy.setCode(source.getCode());
        copy.setName(source.getName());
        copy.setExchange(source.getExchange());
        copy.setMainBusiness(source.getMainBusiness());
        copy.setDay(bar.getDay());
        copy.setOpen(bar.getOpen());
        copy.setHigh(bar.getHigh());
        copy.setLow(bar.getLow());
        copy.setClose(bar.getClose());
        copy.setAmount(bar.getAmount());
        return copy;
    }

    private static QueryContextParam mergeContext(QueryContextParam incoming) {
        QueryContextParam base = QueryContextParam.empty();
        if (incoming == null) {
            return base;
        }
        if (incoming.getUnilateral() != null) {
            base.setUnilateral(com.yh.bigdata.tts.common.param.UnilateralStrategyParams.merge(incoming.getUnilateral()));
        }
        if (incoming.getGc2() != null) {
            base.setGc2(com.yh.bigdata.tts.common.param.Gc2StrategyParams.merge(incoming.getGc2()));
        }
        if (incoming.getDc2() != null) {
            base.setDc2(com.yh.bigdata.tts.common.param.Dc2StrategyParams.merge(incoming.getDc2()));
        }
        if (incoming.getResonance() != null) {
            base.setResonance(incoming.getResonance());
        }
        if (incoming.getRebound() != null) {
            base.setRebound(incoming.getRebound());
        }
        if (incoming.getUltraLow() != null) {
            base.setUltraLow(incoming.getUltraLow());
        }
        if (incoming.getRetest() != null) {
            base.setRetest(incoming.getRetest());
        }
        if (incoming.getPreGolden() != null) {
            base.setPreGolden(incoming.getPreGolden());
        }
        if (incoming.getUltraShort() != null) {
            base.setUltraShort(com.yh.bigdata.tts.common.param.UltraShortStrategyParams.merge(incoming.getUltraShort()));
        }
        if (incoming.getTrendV2() != null) {
            base.setTrendV2(com.yh.bigdata.tts.common.param.TrendV2StrategyParams.merge(incoming.getTrendV2()));
        }
        if (incoming.getMedium() != null) {
            base.setMedium(com.yh.bigdata.tts.common.param.MediumStrategyParams.merge(incoming.getMedium()));
        }
        if (incoming.getLongTerm() != null) {
            base.setLongTerm(com.yh.bigdata.tts.common.param.LongStrategyParams.merge(incoming.getLongTerm()));
        }
        return base;
    }

    private static BacktestSummaryVo buildSummary(StrategyTypeEnum strategyType, int scanDays, int holdDays,
                                                  int stockCount, List<BacktestTradeVo> trades, long elapsedMs) {
        int signalCount = trades.size();
        int winCount = (int) trades.stream().filter(t -> Boolean.TRUE.equals(t.getWin())).count();
        int lossCount = signalCount - winCount;
        double winRate = signalCount == 0 ? 0 : (double) winCount / signalCount;
        double avgPnl = signalCount == 0 ? 0 : trades.stream()
                .mapToDouble(t -> t.getPnlPct() != null ? t.getPnlPct() : 0)
                .average()
                .orElse(0);
        double maxWin = trades.stream()
                .mapToDouble(t -> t.getPnlPct() != null ? t.getPnlPct() : Double.NEGATIVE_INFINITY)
                .max()
                .orElse(0);
        double maxLoss = trades.stream()
                .mapToDouble(t -> t.getPnlPct() != null ? t.getPnlPct() : Double.POSITIVE_INFINITY)
                .min()
                .orElse(0);

        return BacktestSummaryVo.builder()
                .strategy(strategyType.getCode())
                .strategyName(strategyType.getDesc())
                .scanDays(scanDays)
                .holdDays(holdDays)
                .stockCount(stockCount)
                .signalCount(signalCount)
                .winCount(winCount)
                .lossCount(lossCount)
                .winRate(round4(winRate))
                .winRateText(String.format(Locale.CHINA, "%.1f%%", winRate * 100))
                .avgPnlPct(round2(avgPnl))
                .avgPnlText(String.format(Locale.CHINA, "%+.2f%%", avgPnl))
                .maxWinPct(round2(maxWin))
                .maxLossPct(round2(maxLoss))
                .elapsedMs(elapsedMs)
                .build();
    }

    private static String extractTier(String trendMessage) {
        if (StringUtils.isBlank(trendMessage)) {
            return "";
        }
        Matcher m = TIER_PATTERN.matcher(trendMessage);
        return m.find() ? m.group(1) : "";
    }

    private static int clamp(Integer value, int min, int max, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Math.max(min, Math.min(max, value));
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static double round4(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
