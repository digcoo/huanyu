package com.yh.bigdata.tts.spider.crawler;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockMin30Mapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockMin30;
import com.yh.bigdata.tts.common.model.StockMin60;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.common.utils.DateUtil;
import com.yh.bigdata.tts.common.utils.PriceUtil;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment
 */

@Component
public class StockMin30Crawler {

	Logger logger = LoggerFactory.getLogger(StockMin30Crawler.class);

//	String base_url1 = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sh600009&scale=15&ma=5&datalen=32";
//	String base_url2 = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=sh600009&scale=15&ma=5&datalen=32"
	
//	String base_url = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=%s&scale=15&ma=%s&datalen=32";
	String base_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=30&ma=%s&datalen=4";
	
	@Autowired
	StockBaseMapper stockBaseMapper;
	
	@Autowired
	StockMin30Mapper stockMin30Mapper;
	
	@Value("${spider.day.interval}")
	private String sleepMseconds = "1000";

	@Value("${spider.min30.startpage}")
	private String startPage = "1";
	
	private boolean persist = true;
	
	
	public void run() {
		
		long start = System.currentTimeMillis();
		
		logger.info("StockMin30Crawler loop start=========================================================");

		String nowTime = DateFormatUtils.format(Calendar.getInstance(), DateUtil.TIME_FORMAT_HH_MM);
		if (!persist && (nowTime.compareTo("09:26") < 0
							|| nowTime.compareTo("15:01") > 0))	{
			return;
		}
		
		int page = Integer.parseInt(startPage);
		int size = 100;
		try {
			
			//删除记录
			/*
			SELECT t1.code, t1.name, t1.day
			FROM dayk AS t1
			WHERE t1.code IN ('sh600000', 'sz000001', 'sh600265')
			AND (
			    SELECT COUNT(*)
			    FROM dayk AS t2
			    WHERE t2.code = t1.code AND t2.day <= t1.day
			) <= 10
			ORDER BY t1.code, t1.day
			*/
			
			while (true) {
				
				PageHelper.startPage(page, size);
				StockPageQuery pageQuery = new StockPageQuery(page, size);
				pageQuery.setIsSelectMode(false);
//				pageQuery.setCode("sz301510");
				
				Page<StockBase> pages = stockBaseMapper.selectByPageQuery(pageQuery);
				if (!CollectionUtils.isEmpty(pages.getResult())) {
					for (StockBase stockBase : pages) {
						try {
//							if (!stockBase.getCode().equals("sz301510")) {
//								continue;
//							}
							spider(stockBase);
							Thread.sleep(Long.parseLong(sleepMseconds));
						} catch (Exception e) {
							logger.error("StockMin30Crawler run exception, stock = " + JSON.toJSONString(stockBase), e);
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
			
		} catch (Exception e) {
			logger.error("StockMin30Crawler exception.....page = " + page, e);
			System.exit(-1);
		}
		
		logger.info("=========================================================StockMin30Crawler loop finish({}s)",(System.currentTimeMillis() - start)/1000);

	}

	public void spider(StockBase stockBase) throws ClientProtocolException, IOException, InterruptedException {
		@SuppressWarnings("deprecation")
		String url_day = String.format(base_url, stockBase.getCode(), URLEncoder.encode(JSON.toJSONString(Lists.newArrayList(1,5,10,20,30))));
//		String url_ma5 = String.format(base_url, stockBase.getCode(), 5);
//		String url_ma10 = String.format(base_url, stockBase.getCode(), 10);
//		String url_ma20 = String.format(base_url, stockBase.getCode(), 20);
//		String url_ma30 = String.format(base_url, stockBase.getCode(), 30);
		
		List<StockMin30> newMin30s = null;
		String ret_day = Request.Get(url_day)
				.addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sh000001/nc.shtml")			
				.execute().returnContent().asString();
		ret_day = ret_day.substring(ret_day.indexOf("(")+1, ret_day.indexOf(")"));
		if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
			newMin30s = JSON.parseArray(ret_day, StockMin30.class);
		}
////		logger.info("days : " + JSON.toJSONString(days));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockMin30> ma5s = null;
//		String ret_ma5 = Request.Get(url_ma5).execute().returnContent().asString();
//		ret_ma5 = ret_ma5.substring(ret_ma5.indexOf("(")+1, ret_ma5.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma5) && !ret_ma5.equals("null")) {
//			ma5s = JSON.parseArray(ret_ma5, StockMin30.class);
//		}
////		logger.info("ma5s : " + JSON.toJSONString(ma5s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockMin30> ma10s = null;
//		String ret_ma10 = Request.Get(url_ma10).execute().returnContent().asString();
//		ret_ma10 = ret_ma10.substring(ret_ma10.indexOf("(")+1, ret_ma10.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma10) && !ret_ma10.equals("null")) {
//			ma10s = JSON.parseArray(ret_ma10, StockMin30.class);
//		}
////		logger.info("ma10s : " + JSON.toJSONString(ma10s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockMin30> ma20s = null;
//		String ret_ma20 = Request.Get(url_ma20).execute().returnContent().asString();
//		ret_ma20 = ret_ma20.substring(ret_ma20.indexOf("(")+1, ret_ma20.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma20) && !ret_ma20.equals("null")) {
//			ma20s = JSON.parseArray(ret_ma20, StockMin30.class);
//		}
////		logger.info("ma20s : " + JSON.toJSONString(ma20s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockMin30> ma30s = null;
//		String ret_ma30 = Request.Get(url_ma30).execute().returnContent().asString();
//		ret_ma30 = ret_ma30.substring(ret_ma30.indexOf("(")+1, ret_ma30.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma30) && !ret_ma30.equals("null")) {
//			ma30s = JSON.parseArray(ret_ma30, StockMin30.class);
//		}
////		logger.info("ma30s : " + JSON.toJSONString(ma30s));
//		
//		parseStockMin30s(days, ma5s, ma10s, ma20s, ma30s);
		
		stockBase.setMa5(newMin30s.get(newMin30s.size() - 1).getMa5());
		stockBase.setMa10(newMin30s.get(newMin30s.size() - 1).getMa10());
		stockBase.setMa20(newMin30s.get(newMin30s.size() - 1).getMa20());
		stockBase.setMa30(newMin30s.get(newMin30s.size() - 1).getMa30());
		stockBase.setTrade(newMin30s.get(newMin30s.size() - 1).getTrade());
		stockBase.setDay(newMin30s.get(newMin30s.size() - 1).getDay());
		
		
		if (!persist) {
			//覆盖已存在的数据
			List<StockMin30> existMin30s = RealtimeStockCache.min30Map.get(stockBase.getCode());
			if (existMin30s == null) {
				RealtimeStockCache.min30Map.put(stockBase.getCode(), newMin30s);
			} else {
				Map<String, StockMin30> existMin30Map = existMin30s.stream().collect(Collectors.toMap(
								x -> x.getDay(),
								y -> y, 
								(key1, key2) -> key1));
				for (StockMin30 newMin30 : newMin30s) {
					StockMin30 existMin30 = existMin30Map.get(newMin30.getDay());
					if(existMin30 != null) {//覆盖
						existMin30.replace(newMin30);
					} else {
						existMin30s.add(newMin30);
					}
				}
				
			}
			return;
		}
		

		stockBaseMapper.updateByPrimaryKeySelective(stockBase);
		
//		for (StockMin30 stockMin30 : days) {
		for (int i = 0; i < newMin30s.size(); i++) {
		
			StockMin30 stockMin30 = newMin30s.get(i);			
			StockMin30 lastStockMin30 = null;
			if(i == 0) {
				lastStockMin30 = stockMin30Mapper.selectLatestTradeMin30(stockMin30.getCode(), stockMin30.getDay());
			}else {
				lastStockMin30 = newMin30s.get(i-1);
			}
			
			try {
				
				if (stockMin30.getOpen() < 0.001) {
					continue;
				}
				
				stockMin30.setCode(stockBase.getCode());
				stockMin30.setName(stockBase.getName());
				stockMin30.setLastTrade(lastStockMin30!=null?lastStockMin30.getTrade():null);
//				stockMin30.setCrossParams(JSON.toJSONString(PriceUtil.calMaCross(stockMin30, lastStockMin30)));
				
				logger.info("spider min30 : " + JSON.toJSONString(stockMin30));
				
				StockMin30 localStockMin30 = stockMin30Mapper.selectByPrimaryKey(stockMin30.getCode(), stockMin30.getDay());
				if (localStockMin30 == null) {
					stockMin30Mapper.insert(stockMin30);
				}else {
					stockMin30Mapper.updateByPrimaryKey(stockMin30);
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
	
//	
//	public void parseStockMin30s(List<StockMin30> days, List<StockMin30> ma5s, List<StockMin30> ma10s, List<StockMin30> ma20s, List<StockMin30> ma30s) {
//		Map<String, StockMin30> ma5Map = StockMin30ConvertHelper.parseMap(ma5s);
//		Map<String, StockMin30> ma10Map = StockMin30ConvertHelper.parseMap(ma10s);
//		Map<String, StockMin30> ma20Map = StockMin30ConvertHelper.parseMap(ma20s);
//		Map<String, StockMin30> ma30Map = StockMin30ConvertHelper.parseMap(ma30s);
//		for (StockMin30 stockMin30 : days) {
//			StockMin30 ma5Stockday = ma5Map.get(stockMin30.getCode() + "_" + stockMin30.getDay());
//			if (ma5Stockday != null) {
//				stockMin30.setMa5(ma5Stockday.getMa5());
//			}
//			
//			StockMin30 ma10Stockday = ma10Map.get(stockMin30.getCode() + "_" + stockMin30.getDay());
//			if (ma10Stockday != null) {
//				stockMin30.setMa10(ma10Stockday.getMa10());
//			}
//			
//			StockMin30 ma20Stockday = ma20Map.get(stockMin30.getCode() + "_" + stockMin30.getDay());
//			if (ma20Stockday != null) {
//				stockMin30.setMa20(ma20Stockday.getMa20());
//			}
//			
//			StockMin30 ma30Stockday = ma30Map.get(stockMin30.getCode() + "_" + stockMin30.getDay());
//			if (ma30Stockday != null) {
//				stockMin30.setMa30(ma30Stockday.getMa30());
//			}
//		}
//	}
	
	public void setPersist(Boolean persist) {
		this.persist = persist;
	}

	public static void main(String[] args) {
		new StockMin30Crawler().run();
	}
}
