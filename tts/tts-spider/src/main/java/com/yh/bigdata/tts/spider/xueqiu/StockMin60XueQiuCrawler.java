package com.yh.bigdata.tts.spider.xueqiu;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.yh.bigdata.tts.common.model.StockMin30;
import lombok.extern.slf4j.Slf4j;
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
import com.yh.bigdata.tts.common.constants.Constants;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockMin60Mapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockMin60;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.spider.utils.XueQiuHttpUtils;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment
 */

@Component
@Slf4j
public class StockMin60XueQiuCrawler {

	@Autowired
	StockBaseMapper stockBaseMapper;
	
	@Autowired
	StockMin60Mapper stockMin60Mapper;
	
	@Value("${spider.day.interval}")
	private String sleepMseconds = "1000";

	@Value("${spider.day.startpage}")
	private String startPage = "1";

	private boolean persist = true;
	
	public void run() {
		
		long start = System.currentTimeMillis();
		
		log.info("StockMin60XueQiuCrawler loop start=========================================================");

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
							if (stockBase.getCode().startsWith("sh688")) {
								continue;	
							}
							spider(stockBase);
//							Thread.sleep(Long.parseLong(sleepMseconds));
						} catch (Exception e) {
							log.error("StockMin60XueQiuCrawler run exception, stock = " + JSON.toJSONString(stockBase), e);
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
				
				log.info("page = " + page);

			}
			
		} catch (Exception e) {
			log.error("StockMin60XueQiuCrawler exception.....page = " + page, e);
			System.exit(-1);
		}
		
		log.info("=========================================================StockMin60XueQiuCrawler loop finish({}s)",(System.currentTimeMillis() - start)/1000);

	}

	public void spider(StockBase stockBase) throws ClientProtocolException, IOException, InterruptedException {
		
		Long time = System.currentTimeMillis(); 
		
		String url_day = String.format(XueQiuHttpUtils.base_url, stockBase.getCode().toUpperCase(), time, "60m");

		
		List<StockMin60> newMin60s = null;
		
		String ret_day = XueQiuHttpUtils.getData(url_day);
		
		if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
            newMin60s = XueQiuHttpUtils.parseStockTrades(ret_day, stockBase, StockMin60.class);
		}
//		
//		stockBase.setTrade(days.get(days.size() - 1).getTrade());
//		stockBase.setDay(days.get(days.size() - 1).getDay());
//		
//		stockBaseMapper.updateByPrimaryKeySelective(stockBase);
		
		//覆盖已存在的数据
		if (!persist) {
			
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

//			if (stockBase.getCode().equals("sz301141")) {
//				List<StockMin60> min30s = RealtimeStockCache.min30Map.get(stockBase.getCode());
//				System.out.println(JSON.toJSONString(min30s.subList(min30s.size() - 5, min30s.size())));
//			}
			
			return;
		}
		
		
		
		for (int i = 0; i < newMin60s.size(); i++) {
			
			try {
				StockMin60 stockMin60 = newMin60s.get(i);
				
				if (stockMin60.getOpen() < 0.001 || stockMin60.getLow() < 0.001) {
					continue;
				}
				
//				logger.info("spider min30 : " + JSON.toJSONString(stockMin60));
				
				StockMin60 localStockDay = stockMin60Mapper.selectByPrimaryKey(stockMin60.getCode(), stockMin60.getDay());
				if (localStockDay == null) {
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
	
	
	public void setPersist(Boolean persist) {
		this.persist = persist;
	}
}
