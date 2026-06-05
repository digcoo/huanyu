package com.yh.bigdata.tts.spider.crawler;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.yh.bigdata.tts.common.dao.StockDayMapper;
import com.yh.bigdata.tts.common.dao.StockMonthMapper;
import com.yh.bigdata.tts.common.dao.StockWeekMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.model.StockWeek;
import com.yh.bigdata.tts.common.param.StockPageQuery;

@Component
public class StockMA120Calculator {

	Logger logger = LoggerFactory.getLogger(StockMA120Calculator.class);

	@Autowired
	StockBaseMapper stockBaseMapper;

	@Autowired
	StockDayMapper stockDayMapper;

	@Autowired
	StockWeekMapper stockWeekMapper;

	@Autowired
	StockMonthMapper stockMonthMapper;

	
	public void run() {
		
		long start = System.currentTimeMillis();

		logger.info("StockMA120Calculator loop start=========================================================");
		
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
							remove120(stockBase);
						} catch (Exception e) {
							logger.error("StockMA120Calculator run exception, stock = " + JSON.toJSONString(stockBase), e);
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
			logger.error("StockMA120Calculator exception.....page = " + page, e);
		}
		logger.info("=========================================================StockMA120Calculator loop finish({}s)",(System.currentTimeMillis() - start)/1000);

	}

	public void spider(StockBase stockBase) throws ClientProtocolException, IOException, InterruptedException, ParseException {
		int dayNum = 1;
		int weekNum = 1;
		int monthNum = 1;
		
		List<StockDay> stockDays = stockDayMapper.selectLatestDayList(stockBase.getCode(), 120 + dayNum);
		List<StockWeek> stockWeeks = stockWeekMapper.selectLatestWeekList(stockBase.getCode(), 120 + weekNum);
		List<StockMonth> stockMonths = stockMonthMapper.selectLatestMonthList(stockBase.getCode(), 120 + monthNum);

		//day_ma
		List<StockDay> day_120s = null;
		for (int i = 0; i < dayNum; i++) {
			int offset = i;
			
			day_120s = stockDays.subList(stockDays.size()<=120+offset? 0 : stockDays.size() - 120 - offset, stockDays.size() - offset);
			StockDay stockDay0 = day_120s.get(day_120s.size() - 1);
			try {
				Double day_ma120 = day_120s.subList(day_120s.size() - 120, day_120s.size()).stream().mapToDouble(StockDay::getTrade).average().getAsDouble();
				stockBase.setMa120(new BigDecimal(day_ma120).setScale(3, RoundingMode.UP).doubleValue());
				stockDay0.setMa120(new BigDecimal(day_ma120).setScale(3, RoundingMode.UP).doubleValue());
			} catch (Exception e) {
			}
			try {
				Double day_ma60 = day_120s.subList(day_120s.size() - 60, day_120s.size()).stream().mapToDouble(StockDay::getTrade).average().getAsDouble();
				stockBase.setMa60(new BigDecimal(day_ma60).setScale(3, RoundingMode.UP).doubleValue());
				stockDay0.setMa60(new BigDecimal(day_ma60).setScale(3, RoundingMode.UP).doubleValue());
			} catch (Exception e) {
			}
			stockDayMapper.updateByPrimaryKey(stockDay0);
		}

		//week_ma
		List<StockWeek> week_120s = null;
		for (int i = 0; i < weekNum; i++) {
			int offset = i;
			
			week_120s = stockWeeks.subList(stockWeeks.size()<=120+offset? 0 : stockWeeks.size() - 120 - offset, stockWeeks.size() - offset);
			StockWeek stockWeek0 = week_120s.get(week_120s.size() - 1);
			try {
				Double week_ma120 = week_120s.subList(week_120s.size() - 120, week_120s.size()).stream().mapToDouble(StockWeek::getTrade).average().getAsDouble();
				stockWeek0.setMa120(new BigDecimal(week_ma120).setScale(3, RoundingMode.UP).doubleValue());
			} catch (Exception e) {
			}
			try {
				Double week_ma60 = week_120s.subList(week_120s.size() - 60, week_120s.size()).stream().mapToDouble(StockWeek::getTrade).average().getAsDouble();
				stockWeek0.setMa60(new BigDecimal(week_ma60).setScale(3, RoundingMode.UP).doubleValue());
			} catch (Exception e) {
			}
			stockWeekMapper.updateByPrimaryKey(stockWeek0);
		}

		//month_ma
		List<StockMonth> month_120s = null;
		for (int i = 0; i < monthNum; i++) {
			int offset = i;
			
			month_120s = stockMonths.subList(stockMonths.size()<=120+offset? 0 : stockMonths.size() - 120 - offset, stockMonths.size() - offset);
			StockMonth stockMonth0 = month_120s.get(month_120s.size() - 1);
			
			try {
				Double month_ma120 = month_120s.subList(month_120s.size() - 120, month_120s.size()).stream().mapToDouble(StockMonth::getTrade).average().getAsDouble();
				stockMonth0.setMa120(new BigDecimal(month_ma120).setScale(3, RoundingMode.UP).doubleValue());
			} catch (Exception e) {
			}
			try {
				Double month_ma60 = month_120s.subList(month_120s.size() - 60, month_120s.size()).stream().mapToDouble(StockMonth::getTrade).average().getAsDouble();
				stockMonth0.setMa60(new BigDecimal(month_ma60).setScale(3, RoundingMode.UP).doubleValue());
			} catch (Exception e) {
			}
			stockMonthMapper.updateByPrimaryKey(stockMonth0);
		}
		stockBaseMapper.updateByPrimaryKeySelective(stockBase);			
	}
	
	/**
	 * 保留最近的128条记录，其余的清理
	 */
	public void remove120(StockBase stockBase) throws ClientProtocolException, IOException, InterruptedException, ParseException {
		int remainDayNum = 121;
		int remainWeekNum = 121;
		int remainMonthNum = 121;

		//day_ma
		try {
			List<StockDay> alldays = stockDayMapper.selectLatestDayList(stockBase.getCode(), null);
			Collections.sort(alldays);
			alldays = alldays.subList(0, alldays.size() < remainDayNum?0 : alldays.size() - remainDayNum);
			if(!CollectionUtils.isEmpty(alldays)) {
				for (StockDay stockDay : alldays) {
					stockDayMapper.deleteByPrimaryKey(stockDay.getCode(), stockDay.getDay());
				}
			}
		} catch (Exception e) {
		}
		
		//week_ma
		try {
			List<StockWeek> allweeks = stockWeekMapper.selectLatestWeekList(stockBase.getCode(), null);
			Collections.sort(allweeks);
			allweeks = allweeks.subList(0, allweeks.size() < remainWeekNum?0 : allweeks.size() - remainWeekNum);
			if(!CollectionUtils.isEmpty(allweeks)) {
				for (StockWeek stockWeek : allweeks) {
					stockWeekMapper.deleteByPrimaryKey(stockWeek.getCode(), stockWeek.getDay());
				}
			}
		} catch (Exception e) {
		}
		//month_ma
		try {
			List<StockMonth> allmonths = stockMonthMapper.selectLatestMonthList(stockBase.getCode(), null);
			Collections.sort(allmonths);
			allmonths = allmonths.subList(0, allmonths.size() < remainMonthNum?0 : allmonths.size() - remainMonthNum);
			if(!CollectionUtils.isEmpty(allmonths)) {
				for (StockMonth stockMonth : allmonths) {
					stockMonthMapper.deleteByPrimaryKey(stockMonth.getCode(), stockMonth.getDay());
				}
			}
		} catch (Exception e) {
		}
	}
}
