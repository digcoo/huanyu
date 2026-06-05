package com.yh.bigdata.tts.common.dao;

import java.util.List;

import com.yh.bigdata.tts.common.param.StockPageQuery;
import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.yh.bigdata.tts.common.model.StockDay;

public interface StockDayMapper {

  int deleteByPrimaryKey(@Param("code")String code, @Param("day")String day);

  int insert(StockDay record);

  StockDay selectByPrimaryKey(@Param("code")String code, @Param("day")String day);

  int updateByPrimaryKey(StockDay record);

  Page<StockDay> selectByPageQuery(StockPageQuery pageQuery);
  
  String selectLastTradeDay(@Param("fromDay")String fromDay);

  List<StockDay> selectLatestDayList(@Param("code")String code, @Param("ndays")Integer ndays);
  
  StockDay selectLatestTradeDay(@Param("code")String code, @Param("fromDay")String fromDay);
  
  List<StockDay> selectAll(@Param("codes")List<String> codes);

}