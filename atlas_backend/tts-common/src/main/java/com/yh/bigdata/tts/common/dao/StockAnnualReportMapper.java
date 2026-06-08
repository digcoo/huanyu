package com.yh.bigdata.tts.common.dao;

import com.yh.bigdata.tts.common.model.IndustryYearlyMetrics;
import com.yh.bigdata.tts.common.model.StockAnnualReport;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StockAnnualReportMapper {

    int upsert(StockAnnualReport record);

    List<StockAnnualReport> selectByCode(@Param("code") String code, @Param("limit") int limit);

    StockAnnualReport selectLatestByCode(@Param("code") String code);

    StockAnnualReport selectByCodeAndYear(@Param("code") String code, @Param("reportYear") int reportYear);

    int countByCode(@Param("code") String code);

    /** 删除 (code, report_year) 重复行，保留 id 最大的一条 */
    int deleteDuplicateCodeYears();

    int countDistinctCodes();

    int countTotalRows();

    /** 年报条数 >= minYears 的股票数 */
    int countCodesWithMinYears(@Param("minYears") int minYears);

    List<IndustryYearlyMetrics> selectIndustryYearlyMetrics(@Param("industry") String industry);
}
