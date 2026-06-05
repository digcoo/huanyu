package com.yh.bigdata.tts.common.constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yh.bigdata.tts.common.indicator.ZaoPanUtils;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockTarget;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.DateUtil;

public class RealtimeStockCache {

//	public static List<StockBase> filterStocks = null;
	public static Map<String, StockBase> filterStockMap = Maps.newConcurrentMap();
	public static Map<String, List> min30Map = Maps.newConcurrentMap();
	public static Map<String, List> min60Map = Maps.newConcurrentMap();
	public static Map<String, List> dayMap = Maps.newConcurrentMap();
	public static Map<String, List> weekMap = Maps.newConcurrentMap();
	public static Map<String, List> monthMap = Maps.newConcurrentMap();
	public static Map<String, List> yearMap = Maps.newConcurrentMap();
	public static Map<String, List> quarterMap = Maps.newConcurrentMap();
	public static Set<StockTarget> oldTargetStocks = Sets.newConcurrentHashSet();

    //code -> 时间窗口
    public static final Map<String, Long> lastSentTimeMap = new ConcurrentHashMap<>();

    //时间窗口 -> 代码list
    public static final Map<Long, Set<StockBase>> windowToCodeMap = new ConcurrentHashMap<>();


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List<Trade> getAllTrades(String code, PeriodTypeEnum monthOrWeek) {
		List<Trade> trades = null;
		switch (monthOrWeek) {
		case YEAR:
			trades = RealtimeStockCache.yearMap.get(code);
			break;
		case QUARTER:
			trades = RealtimeStockCache.quarterMap.get(code);
			break;
		case MONTH:
			trades = RealtimeStockCache.monthMap.get(code);
			break;
		case WEEK:
			trades = RealtimeStockCache.weekMap.get(code);
			break;
		case DAY:
			trades = RealtimeStockCache.dayMap.get(code);
			break;
		case MIN30:
			trades = RealtimeStockCache.min30Map.get(code);
			break;
		default:
			break;
		}
		return trades == null ? new ArrayList() : trades;
	}

	public static StockBase getStockBase(String code) {
		return filterStockMap.get(code);
	}

	public static Trade getLastTrade(StockBase stockBase, PeriodTypeEnum monthOrWeek, int leftOffset) {
		leftOffset = -Math.abs(leftOffset);
		List<Trade> trades = getAllTrades(stockBase.getCode(), monthOrWeek);
		if (trades != null && trades.size() > Math.abs(leftOffset)) {
			return trades.get(trades.size() - 1 + leftOffset);
		}
		return null;
	}

    public static Trade getLastZaoPanTrade(StockBase stockBase, PeriodTypeEnum periodTypeEnum, int leftOffset) {
        leftOffset = -Math.abs(leftOffset);
        List<Trade> trades = getLastZaoPanTrades(stockBase, periodTypeEnum,100);
        if (trades != null && trades.size() > Math.abs(leftOffset)) {
            return trades.get(trades.size() - 1 + leftOffset);
        }
        return null;
    }

	public static List<Trade> getLastTrades(StockBase stockBase, PeriodTypeEnum monthOrWeek, int num) {
		List<Trade> trades = getAllTrades(stockBase.getCode(), monthOrWeek);
		if (trades.size() > num) {
			return trades.subList(trades.size() - num, trades.size());
		}
		return trades;
	}

    public static List<Trade> getLastZaoPanTrades(StockBase stockBase, PeriodTypeEnum periodTypeEnum, int num) {
        List<Trade> trades = getAllTrades(stockBase.getCode(), periodTypeEnum);
        List<Trade> allZaoPanList = ZaoPanUtils.getAllZaoPanList(trades);
        if (allZaoPanList.size() > num) {
            return allZaoPanList.subList(allZaoPanList.size() - num, allZaoPanList.size());
        }
        return allZaoPanList;
    }

	public static List<Trade> getLastTrades(StockBase stockBase, PeriodTypeEnum monthOrWeek, long fromTime,
			long toTime) {

		List<Trade> trades = getAllTrades(stockBase.getCode(), monthOrWeek);
		return trades.stream().filter(trade -> {
			long time = DateUtil.parseDate(trade.getDay()).getTime();

			return time >= fromTime && time <= toTime;
		}).collect(Collectors.toList());
	}

    public static long getCurrentWindow() {
        return System.currentTimeMillis() / (30 * 60 * 1000);
    }
}
