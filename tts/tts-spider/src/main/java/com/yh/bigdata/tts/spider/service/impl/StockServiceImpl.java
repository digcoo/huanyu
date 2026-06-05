package com.yh.bigdata.tts.spider.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yh.bigdata.tts.common.dao.StockBaseMapper;
import com.yh.bigdata.tts.common.dao.StockDayMapper;
import com.yh.bigdata.tts.common.dao.StockMin30Mapper;
import com.yh.bigdata.tts.common.dao.StockMin60Mapper;
import com.yh.bigdata.tts.common.dao.StockMonthMapper;
import com.yh.bigdata.tts.common.dao.StockQuarterMapper;
import com.yh.bigdata.tts.common.dao.StockWeekMapper;
import com.yh.bigdata.tts.common.dao.StockYearMapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.StockMin30;
import com.yh.bigdata.tts.common.model.StockMin60;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.model.StockQuarter;
import com.yh.bigdata.tts.common.model.StockWeek;
import com.yh.bigdata.tts.common.model.StockYear;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import com.yh.bigdata.tts.spider.service.StockService;

/**
 * Created by zhou1 on 2018/12/20.
 */
@Service
public class StockServiceImpl implements StockService {
	
    @Autowired
    StockBaseMapper stockBaseMapper;
    
    @Autowired
    StockDayMapper stockDayMapper;
    
    @Autowired
    StockWeekMapper stockWeekMapper;
    
    @Autowired
    StockMonthMapper stockMonthMapper;

    @Autowired
    StockQuarterMapper stockQuarterMapper;

    @Autowired
    StockYearMapper stockYearMapper;

    @Autowired
    StockMin30Mapper stockMin30Mapper;

    @Autowired
    StockMin60Mapper stockMin60Mapper;

	@Override
	public Page<StockBase> findByPageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectByPageQuery(pageQuery);
	}


	@Override
	public String findLastTradeDay(String fromDay) {
		return stockDayMapper.selectLastTradeDay(fromDay);
	}


	@Override
	public String findLastTradeWeek(String fromDay) {
		return stockWeekMapper.selectLastTradeWeek(fromDay);
	}


	@Override
	public String findLastTradeMonth(String fromDay) {
		return stockMonthMapper.selectLastTradeMonth(fromDay);
	}


	@Override
	public Page<StockBase> findDabanByPageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectDabanByPageQuery(pageQuery);
	}


	@Override
	public Page<StockBase> findKaipanByPageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectKaipanByPageQuery(pageQuery);
	}


	@Override
	public Page<StockBase> findNianxianByPageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectNianxianByPageQuery(pageQuery);
	}


	@Override
	public Page<StockBase> findFanbaoByPageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectFanbaoByPageQuery(pageQuery);
	}

	@Override
	public Page<StockBase> findNFanbaoByPageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectNFanbaoByPageQuery(pageQuery);
	}
	
	@Override
	public Page<StockBase> findXuanguByPageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectXuanguByPageQuery(pageQuery);
	}


	@Override
	public Page<StockBase> selectLPingtoudingByPageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectLPingtoudingByPageQuery(pageQuery);
	}
	
	@Override
	public List<StockDay> selectLatestDayList(int ndays) {
		return stockDayMapper.selectLatestDayList(null, ndays);
	}


	@Override
	public List<StockWeek> findLatestWeekList(int nweeks) {
		return stockWeekMapper.selectLatestWeekList(null, nweeks);
	}

	@Override
	public List<StockMonth> findLatestMonthList(int nmoths) {
		return stockMonthMapper.selectLatestMonthList(null, nmoths);
	}
	
	@Override
	public List<StockQuarter> findLatestQuarterList(int nquarters) {
		return stockQuarterMapper.selectLatestQuarterList(null, nquarters);
	}

	@Override
	public List<StockYear> findLatestYearList(int nyears) {
		return stockYearMapper.selectLatestYearList(null, nyears);
	}

    @Override
    public void deleteAllMin30s() {
        stockMin30Mapper.deleteAll();
    }

	@Override
	public List<StockMin30> selectLatestMin30List(int nmin30s) {
		return stockMin30Mapper.selectLatestMin30List(null, nmin30s);
	}

	@Override
	public List<StockMin60> selectLatestMin60List(int nmin60s) {
		return stockMin60Mapper.selectLatestMin60List(null, nmin60s);
	}
	
	@Override
	public List<StockBase> findAllStocks() {
		return stockBaseMapper.selectAll();
	}


	@Override
	public Page<StockBase> findZhangfuByPageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectZhangfuByPageQuery(pageQuery);
	
	}


	@Override
	public Page<StockBase> findShangyingxianByPageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectShangyingxianPageQuery(pageQuery);
	}


	@Override
	public Page<StockBase> findTestPageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectTestByPageQuery(pageQuery);
	}


	@Override
	public Page<StockBase> findTest2PageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectTest2ByPageQuery(pageQuery);
	}
	


	@Override
	public Page<StockBase> findFridayPageQuery(StockPageQuery pageQuery) {
		PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize()/*, pageQuery.getOrderBy()*/);
		return stockBaseMapper.selectFridayByPageQuery(pageQuery);
	}

	@Override
	public StockBase findStockBase(String code) {
		return stockBaseMapper.selectByPrimaryKey(code);
	}

	@Override
	public StockBase findStockBaseByName(String name) {
		return stockBaseMapper.selectByName(name);
	}

	@Override
	public List<StockMin30> findAllStockMin30s(List<String> codes) {
		return stockMin30Mapper.selectAll(codes);

	}


	@Override
	public List<StockMin60> findAllStockMin60s(List<String> codes) {
		return stockMin60Mapper.selectAll(codes);
	}


	@Override
	public List<StockDay> findAllStockDays(List<String> codes) {
		return stockDayMapper.selectAll(codes);
	}


	@Override
	public List<StockWeek> findAllStockWeeks(List<String> codes) {
		return stockWeekMapper.selectAll(codes);
	}


	@Override
	public List<StockMonth> findAllStockMonths(List<String> codes) {
		return stockMonthMapper.selectAll(codes);
	}


	@Override
	public List<StockYear> findAllStockYears(List<String> codes) {
		return stockYearMapper.selectAll(codes);
	}


	@Override
	public List<StockQuarter> findAllStockQuarters(List<String> codes) {
		return stockQuarterMapper.selectAll(codes);
	}

}
