package com.yh.bigdata.tts.common.dao;

import java.util.List;

import com.yh.bigdata.tts.common.param.StockPageQuery;
import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.yh.bigdata.tts.common.model.StockYear;

public interface StockYearMapper {

	int deleteByPrimaryKey(@Param("code") String code, @Param("day") String day);

	int insert(StockYear record);

	StockYear selectByPrimaryKey(@Param("code") String code, @Param("day") String day);

	int updateByPrimaryKey(StockYear record);

	Page<StockYear> selectByPageQuery(StockPageQuery pageQuery);

	String selectLastTradeYear(@Param("fromDay") String fromDay);

	StockYear selectLatestTradeYear(@Param("code") String code, @Param("fromDay") String fromDay);

	List<StockYear> selectLatestYearList(@Param("code") String code, @Param("nyears") Integer nyears);
	
	List<StockYear> selectAll(@Param("codes")List<String> codes);

}