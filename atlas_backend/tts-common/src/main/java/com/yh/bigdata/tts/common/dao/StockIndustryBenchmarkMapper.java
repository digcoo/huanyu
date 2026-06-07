package com.yh.bigdata.tts.common.dao;

import com.yh.bigdata.tts.common.model.StockIndustryBenchmark;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StockIndustryBenchmarkMapper {

    int deleteAll();

    int upsert(StockIndustryBenchmark row);

    StockIndustryBenchmark selectByIndustry(@Param("industry") String industry);

    List<StockIndustryBenchmark> selectAll();
}
