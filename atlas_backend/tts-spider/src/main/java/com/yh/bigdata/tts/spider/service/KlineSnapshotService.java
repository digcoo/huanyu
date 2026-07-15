package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.model.KlineSnapshotBar;

import java.util.List;
import java.util.Map;

public interface KlineSnapshotService {

    boolean isBuilding();

    Map<String, Object> lastBuildResult();

    String resolveLatestTradingDay();

    /** 单股：截至 asOfDay 生成周/月/年快照（数据源 backtest_dayk） */
    void buildPeriodSnapshots(String code, String asOfDay);

    /** 全市场：截至 asOfDay 生成周/月/年快照 */
    int buildPeriodSnapshotsAll(String asOfDay);

    /** 对区间内每个交易日全市场生成快照 */
    int buildPeriodSnapshotsRange(String fromDay, String toDay);

    List<KlineSnapshotBar> loadWeekSnapshot(String code, String asOfDay);

    List<KlineSnapshotBar> loadMonthSnapshot(String code, String asOfDay);

    List<KlineSnapshotBar> loadYearSnapshot(String code, String asOfDay);
}
