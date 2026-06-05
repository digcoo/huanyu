package com.yh.bigdata.tts.spider.xueqiu;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yh.bigdata.tts.common.model.Trade;
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
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockQuarterMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockQuarter;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.common.utils.DateUtil;
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
public class StockQuarterXueQiuCrawler {
	
	@Autowired
	StockBaseMapper stockBaseMapper;
	
	@Autowired
	StockQuarterMapper stockDayMapper;
	
	@Value("${spider.day.interval}")
	private String sleepMseconds = "1000";

	@Value("${spider.day.startpage}")
	private String startPage = "1";
	
	public void run(String code, int countX) {
		
		long start = System.currentTimeMillis();
		
		log.info("StockQuarterXueQiuCrawler loop start=========================================================");

		int page = Integer.parseInt(startPage);
		int size = 100;
		try {
			while (true) {

                log.info("spider quarter page = {}", page);

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
							if(StringUtils.isBlank(code)) {
								
								spider(stockBase, countX);
//								Thread.sleep(Long.parseLong(sleepMseconds));
								
							} else {
								
								if (stockBase.getCode().equals(code)) {
									spider(stockBase, countX);
									break;
								}
								
							}
//							Thread.sleep(Long.parseLong(sleepMseconds));
						} catch (Exception e) {
							log.error("StockQuarterXueQiuCrawler run exception, stock: {}", JSON.toJSONString(stockBase), e);
							if (e instanceof HttpResponseException) {
								Thread.sleep(20 * 60 * 1000 + 10 * 1000);
								
								spider(stockBase, countX);
							}
						}
					}
				}
				
				page++;
				
				if (page > pages.getPages()/* || page > 50*/) {
					break;
				}

			}
			
		} catch (Exception e) {
			log.info("StockQuarterXueQiuCrawler exception.....page: {}", page, e);
			System.exit(-1);
		}
		
		log.info("=========================================================StockQuarterXueQiuCrawler loop finish({}s)", (System.currentTimeMillis() - start)/1000);

	}

	public void spider(StockBase stockBase, int countX) throws ClientProtocolException, IOException, InterruptedException {
		
		Long time = System.currentTimeMillis(); 
		
//		String url_day = String.format(base_url, stockBase.getCode().toUpperCase(), time, "quarter");
		int count = -countX;

		if (stockBase.getName().contains("X")) {
			count = -100;
		}
		String url_day = String.format(XueQiuHttpUtils.base_url, stockBase.getCode().toUpperCase(), time, "quarter", count);


		List<StockQuarter> days = null;
		
		String ret_day = XueQiuHttpUtils.getData(url_day);

		
		if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
			days = XueQiuHttpUtils.parseStockTrades(ret_day, stockBase, StockQuarter.class);
		}
		
//		stockBase.setTrade(days.get(days.size() - 1).getTrade());
//		stockBase.setDay(days.get(days.size() - 1).getDay());
//		
//		stockBaseMapper.updateByPrimaryKeySelective(stockBase);
		
		for (StockQuarter stockYear : days) {
			try {
				
				if (stockYear.getOpen() < 0.001 || stockYear.getLow() < 0.001) {
					continue;
				}

//				if (DateUtil.isSameMonth(stockYear.getDay())) {
					stockYear.setDay(DateUtil.parse2QuarterLastDay(stockYear.getDay()));
//				}
				
//				logger.info("spider month : " + JSON.toJSONString(stockYear));
				
				StockQuarter localStockDay = stockDayMapper.selectByPrimaryKey(stockYear.getCode(), stockYear.getDay());
				if (localStockDay == null) {
					stockDayMapper.insert(stockYear);
				}else {
					stockDayMapper.updateByPrimaryKey(stockYear);
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


}
