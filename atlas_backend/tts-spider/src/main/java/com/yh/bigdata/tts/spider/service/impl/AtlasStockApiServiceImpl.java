package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dto.atlas.*;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.StockCodeUtil;
import com.yh.bigdata.tts.spider.service.AtlasStockApiService;
import com.yh.bigdata.tts.spider.service.StockService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AtlasStockApiServiceImpl implements AtlasStockApiService {

    @Autowired
    private StockBaseMapper stockBaseMapper;

    @Autowired
    private StockService stockService;

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
    public AtlasStockDetailVo getDetail(String code) {
        StockBase stock = requireStock(code);
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("businessOneLiner", StringUtils.defaultIfBlank(stock.getMainBusiness(), stock.getName() + " · A股标的"));
        profile.put("industryPosition", "基于行情与主营业务数据的 Demo 详情，完整财报画像后续接入。");
        profile.put("strengths", Arrays.asList("纳入 Atlas 行情缓存", "支持多周期 K 线查询"));
        profile.put("risks", Arrays.asList("财务深度数据待接入", "请以官方披露为准"));

        List<Map<String, String>> keyMetrics = new ArrayList<>();
        keyMetrics.add(metric("最新价", formatPrice(stock.getTrade())));
        keyMetrics.add(metric("涨跌幅", formatPct(stock.getChangeRate())));
        if (stock.getTurnoverRate() != null) {
            keyMetrics.add(metric("换手率", formatPct(stock.getTurnoverRate())));
        }
        if (stock.getAmount() != null) {
            keyMetrics.add(metric("成交额", formatAmountYi(stock.getAmount())));
        }

        return AtlasStockDetailVo.builder()
                .code(stock.getCode())
                .name(stock.getName())
                .market("cn")
                .price(stock.getTrade())
                .changePct(roundPct(stock.getChangeRate()))
                .industry("综合")
                .mainBusiness(stock.getMainBusiness())
                .profile(profile)
                .keyMetrics(keyMetrics)
                .build();
    }

    @Override
    public Map<String, AtlasCompassModuleVo> getCompass(String code) {
        StockBase stock = requireStock(code);
        List<Trade> yearBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.YEAR, 8);
        if (yearBars.isEmpty()) {
            yearBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.MONTH, 24);
        }

        Map<String, AtlasCompassModuleVo> compass = new LinkedHashMap<>();
        compass.put("financial", module("财务动能", "#0ecb81",
                "基于年/月 K 线收盘价、成交额、成交量序列（Demo 聚合，非财报口径）。",
                Arrays.asList(
                        chart("收盘价", "元", "#0ecb81", seriesFrom(yearBars, SeriesType.CLOSE)),
                        chart("成交额", "亿", "#f0b90b", seriesFrom(yearBars, SeriesType.AMOUNT_YI)),
                        chart("成交量", "万手", "#848e9c", seriesFrom(yearBars, SeriesType.VOLUME_WAN))
                )));
        compass.put("operation", module("运营人效", "#f0b90b",
                "以换手率为 proxy 观察活跃度变化（有数据则展示）。",
                Collections.singletonList(
                        chart("换手率", "%", "#0ecb81", seriesFrom(yearBars, SeriesType.TURNOVER))
                )));
        compass.put("chain", module("产业链地位", "#a78bfa",
                "以 K 线振幅观察波动区间（Demo proxy，非毛利率）。",
                Collections.singletonList(
                        chart("振幅", "%", "#ff9500", seriesFrom(yearBars, SeriesType.SHOCK))
                )));
        compass.put("capital", module("资本结构", "#f6465d",
                "资本结构深度指标待财报爬虫接入；暂展示成交额趋势。",
                Collections.singletonList(
                        chart("成交额", "亿", "#f6465d", seriesFrom(yearBars, SeriesType.AMOUNT_YI))
                )));
        return compass;
    }

    @Override
    public StockBase requireStock(String code) {
        String normalized = StockCodeUtil.normalizeCnCode(code);
        StockBase cached = RealtimeStockCache.getStockBase(normalized);
        if (cached != null) {
            return cached;
        }
        StockBase fromDb = stockService.findStockBase(normalized);
        if (fromDb == null) {
            throw new NoSuchElementException("stock not found: " + normalized);
        }
        return fromDb;
    }

    private AtlasStockSummaryVo toSummary(StockBase stock) {
        return AtlasStockSummaryVo.builder()
                .code(stock.getCode())
                .name(stock.getName())
                .market("cn")
                .price(stock.getTrade())
                .changePct(roundPct(stock.getChangeRate()))
                .mainBusiness(stock.getMainBusiness())
                .summary(StringUtils.defaultIfBlank(stock.getMainBusiness(), stock.getName()))
                .trendMessage(stock.getTrendMessage())
                .signalMessage(stock.getSignalMessage())
                .build();
    }

    private AtlasKlineBarVo toKlineBar(Trade trade) {
        return AtlasKlineBarVo.builder()
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
