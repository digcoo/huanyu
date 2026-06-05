package com.yh.bigdata.tts.common.dao;

import com.github.pagehelper.Page;
import com.yh.bigdata.tts.common.model.CryptoBase;
import com.yh.bigdata.tts.common.param.CryptoPageQuery;
import com.yh.bigdata.tts.common.param.StockPageQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CryptoBaseMapper {

    int deleteByPrimaryKey(Long id);

    int insert(CryptoBase record);

    CryptoBase selectByPrimaryKey(@Param("symbol") String symbol);

    int updateByPrimaryKeySelective(CryptoBase record);

    Page<CryptoBase> selectByPageQuery(CryptoPageQuery pageQuery);

    List<CryptoBase> selectAll();
  
}