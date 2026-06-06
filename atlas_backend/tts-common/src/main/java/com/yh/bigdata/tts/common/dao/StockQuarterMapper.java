package com.yh.bigdata.tts.common.dao;

import java.util.List;

import com.yh.bigdata.tts.common.param.StockPageQuery;
import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.yh.bigdata.tts.common.model.StockQuarter;

public interface StockQuarterMapper {

	int insert(StockQuarter record);

	StockQuarter selectByPrimaryKey(@Param("code") String code, @Param("day") String day);

	int updateByPrimaryKey(StockQuarter record);

	Page<StockQuarter> selectByPageQuery(StockPageQuery pageQuery);

	String selectLastTradeQuarter(@Param("fromDay") String fromDay);

	StockQuarter selectLatestTradeQuarter(@Param("code") String code, @Param("fromDay") String fromDay);
	
	List<StockQuarter> selectLatestQuarterList(@Param("code") String code, @Param("nquarters")Integer nquarters);
	
	List<StockQuarter> selectAll(@Param("codes")List<String> codes);
	
}