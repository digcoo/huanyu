package com.yh.bigdata.tts.common.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.yh.bigdata.tts.common.model.StockMin30;

public interface StockMin30Mapper {

	int deleteByPrimaryKey(@Param("code") String code, @Param("day") String day);

	int insert(StockMin30 record);

	StockMin30 selectByPrimaryKey(@Param("code") String code, @Param("day") String day);

	int updateByPrimaryKey(StockMin30 record);

	List<StockMin30> selectAll(@Param("codes") List<String> codes);
}
