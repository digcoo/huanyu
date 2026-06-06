package com.yh.bigdata.tts.common.dao;

import com.yh.bigdata.tts.common.model.StockTarget;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StockTargetMapper {

	int insert(StockTarget record);

    StockTarget selectByPrimaryKey(@Param("code")String code, @Param("day")String day, @Param("strategy")String strategy);

    int update(StockTarget record);

    List<StockTarget> selectListByDay(@Param("day") String day);

    String selectLatestDay();

}