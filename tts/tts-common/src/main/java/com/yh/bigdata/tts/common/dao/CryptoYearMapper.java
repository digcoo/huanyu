package com.yh.bigdata.tts.common.dao;

import com.github.pagehelper.Page;
import com.yh.bigdata.tts.common.binance.model.Candlestick;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CryptoYearMapper {

    int deleteByPrimaryKey(@Param("symbol")String symbol, @Param("day")String day);

    int insert(Candlestick record);

    Candlestick selectByPrimaryKey(@Param("symbol")String symbol, @Param("day")String day);

    int updateByPrimaryKey(Candlestick record);

    List<Candlestick> selectAll(@Param("symbols")List<String> symbol);


}