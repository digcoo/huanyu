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
import com.yh.bigdata.tts.common.dao.StockMin30Mapper;
import com.yh.bigdata.tts.common.dao.StockMin60Mapper;
import com.yh.bigdata.tts.common.dao.StockMonthMapper;
import com.yh.bigdata.tts.common.dao.StockWeekMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.StockMin15;
import com.yh.bigdata.tts.common.model.StockMin30;
import com.yh.bigdata.tts.common.model.StockMin60;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.model.StockWeek;
import com.yh.bigdata.tts.common.param.StockPageQuery;

/**
 * @author duyp
 * 
 * @date 2019/04/12
 * 
 * @comment
 */

@Component
public class StockMA120Calculator {

	Logger logger = LoggerFactory.getLogger(StockMA120Calculator.class);

	@Autowired
	StockBaseMapper stockBaseMapper;

	@Autowired
	StockMin30Mapper stockMin30Mapper;
	
	@Autowired
	StockMin60Mapper stockMin60Mapper;

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
		int min15Num = 16;
		int min30Num = 8;
		int min60Num = 4;
		int dayNum = 1;
		int weekNum = 1;
		int monthNum = 1;
		
		List<StockMin30> stockMin30s = stockMin30Mapper.selectLatestMin30List(stockBase.getCode(), 120 + min30Num);
		List<StockMin60> stockMin60s = stockMin60Mapper.selectLatestMin60List(stockBase.getCode(), 120 + min60Num);
		List<StockDay> stockDays = stockDayMapper.selectLatestDayList(stockBase.getCode(), 120 + dayNum);
		List<StockWeek> stockWeeks = stockWeekMapper.selectLatestWeekList(stockBase.getCode(), 120 + weekNum);
		List<StockMonth> stockMonths = stockMonthMapper.selectLatestMonthList(stockBase.getCode(), 120 + monthNum);

		//min30_ma
		List<StockMin30> min30_120s = null;
		for (int i = 0; i < min30Num; i++) {
			int offset = i;
			
			min30_120s = stockMin30s.subList(stockMin30s.size()<= 120+offset? 0 : stockMin30s.size() - 120 - offset, stockMin30s.size() - offset);
			StockMin30 stockMin30_0 = min30_120s.get(min30_120s.size() - 1);
			try {
				Double min30_ma120 = min30_120s.subList(min30_120s.size() - 120, min30_120s.size()).stream().mapToDouble(StockMin30::getTrade).average().getAsDouble();
//				stockBase.setMin30ma120(new BigDecimal(min30_ma120).setScale(3, RoundingMode.UP).doubleValue());
				stockMin30_0.setMa120(new BigDecimal(min30_ma120).setScale(3, RoundingMode.UP).doubleValue());
			} catch (Exception e) {
			}
			try {
				Double min30_ma60 = min30_120s.subList(min30_120s.size() - 60, min30_120s.size()).stream().mapToDouble(StockMin30::getTrade).average().getAsDouble();
//				stockBase.setMin30ma60(new BigDecimal(min30_ma60).setScale(3, RoundingMode.UP).doubleValue());
				stockMin30_0.setMa60(new BigDecimal(min30_ma60).setScale(3, RoundingMode.UP).doubleValue());
			} catch (Exception e) {
			}
			stockMin30Mapper.updateByPrimaryKey(stockMin30_0);
		}
		
		//min60_ma
		List<StockMin60> min60_120s = null;
		for (int i = 0; i < min60Num; i++) {
			int offset = i;
			
			min60_120s = stockMin60s.subList(stockMin60s.size()<=120+offset? 0 : stockMin60s.size() - 120 - offset, stockMin60s.size() - offset);
			StockMin60 stockMin60_0 = min60_120s.get(min60_120s.size() - 1);
			try {
				Double min60_ma120 = min60_120s.subList(min60_120s.size() - 120, min60_120s.size()).stream().mapToDouble(StockMin60::getTrade).average().getAsDouble();
//				stockBase.setMin60ma120(new BigDecimal(min60_ma120).setScale(3, RoundingMode.UP).doubleValue());
				stockMin60_0.setMa120(new BigDecimal(min60_ma120).setScale(3, RoundingMode.UP).doubleValue());
			} catch (Exception e) {
			}
			try {
				Double min60_ma60 = min60_120s.subList(min60_120s.size() - 60, min60_120s.size()).stream().mapToDouble(StockMin60::getTrade).average().getAsDouble();
//				stockBase.setMin60ma60(new BigDecimal(min60_ma60).setScale(3, RoundingMode.UP).doubleValue());
				stockMin60_0.setMa60(new BigDecimal(min60_ma60).setScale(3, RoundingMode.UP).doubleValue());
			} catch (Exception e) {
			}
			stockMin60Mapper.updateByPrimaryKey(stockMin60_0);
		}
		
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
//				stockBase.setWeekma120(new BigDecimal(week_ma120).setScale(3, RoundingMode.UP).doubleValue());
				stockWeek0.setMa120(new BigDecimal(week_ma120).setScale(3, RoundingMode.UP).doubleValue());
			} catch (Exception e) {
			}
			try {
				Double week_ma60 = week_120s.subList(week_120s.size() - 60, week_120s.size()).stream().mapToDouble(StockWeek::getTrade).average().getAsDouble();
//				stockBase.setWeekma60(new BigDecimal(week_ma60).setScale(3, RoundingMode.UP).doubleValue());
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
//				stockBase.setMonthma120(new BigDecimal(month_ma120).setScale(3, RoundingMode.UP).doubleValue());
				stockMonth0.setMa120(new BigDecimal(month_ma120).setScale(3, RoundingMode.UP).doubleValue());
			} catch (Exception e) {
			}
			try {
				Double month_ma60 = month_120s.subList(month_120s.size() - 60, month_120s.size()).stream().mapToDouble(StockMonth::getTrade).average().getAsDouble();
//				stockBase.setMonthma60(new BigDecimal(month_ma60).setScale(3, RoundingMode.UP).doubleValue());
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
		int remainMin15Num = 136;
		int remainMin30Num = 128;
		int remainMin60Num = 124;
		int remainDayNum = 121;
		int remainWeekNum = 121;
		int remainMonthNum = 121;

        //min30_ma
		try {
			List<StockMin30> allmin30s = stockMin30Mapper.selectLatestMin30List(stockBase.getCode(), null);
			Collections.sort(allmin30s);
			allmin30s = allmin30s.subList(0, allmin30s.size() < remainMin30Num?0 : allmin30s.size() - remainMin30Num);
			if(!CollectionUtils.isEmpty(allmin30s)) {
				for (StockMin30 stockMin30 : allmin30s) {
					stockMin30Mapper.deleteByPrimaryKey(stockMin30.getCode(), stockMin30.getDay());
				}
			}
		} catch (Exception e) {
		}
		
		//min60_ma
		try {
			List<StockMin60> allmin60s = stockMin60Mapper.selectLatestMin60List(stockBase.getCode(), null);
			Collections.sort(allmin60s);
			allmin60s = allmin60s.subList(0, allmin60s.size() < remainMin60Num?0 : allmin60s.size() - remainMin60Num);
			if(!CollectionUtils.isEmpty(allmin60s)) {
				for (StockMin60 stockMin60 : allmin60s) {
					stockMin60Mapper.deleteByPrimaryKey(stockMin60.getCode(), stockMin60.getDay());
				}
			}
		} catch (Exception e) {
		}
		
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
