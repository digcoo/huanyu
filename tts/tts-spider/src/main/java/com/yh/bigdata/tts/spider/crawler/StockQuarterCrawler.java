package com.yh.bigdata.tts.spider.crawler;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockMonthMapper;
import com.yh.bigdata.tts.common.dao.StockQuarterMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.model.StockQuarter;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.common.utils.MathUtil;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment
 */

@Component
public class StockQuarterCrawler {

	Logger logger = LoggerFactory.getLogger(StockQuarterCrawler.class);

	@Autowired
	StockBaseMapper stockBaseMapper;
	
	@Autowired
	StockMonthMapper stockMonthMapper;
	
	@Autowired
	StockQuarterMapper stockQuarterMapper;
	
	public void run() {
		
		long start = System.currentTimeMillis();

		logger.info("StockQuarterCrawler loop start=========================================================");
		
		int page = 1;
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
							spider(stockBase);
						} catch (Exception e) {
							logger.error("StockQuarterCrawler run exception, stock = " + JSON.toJSONString(stockBase), e);
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
			logger.error("StockQuarterCrawler exception.....page = " + page, e);
		}
		logger.info("=========================================================StockQuarterCrawler loop finish({}s)",(System.currentTimeMillis() - start)/1000);

	}

	public void spider(StockBase stockBase) throws ClientProtocolException, IOException, InterruptedException, ParseException {
		
		List<StockMonth> stockMonths = stockMonthMapper.selectLatestMonthList(stockBase.getCode(), 9);
		Collections.sort(stockMonths);
		
		Double open = 0.0;
		Double trade = 0.0;
		Double lastTrade = 0.0;
		Double high = Double.MIN_VALUE;
		Double low = Double.MAX_VALUE;
		StockQuarter stockQuarter = new StockQuarter();
		stockQuarter.setCode(stockBase.getCode());
		stockQuarter.setName(stockBase.getName());
		
		Boolean isNewQuarterLoop = true;
		for (int i = 0; i < stockMonths.size(); i++) {
			StockMonth stockMonth = stockMonths.get(i);
			high = MathUtil.max(high, stockMonth.getHigh());
			low = MathUtil.min(low, stockMonth.getLow());
			if (isNewQuarterLoop) {
				open = stockMonth.getOpen();
				lastTrade = stockMonth.getLastTrade();
				isNewQuarterLoop = false;
			}
			if ((i + 1) % 3 == 0 && !isNewQuarterLoop) {
				trade = stockMonth.getTrade();
				
				stockQuarter.setOpen(open);
				stockQuarter.setTrade(trade);
				stockQuarter.setHigh(high);
				stockQuarter.setLow(low);
				stockQuarter.setLastTrade(lastTrade);
				stockQuarter.setDay(stockMonth.getDay());
				
				if(stockQuarterMapper.selectByPrimaryKey(stockQuarter.getCode(), stockQuarter.getDay()) == null){
					stockQuarterMapper.insert(stockQuarter);
				}else{
					stockQuarterMapper.updateByPrimaryKey(stockQuarter);
				}
				
				open = 0.0;
				trade = 0.0;
				lastTrade = 0.0;
				high = Double.MIN_VALUE;
				low = Double.MAX_VALUE;
				stockQuarter = new StockQuarter();
				stockQuarter.setCode(stockBase.getCode());
				stockQuarter.setName(stockBase.getName());
				isNewQuarterLoop = true;
			}
		}
	}

}
