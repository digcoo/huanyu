package com.yh.bigdata.tts.common.dao;

import java.util.List;

import com.yh.bigdata.tts.common.param.StockPageQuery;
import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.yh.bigdata.tts.common.model.StockMin30;

public interface StockMin30Mapper {
	
  int deleteByPrimaryKey(@Param("code")String code, @Param("min30")String min30);

  int insert(StockMin30 record);

  void deleteAll();

  StockMin30 selectByPrimaryKey(@Param("code")String code, @Param("min30")String min30);

  int updateByPrimaryKey(StockMin30 record);

  Page<StockMin30> selectByPageQuery(StockPageQuery pageQuery);
  
  String selectLastTradeMin30(@Param("fromMin30")String fromMin30);

  List<StockMin30> selectLatestMin30List(@Param("code")String code, @Param("nmin30s")Integer nmin30s);
  
  StockMin30 selectLatestTradeMin30(@Param("code")String code, @Param("fromMin30")String fromMin30);
  
  List<StockMin30> selectAll(@Param("codes")List<String> codes);

}