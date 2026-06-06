package com.yh.bigdata.tts.common.dao;

import java.util.List;

import com.yh.bigdata.tts.common.param.StockPageQuery;
import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.yh.bigdata.tts.common.model.StockMonth;

public interface StockMonthMapper {

	int deleteByPrimaryKey(@Param("code") String code, @Param("day") String day);

	int insert(StockMonth record);

	StockMonth selectByPrimaryKey(@Param("code") String code, @Param("day") String day);

	int updateByPrimaryKey(StockMonth record);

	Page<StockMonth> selectByPageQuery(StockPageQuery pageQuery);

	String selectLastTradeMonth(@Param("fromDay") String fromDay);

	StockMonth selectLatestTradeMonth(@Param("code") String code, @Param("fromDay") String fromDay);
	
	List<StockMonth> selectLatestMonthList(@Param("code") String code, @Param("nmonths")Integer nmonths);
	
	List<StockMonth> selectAll(@Param("codes")List<String> codes);
}