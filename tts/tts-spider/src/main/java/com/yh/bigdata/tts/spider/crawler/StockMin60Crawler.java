package com.yh.bigdata.tts.spider.crawler;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockMin60Mapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockMin60;
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
public class StockMin60Crawler {

	Logger logger = LoggerFactory.getLogger(StockMin60Crawler.class);

//	String base_url1 = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=sh600009&scale=15&ma=5&datalen=32";
//	String base_url2 = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=sh600009&scale=15&ma=5&datalen=32"
	
//	String base_url = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol=%s&scale=15&ma=%s&datalen=32";
	String base_url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=%s&scale=60&ma=%s&datalen=60";
	
	@Autowired
	StockBaseMapper stockBaseMapper;
	
	@Autowired
	StockMin60Mapper stockMin60Mapper;
	
	@Value("${spider.day.interval}")
	private String sleepMseconds = "1000";

	@Value("${spider.min30.startpage}")
	private String startPage = "1";
	
	private boolean persist = true;
	
	
	public void run() {
		
		long start = System.currentTimeMillis();
		
		logger.info("StockMin60Crawler loop start=========================================================");

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
							logger.error("StockMin60Crawler run exception, stock = " + JSON.toJSONString(stockBase), e);
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
			logger.error("StockMin60Crawler exception.....page = " + page, e);
			System.exit(-1);
		}
		
		logger.info("=========================================================StockMin60Crawler loop finish({}s)",(System.currentTimeMillis() - start)/1000);

	}

	public void spider(StockBase stockBase) throws ClientProtocolException, IOException, InterruptedException {
		@SuppressWarnings("deprecation")
		String url_day = String.format(base_url, stockBase.getCode(), URLEncoder.encode(JSON.toJSONString(Lists.newArrayList(1,5,10,20,30))));
//		String url_ma5 = String.format(base_url, stockBase.getCode(), 5);
//		String url_ma10 = String.format(base_url, stockBase.getCode(), 10);
//		String url_ma20 = String.format(base_url, stockBase.getCode(), 20);
//		String url_ma30 = String.format(base_url, stockBase.getCode(), 30);
		
		List<StockMin60> newMin60s = null;
		String ret_day = Request.Get(url_day)
				.addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sh000001/nc.shtml")			
				.execute().returnContent().asString();
		ret_day = ret_day.substring(ret_day.indexOf("(")+1, ret_day.indexOf(")"));
		if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
			newMin60s = JSON.parseArray(ret_day, StockMin60.class);
		}
////		logger.info("days : " + JSON.toJSONString(days));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockMin60> ma5s = null;
//		String ret_ma5 = Request.Get(url_ma5).execute().returnContent().asString();
//		ret_ma5 = ret_ma5.substring(ret_ma5.indexOf("(")+1, ret_ma5.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma5) && !ret_ma5.equals("null")) {
//			ma5s = JSON.parseArray(ret_ma5, StockMin60.class);
//		}
////		logger.info("ma5s : " + JSON.toJSONString(ma5s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockMin60> ma10s = null;
//		String ret_ma10 = Request.Get(url_ma10).execute().returnContent().asString();
//		ret_ma10 = ret_ma10.substring(ret_ma10.indexOf("(")+1, ret_ma10.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma10) && !ret_ma10.equals("null")) {
//			ma10s = JSON.parseArray(ret_ma10, StockMin60.class);
//		}
////		logger.info("ma10s : " + JSON.toJSONString(ma10s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockMin60> ma20s = null;
//		String ret_ma20 = Request.Get(url_ma20).execute().returnContent().asString();
//		ret_ma20 = ret_ma20.substring(ret_ma20.indexOf("(")+1, ret_ma20.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma20) && !ret_ma20.equals("null")) {
//			ma20s = JSON.parseArray(ret_ma20, StockMin60.class);
//		}
////		logger.info("ma20s : " + JSON.toJSONString(ma20s));
//		Thread.sleep(Long.parseLong(sleepMseconds));
//		List<StockMin60> ma30s = null;
//		String ret_ma30 = Request.Get(url_ma30).execute().returnContent().asString();
//		ret_ma30 = ret_ma30.substring(ret_ma30.indexOf("(")+1, ret_ma30.indexOf(")"));
//		if (StringUtils.isNotBlank(ret_ma30) && !ret_ma30.equals("null")) {
//			ma30s = JSON.parseArray(ret_ma30, StockMin60.class);
//		}
////		logger.info("ma30s : " + JSON.toJSONString(ma30s));
//		
//		parseStockMin60s(days, ma5s, ma10s, ma20s, ma30s);
//		
//		stockBase.setMa5(newMin60s.get(newMin60s.size() - 1).getMa5());
//		stockBase.setMa10(newMin60s.get(newMin60s.size() - 1).getMa10());
//		stockBase.setMa20(newMin60s.get(newMin60s.size() - 1).getMa20());
//		stockBase.setMa30(newMin60s.get(newMin60s.size() - 1).getMa30());
//		stockBase.setTrade(newMin60s.get(newMin60s.size() - 1).getTrade());
//		stockBase.setDay(newMin60s.get(newMin60s.size() - 1).getDay());
		
		//覆盖已存在的数据
		List<StockMin60> existMin60s = RealtimeStockCache.min60Map.get(stockBase.getCode());
		if (existMin60s == null) {
			RealtimeStockCache.min60Map.put(stockBase.getCode(), newMin60s);
		} else {
			Map<String, StockMin60> existMin60Map = existMin60s.stream().collect(Collectors.toMap(
							x -> x.getDay(),
							y -> y, 
							(key1, key2) -> key1));
			for (StockMin60 newMin60 : newMin60s) {
				StockMin60 existMin60 = existMin60Map.get(newMin60.getDay());
				if(existMin60 != null) {//覆盖
					existMin60.replace(newMin60);
				} else {
					existMin60s.add(newMin60);
				}
			}
			
		}
				
		if (!persist) {
			return;
		}

//		stockBaseMapper.updateByPrimaryKeySelective(stockBase);
		
//		for (StockMin60 stockMin60 : days) {
		for (int i = 0; i < newMin60s.size(); i++) {
		
			StockMin60 stockMin60 = newMin60s.get(i);
			StockMin60 lastStockMin60 = null;
			if(i == 0) {
				lastStockMin60 = stockMin60Mapper.selectLatestTradeMin60(stockMin60.getCode(), stockMin60.getDay());
			}else {
				lastStockMin60 = newMin60s.get(i-1);
			}
			try {
				
				if (stockMin60.getOpen() < 0.001) {
					continue;
				}
				
				stockMin60.setCode(stockBase.getCode());
				stockMin60.setName(stockBase.getName());
				stockMin60.setLastTrade(lastStockMin60!=null?lastStockMin60.getTrade():null);
//				stockMin60.setCrossParams(JSON.toJSONString(PriceUtil.calMaCross(stockMin60, lastStockMin60)));
				
				logger.info("spider Min60 : " + JSON.toJSONString(stockMin60));
				
				StockMin60 localStockMin60 = stockMin60Mapper.selectByPrimaryKey(stockMin60.getCode(), stockMin60.getDay());
				if (localStockMin60 == null) {
					stockMin60Mapper.insert(stockMin60);
				}else {
					stockMin60Mapper.updateByPrimaryKey(stockMin60);
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
//	public void parseStockMin60s(List<StockMin60> days, List<StockMin60> ma5s, List<StockMin60> ma10s, List<StockMin60> ma20s, List<StockMin60> ma30s) {
//		Map<String, StockMin60> ma5Map = StockMin60ConvertHelper.parseMap(ma5s);
//		Map<String, StockMin60> ma10Map = StockMin60ConvertHelper.parseMap(ma10s);
//		Map<String, StockMin60> ma20Map = StockMin60ConvertHelper.parseMap(ma20s);
//		Map<String, StockMin60> ma30Map = StockMin60ConvertHelper.parseMap(ma30s);
//		for (StockMin60 stockMin60 : days) {
//			StockMin60 ma5Stockday = ma5Map.get(stockMin60.getCode() + "_" + stockMin60.getDay());
//			if (ma5Stockday != null) {
//				stockMin60.setMa5(ma5Stockday.getMa5());
//			}
//			
//			StockMin60 ma10Stockday = ma10Map.get(stockMin60.getCode() + "_" + stockMin60.getDay());
//			if (ma10Stockday != null) {
//				stockMin60.setMa10(ma10Stockday.getMa10());
//			}
//			
//			StockMin60 ma20Stockday = ma20Map.get(stockMin60.getCode() + "_" + stockMin60.getDay());
//			if (ma20Stockday != null) {
//				stockMin60.setMa20(ma20Stockday.getMa20());
//			}
//			
//			StockMin60 ma30Stockday = ma30Map.get(stockMin60.getCode() + "_" + stockMin60.getDay());
//			if (ma30Stockday != null) {
//				stockMin60.setMa30(ma30Stockday.getMa30());
//			}
//		}
//	}
	
	public void setPersist(Boolean persist) {
		this.persist = persist;
	}

	public static void main(String[] args) {
		new StockMin60Crawler().run();
	}
}
