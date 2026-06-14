package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dto.atlas.*;
import com.yh.bigdata.tts.common.model.StockAnnualReport;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import com.yh.bigdata.tts.common.utils.StockQuoteUtils;
import com.yh.bigdata.tts.common.dao.StockIndustryBenchmarkMapper;
import com.yh.bigdata.tts.common.model.StockIndustryBenchmark;
import com.yh.bigdata.tts.spider.service.AtlasAnnualReportService;
import com.yh.bigdata.tts.spider.service.AtlasDetailComputeService;
import com.yh.bigdata.tts.spider.service.AtlasIndustryChainService;
import com.yh.bigdata.tts.spider.service.AtlasStockApiService;
import com.yh.bigdata.tts.spider.service.StockService;
import com.yh.bigdata.tts.spider.utils.SinaIndexClient;
import com.yh.bigdata.tts.spider.utils.SinaIndexClient.IndexDef;
import com.yh.bigdata.tts.spider.utils.SinaIndexClient.Quote;
import com.yh.bigdata.tts.spider.utils.XueQiuHttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AtlasStockApiServiceImpl implements AtlasStockApiService {

    private static final Logger log = LoggerFactory.getLogger(AtlasStockApiServiceImpl.class);

    @Autowired
    private StockBaseMapper stockBaseMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private AtlasAnnualReportService atlasAnnualReportService;

    @Autowired
    private AtlasDetailComputeService atlasDetailComputeService;

    @Autowired
    private StockIndustryBenchmarkMapper stockIndustryBenchmarkMapper;

    @Autowired
    private AtlasIndustryChainService atlasIndustryChainService;

    @Override
    public boolean isCacheReady() {
        return RealtimeStockCache.filterStockMap != null && !RealtimeStockCache.filterStockMap.isEmpty();
    }

    @Override
    public List<AtlasStockSummaryVo> search(String keyword, int limit) {
        if (StringUtils.isBlank(keyword)) {
            return Collections.emptyList();
        }
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return stockBaseMapper.searchByKeyword(keyword.trim(), safeLimit).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public AtlasStockSummaryVo getSummary(String code) {
        StockBase stock = requireStock(code);
        return toSummary(stock);
    }

    @Override
    public List<AtlasKlineBarVo> getKlines(String code, String period, int limit) {
        StockBase stock = requireStock(code);
        PeriodTypeEnum periodType = PeriodTypeEnum.getByCode(period);
        if (periodType == null) {
            periodType = PeriodTypeEnum.WEEK;
        }
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        List<Trade> trades = RealtimeStockCache.getLastTrades(stock, periodType, safeLimit);
        return trades.stream().map(this::toKlineBar).collect(Collectors.toList());
    }

    @Override
    public List<AtlasKlineBarVo> refreshKlines(String code, String period, int limit) {
        StockBase stock = requireStock(code);
        PeriodTypeEnum periodType = PeriodTypeEnum.getByCode(period);
        if (periodType == null) {
            periodType = PeriodTypeEnum.WEEK;
        }
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        String url = String.format(
                XueQiuHttpUtils.base_url,
                stock.getCode().toUpperCase(),
                System.currentTimeMillis(),
                periodType.getCode(),
                -safeLimit);
        try {
            String json = XueQiuHttpUtils.getData(url);
            if (StringUtils.isBlank(json) || "null".equals(json.trim())) {
                return Collections.emptyList();
            }
            List<Trade> trades = XueQiuHttpUtils.parseStockTrades(json, stock, Trade.class);
            if (trades == null || trades.isEmpty()) {
                return Collections.emptyList();
            }
            if (trades.size() > safeLimit) {
                trades = new ArrayList<>(trades.subList(trades.size() - safeLimit, trades.size()));
            }
            return trades.stream().map(this::toKlineBar).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("refreshKlines failed, code={}, period={}", stock.getCode(), periodType.getCode(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public AtlasStockDetailVo getDetail(String code) {
        StockBase stock = requireStock(code);
        StockBase dbStock = stockBaseMapper.selectByPrimaryKey(stock.getCode());
        if (dbStock != null) {
            mergeDetailFields(stock, dbStock);
        }
        atlasAnnualReportService.refreshIfMissingAsync(stock.getCode(), stock.getName());

        StockAnnualReport latest = atlasAnnualReportService.getLatest(stock.getCode());
        atlasIndustryChainService.ensureSegments(stock.getCode());
        Map<String, Object> profile = atlasAnnualReportService.buildProfileFromAnnual(stock, latest);
        enrichProfile(stock, profile);
        List<Map<String, String>> keyMetrics = buildKeyMetrics(stock, latest);

        String industry = StringUtils.defaultIfBlank(stock.getIndustry(), "综合");
        StockIndustryBenchmark benchmark = stockIndustryBenchmarkMapper.selectByIndustry(industry);

        Map<String, Object> stageRaw = atlasDetailComputeService.computeStage(latest);
        String stageHint = (String) stageRaw.remove("stageHint");
        Map<String, Object> health = atlasDetailComputeService.computeHealth(stock, latest, benchmark);
        Map<String, Object> radar = atlasDetailComputeService.computeRadar(latest, benchmark);

        StockQuoteUtils.overlayLatestDayQuote(stock);
        return AtlasStockDetailVo.builder()
                .code(stock.getCode())
                .name(stock.getName())
                .market("cn")
                .price(stock.getClose())
                .changePct(roundPct(stock.getChangeRate()))
                .industry(industry)
                .mainBusiness(displayBusiness(stock))
                .businessBrief(displayBusiness(stock))
                .businessScope(stock.getBusinessScope())
                .profile(profile)
                .keyMetrics(keyMetrics)
                .stage(stageRaw)
                .stageHint(stageHint)
                .healthScore((Integer) health.get("healthScore"))
                .healthRank((String) health.get("healthRank"))
                .healthBreakdown((Map<String, Integer>) health.get("healthBreakdown"))
                .radar(radar)
                .competitors(atlasDetailComputeService.buildCompetitorList(stock.getCode()))
                .industryChain(atlasIndustryChainService.buildIndustryChain(stock.getCode()))
                .portraitDimensions(buildPortraitDimensions(stageRaw, health, radar))
                .build();
    }

    private void mergeDetailFields(StockBase target, StockBase db) {
        target.setMainBusiness(db.getMainBusiness());
        target.setBusinessScope(db.getBusinessScope());
        target.setBusinessBrief(db.getBusinessBrief());
        target.setIndustry(db.getIndustry());
        target.setIndustryCsrc(db.getIndustryCsrc());
        target.setOrgProfile(db.getOrgProfile());
        target.setPeTtm(db.getPeTtm());
        target.setPb(db.getPb());
        target.setPsTtm(db.getPsTtm());
        target.setDividendYield(db.getDividendYield());
        target.setHigh52w(db.getHigh52w());
        target.setLow52w(db.getLow52w());
        target.setTotalMvYi(db.getTotalMvYi());
        target.setQuancheng(db.getQuancheng());
    }

    private void enrichProfile(StockBase stock, Map<String, Object> profile) {
        if (StringUtils.isNotBlank(stock.getIndustry())) {
            profile.put("industryTag", stock.getIndustry());
        }
    }

    private String displayBusiness(StockBase stock) {
        if (stock == null) {
            return null;
        }
        if (StringUtils.isNotBlank(stock.getBusinessBrief())) {
            return stock.getBusinessBrief();
        }
        return stock.getMainBusiness();
    }

    private List<Map<String, String>> buildKeyMetrics(StockBase stock, StockAnnualReport latest) {
        List<Map<String, String>> metrics = new ArrayList<>(atlasAnnualReportService.buildKeyMetricsFromAnnual(stock, latest));
        if (stock.getPeTtm() != null) {
            metrics.add(metric("PE(TTM)", formatNum(stock.getPeTtm())));
        }
        if (stock.getPb() != null) {
            metrics.add(metric("PB", formatNum(stock.getPb())));
        }
        if (stock.getDividendYield() != null && stock.getDividendYield() > 0) {
            metrics.add(metric("股息率", formatNum(stock.getDividendYield()) + "%"));
        }
        if (stock.getTotalMvYi() != null) {
            metrics.add(metric("总市值", formatNum(stock.getTotalMvYi()) + "亿"));
        }
        if (stock.getHigh52w() != null) {
            metrics.add(metric("52周最高", formatNum(stock.getHigh52w())));
        }
        if (stock.getLow52w() != null) {
            metrics.add(metric("52周最低", formatNum(stock.getLow52w())));
        }
        return metrics;
    }

    private List<Map<String, Object>> buildPortraitDimensions(Map<String, Object> stage,
                                                              Map<String, Object> health,
                                                              Map<String, Object> radar) {
        Map<String, Integer> breakdown = (Map<String, Integer>) health.get("healthBreakdown");
        List<Map<String, Object>> insights = radar != null ? (List<Map<String, Object>>) radar.get("insights") : Collections.emptyList();
        List<Map<String, Object>> dims = new ArrayList<>();
        dims.add(portrait("profit", "盈利质量", breakdown.get("profit"),
                insightText(insights, 0, "ROE 与净利率综合评估")));
        dims.add(portrait("growth", "成长动能", breakdown.get("growth"),
                stage.get("label") + "阶段，" + stage.get("desc")));
        dims.add(portrait("debt", "财务安全", breakdown.get("debt"),
                insightText(insights, 3, "偿债与杠杆综合评估")));
        dims.add(portrait("operation", "运营效率", breakdown.get("operation"),
                insightText(insights, 1, "毛利率与运营效率评估")));
        return dims;
    }

    private Map<String, Object> portrait(String key, String label, Integer score, String text) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("key", key);
        m.put("label", label);
        m.put("score", score != null ? score : 60);
        m.put("text", text);
        return m;
    }

    private String insightText(List<Map<String, Object>> insights, int idx, String fallback) {
        if (insights != null && insights.size() > idx && insights.get(idx).get("text") != null) {
            return String.valueOf(insights.get(idx).get("text"));
        }
        return fallback;
    }

    @Override
    public Map<String, AtlasCompassModuleVo> getCompass(String code) {
        StockBase stock = requireStock(code);
        StockBase dbStock = stockBaseMapper.selectByPrimaryKey(stock.getCode());
        if (dbStock != null) {
            mergeDetailFields(stock, dbStock);
        }
        atlasAnnualReportService.refreshIfMissingAsync(stock.getCode(), stock.getName());

        Map<String, AtlasCompassModuleVo> fromAnnual = atlasAnnualReportService.buildCompassFromAnnual(stock);
        if (fromAnnual != null && !fromAnnual.isEmpty()) {
            return fromAnnual;
        }
        return buildCompassFromKline(stock);
    }

    @Override
    public List<AtlasMarketIndexVo> getMarketIndices(String market, String period, int limit) {
        if (!"cn".equalsIgnoreCase(StringUtils.defaultString(market, "cn"))) {
            return Collections.emptyList();
        }
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        List<AtlasMarketIndexVo> result = new ArrayList<>();
        for (IndexDef def : SinaIndexClient.CN_INDICES) {
            Quote quote = SinaIndexClient.fetchQuote(def);
            List<AtlasKlineBarVo> klines = SinaIndexClient.fetchKlines(def.symbol, period, safeLimit);
            result.add(AtlasMarketIndexVo.builder()
                    .code(def.symbol)
                    .displayCode(def.displayCode)
                    .name(def.name)
                    .price(quote != null ? quote.price : null)
                    .changePct(quote != null ? quote.changePct : null)
                    .klines(klines)
                    .build());
        }
        return result;
    }

    private Map<String, AtlasCompassModuleVo> buildCompassFromKline(StockBase stock) {
        List<Trade> yearBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.YEAR, 8);
        if (yearBars.isEmpty()) {
            yearBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.MONTH, 24);
        }

        Map<String, AtlasCompassModuleVo> compass = new LinkedHashMap<>();
        compass.put("financial", module("财务动能", "#0ecb81",
                "年报数据待爬取；暂以 K 线 proxy 展示。",
                Arrays.asList(
                        chart("收盘价", "元", "#0ecb81", seriesFrom(yearBars, SeriesType.CLOSE)),
                        chart("成交额", "亿", "#f0b90b", seriesFrom(yearBars, SeriesType.AMOUNT_YI)),
                        chart("成交量", "万手", "#848e9c", seriesFrom(yearBars, SeriesType.VOLUME_WAN))
                )));
        compass.put("operation", module("运营人效", "#f0b90b",
                "年报数据待爬取；暂以换手率 proxy。",
                Collections.singletonList(
                        chart("换手率", "%", "#0ecb81", seriesFrom(yearBars, SeriesType.TURNOVER))
                )));
        compass.put("chain", module("产业链地位", "#a78bfa",
                "年报数据待爬取；暂以振幅 proxy。",
                Collections.singletonList(
                        chart("振幅", "%", "#ff9500", seriesFrom(yearBars, SeriesType.SHOCK))
                )));
        compass.put("capital", module("资本结构", "#f6465d",
                "年报数据待爬取；暂展示成交额趋势。",
                Collections.singletonList(
                        chart("成交额", "亿", "#f6465d", seriesFrom(yearBars, SeriesType.AMOUNT_YI))
                )));
        return compass;
    }

    @Override
    public StockBase requireStock(String code) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        StockBase stock = RealtimeStockCache.getStockBase(normalized);
        if (stock == null) {
            stock = stockService.findStockBase(normalized);
            if (stock == null) {
                throw new NoSuchElementException("stock not found: " + normalized);
            }
        }
        StockQuoteUtils.overlayLatestDayQuote(stock);
        return stock;
    }

    private AtlasStockSummaryVo toSummary(StockBase stock) {
        StockQuoteUtils.overlayLatestDayQuote(stock);
        return AtlasStockSummaryVo.builder()
                .code(stock.getCode())
                .name(stock.getName())
                .market("cn")
                .price(stock.getClose())
                .changePct(roundPct(stock.getChangeRate()))
                .mainBusiness(displayBusiness(stock))
                .summary(StringUtils.defaultIfBlank(displayBusiness(stock), stock.getName()))
                .trendMessage(stock.getTrendMessage())
                .signalMessage(stock.getSignalMessage())
                .build();
    }

    private AtlasKlineBarVo toKlineBar(Trade trade) {
        return AtlasKlineBarVo.builder()
                .day(trade.getDay())
                .open(nullSafe(trade.getOpen()))
                .high(nullSafe(trade.getHigh()))
                .low(nullSafe(trade.getLow()))
                .close(nullSafe(trade.getClose()))
                .build();
    }

    private AtlasCompassModuleVo module(String title, String color, String insight, List<AtlasChartVo> charts) {
        return AtlasCompassModuleVo.builder()
                .title(title)
                .color(color)
                .insight(insight)
                .charts(charts)
                .build();
    }

    private AtlasChartVo chart(String name, String unit, String color, List<AtlasSeriesPointVo> data) {
        return AtlasChartVo.builder()
                .name(name)
                .unit(unit)
                .color(color)
                .data(data)
                .build();
    }

    private enum SeriesType {
        CLOSE, AMOUNT_YI, VOLUME_WAN, TURNOVER, SHOCK
    }

    private List<AtlasSeriesPointVo> seriesFrom(List<Trade> bars, SeriesType type) {
        return bars.stream()
                .map(bar -> AtlasSeriesPointVo.builder()
                        .year(extractYearLabel(bar.getDay()))
                        .value(round2(resolveValue(bar, type)))
                        .build())
                .collect(Collectors.toList());
    }

    private double resolveValue(Trade bar, SeriesType type) {
        switch (type) {
            case AMOUNT_YI:
                return bar.getAmount() == null ? 0 : bar.getAmount() / 100_000_000D;
            case VOLUME_WAN:
                return bar.getVolume() == null ? 0 : bar.getVolume() / 10000D;
            case TURNOVER:
                return bar.getTurnoverRate() == null ? 0 : bar.getTurnoverRate() * 100;
            case SHOCK:
                return bar.getLow() == null || bar.getLow() == 0 ? 0 : (bar.getHigh() - bar.getLow()) / bar.getLow() * 100;
            case CLOSE:
            default:
                return bar.getClose() == null ? 0 : bar.getClose();
        }
    }

    private String extractYearLabel(String day) {
        if (StringUtils.isBlank(day)) {
            return "";
        }
        return day.length() >= 4 ? day.substring(0, 4) : day;
    }

    private Map<String, String> metric(String label, String value) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        return item;
    }

    private String formatNum(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private double nullSafe(Double value) {
        return value == null ? 0D : value;
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private Double roundPct(Double rate) {
        if (rate == null) {
            return 0D;
        }
        return round2(rate * 100);
    }

    private String formatPrice(double price) {
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatPct(Double rate) {
        if (rate == null) {
            return "0%";
        }
        return round2(rate * 100) + "%";
    }

    private String formatAmountYi(Double amount) {
        if (amount == null) {
            return "0";
        }
        return round2(amount / 100_000_000D) + "亿";
    }
}
