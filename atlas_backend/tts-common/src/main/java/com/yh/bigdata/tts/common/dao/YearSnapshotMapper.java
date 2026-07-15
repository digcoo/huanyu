package com.yh.bigdata.tts.common.dao;

import com.yh.bigdata.tts.common.model.KlineSnapshotBar;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface YearSnapshotMapper {

    int deleteByCodeAndAsOfDay(@Param("code") String code, @Param("asOfDay") String asOfDay);

    int deleteByAsOfDay(@Param("asOfDay") String asOfDay);

    int insertBatch(@Param("list") List<KlineSnapshotBar> list);

    List<KlineSnapshotBar> selectByCodeAndAsOfDay(@Param("code") String code, @Param("asOfDay") String asOfDay);

    int countByCodeAndAsOfDay(@Param("code") String code, @Param("asOfDay") String asOfDay);
}
