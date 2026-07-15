package com.yh.bigdata.tts.common.dao;

import com.yh.bigdata.tts.common.model.StockDay;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BacktestDaykMapper {

    int insert(StockDay record);

    int updateByPrimaryKey(StockDay record);

    StockDay selectByPrimaryKey(@Param("code") String code, @Param("day") String day);

    List<StockDay> selectUpToDay(@Param("code") String code, @Param("asOfDay") String asOfDay);

    List<String> selectDistinctCodes();

    List<String> selectDistinctTradingDays(@Param("fromDay") String fromDay, @Param("toDay") String toDay);

    String selectMaxTradingDay();

    String selectMinDayByCode(@Param("code") String code);

    String selectMaxDayByCode(@Param("code") String code);

    int countByCode(@Param("code") String code);
}
