package com.yh.bigdata.tts.common.dao;

import java.util.List;

import com.yh.bigdata.tts.common.param.StockPageQuery;
import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.yh.bigdata.tts.common.model.StockMin60;

public interface StockMin60Mapper {

  int deleteByPrimaryKey(@Param("code")String code, @Param("min60")String min60);

  int insert(StockMin60 record);

  StockMin60 selectByPrimaryKey(@Param("code")String code, @Param("min60")String min60);

  int updateByPrimaryKey(StockMin60 record);

  Page<StockMin60> selectByPageQuery(StockPageQuery pageQuery);
  
  String selectLastTradeMin60(@Param("fromMin60")String fromMin60);

  List<StockMin60> selectLatestMin60List(@Param("code")String code, @Param("nmin60s")Integer nmin60s);
  
  StockMin60 selectLatestTradeMin60(@Param("code")String code, @Param("fromMin60")String fromMin60);
  
  List<StockMin60> selectAll(@Param("codes")List<String> codes);

}