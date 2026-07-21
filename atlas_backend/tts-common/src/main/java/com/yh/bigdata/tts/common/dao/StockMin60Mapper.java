package com.yh.bigdata.tts.common.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.yh.bigdata.tts.common.model.StockMin60;

public interface StockMin60Mapper {

	int deleteByPrimaryKey(@Param("code") String code, @Param("day") String day);

	int insert(StockMin60 record);

	StockMin60 selectByPrimaryKey(@Param("code") String code, @Param("day") String day);

	int updateByPrimaryKey(StockMin60 record);

	List<StockMin60> selectAll(@Param("codes") List<String> codes);

	List<StockMin60> selectRecentByCode(@Param("code") String code, @Param("limit") int limit);
}
