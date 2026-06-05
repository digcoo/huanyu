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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.yh.bigdata.tts.common.constants.SourceTypeEnum;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockWeekMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockWeek;
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
@EnableScheduling
public class StockWeekCrawler {

	Logger logger = LoggerFactory.getLogger(StockWeekCrawler.class);

//	String base_url = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sh600009&scale=1200&ma=%s&datalen=5";
//	String base_url = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=%s&scale=1200&ma=%s&datalen=5";

//	String base_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=sh600958&scale=1200&ma=5&datalen=5";
	String base_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=1200&ma=%s&datalen=1";

	
	
	@Autowired
	StockBaseMapper stockBaseMapper;
	
	@Autowired
	StockWeekMapper stockWeekMapper;
	
	@Value("${spider.day.interval}")
	private String sleepMseconds = "500";

	@Value("${spider.week.startpage}")
	private String startPage = "1";

	boolean switchOn = false;
	
	public void run() {
		long start = System.currentTimeMillis();

		logger.info("StockWeekCrawler loop start=========================================================");
		
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
//							if (stockBase.getCode().startsWith("sz3")) {
//								continue;	
//							}
							
							spider(stockBase);
							Thread.sleep(Long.parseLong(sleepMseconds));
						} catch (Exception e) {
							logger.error("StockWeekCrawler run exception, stock = " + JSON.toJSONString(stockBase), e);
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
			logger.error("StockWeekCrawler exception.....page = " + page, e);
			System.exit(-1);
		}
		
		logger.info("=========================================================StockWeekCrawler loop finish({}s)",(System.currentTimeMillis() - start)/1000);

	}

	public void spider(StockBase stockBase) throws ClientProtocolException, IOException, InterruptedException, ParseException {
		@SuppressWarnings("deprecation")
		String url_day = String.format(base_url, stockBase.getCode(), URLEncoder.encode(JSON.toJSONString(Lists.newArrayList(1,5,10,20,30))));
//		String url_ma5 = String.format(base_url, stockBase.getCode(), 5);
//		String url_ma10 = String.format(base_url, stockBase.getCode(), 10);
//		String url_ma20 = String.format(base_url, stockBase.getCode(), 20);
//		String url_ma30 = String.format(base_url, stockBase.getCode(), 30);
		
		List<StockWeek> weeks = null;
		String ret_day = Request.Get(url_day)
				.addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sh000001/nc.shtml")			
				.execute().returnContent().asString();
		ret_day = ret_day.substring(ret_day.indexOf("(")+1, ret_day.indexOf(")"));
		if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
			weeks = JSON.parseArray(ret_day, StockWeek.class);
		}
////		logger.info("days : " + JSON.toJSONString(days));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockWeek> ma5s = null;
//		String ret_ma5 = Request.Get(url_ma5).execute().returnContent().asString();
//		ret_ma5 = ret_ma5.substring(ret_ma5.indexOf("(")+1, ret_ma5.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma5) && !ret_ma5.equals("null")) {
//			ma5s = JSON.parseArray(ret_ma5, StockWeek.class);
//		}
////		logger.info("ma5s : " + JSON.toJSONString(ma5s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockWeek> ma10s = null;
//		String ret_ma10 = Request.Get(url_ma10).execute().returnContent().asString();
//		ret_ma10 = ret_ma10.substring(ret_ma10.indexOf("(")+1, ret_ma10.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma10) && !ret_ma10.equals("null")) {
//			ma10s = JSON.parseArray(ret_ma10, StockWeek.class);
//		}
////		logger.info("ma10s : " + JSON.toJSONString(ma10s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockWeek> ma20s = null;
//		String ret_ma20 = Request.Get(url_ma20).execute().returnContent().asString();
//		ret_ma20 = ret_ma20.substring(ret_ma20.indexOf("(")+1, ret_ma20.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma20) && !ret_ma20.equals("null")) {
//			ma20s = JSON.parseArray(ret_ma20, StockWeek.class);
//		}
////		logger.info("ma20s : " + JSON.toJSONString(ma20s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockWeek> ma30s = null;
//		String ret_ma30 = Request.Get(url_ma30).execute().returnContent().asString();
//		ret_ma30 = ret_ma30.substring(ret_ma30.indexOf("(")+1, ret_ma30.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma30) && !ret_ma30.equals("null")) {
//			ma30s = JSON.parseArray(ret_ma30, StockWeek.class);
//		}
////		logger.info("ma30s : " + JSON.toJSONString(ma30s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		
//		parseStockWeeks(weeks, ma5s, ma10s, ma20s, ma30s);
		
		for (StockWeek stockWeek : weeks) {
//			if (DateUtil.isSameWeek(stockWeek.getDay())) {
//				continue;
//			}

			if (stockWeek.getOpen() < 0.001 || stockWeek.getLow() < 0.001) {
				continue;
			}
			
			stockWeek.setCode(stockBase.getCode());
			stockWeek.setName(stockBase.getName());
			stockWeek.setDay(DateUtil.parse2Friday(stockWeek.getDay()));
			stockWeek.setSourceType(SourceTypeEnum.WEEK.getCode());
			
			//lastTrade
			StockWeek lastTradeWeek = stockWeekMapper.selectLatestTradeWeek(stockWeek.getCode(), stockWeek.getDay());
			if (lastTradeWeek != null) {
				stockWeek.setLastTrade(lastTradeWeek.getTrade());
//				stockWeek.setCrossParams(JSON.toJSONString(PriceUtil.calMaCross(stockWeek, lastTradeWeek)));
			}
			
			logger.info("spider week : " + JSON.toJSONString(stockWeek));
			if (stockWeekMapper.selectByPrimaryKey(stockWeek.getCode(), stockWeek.getDay()) == null) {
				stockWeekMapper.insert(stockWeek);
			}else {
				stockWeekMapper.updateByPrimaryKey(stockWeek);
			}
			
//			if (DateUtil.isSameWeek(stockWeek.getDay(), stockBase.getDay())) {
//				stockBase.setWeekma5(stockWeek.getMa5());
//				stockBase.setWeekma10(stockWeek.getMa10());
//				stockBase.setWeekma20(stockWeek.getMa20());
//				stockBase.setWeekma30(stockWeek.getMa30());
//				stockBaseMapper.updateByPrimaryKeySelective(stockBase);
//			}
		}
		
	}
	
	
	public void parseStockWeeks(List<StockWeek> weeks, List<StockWeek> ma5s, List<StockWeek> ma10s, List<StockWeek> ma20s, List<StockWeek> ma30s) {
		Map<String, StockWeek> ma5Map = TradeConvertHelper.parseMap(ma5s);
		Map<String, StockWeek> ma10Map = TradeConvertHelper.parseMap(ma10s);
		Map<String, StockWeek> ma20Map = TradeConvertHelper.parseMap(ma20s);
		Map<String, StockWeek> ma30Map = TradeConvertHelper.parseMap(ma30s);
		for (StockWeek stockWeek : weeks) {
			StockWeek ma5 = ma5Map.get(stockWeek.getCode() + "_" + stockWeek.getDay());
			if (ma5 != null) {
				stockWeek.setMa5(ma5.getMa5());
			}
			
			StockWeek ma10 = ma10Map.get(stockWeek.getCode() + "_" + stockWeek.getDay());
			if (ma10 != null) {
				stockWeek.setMa10(ma10.getMa10());
			}
			
			StockWeek ma20 = ma20Map.get(stockWeek.getCode() + "_" + stockWeek.getDay());
			if (ma20 != null) {
				stockWeek.setMa20(ma20.getMa20());
			}
			
			StockWeek ma30 = ma30Map.get(stockWeek.getCode() + "_" + stockWeek.getDay());
			if (ma30 != null) {
				stockWeek.setMa30(ma30.getMa30());
			}
		}
	}
	

	public static void main(String[] args) {
		new StockMonthCrawler().run();
	}
}
