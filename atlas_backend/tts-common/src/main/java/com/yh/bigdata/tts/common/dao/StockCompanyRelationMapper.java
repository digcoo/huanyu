package com.yh.bigdata.tts.common.dao;

import com.yh.bigdata.tts.common.model.StockCompanyRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StockCompanyRelationMapper {

    int deleteByCode(@Param("code") String code);

    int insertBatch(@Param("list") List<StockCompanyRelation> list);

    List<StockCompanyRelation> selectByCode(@Param("code") String code);
}
