package com.yh.bigdata.tts.common.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.StockPageQuery;

public interface StockBaseMapper {

  int deleteByPrimaryKey(Long id);

  int insert(StockBase record);

  StockBase selectByPrimaryKey(String code);
  
  StockBase selectByName(@Param("name")String name);

//  int updateByPrimaryKey(StockBase record);

  int updateByPrimaryKeySelective(StockBase record);

  Page<StockBase> selectByPageQuery(StockPageQuery pageQuery);
  
  Page<StockBase> selectDabanByPageQuery(StockPageQuery pageQuery);
  
  Page<StockBase> selectKaipanByPageQuery(StockPageQuery pageQuery);

  Page<StockBase> selectNianxianByPageQuery(StockPageQuery pageQuery);

  Page<StockBase> selectFanbaoByPageQuery(StockPageQuery pageQuery);

  Page<StockBase> selectShizixingByPageQuery(StockPageQuery pageQuery);
  
  Page<StockBase> selectNFanbaoByPageQuery(StockPageQuery pageQuery);
  
  Page<StockBase> selectNLFanbaoByPageQuery(StockPageQuery pageQuery);

  Page<StockBase> selectLPingtoudingByPageQuery(StockPageQuery pageQuery);

  Page<StockBase> selectZhangfuByPageQuery(StockPageQuery pageQuery);

  Page<StockBase> selectShangyingxianPageQuery(StockPageQuery pageQuery);

  Page<StockBase> selectTestByPageQuery(StockPageQuery pageQuery);

  Page<StockBase> selectTest2ByPageQuery(StockPageQuery pageQuery);
  
  Page<StockBase> selectFridayByPageQuery(StockPageQuery pageQuery);
  
  Page<StockBase> selectXuanguByPageQuery(StockPageQuery pageQuery);
  
  Page<StockBase> selectDaZhangByPageQuery(StockPageQuery pageQuery);

  List<StockBase> selectAll();

  List<StockBase> searchByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);

  int countAll();

  int countWithIndustry();

  int countWithMainBusiness();

  int countWithBusinessBrief();

  int countWithPeTtm();

  /** pe_ttm 为空或 <=0，用于年报兜底计算 */
  List<StockBase> selectWithoutPositivePeTtm();

}