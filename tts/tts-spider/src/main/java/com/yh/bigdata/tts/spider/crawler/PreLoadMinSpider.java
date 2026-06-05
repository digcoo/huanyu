package com.yh.bigdata.tts.spider.crawler;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.StockFenshi;
import com.yh.bigdata.tts.common.model.StockMin15;
import com.yh.bigdata.tts.common.model.StockMin30;
import com.yh.bigdata.tts.common.model.StockMin60;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.model.StockWeek;
import com.yh.bigdata.tts.common.utils.DateUtil;
import com.yh.bigdata.tts.spider.utils.SinaHttpUtils;

/**
 * @author duyp
 * 
 * @date 2021/09/24
 * 
 * @comment
 */
@Slf4j
public class PreLoadMinSpider {

	Logger logger = LoggerFactory.getLogger(PreLoadMinSpider.class);

	String stock_realtime_url = "http://hq.sinajs.cn/list=%s";
	String stock_min15_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=15&ma=%s&datalen=%s";
	String stock_min30_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=30&ma=%s&datalen=%s";
	String stock_min60_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=60&ma=%s&datalen=%s";
	
	int sleep = 30;
	
	public void run() {
		try {

			spiderRealtime();
			spiderStockMin30();
			spiderStockMin60();
			
		} catch (Exception e) {
			logger.error("RealTimeMinSpider run exception...", e);
		}
	}
	
	private void spiderRealtime() throws ClientProtocolException, IOException, InterruptedException {
		
		long start = System.currentTimeMillis();
		logger.info("start spider pre load realtime...");
		
		List<List<StockBase>> stockSplits = Lists.partition(Lists.newArrayList(RealtimeStockCache.filterStockMap.values()), 100);
		for (int k = 0; k < stockSplits.size(); k++) {
			try {
				List<String> codes = stockSplits.get(k).stream().map(StockBase::getCode).collect(Collectors.toList());
				String codestr = JSON.toJSONString(codes);
				String url = String.format(stock_realtime_url, codestr.substring(1, codestr.length() - 1).replace("\"", ""));
				String ret_renshi = Request.Get(url).execute().returnContent().asString();
				if (StringUtils.isNotBlank(ret_renshi) && !ret_renshi.equals("null")) {
					
					String[] split = ret_renshi.split("\n");
					for (int i = 0; i < split.length; i++) {
						StockFenshi stockFenshi = SinaHttpUtils.parseStockFenshi(split[i]);
						if (stockFenshi.getTime().compareTo("15:00:00") >= 0) {
							stockFenshi.setTime("15:00:00");
						}
						
//						logger.info("stock fenshi " + JSON.toJSONString(stockFenshi));
						
//						if (!stockFenshi.getDay().equals(DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd"))) {
//							continue;
//						}
						
						StockBase stockBase = new StockBase();
						stockBase.setName(stockFenshi.getName());
						stockBase.setCode(stockFenshi.getCode());
						stockBase.setOpen(stockFenshi.getOpen());
						stockBase.setTrade(stockFenshi.getTrade());
						stockBase.setDay(stockFenshi.getDay());
						stockBase.setLastTrade(stockFenshi.getLastTrade());
						stockBase.setHigh(stockFenshi.getHigh());
						stockBase.setLow(stockFenshi.getLow());
						stockBase.setAmount(stockFenshi.getAmount());
						stockBase.setVolume(stockFenshi.getVolume());
						stockBase.setIsTrade(stockFenshi.getOpen() < 0.1 || stockBase.getName().contains("退")? false : true);
						
//						stockBase.setBuy1Price(stockFenshi.getBuy1Price());
//						stockBase.setBuy1Volume(stockFenshi.getBuy1Volume());
//						stockBase.setBuy2Price(stockFenshi.getBuy2Price());
//						stockBase.setBuy2Volume(stockFenshi.getBuy2Volume());
//						stockBase.setBuy3Price(stockFenshi.getBuy3Price());
//						stockBase.setBuy3Volume(stockFenshi.getBuy3Volume());
//						stockBase.setBuy4Price(stockFenshi.getBuy4Price());
//						stockBase.setBuy4Volume(stockFenshi.getBuy4Volume());
//						stockBase.setBuy5Price(stockFenshi.getBuy5Price());
//						stockBase.setBuy5Volume(stockFenshi.getBuy5Volume());
//						stockBase.setSell1Price(stockFenshi.getSell1Price());
//						stockBase.setSell1Volume(stockFenshi.getSell1Volume());
//						stockBase.setSell2Price(stockFenshi.getSell2Price());
//						stockBase.setSell2Volume(stockFenshi.getSell2Volume());
//						stockBase.setSell3Price(stockFenshi.getSell3Price());
//						stockBase.setSell3Volume(stockFenshi.getSell3Volume());
//						stockBase.setSell4Price(stockFenshi.getSell4Price());
//						stockBase.setSell4Volume(stockFenshi.getSell4Volume());
//						stockBase.setSell5Price(stockFenshi.getSell5Price());
//						stockBase.setSell5Volume(stockFenshi.getSell5Volume());
						
						updateStockDay(stockBase);
						updateStockWeek(stockBase);
						updateStockMonth(stockBase);
						
					}
				}
				logger.info("realtime spider(pre load), partition = {}, cost = {}", (k + 1), (System.currentTimeMillis() - start) / 1000);

			} catch (Exception e) {
				logger.error("RealTimeMinSpider exception.. ", e);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void spiderStockMin30() throws ClientProtocolException, IOException {
		logger.info("start spider(pre load) min30...");

		long start = System.currentTimeMillis();
		
		int min30Num = DateUtil.getMANumFrom0930(30);
		if (min30Num < 1) {
			return;
		}

		List<StockMin30> days = null;
		for (StockBase stockBase : RealtimeStockCache.filterStockMap.values()) {
			try {
				String url = String.format(stock_min30_url
						, stockBase.getCode()
						, URLEncoder.encode(JSON.toJSONString(Lists.newArrayList(1,5,10,20,30)))
						, min30Num);
				String ret_day = Request.Get(url).execute().returnContent().asString();
				ret_day = ret_day.substring(ret_day.indexOf("(")+1, ret_day.indexOf(")"));
				if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
					days = JSON.parseArray(ret_day, StockMin30.class);
//					logger.info("min30 : {} : {}", stockBase.getCode(),JSON.toJSONString(days));
				}
				
				for (StockMin30 stockMin30 : days) {
						
					if (stockMin30.getOpen() < 0.001) {
						continue;
					}
					
					stockMin30.setCode(stockBase.getCode());
					
					if (!RealtimeStockCache.min30Map.get(stockBase.getCode()).contains(stockMin30)) {
						RealtimeStockCache.min30Map.get(stockBase.getCode()).add(stockMin30);
					}
				}
				
				
				//MA
				List<StockMin30> stockMin30s = RealtimeStockCache.min30Map.get(stockBase.getCode());
				for (int i = 0; i < min30Num; i++) {
					int offset = i;
					List<StockMin30> min30_120s = stockMin30s.subList(stockMin30s.size()<120 + offset? 0 : stockMin30s.size() - 120 - offset, stockMin30s.size() - offset);
					StockMin30 stockMin30_0 = stockMin30s.get(stockMin30s.size() - offset - 1);
					
					try {
						Double min30_ma120 = min30_120s.subList(min30_120s.size() - 120, min30_120s.size()).stream().mapToDouble(StockMin30::getTrade).average().getAsDouble();
						stockBase.setMa120(new BigDecimal(min30_ma120).setScale(3, RoundingMode.UP).doubleValue());
						stockMin30_0.setMa120(new BigDecimal(min30_ma120).setScale(3, RoundingMode.UP).doubleValue());
					} catch (Exception e) {
					}
					try {
						Double min30_ma60 = min30_120s.subList(min30_120s.size() - 60, min30_120s.size()).stream().mapToDouble(StockMin30::getTrade).average().getAsDouble();
						stockBase.setMa60(new BigDecimal(min30_ma60).setScale(3, RoundingMode.UP).doubleValue());
						stockMin30_0.setMa60(new BigDecimal(min30_ma60).setScale(3, RoundingMode.UP).doubleValue());
					} catch (Exception e) {
					}
				}
				
				Thread.sleep(sleep);

			} catch (Exception e) {
				logger.error("RealTimeMinSpider exception....", e);
				if (e instanceof HttpResponseException ) {
					break;
				}
			}
		}
		logger.info("spider min30 cost = {}", (System.currentTimeMillis() - start) / 1000);
	}
	
	@SuppressWarnings("deprecation")
	private void spiderStockMin60() throws ClientProtocolException, IOException {
			
		long start = System.currentTimeMillis();
		
		logger.info("start spider(pre load) min60...");

		
		int min60Num = DateUtil.getMANumFrom0930(60);
		if (min60Num < 1) {
			return;
		}

		List<StockMin60> days = null;
		for (StockBase stockBase : RealtimeStockCache.filterStockMap.values()) {
			try {
				String url = String.format(stock_min60_url
						, stockBase.getCode()
						, URLEncoder.encode(JSON.toJSONString(Lists.newArrayList(1,5,10,20,30)))
						, min60Num);
				String ret_day = Request.Get(url).execute().returnContent().asString();
				ret_day = ret_day.substring(ret_day.indexOf("(")+1, ret_day.indexOf(")"));
				if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
					days = JSON.parseArray(ret_day, StockMin60.class);
//					logger.info("min60 : {} : {}", stockBase.getCode(),JSON.toJSONString(days));
				}
				for (StockMin60 stockMin60 : days) {
						
					if (stockMin60.getOpen() < 0.001) {
						continue;
					}
					
					stockMin60.setCode(stockBase.getCode());
					
					if (!RealtimeStockCache.min60Map.get(stockBase.getCode()).contains(stockMin60)) {
						RealtimeStockCache.min60Map.get(stockBase.getCode()).add(stockMin60);
					}
				}
				
				//MA
				List<StockMin60> stockMin60s = RealtimeStockCache.min60Map.get(stockBase.getCode());
				for (int i = 0; i < min60Num; i++) {
					int offset = i;
					
					StockMin60 stockMin60_0 = stockMin60s.get(stockMin60s.size() - offset - 1);
					
					List<StockMin60> min60_120s = stockMin60s.subList(stockMin60s.size()<120 + offset? 0 : stockMin60s.size() - 120 - offset, stockMin60s.size() - offset);
					try {
						Double min60_ma120 = min60_120s.subList(min60_120s.size() - 120, min60_120s.size()).stream().mapToDouble(StockMin60::getTrade).average().getAsDouble();
						stockBase.setMa120(new BigDecimal(min60_ma120).setScale(3, RoundingMode.UP).doubleValue());
						stockMin60_0.setMa120(new BigDecimal(min60_ma120).setScale(3, RoundingMode.UP).doubleValue());
					} catch (Exception e) {
					}
					try {
						Double min60_ma60 = min60_120s.subList(min60_120s.size() - 60, min60_120s.size()).stream().mapToDouble(StockMin60::getTrade).average().getAsDouble();
						stockBase.setMa60(new BigDecimal(min60_ma60).setScale(3, RoundingMode.UP).doubleValue());
						stockMin60_0.setMa60(new BigDecimal(min60_ma60).setScale(3, RoundingMode.UP).doubleValue());
					} catch (Exception e) {
					}
				}
				
				Thread.sleep(sleep);

			} catch (Exception e) {
				logger.error("RealTimeMinSpider exception....", e);
				if (e instanceof HttpResponseException ) {
					break;
				}
			}

		}
		logger.info("spider min60 cost = {}", (System.currentTimeMillis() - start) / 1000);
	}
	
	
	public void updateStockDay(StockBase stockBase) {
		String thisDay = DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd");
		
		List<StockDay> stockDays = RealtimeStockCache.dayMap.get(stockBase.getCode());
		StockDay stockDay0 = stockDays.get(stockDays.size() - 1);
		if (!stockDay0.equals(stockDay0)){
			StockDay stockDay = new StockDay();
			stockDay.setCode(stockBase.getCode());
			stockDay.setTrade(stockBase.getTrade());
			stockDay.setOpen(stockBase.getOpen());
			stockDay.setHigh(stockBase.getHigh());
			stockDay.setLow(stockBase.getLow());
			stockDay.setLastTrade(stockBase.getLastTrade());
			stockDay.setVolume(stockBase.getVolume());
			stockDay.setAmount(stockBase.getAmount());
			stockDay.setDay(thisDay);
			RealtimeStockCache.dayMap.get(stockBase.getCode()).add(stockDay);
		}else {
			stockDay0.setTrade(stockBase.getTrade());
			stockDay0.setHigh(stockBase.getHigh() > stockDay0.getHigh()?stockBase.getHigh():stockDay0.getHigh());
			stockDay0.setLow(stockBase.getLow() < stockDay0.getLow()?stockBase.getLow():stockDay0.getLow());
			stockDay0.setVolume(stockBase.getVolume());
			stockDay0.setAmount(stockBase.getAmount());
		}
		
		//MA
		List<StockDay> day_120s = stockDays.subList(stockDays.size()<120? 0 : stockDays.size() - 120, stockDays.size());
		try {
			Double day_ma120 = day_120s.subList(day_120s.size() - 120, day_120s.size()).stream().mapToDouble(StockDay::getTrade).average().getAsDouble();
			stockBase.setMa120(new BigDecimal(day_ma120).setScale(3, RoundingMode.UP).doubleValue());
			stockDay0.setMa120(new BigDecimal(day_ma120).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double day_ma60 = day_120s.subList(day_120s.size() - 60, day_120s.size()).stream().mapToDouble(StockDay::getTrade).average().getAsDouble();
			stockBase.setMa60(new BigDecimal(day_ma60).setScale(3, RoundingMode.UP).doubleValue());
			stockDay0.setMa60(new BigDecimal(day_ma60).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double day_ma30 = day_120s.subList(day_120s.size() - 30, day_120s.size()).stream().mapToDouble(StockDay::getTrade).average().getAsDouble();
			stockBase.setMa30(new BigDecimal(day_ma30).setScale(3, RoundingMode.UP).doubleValue());
			stockDay0.setMa30(new BigDecimal(day_ma30).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double day_ma20 = day_120s.subList(day_120s.size() - 20, day_120s.size()).stream().mapToDouble(StockDay::getTrade).average().getAsDouble();
			stockBase.setMa20(new BigDecimal(day_ma20).setScale(3, RoundingMode.UP).doubleValue());
			stockDay0.setMa20(new BigDecimal(day_ma20).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double day_ma10 = day_120s.subList(day_120s.size() - 10, day_120s.size()).stream().mapToDouble(StockDay::getTrade).average().getAsDouble();
			stockBase.setMa10(new BigDecimal(day_ma10).setScale(3, RoundingMode.UP).doubleValue());
			stockDay0.setMa10(new BigDecimal(day_ma10).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double day_ma5 = day_120s.subList(day_120s.size() - 5, day_120s.size()).stream().mapToDouble(StockDay::getTrade).average().getAsDouble();
			stockBase.setMa5(new BigDecimal(day_ma5).setScale(3, RoundingMode.UP).doubleValue());
			stockDay0.setMa5(new BigDecimal(day_ma5).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
	}
	
	public void updateStockWeek(StockBase stockBase) {
		String thisWeek = DateUtil.getTodayWeek();
		
		List<StockWeek> stockWeeks = RealtimeStockCache.weekMap.get(stockBase.getCode());
		StockWeek stockWeek0 = stockWeeks.get(stockWeeks.size() - 1);

		if (!stockWeek0.getDay().equals(thisWeek)){
			StockWeek stockWeek = new StockWeek();
			stockWeek.setCode(stockBase.getCode());
			stockWeek.setTrade(stockBase.getTrade());
			stockWeek.setOpen(stockBase.getOpen());
			stockWeek.setHigh(stockBase.getHigh());
			stockWeek.setLow(stockBase.getLow());
			stockWeek.setLastTrade(stockBase.getLastTrade());
			stockWeek.setVolume(stockBase.getVolume());
			stockWeek.setAmount(stockBase.getAmount());
			stockWeek.setDay(thisWeek);
			RealtimeStockCache.weekMap.get(stockBase.getCode()).add(stockWeek);
		}else {
			stockWeek0.setTrade(stockBase.getTrade());
			stockWeek0.setHigh(stockBase.getHigh() > stockWeek0.getHigh()?stockBase.getHigh():stockWeek0.getHigh());
			stockWeek0.setLow(stockBase.getLow() < stockWeek0.getLow()?stockBase.getLow():stockWeek0.getLow());
		}
		
		//week_ma
		List<StockWeek> week_120s = stockWeeks.subList(stockWeeks.size()<120? 0 : stockWeeks.size() - 120, stockWeeks.size());
		try {
			Double week_ma120 = week_120s.subList(week_120s.size() - 120, week_120s.size()).stream().mapToDouble(StockWeek::getTrade).average().getAsDouble();
//			stockBase.setWeekma120(new BigDecimal(week_ma120).setScale(3, RoundingMode.UP).doubleValue());
			stockWeek0.setMa120(new BigDecimal(week_ma120).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double week_ma60 = week_120s.subList(week_120s.size() - 60, week_120s.size()).stream().mapToDouble(StockWeek::getTrade).average().getAsDouble();
//			stockBase.setWeekma60(new BigDecimal(week_ma60).setScale(3, RoundingMode.UP).doubleValue());
			stockWeek0.setMa60(new BigDecimal(week_ma60).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double week_ma30 = week_120s.subList(week_120s.size() - 30, week_120s.size()).stream().mapToDouble(StockWeek::getTrade).average().getAsDouble();
//			stockBase.setWeekma30(new BigDecimal(week_ma30).setScale(3, RoundingMode.UP).doubleValue());
			stockWeek0.setMa30(new BigDecimal(week_ma30).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}		
		try {
			Double week_ma20 = week_120s.subList(week_120s.size() - 20, week_120s.size()).stream().mapToDouble(StockWeek::getTrade).average().getAsDouble();
//			stockBase.setWeekma20(new BigDecimal(week_ma20).setScale(3, RoundingMode.UP).doubleValue());
			stockWeek0.setMa20(new BigDecimal(week_ma20).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}		
		try {
			Double week_ma10 = week_120s.subList(week_120s.size() - 10, week_120s.size()).stream().mapToDouble(StockWeek::getTrade).average().getAsDouble();
//			stockBase.setWeekma10(new BigDecimal(week_ma10).setScale(3, RoundingMode.UP).doubleValue());
			stockWeek0.setMa10(new BigDecimal(week_ma10).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double week_ma5 = week_120s.subList(week_120s.size() - 5, week_120s.size()).stream().mapToDouble(StockWeek::getTrade).average().getAsDouble();
//			stockBase.setWeekma5(new BigDecimal(week_ma5).setScale(3, RoundingMode.UP).doubleValue());
			stockWeek0.setMa5(new BigDecimal(week_ma5).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
	}

	public void updateStockMonth(StockBase stockBase) throws ParseException {
		
		String thisMonth = DateUtil.getMonthLastDay();

		List<StockMonth> stockMonths = RealtimeStockCache.monthMap.get(stockBase.getCode());
//		Collections.sort(stockMonths);
		
		StockMonth stockMonth0 = stockMonths.get(stockMonths.size() - 1);

		if (!stockMonth0.getDay().equals(thisMonth)){
			StockMonth stockMonth = new StockMonth();
			stockMonth.setCode(stockBase.getCode());
			stockMonth.setTrade(stockBase.getTrade());
			stockMonth.setOpen(stockBase.getOpen());
			stockMonth.setHigh(stockBase.getHigh());
			stockMonth.setLow(stockBase.getLow());
			stockMonth.setLastTrade(stockBase.getLastTrade());
			stockMonth.setVolume(stockBase.getVolume());
			stockMonth.setAmount(stockBase.getAmount());
			stockMonth.setDay(thisMonth);
			RealtimeStockCache.monthMap.get(stockBase.getCode()).add(stockMonth);
		}else {
			stockMonth0.setTrade(stockBase.getTrade());
			stockMonth0.setHigh(stockBase.getHigh() > stockMonth0.getHigh()?stockBase.getHigh():stockMonth0.getHigh());
			stockMonth0.setLow(stockBase.getLow() < stockMonth0.getLow()?stockBase.getLow():stockMonth0.getLow());
		}
		
		//month_ma
		List<StockMonth> month_120s = stockMonths.subList(stockMonths.size()<120? 0 : stockMonths.size() - 120, stockMonths.size());
		
		try {
			Double month_ma120 = month_120s.subList(month_120s.size() - 120, month_120s.size()).stream().mapToDouble(StockMonth::getTrade).average().getAsDouble();
//			stockBase.setMonthma120(new BigDecimal(month_ma120).setScale(3, RoundingMode.UP).doubleValue());
			stockMonth0.setMa120(new BigDecimal(month_ma120).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double month_ma60 = month_120s.subList(month_120s.size() - 60, month_120s.size()).stream().mapToDouble(StockMonth::getTrade).average().getAsDouble();
//			stockBase.setMonthma60(new BigDecimal(month_ma60).setScale(3, RoundingMode.UP).doubleValue());
			stockMonth0.setMa60(new BigDecimal(month_ma60).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double month_ma30 = month_120s.subList(month_120s.size() - 30, month_120s.size()).stream().mapToDouble(StockMonth::getTrade).average().getAsDouble();
//			stockBase.setMonthma30(new BigDecimal(month_ma30).setScale(3, RoundingMode.UP).doubleValue());
			stockMonth0.setMa30(new BigDecimal(month_ma30).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double month_ma20 = month_120s.subList(month_120s.size() - 20, month_120s.size()).stream().mapToDouble(StockMonth::getTrade).average().getAsDouble();
//			stockBase.setMonthma20(new BigDecimal(month_ma20).setScale(3, RoundingMode.UP).doubleValue());
			stockMonth0.setMa20(new BigDecimal(month_ma20).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double month_ma10 = month_120s.subList(month_120s.size() - 10, month_120s.size()).stream().mapToDouble(StockMonth::getTrade).average().getAsDouble();
//			stockBase.setMonthma10(new BigDecimal(month_ma10).setScale(3, RoundingMode.UP).doubleValue());
			stockMonth0.setMa10(new BigDecimal(month_ma10).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
		try {
			Double month_ma5 = month_120s.subList(month_120s.size() - 5, month_120s.size()).stream().mapToDouble(StockMonth::getTrade).average().getAsDouble();
//			stockBase.setMonthma5(new BigDecimal(month_ma5).setScale(3, RoundingMode.UP).doubleValue());
			stockMonth0.setMa5(new BigDecimal(month_ma5).setScale(3, RoundingMode.UP).doubleValue());
		} catch (Exception e) {
		}
	}
}
