package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.dao.StockMin30Mapper;
import com.yh.bigdata.tts.common.dao.StockMin60Mapper;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockMin30;
import com.yh.bigdata.tts.common.model.StockMin60;
import com.yh.bigdata.tts.common.model.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 策略评估读 K 线：优先内存缓存，Min30/Min60 不足时回退 DB（与 API K 线接口一致）。
 */
@Service
public class KlineLoadService {

    private static KlineLoadService instance;

    @Autowired
    private StockMin30Mapper stockMin30Mapper;

    @Autowired
    private StockMin60Mapper stockMin60Mapper;

    @PostConstruct
    void register() {
        instance = this;
    }

    public static List<Trade> getLastTrades(StockBase stock, PeriodTypeEnum period, int limit) {
        if (stock == null || period == null || limit < 1) {
            return Collections.emptyList();
        }
        List<Trade> cached = RealtimeStockCache.getLastTrades(stock, period, limit);
        if (period != PeriodTypeEnum.MIN30 && period != PeriodTypeEnum.MIN60) {
            return cached;
        }
        if (!CollectionUtils.isEmpty(cached) && cached.size() >= Math.min(limit, 3)) {
            return cached;
        }
        if (instance == null) {
            return cached;
        }
        List<Trade> fromDb = instance.loadRecentFromDb(stock.getCode(), period, limit);
        if (!CollectionUtils.isEmpty(fromDb)) {
            return fromDb;
        }
        return cached;
    }

    private List<Trade> loadRecentFromDb(String code, PeriodTypeEnum period, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        if (period == PeriodTypeEnum.MIN60) {
            List<StockMin60> rows = stockMin60Mapper.selectRecentByCode(code, safeLimit);
            return reverseCopy(rows);
        }
        if (period == PeriodTypeEnum.MIN30) {
            List<StockMin30> rows = stockMin30Mapper.selectRecentByCode(code, safeLimit);
            return reverseCopy(rows);
        }
        return Collections.emptyList();
    }

    private static List<Trade> reverseCopy(List<? extends Trade> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyList();
        }
        List<Trade> trades = new ArrayList<>(rows);
        Collections.reverse(trades);
        return trades;
    }
}
