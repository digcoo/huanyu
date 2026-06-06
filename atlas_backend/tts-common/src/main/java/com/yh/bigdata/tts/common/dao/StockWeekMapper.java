package com.yh.bigdata.tts.common.dao;

import java.util.List;

import com.yh.bigdata.tts.common.param.StockPageQuery;
import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.yh.bigdata.tts.common.model.StockWeek;

public interface StockWeekMapper {

  int deleteByPrimaryKey(@Param("code")String code, @Param("day")String day);

  int insert(StockWeek record);

  StockWeek selectByPrimaryKey(@Param("code")String code, @Param("day")String day);

  int updateByPrimaryKey(StockWeek record);

  Page<StockWeek> selectByPageQuery(StockPageQuery pageQuery);
  
  String selectLastTradeWeek(@Param("fromDay")String fromDay);
  
  StockWeek selectLatestTradeWeek(@Param("code")String code, @Param("fromDay")String fromDay);
  
  List<StockWeek> selectLatestWeekList(@Param("code")String code, @Param("nweeks")Integer nweeks);

  List<StockWeek> selectAll(@Param("codes")List<String> codes);

}