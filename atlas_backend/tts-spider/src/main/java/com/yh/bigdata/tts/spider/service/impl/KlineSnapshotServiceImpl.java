package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.backtest.KlineSnapshotBuilder;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dao.BacktestDaykMapper;
import com.yh.bigdata.tts.common.dao.MonthSnapshotMapper;
import com.yh.bigdata.tts.common.dao.WeekSnapshotMapper;
import com.yh.bigdata.tts.common.dao.YearSnapshotMapper;
import com.yh.bigdata.tts.common.model.KlineSnapshotBar;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.service.KlineSnapshotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class KlineSnapshotServiceImpl implements KlineSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(KlineSnapshotServiceImpl.class);
    private static final int BATCH_SIZE = 500;

    private final AtomicBoolean building = new AtomicBoolean(false);
    private volatile Map<String, Object> lastBuildResult = new HashMap<>();

    @Autowired
    private BacktestDaykMapper backtestDaykMapper;
    @Autowired
    private WeekSnapshotMapper weekSnapshotMapper;
    @Autowired
    private MonthSnapshotMapper monthSnapshotMapper;
    @Autowired
    private YearSnapshotMapper yearSnapshotMapper;

    @Override
    public boolean isBuilding() {
        return building.get();
    }

    @Override
    public Map<String, Object> lastBuildResult() {
        return lastBuildResult;
    }

    @Override
    public String resolveLatestTradingDay() {
        return backtestDaykMapper.selectMaxTradingDay();
    }

    @Override
    @Transactional
    public void buildPeriodSnapshots(String code, String asOfDay) {
        List<StockDay> dayRows = backtestDaykMapper.selectUpToDay(code, asOfDay);
        if (dayRows == null || dayRows.isEmpty()) {
            return;
        }
        List<Trade> dayBars = new ArrayList<>(dayRows);

        weekSnapshotMapper.deleteByCodeAndAsOfDay(code, asOfDay);
        monthSnapshotMapper.deleteByCodeAndAsOfDay(code, asOfDay);
        yearSnapshotMapper.deleteByCodeAndAsOfDay(code, asOfDay);

        insertBatched(weekSnapshotMapper,
                KlineSnapshotBuilder.buildPeriodRows(dayBars, asOfDay, PeriodTypeEnum.WEEK));
        insertBatched(monthSnapshotMapper,
                KlineSnapshotBuilder.buildPeriodRows(dayBars, asOfDay, PeriodTypeEnum.MONTH));
        insertBatched(yearSnapshotMapper,
                KlineSnapshotBuilder.buildPeriodRows(dayBars, asOfDay, PeriodTypeEnum.YEAR));
    }

    @Override
    public int buildPeriodSnapshotsAll(String asOfDay) {
        List<String> codes = backtestDaykMapper.selectDistinctCodes();
        if (codes == null || codes.isEmpty()) {
            return 0;
        }
        int done = 0;
        for (String code : codes) {
            try {
                buildPeriodSnapshots(code, asOfDay);
                done++;
            } catch (Exception e) {
                log.warn("period snapshot failed code={} asOfDay={}", code, asOfDay, e);
            }
        }
        log.info("period snapshot built asOfDay={} stocks={}/{}", asOfDay, done, codes.size());
        return done;
    }

    @Override
    public int buildPeriodSnapshotsRange(String fromDay, String toDay) {
        if (!building.compareAndSet(false, true)) {
            log.warn("snapshot build already running");
            return 0;
        }
        long start = System.currentTimeMillis();
        int totalStocks = 0;
        try {
            List<String> tradingDays = backtestDaykMapper.selectDistinctTradingDays(fromDay, toDay);
            if (tradingDays == null || tradingDays.isEmpty()) {
                lastBuildResult = resultMap(fromDay, toDay, 0, 0, start);
                return 0;
            }
            for (String asOfDay : tradingDays) {
                totalStocks += buildPeriodSnapshotsAll(asOfDay);
            }
            lastBuildResult = resultMap(fromDay, toDay, tradingDays.size(), totalStocks, start);
            log.info("period snapshot range done days={} stockRuns={} cost={}s",
                    tradingDays.size(), totalStocks, (System.currentTimeMillis() - start) / 1000);
            return totalStocks;
        } finally {
            building.set(false);
        }
    }

    @Override
    public List<KlineSnapshotBar> loadWeekSnapshot(String code, String asOfDay) {
        return weekSnapshotMapper.selectByCodeAndAsOfDay(code, asOfDay);
    }

    @Override
    public List<KlineSnapshotBar> loadMonthSnapshot(String code, String asOfDay) {
        return monthSnapshotMapper.selectByCodeAndAsOfDay(code, asOfDay);
    }

    @Override
    public List<KlineSnapshotBar> loadYearSnapshot(String code, String asOfDay) {
        return yearSnapshotMapper.selectByCodeAndAsOfDay(code, asOfDay);
    }

    private Map<String, Object> resultMap(String from, String to, int days, int stockRuns, long start) {
        Map<String, Object> m = new HashMap<>();
        m.put("fromDay", from);
        m.put("toDay", to);
        m.put("tradingDays", days);
        m.put("stockRuns", stockRuns);
        m.put("elapsedMs", System.currentTimeMillis() - start);
        return m;
    }

    private void insertBatched(WeekSnapshotMapper mapper, List<KlineSnapshotBar> rows) {
        insertChunks(rows, mapper::insertBatch);
    }

    private void insertBatched(MonthSnapshotMapper mapper, List<KlineSnapshotBar> rows) {
        insertChunks(rows, mapper::insertBatch);
    }

    private void insertBatched(YearSnapshotMapper mapper, List<KlineSnapshotBar> rows) {
        insertChunks(rows, mapper::insertBatch);
    }

    private void insertChunks(List<KlineSnapshotBar> rows,
                              java.util.function.Function<List<KlineSnapshotBar>, Integer> inserter) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (int i = 0; i < rows.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, rows.size());
            inserter.apply(rows.subList(i, end));
        }
    }
}
