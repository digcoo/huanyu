package com.yh.bigdata.tts.spider.service;

import java.util.List;

import com.github.pagehelper.Page;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.model.StockQuarter;
import com.yh.bigdata.tts.common.model.StockWeek;
import com.yh.bigdata.tts.common.model.StockYear;
import com.yh.bigdata.tts.common.param.StockPageQuery;

/**
 * Created by zhou1 on 2018/12/20.
 */
public interface StockService {

	Page<StockBase> findByPageQuery(StockPageQuery pageQuery);
	
	Page<StockBase> findDabanByPageQuery(StockPageQuery pageQuery);
	
	Page<StockBase> findKaipanByPageQuery(StockPageQuery pageQuery);

	Page<StockBase> findNianxianByPageQuery(StockPageQuery pageQuery);

	Page<StockBase> findFanbaoByPageQuery(StockPageQuery pageQuery);

	Page<StockBase> findNFanbaoByPageQuery(StockPageQuery pageQuery);

	Page<StockBase> selectLPingtoudingByPageQuery(StockPageQuery pageQuery);

	Page<StockBase> findShangyingxianByPageQuery(StockPageQuery pageQuery);

	String findLastTradeDay(String fromDay);

	String findLastTradeWeek(String fromDay);

	String findLastTradeMonth(String fromDay);
	
	Page<StockBase> findZhangfuByPageQuery(StockPageQuery pageQuery);

	Page<StockBase> findTestPageQuery(StockPageQuery pageQuery);
	
	Page<StockBase> findTest2PageQuery(StockPageQuery pageQuery);

	Page<StockBase> findFridayPageQuery(StockPageQuery pageQuery);
	
	Page<StockBase> findXuanguByPageQuery(StockPageQuery pageQuery);
	
	List<StockDay> selectLatestDayList(int ndays);
	
	List<StockWeek> findLatestWeekList(int nweeks);

	List<StockMonth> findLatestMonthList(int nmonths);

	List<StockQuarter> findLatestQuarterList(int nquarters);

	List<StockYear> findLatestYearList(int nyears);

	List<StockBase> findAllStocks();

	StockBase findStockBase(String code);
	
	StockBase findStockBaseByName(String name);

	List<StockDay> findAllStockDays(List<String> codes);
	List<StockWeek> findAllStockWeeks(List<String> codes);
	List<StockMonth> findAllStockMonths(List<String> codes);
	List<StockYear> findAllStockYears(List<String> codes);
	List<StockQuarter> findAllStockQuarters(List<String> codes);

}
