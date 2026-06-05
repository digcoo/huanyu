package com.yh.bigdata.tts.spider.scheduler;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockCapital;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.StockFenshi;
import com.yh.bigdata.tts.common.model.StockMin30;
import com.yh.bigdata.tts.common.model.StockMin60;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.model.StockWeek;
import com.yh.bigdata.tts.common.model.StockYear;
import com.yh.bigdata.tts.common.utils.DateUtil;
import com.yh.bigdata.tts.common.utils.MathUtil;
import com.yh.bigdata.tts.spider.utils.DfcfHttpUtils;
import com.yh.bigdata.tts.spider.utils.SinaHttpUtils;

/**
 * @author duyp
 * 
 * @date 2021/09/24
 * 
 * @comment
 */

@Component
@EnableScheduling
public class RealTimeSpiderScheduler {

	Logger logger = LoggerFactory.getLogger(RealTimeSpiderScheduler.class);

	String stock_realtime_url = "http://hq.sinajs.cn/list=%s";
//	String stock_min15_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=15&ma=%s&datalen=1";
//	String stock_min30_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=30&ma=%s&datalen=1";
//	String stock_min60_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=60&ma=%s&datalen=1";
	String stock_realtime_capital_url = "http://push2.eastmoney.com/api/qt/clist/get?cb=&fid=f62&po=1&pz=%s&pn=%s&np=1&fltt=2&invt=2&ut=ut&fs=m:0+t:6+f:!2,m:0+t:13+f:!2,m:0+t:80+f:!2,m:1+t:2+f:!2,m:1+t:23+f:!2,m:0+t:7+f:!2,m:1+t:3+f:!2&fields=f12,f14,f2,f3,f62,f184,f66,f69,f72,f75,f78,f81,f84,f87,f124";
	
	Boolean isTradeOfCurrent = false;
	String initDay = null;
	

	static long startTimestamp = 0l;
	static long endTimestamp = 0l;
	static boolean ifWeekendDay = false;

    @Value("${realtime.spider.on}")
    private boolean spiderEnable = true;
	
	static {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 9);
		calendar.set(Calendar.MINUTE, 26);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		startTimestamp = calendar.getTimeInMillis();
		   
		calendar.set(Calendar.HOUR_OF_DAY, 15);
		calendar.set(Calendar.MINUTE, 1);
		endTimestamp = calendar.getTimeInMillis();

		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		ifWeekendDay = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;

	}
	
	@Scheduled(cron="${realtime.spider.cron}")
	public void run() {
		try {

            if (!spiderEnable) {
                return;
            }

			long taskStart = System.currentTimeMillis();
			
			if (taskStart < startTimestamp
					|| taskStart > endTimestamp
					|| ifWeekendDay
					) {
				return;
			}
			
//			if (initDay == null || !initDay.equals(DateUtil.getCurrentDay())) {
//				initDay = DateUtil.getCurrentDay();
//				if (!SinaHttpUtils.isTradeOfCurrentDay()) {
//					isTradeOfCurrent = false;
//					return;
//				}else {
//					isTradeOfCurrent = true;
//				}
//			}
//			
//			if (!isTradeOfCurrent) {
//				return;
//			}
			
			spiderRealtime();
//			spiderCapitalFlow();
			
			logger.info("spider realtime fenshi cost : {}", (System.currentTimeMillis() - taskStart) / 1000);
			
		} catch (Exception e) {
			logger.error("RealTimeSpider run exception...", e);
		}
	}

	private void spiderRealtime() throws ClientProtocolException, IOException, InterruptedException {
		String time = "";
		List<List<StockBase>> stockSplits = Lists.partition(Lists.newArrayList(RealtimeStockCache.filterStockMap.values()), 100);
		for (List<StockBase> stocks : stockSplits) {
			try {
				List<String> codes = stocks.stream().map(StockBase::getCode).collect(Collectors.toList());
				String codestr = JSON.toJSONString(codes);
				String url = String.format(stock_realtime_url, codestr.substring(1, codestr.length() - 1).replace("\"", ""));
				String ret_renshi = Request.Get(url)
						.addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sz002015/nc.shtml")			
						.execute().returnContent().asString();
				if (StringUtils.isNotBlank(ret_renshi) && !ret_renshi.equals("null")) {
					
					String[] split = ret_renshi.split("\n");
					for (int i = 0; i < split.length; i++) {
						StockFenshi stockFenshi = SinaHttpUtils.parseStockFenshi(split[i]);
						if (stockFenshi.getTime().compareTo("15:00:00") >= 0) {
							stockFenshi.setTime("15:00:00");
						}
						
						time = stockFenshi.getTime();
						
//						logger.info("stock fenshi " + JSON.toJSONString(stockFenshi));
						
//						if (!stockFenshi.getDay().equals(DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd"))) {
//							continue;
//						}
						
						StockBase stockBase = RealtimeStockCache.filterStockMap.get(stockFenshi.getCode());
						stockBase.setOpen(stockFenshi.getOpen());
						stockBase.setTrade(stockFenshi.getTrade());
						stockBase.setDay(stockFenshi.getDay());
						stockBase.setLastTrade(stockFenshi.getLastTrade());
						stockBase.setHigh(stockFenshi.getHigh());
						stockBase.setLow(stockFenshi.getLow());
						stockBase.setAmount(stockFenshi.getAmount());
						stockBase.setVolume(stockFenshi.getVolume());
						stockBase.setIsTrade(stockFenshi.getOpen() > 0.1 && !stockBase.getName().contains("退"));			


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


//						if (stockBase.isApproachZhangTing() && !stockBase.isZhangTing()) {
//							RealtimeStockCache.watchStockMap.put(stockBase.getCode(), stockBase);
//						}
					}
				}
				
			} catch (Exception e) {
				logger.error("spider realtime fenshi exception.. ", e);
			}
		}

		
		for (StockBase stockBase : RealtimeStockCache.filterStockMap.values()) {
			try {
				
				calculateMinMA(stockBase, time);
				calculateDayMA(stockBase);
				calculateWeekMA(stockBase);
				calculateMonthMA(stockBase);
				calculateYearMA(stockBase);
			} catch (Exception e) {
                logger.error("cal MA exception, code: {}", stockBase.getCode());
//				e.printStackTrace();
			}
		}
	}
	
	public void calculateMinMA(StockBase stockBase, String time) {
//		calculateMin5MA(stockBase, time);
		calculateMin30MA(stockBase, time);
//		calculateMin60MA(stockBase, time);
	}

	private void calculateMin30MA(StockBase stockBase, String time) {
		String maTime= DateUtil.getMATime(time, 30);
		String thisTime = stockBase.getDay() + " " + maTime;
		
		StockMin30 targetStockMin30 = null;

		List<StockMin30> stockMin30s = RealtimeStockCache.min30Map.get(stockBase.getCode());
		StockMin30 stockMin30_1 = stockMin30s.get(stockMin30s.size() - 1);	//最后一个min30
		if (stockMin30_1.getDay().equals(thisTime)) {		//覆盖
			
			stockMin30_1.setHigh(stockBase.getTrade() > stockMin30_1.getHigh()? stockBase.getTrade() : stockMin30_1.getHigh());
			stockMin30_1.setLow(stockBase.getTrade() < stockMin30_1.getLow()? stockBase.getTrade() : stockMin30_1.getLow());
			stockMin30_1.setTrade(stockBase.getTrade());
			
			targetStockMin30 = stockMin30_1;
			
		}else {
			StockMin30 stockMin30_0 = new StockMin30();
			//开盘价暂定位上一个收盘价:如果是当天第一个小周期，则开盘价即为分时开盘价，否则位拉一个周期收盘价
			if(maTime.equals("10:00:00")) {
				stockMin30_0.setOpen(stockBase.getOpen());
				stockMin30_0.setLastTrade(stockBase.getLastTrade());
			}else {
				stockMin30_0.setOpen(stockMin30_1.getTrade());
				stockMin30_0.setLastTrade(stockMin30_1.getTrade());
			}
			stockMin30_0.setHigh(stockBase.getTrade());
			stockMin30_0.setLow(stockBase.getTrade());
			stockMin30_0.setTrade(stockBase.getTrade());
			stockMin30_0.setDay(thisTime);
			stockMin30_0.setPeriodTypeEnum(PeriodTypeEnum.MIN30);
			
			//暂时用上一个周期的MA替换
			stockMin30_0.setMa5(stockMin30_1.getMa5());
			stockMin30_0.setMa10(stockMin30_1.getMa10());
			stockMin30_0.setMa20(stockMin30_1.getMa20());
			stockMin30_0.setMa30(stockMin30_1.getMa30());
            stockMin30_0.setName(stockBase.getName());
			
			stockMin30s.add(stockMin30_0);
			targetStockMin30 = stockMin30_0;
		}		

	}
//
//	private void calculateMin60MA(StockBase stockBase, String time) {
//		String maTime= DateUtil.getMATime(time, 60);
//		String thisTime = stockBase.getDay() + " " + maTime;
//
//		StockMin60 targetStockMin60 = null;
//
//		List<StockMin60> stockMin60s = RealtimeStockCache.min60Map.get(stockBase.getCode());
//
//		Optional<StockMin60> stockMin60Optional =  stockMin60s.subList(MathUtil.max(stockMin60s.size() - 5, 0), stockMin60s.size()).stream()
//				.filter(x -> x.getDay().equals(thisTime)).findFirst();
//		StockMin60 stockMin60_1 = stockMin60s.get(stockMin60s.size() - 1);	//上一个min60
//
////		if (stockMin60_1.getDay().equals(thisTime)) {		//覆盖
//		if(stockMin60Optional.isPresent()) {
//			stockMin60_1.setHigh(stockBase.getTrade() > stockMin60_1.getHigh()? stockBase.getTrade() : stockMin60_1.getHigh());
//			stockMin60_1.setLow(stockBase.getTrade() < stockMin60_1.getLow()? stockBase.getTrade() : stockMin60_1.getLow());
//			stockMin60_1.setTrade(stockBase.getTrade());
//
//			targetStockMin60 = stockMin60_1;
//
//		}else {
//			StockMin60 stockMin60_0 = new StockMin60();
//			if(maTime.equals("10:30:00")) {
//				stockMin60_0.setOpen(stockBase.getOpen());
//				stockMin60_0.setLastTrade(stockBase.getLastTrade());
//			}else {
//				stockMin60_0.setOpen(stockMin60_1.getTrade());			//开盘价暂定位上一个收盘价:如果是当天第一个小周期，则开盘价即为分时开盘价，否则位拉一个周期收盘价
//				stockMin60_0.setLastTrade(stockMin60_1.getTrade());
//			}
//			stockMin60_0.setHigh(stockBase.getTrade());
//			stockMin60_0.setLow(stockBase.getTrade());
//			stockMin60_0.setTrade(stockBase.getTrade());
//			stockMin60_0.setDay(thisTime);
//			stockMin60_0.setPeriodTypeEnum(PeriodTypeEnum.MIN60);
//
//			//暂时用上一个周期的MA替换
//			stockMin60_0.setMa5(stockMin60_1.getMa5());
//			stockMin60_0.setMa10(stockMin60_1.getMa10());
//			stockMin60_0.setMa20(stockMin60_1.getMa20());
//			stockMin60_0.setMa30(stockMin60_1.getMa30());
//            stockMin60_0.setName(stockBase.getName());
//
//			stockMin60s.add(stockMin60_0);
//			targetStockMin60 = stockMin60_0;
//		}
//	}

	public void calculateDayMA(StockBase stockBase) {
		String thisDay = DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd");
		
		List<StockDay> stockDays = RealtimeStockCache.dayMap.get(stockBase.getCode());
		StockDay stockDay_1 = stockDays.get(stockDays.size() - 1);
		if (stockDay_1.getDay().equals(thisDay)){
			stockDay_1.setTrade(stockBase.getTrade());
			stockDay_1.setHigh(stockBase.getHigh() > stockDay_1.getHigh()?stockBase.getHigh():stockDay_1.getHigh());
			stockDay_1.setLow(stockBase.getLow() < stockDay_1.getLow()?stockBase.getLow():stockDay_1.getLow());
			stockDay_1.setVolume(stockBase.getVolume());
			stockDay_1.setAmount(stockBase.getAmount());
		}else {
			StockDay stockDay_0 = new StockDay();
			stockDay_0.setCode(stockBase.getCode());
			stockDay_0.setTrade(stockBase.getTrade());
			stockDay_0.setOpen(stockBase.getOpen());
			stockDay_0.setHigh(stockBase.getHigh());
			stockDay_0.setLow(stockBase.getLow());
			stockDay_0.setLastTrade(stockBase.getLastTrade());
			stockDay_0.setVolume(stockBase.getVolume());
			stockDay_0.setAmount(stockBase.getAmount());
			

			//暂时用上一个周期的MA替换
			stockDay_0.setMa5(stockDay_1.getMa5());
			stockDay_0.setMa10(stockDay_1.getMa10());
			stockDay_0.setMa20(stockDay_1.getMa20());
			stockDay_0.setMa30(stockDay_1.getMa30());
			stockDay_0.setPeriodTypeEnum(PeriodTypeEnum.DAY);
            stockDay_0.setName(stockBase.getName());
			
			stockDay_0.setDay(thisDay);
			RealtimeStockCache.dayMap.get(stockBase.getCode()).add(stockDay_0);
		}
	}
	
	public void calculateWeekMA(StockBase stockBase) {
		String thisWeek = DateUtil.getTodayWeek();
		
		List<StockWeek> stockWeeks = RealtimeStockCache.weekMap.get(stockBase.getCode());
		StockWeek stockWeek_1 = stockWeeks.get(stockWeeks.size() - 1);
		if (stockWeek_1.getDay().equals(thisWeek)){
			stockWeek_1.setTrade(stockBase.getTrade());
			stockWeek_1.setHigh(stockBase.getHigh() > stockWeek_1.getHigh()?stockBase.getHigh():stockWeek_1.getHigh());
			stockWeek_1.setLow(stockBase.getLow() < stockWeek_1.getLow()?stockBase.getLow():stockWeek_1.getLow());
		}else {
			StockWeek stockWeek_0 = new StockWeek();
			stockWeek_0.setCode(stockBase.getCode());
			stockWeek_0.setTrade(stockBase.getTrade());
			stockWeek_0.setOpen(stockBase.getOpen());
			stockWeek_0.setHigh(stockBase.getHigh());
			stockWeek_0.setLow(stockBase.getLow());
			stockWeek_0.setLastTrade(stockBase.getLastTrade());
			stockWeek_0.setVolume(stockBase.getVolume());
			stockWeek_0.setAmount(stockBase.getAmount());
			
			//暂时用上一个周期的MA替换
			stockWeek_0.setMa5(stockWeek_1.getMa5());
			stockWeek_0.setMa10(stockWeek_1.getMa10());
			stockWeek_0.setMa20(stockWeek_1.getMa20());
			stockWeek_0.setMa30(stockWeek_1.getMa30());
			stockWeek_0.setPeriodTypeEnum(PeriodTypeEnum.WEEK);
            stockWeek_0.setName(stockBase.getName());
			
			stockWeek_0.setDay(thisWeek);
			RealtimeStockCache.weekMap.get(stockBase.getCode()).add(stockWeek_0);
		}
	}

	public void calculateMonthMA(StockBase stockBase) {
		
		String thisMonth = DateUtil.getMonthLastDay();

		List<StockMonth> stockMonths = RealtimeStockCache.monthMap.get(stockBase.getCode());
		StockMonth stockMonth_1 = stockMonths.get(stockMonths.size() - 1);

		if (stockMonth_1.getDay().equals(thisMonth)){
			stockMonth_1.setTrade(stockBase.getTrade());
			stockMonth_1.setHigh(stockBase.getHigh() > stockMonth_1.getHigh()?stockBase.getHigh():stockMonth_1.getHigh());
			stockMonth_1.setLow(stockBase.getLow() < stockMonth_1.getLow()?stockBase.getLow():stockMonth_1.getLow());
		}else {
			StockMonth stockMonth_0 = new StockMonth();
			stockMonth_0.setCode(stockBase.getCode());
			stockMonth_0.setTrade(stockBase.getTrade());
			stockMonth_0.setOpen(stockBase.getOpen());
			stockMonth_0.setHigh(stockBase.getHigh());
			stockMonth_0.setLow(stockBase.getLow());
			stockMonth_0.setLastTrade(stockBase.getLastTrade());
			stockMonth_0.setVolume(stockBase.getVolume());
			stockMonth_0.setAmount(stockBase.getAmount());

			
			//暂时用上一个周期的MA替换
			stockMonth_0.setMa5(stockMonth_1.getMa5());
			stockMonth_0.setMa10(stockMonth_1.getMa10());
			stockMonth_0.setMa20(stockMonth_1.getMa20());
			stockMonth_0.setMa30(stockMonth_1.getMa30());
			stockMonth_0.setPeriodTypeEnum(PeriodTypeEnum.MONTH);
            stockMonth_0.setName(stockBase.getName());
			
			stockMonth_0.setDay(thisMonth);
			RealtimeStockCache.monthMap.get(stockBase.getCode()).add(stockMonth_0);
		}
	}
	

	public void calculateYearMA(StockBase stockBase) {
		
		String thisYear = DateUtil.getYearLastDay();

		List<StockYear> stockYears = RealtimeStockCache.yearMap.get(stockBase.getCode());
		StockYear stockYear_1 = stockYears.get(stockYears.size() - 1);

		if (stockYear_1.getDay().equals(thisYear)){
			stockYear_1.setTrade(stockBase.getTrade());
			stockYear_1.setHigh(stockBase.getHigh() > stockYear_1.getHigh()?stockBase.getHigh():stockYear_1.getHigh());
			stockYear_1.setLow(stockBase.getLow() < stockYear_1.getLow()?stockBase.getLow():stockYear_1.getLow());
		}else {
			StockYear stockYear_0 = new StockYear();
			stockYear_0.setCode(stockBase.getCode());
			stockYear_0.setTrade(stockBase.getTrade());
			stockYear_0.setOpen(stockBase.getOpen());
			stockYear_0.setHigh(stockBase.getHigh());
			stockYear_0.setLow(stockBase.getLow());
			stockYear_0.setLastTrade(stockBase.getLastTrade());
			stockYear_0.setVolume(stockBase.getVolume());
			stockYear_0.setAmount(stockBase.getAmount());

			
			//暂时用上一个周期的MA替换
			stockYear_0.setMa5(stockYear_1.getMa5());
			stockYear_0.setMa10(stockYear_1.getMa10());
			stockYear_0.setMa20(stockYear_1.getMa20());
			stockYear_0.setMa30(stockYear_1.getMa30());
			stockYear_0.setPeriodTypeEnum(PeriodTypeEnum.YEAR);
            stockYear_0.setName(stockBase.getName());
			
			stockYear_0.setDay(thisYear);
			RealtimeStockCache.yearMap.get(stockBase.getCode()).add(stockYear_0);
			
		}
	}
	
	private void spiderCapitalFlow() {
		
		int page = 1;
		int size = 100;
		try {
			while (true) {

				String url_day = String.format(stock_realtime_capital_url
						, size
						, page++);
				
				String ret_day = Request.Get(url_day).execute().returnContent().asString();
				
				List<StockCapital> stockCapitals = DfcfHttpUtils.parseStockCapitalRealtime(ret_day);
				
				if(stockCapitals.size() < size) {
					break;
				}
				
				logger.info("spiderCapital : page = " + page);
				
				Thread.sleep(1l);
				
			}
			
		} catch (Exception e) {
		}
	}
	
}
