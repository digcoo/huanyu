package com.yh.bigdata.tts.spider.xueqiu;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import com.yh.bigdata.tts.common.constants.Constants;
import com.yh.bigdata.tts.common.constants.SourceTypeEnum;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockWeekMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockWeek;
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
public class StockWeekXueQiuCrawler {

	@Autowired
	StockBaseMapper stockBaseMapper;
	
	@Autowired
	StockWeekMapper stockDayMapper;
	
	@Value("${spider.day.interval}")
	private long sleepMs = 1000;

	@Value("${spider.day.startpage}")
	private Integer startPage = 1;
	
	public void run(String code, int countX) {
		
		long start = System.currentTimeMillis();
		
		log.info("StockWeekXueQiuCrawler loop start...");

		int page = startPage;
		int size = 100;
		try {
			while (true) {
                log.info("spider week page = {}", page);

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
//								Thread.sleep(sleepMs);
								
							} else {
								
								if (stockBase.getCode().equals(code)) {
									spider(stockBase, countX);
									break;
								}
								
							}
//							Thread.sleep(Long.parseLong(sleepMseconds));
						} catch (Exception e) {
							log.error("StockWeekXueQiuCrawler run exception, stock: {}", JSON.toJSONString(stockBase), e);
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
			log.error("StockWeekXueQiuCrawler exception.....page: {}", page, e);
			System.exit(-1);
		}

        log.info("StockWeekXueQiuCrawler loop finish({}s)\n\n", (System.currentTimeMillis() - start)/1000);

	}

	public void spider(StockBase stockBase, int countX) throws ClientProtocolException, IOException, InterruptedException {
		
		Long time = System.currentTimeMillis(); 
		
//		String url_day = String.format(base_url, stockBase.getCode().toUpperCase(), time, "week");

		int count = -countX;

		if (stockBase.getName().contains("X")) {
			count = -100;
		}
		String url_day = String.format(XueQiuHttpUtils.base_url, stockBase.getCode().toUpperCase(), time, "week", count);

		
		List<StockWeek> days = null;
		
		String ret_day = XueQiuHttpUtils.getData(url_day);
		
		if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
			days = XueQiuHttpUtils.parseStockTrades(ret_day, stockBase, StockWeek.class);
		}
		
//		stockBase.setTrade(days.get(days.size() - 1).getTrade());
//		stockBase.setDay(days.get(days.size() - 1).getDay());
//		
//		stockBaseMapper.updateByPrimaryKeySelective(stockBase);

        for (StockWeek stockWeek : days) {
			try {
				
				if (stockWeek.getOpen() < 0.001 || stockWeek.getLow() < 0.001) {
					continue;
				}

//				if (DateUtil.isSameWeek(stockWeek.getDay())) {
					stockWeek.setDay(DateUtil.parse2Friday(stockWeek.getDay()));
//				}
				
//				logger.info("spider week : " + JSON.toJSONString(stockWeek));

				StockWeek localStockDay = stockDayMapper.selectByPrimaryKey(stockWeek.getCode(), stockWeek.getDay());
				if (localStockDay == null) {
					stockDayMapper.insert(stockWeek);
				}else {
					stockDayMapper.updateByPrimaryKey(stockWeek);
				}
				
			} catch (Exception e) {
				
				if (e instanceof DuplicateKeyException) {
                    continue;
				}else {
					log.error("stock week crawler exception.. url_day = {}", url_day, e);
				}
			}
		}
		
	}

}
