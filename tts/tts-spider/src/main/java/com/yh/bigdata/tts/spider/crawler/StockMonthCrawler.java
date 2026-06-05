package com.yh.bigdata.tts.spider.crawler;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import com.yh.bigdata.tts.common.param.TradeConvertHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockMonthMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.common.utils.DateUtil;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment
 */

@Component
public class StockMonthCrawler {

	Logger logger = LoggerFactory.getLogger(StockMonthCrawler.class);

//	String base_url = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sh600009&scale=7200&ma=5&datalen=5";
//	String base_url = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=%s&scale=7200&ma=%s&datalen=5";

//	String base_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=sh600009&scale=7200&ma=5&datalen=5";
	String base_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=7200&ma=%s&datalen=1";
	
	@Autowired
	StockBaseMapper stockBaseMapper;
	
	@Autowired
	StockMonthMapper stockMonthMapper;
	
	@Value("${spider.day.interval}")
	private String sleepMseconds = "500";

	@Value("${spider.month.startpage}")
	private String startPage = "1";
	
	public void run() {
		
		long start = System.currentTimeMillis();

		logger.info("StockMonthCrawler loop start=========================================================");
		boolean switchOn = false;
		
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
//							if (!switchOn) {
//								if (stockBase.getCode().equals("sz301550")) {
//									switchOn = true;
//								}
//								continue;
//							}
							
							spider(stockBase);
							Thread.sleep(Long.parseLong(sleepMseconds));
						} catch (Exception e) {
							logger.error("StockMonthCrawler run exception, stock = " + JSON.toJSONString(stockBase), e);
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
			logger.error("StockMonthCrawler exception.....page = " + page, e);
			System.exit(-1);
		}
		logger.info("=========================================================StockMonthCrawler loop finish({}s)",(System.currentTimeMillis() - start)/1000);

	}

	public void spider(StockBase stockBase) throws ClientProtocolException, IOException, InterruptedException, ParseException {
		String url_day = String.format(base_url, stockBase.getCode(), URLEncoder.encode(JSON.toJSONString(Lists.newArrayList(1,5,10,20,30))));
//		String url_ma5 = String.format(base_url, stockBase.getCode(), 5);
//		String url_ma10 = String.format(base_url, stockBase.getCode(), 10);
//		String url_ma20 = String.format(base_url, stockBase.getCode(), 20);
//		String url_ma30 = String.format(base_url, stockBase.getCode(), 30);
		
		List<StockMonth> months = null;
		String ret_day = Request.Get(url_day)
				.addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sh000001/nc.shtml")			
				.execute().returnContent().asString();
		ret_day = ret_day.substring(ret_day.indexOf("(")+1, ret_day.indexOf(")"));
		if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
			months = JSON.parseArray(ret_day, StockMonth.class);
		}
//		logger.info("days : " + JSON.toJSONString(days));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockMonth> ma5s = null;
//		String ret_ma5 = Request.Get(url_ma5).execute().returnContent().asString();
//		ret_ma5 = ret_ma5.substring(ret_ma5.indexOf("(")+1, ret_ma5.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma5) && !ret_ma5.equals("null")) {
//			ma5s = JSON.parseArray(ret_ma5, StockMonth.class);
//		}
////		logger.info("ma5s : " + JSON.toJSONString(ma5s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockMonth> ma10s = null;
//		String ret_ma10 = Request.Get(url_ma10).execute().returnContent().asString();
//		ret_ma10 = ret_ma10.substring(ret_ma10.indexOf("(")+1, ret_ma10.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma10) && !ret_ma10.equals("null")) {
//			ma10s = JSON.parseArray(ret_ma10, StockMonth.class);
//		}
////		logger.info("ma10s : " + JSON.toJSONString(ma10s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockMonth> ma20s = null;
//		String ret_ma20 = Request.Get(url_ma20).execute().returnContent().asString();
//		ret_ma20 = ret_ma20.substring(ret_ma20.indexOf("(")+1, ret_ma20.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma20) && !ret_ma20.equals("null")) {
//			ma20s = JSON.parseArray(ret_ma20, StockMonth.class);
//		}
////		logger.info("ma20s : " + JSON.toJSONString(ma20s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockMonth> ma30s = null;
//		String ret_ma30 = Request.Get(url_ma30).execute().returnContent().asString();
//		ret_ma30 = ret_ma30.substring(ret_ma30.indexOf("(")+1, ret_ma30.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma30) && !ret_ma30.equals("null")) {
//			ma30s = JSON.parseArray(ret_ma30, StockMonth.class);
//		}
////		logger.info("ma30s : " + JSON.toJSONString(ma30s));
////		Thread.sleep(Long.parseLong(sleepMseconds));
//		
//		parseStockMonths(months, ma5s, ma10s, ma20s, ma30s);
		
		for (StockMonth stockMonth : months) {
//			if (DateUtil.isSameMonth(stockMonth.getDay())) {
//				continue;
//			}
			
			if (stockMonth.getOpen() < 0.001) {
				continue;
			}
			
			stockMonth.setCode(stockBase.getCode());
			stockMonth.setName(stockBase.getName());
			stockMonth.setDay(DateUtil.parse2MonthLastDay(stockMonth.getDay()));
			
			//lastTrade
			StockMonth lastTradeMonth = stockMonthMapper.selectLatestTradeMonth(stockMonth.getCode(), stockMonth.getDay());
			if (lastTradeMonth != null) {
				stockMonth.setLastTrade(lastTradeMonth.getTrade());
//				stockMonth.setCrossParams(JSON.toJSONString(PriceUtil.calMaCross(stockMonth, lastTradeMonth)));
			}
			
			
			logger.info("spider month : " + JSON.toJSONString(stockMonth));
			if (stockMonthMapper.selectByPrimaryKey(stockMonth.getCode(), stockMonth.getDay()) == null) {
				stockMonthMapper.insert(stockMonth);
			}else {
				stockMonthMapper.updateByPrimaryKey(stockMonth);
			}
			
//			if (DateUtil.isSameMonth(stockMonth.getDay(), stockBase.getDay())) {
//				stockBase.setMonthma5(stockMonth.getMa5());
//				stockBase.setMonthma10(stockMonth.getMa10());
//				stockBase.setMonthma20(stockMonth.getMa20());
//				stockBase.setMonthma30(stockMonth.getMa30());
//				stockBaseMapper.updateByPrimaryKeySelective(stockBase);
//			}
			
		}
		
	}
	
	
	public void parseStockMonths(List<StockMonth> months, List<StockMonth> ma5s, List<StockMonth> ma10s, List<StockMonth> ma20s, List<StockMonth> ma30s) {
		Map<String, StockMonth> ma5Map = TradeConvertHelper.parseMap(ma5s);
		Map<String, StockMonth> ma10Map = TradeConvertHelper.parseMap(ma10s);
		Map<String, StockMonth> ma20Map = TradeConvertHelper.parseMap(ma20s);
		Map<String, StockMonth> ma30Map = TradeConvertHelper.parseMap(ma30s);
		for (StockMonth stockMonth : months) {
			StockMonth ma5Stockday = ma5Map.get(stockMonth.getCode() + "_" + stockMonth.getDay());
			if (ma5Stockday != null) {
				stockMonth.setMa5(ma5Stockday.getMa5());
			}
			
			StockMonth ma10Stockday = ma10Map.get(stockMonth.getCode() + "_" + stockMonth.getDay());
			if (ma10Stockday != null) {
				stockMonth.setMa10(ma10Stockday.getMa10());
			}
			
			StockMonth ma20Stockday = ma20Map.get(stockMonth.getCode() + "_" + stockMonth.getDay());
			if (ma20Stockday != null) {
				stockMonth.setMa20(ma20Stockday.getMa20());
			}
			
			StockMonth ma30Stockday = ma30Map.get(stockMonth.getCode() + "_" + stockMonth.getDay());
			if (ma30Stockday != null) {
				stockMonth.setMa30(ma30Stockday.getMa30());
			}
		}
	}
	

	public static void main(String[] args) {
		new StockMonthCrawler().run();
	}
}
