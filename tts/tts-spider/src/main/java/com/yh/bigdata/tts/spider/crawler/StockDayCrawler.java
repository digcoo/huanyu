package com.yh.bigdata.tts.spider.crawler;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockDayMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.common.utils.PriceUtil;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment
 */

@Component
public class StockDayCrawler {

	Logger logger = LoggerFactory.getLogger(StockDayCrawler.class);

//	String base_url1 = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sh600958&scale=240&ma=5&datalen=5";
//	String base_url1 = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=sh600958&scale=240&ma=5&datalen=5";
//	String base_url = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=%s&scale=240&ma=%s&datalen=5";
	String base_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=240&ma=%s&datalen=5";
	
	@Autowired
	StockBaseMapper stockBaseMapper;
	
	@Autowired
	StockDayMapper stockDayMapper;
	
	@Value("${spider.day.interval}")
	private String sleepMseconds = "1000";

	@Value("${spider.day.startpage}")
	private String startPage = "1";
	
	public void run() {
		
		long start = System.currentTimeMillis();
		
		logger.info("StockDayCrawler loop start=========================================================");

		int page = Integer.parseInt(startPage);
		int size = 100;
		try {
			while (true) {
				
				PageHelper.startPage(page, size);
				StockPageQuery pageQuery = new StockPageQuery(page, size);
				pageQuery.setIsSelectMode(false);
				
				Page<StockBase> pages = stockBaseMapper.selectByPageQuery(pageQuery);
				if (!CollectionUtils.isEmpty(pages.getResult())) {
					for (StockBase stockBase : pages) {
						try {
//							if (!stockBase.getCode().equals("sz002642")) {
//								continue;	
//							}
							spider(stockBase);
							Thread.sleep(Long.parseLong(sleepMseconds));
						} catch (Exception e) {
							logger.error("StockDayCrawler run exception, stock = " + JSON.toJSONString(stockBase), e);
							if (e instanceof HttpResponseException) {
								Thread.sleep(20 * 60 * 1000 + 10 * 1000);
								
								spider(stockBase);
							}
						}
					}
				}
				
				page++;
				
				if (page > pages.getPages()/* || page > 50*/) {
					break;
				}
				
				logger.info("page = " + page);

			}
			
//			spider(stockBaseMapper.selectByPrimaryKey("sh000001"));
//			spider("sz002638", "勤上股份");
//			spider("sz002891", "中宠股份");
//			spider("sz002892", "科力尔");
//			spider("sz002893", "华通热力");
//			spider("sz002895", "川恒股份");
//			spider("sz002896", "中大力德");
			
		} catch (Exception e) {
			logger.error("StockDayCrawler exception.....page = " + page, e);
			System.exit(-1);
		}
		
		logger.info("=========================================================StockDayCrawler loop finish({}s)",(System.currentTimeMillis() - start)/1000);

	}

	public void spider(StockBase stockBase) throws ClientProtocolException, IOException, InterruptedException {
		@SuppressWarnings("deprecation")
		String url_day = String.format(base_url, stockBase.getCode(), URLEncoder.encode(JSON.toJSONString(Lists.newArrayList(1,5,10,20,30))));
//		String url_day = String.format(base_url, stockBase.getCode(), 1);
//		String url_ma5 = String.format(base_url, stockBase.getCode(), 5);
//		String url_ma10 = String.format(base_url, stockBase.getCode(), 10);
//		String url_ma20 = String.format(base_url, stockBase.getCode(), 20);
//		String url_ma30 = String.format(base_url, stockBase.getCode(), 30);
		
		List<StockDay> days = null;
		String ret_day = Request.Get(url_day)
				.addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sh000001/nc.shtml")			
				.execute().returnContent().asString();
		ret_day = ret_day.substring(ret_day.indexOf("(")+1, ret_day.indexOf(")"));
		if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
			days = JSON.parseArray(ret_day, StockDay.class);
		}
//		logger.info("days : " + JSON.toJSONString(days));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockDay> ma5s = null;
//		String ret_ma5 = Request.Get(url_ma5).execute().returnContent().asString();
//		ret_ma5 = ret_ma5.substring(ret_ma5.indexOf("(")+1, ret_ma5.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma5) && !ret_ma5.equals("null")) {
//			ma5s = JSON.parseArray(ret_ma5, StockDay.class);
//		}
////		logger.info("ma5s : " + JSON.toJSONString(ma5s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockDay> ma10s = null;
//		String ret_ma10 = Request.Get(url_ma10).execute().returnContent().asString();
//		ret_ma10 = ret_ma10.substring(ret_ma10.indexOf("(")+1, ret_ma10.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma10) && !ret_ma10.equals("null")) {
//			ma10s = JSON.parseArray(ret_ma10, StockDay.class);
//		}
////		logger.info("ma10s : " + JSON.toJSONString(ma10s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockDay> ma20s = null;
//		String ret_ma20 = Request.Get(url_ma20).execute().returnContent().asString();
//		ret_ma20 = ret_ma20.substring(ret_ma20.indexOf("(")+1, ret_ma20.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma20) && !ret_ma20.equals("null")) {
//			ma20s = JSON.parseArray(ret_ma20, StockDay.class);
//		}
////		logger.info("ma20s : " + JSON.toJSONString(ma20s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockDay> ma30s = null;
//		String ret_ma30 = Request.Get(url_ma30).execute().returnContent().asString();
//		ret_ma30 = ret_ma30.substring(ret_ma30.indexOf("(")+1, ret_ma30.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma30) && !ret_ma30.equals("null")) {
//			ma30s = JSON.parseArray(ret_ma30, StockDay.class);
//		}
////		logger.info("ma30s : " + JSON.toJSONString(ma30s));
		
//		parseStockDays(days, ma5s, ma10s, ma20s, ma30s);
		
		stockBase.setMa5(days.get(days.size() - 1).getMa5());
		stockBase.setMa10(days.get(days.size() - 1).getMa10());
		stockBase.setMa20(days.get(days.size() - 1).getMa20());
		stockBase.setMa30(days.get(days.size() - 1).getMa30());
		stockBase.setTrade(days.get(days.size() - 1).getTrade());
		stockBase.setDay(days.get(days.size() - 1).getDay());
		
		stockBaseMapper.updateByPrimaryKeySelective(stockBase);
		
		for (StockDay stockDay : days) {
			try {
				
				if (stockDay.getOpen() < 0.001 || stockDay.getLow() < 0.001) {
					continue;
				}
				
				stockDay.setCode(stockBase.getCode());
				stockDay.setName(stockBase.getName());
				
				
				//lastTrade
				StockDay lastTradeDay = stockDayMapper.selectLatestTradeDay(stockDay.getCode(), stockDay.getDay());
				if (lastTradeDay != null) {
					stockDay.setLastTrade(lastTradeDay.getTrade());
//					stockDay.setCrossParams(JSON.toJSONString(PriceUtil.calMaCross(stockDay, lastTradeDay)));
				}
				
				logger.info("spider day : " + JSON.toJSONString(stockDay));
				
				StockDay localStockDay = stockDayMapper.selectByPrimaryKey(stockDay.getCode(), stockDay.getDay());
				if (localStockDay == null) {
					stockDayMapper.insert(stockDay);
				}else {
					stockDay.setDayRank(localStockDay.getDayRank());
					stockDayMapper.updateByPrimaryKey(stockDay);
				}
				
			} catch (Exception e) {
				
				if (e instanceof DuplicateKeyException) {
					continue;
				}else {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	/**
	 * 修复刚上市的股票，缺少MA线
	 */
	public void modifyMA(StockBase stockBase) {
		
	}
	
	
//	
//	public void parseStockDays(List<StockDay> days, List<StockDay> ma5s, List<StockDay> ma10s, List<StockDay> ma20s, List<StockDay> ma30s) {
//		Map<String, StockDay> ma5Map = StockDayConvertHelper.parseMap(ma5s);
//		Map<String, StockDay> ma10Map = StockDayConvertHelper.parseMap(ma10s);
//		Map<String, StockDay> ma20Map = StockDayConvertHelper.parseMap(ma20s);
//		Map<String, StockDay> ma30Map = StockDayConvertHelper.parseMap(ma30s);
//		for (StockDay stockDay : days) {
//			StockDay ma5Stockday = ma5Map.get(stockDay.getCode() + "_" + stockDay.getDay());
//			if (ma5Stockday != null) {
//				stockDay.setMa5(ma5Stockday.getMa5());
//			}
//			
//			StockDay ma10Stockday = ma10Map.get(stockDay.getCode() + "_" + stockDay.getDay());
//			if (ma10Stockday != null) {
//				stockDay.setMa10(ma10Stockday.getMa10());
//			}
//			
//			StockDay ma20Stockday = ma20Map.get(stockDay.getCode() + "_" + stockDay.getDay());
//			if (ma20Stockday != null) {
//				stockDay.setMa20(ma20Stockday.getMa20());
//			}
//			
//			StockDay ma30Stockday = ma30Map.get(stockDay.getCode() + "_" + stockDay.getDay());
//			if (ma30Stockday != null) {
//				stockDay.setMa30(ma30Stockday.getMa30());
//			}
//		}
//	}
//	

	public static void main(String[] args) {
		new StockDayCrawler().run();
	}
}
