package com.yh.bigdata.tts.spider.realtime;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.StockMin30;
import com.yh.bigdata.tts.common.model.StockMin60;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.model.StockWeek;
import com.yh.bigdata.tts.common.model.StockYear;
import com.yh.bigdata.tts.common.utils.DateUtil;

/**
 * 用新浪实时价在内存中增量合成/更新最新 min30、min60、日/周/月/年 K。
 */
@Component
public class RealtimeKlineUpdater {

    public void updateAll(StockBase stockBase, String time) {
        updateMin30(stockBase, time);
        updateMin60(stockBase, time);
        updateDay(stockBase);
        updateWeek(stockBase);
        updateMonth(stockBase);
        updateYear(stockBase);
    }

    public void updateMin30(StockBase stockBase, String time) {
        List<StockMin30> bars = RealtimeStockCache.min30Map.get(stockBase.getCode());
        if (CollectionUtils.isEmpty(bars)) {
            return;
        }

        String maTime = DateUtil.getMATime(time, 30);
        String thisTime = stockBase.getDay() + " " + maTime;

        StockMin30 last = bars.get(bars.size() - 1);
        double price = stockBase.getClose() != null ? stockBase.getClose() : 0D;

        if (last.getDay().equals(thisTime)) {
            last.setHigh(Math.max(price, last.getHigh() != null ? last.getHigh() : price));
            last.setLow(Math.min(price, last.getLow() != null ? last.getLow() : price));
            last.setClose(price);
            return;
        }

        StockMin30 bar = new StockMin30();
        if ("10:00:00".equals(maTime)) {
            bar.setOpen(stockBase.getOpen());
            bar.setPrevClose(stockBase.getPrevClose());
        } else {
            bar.setOpen(last.getClose());
            bar.setPrevClose(last.getClose());
        }
        bar.setCode(stockBase.getCode());
        bar.setName(stockBase.getName());
        bar.setHigh(price);
        bar.setLow(price);
        bar.setClose(price);
        bar.setDay(thisTime);
        bar.setPeriodTypeEnum(PeriodTypeEnum.MIN30);
        bar.setVolume(0L);
        copyMa(last, bar);
        bars.add(bar);
        Collections.sort(bars);
    }

    public void updateMin60(StockBase stockBase, String time) {
        List<StockMin60> bars = RealtimeStockCache.min60Map.get(stockBase.getCode());
        if (CollectionUtils.isEmpty(bars)) {
            return;
        }

        String maTime = DateUtil.getMATime(time, 60);
        String thisTime = stockBase.getDay() + " " + maTime;
        double price = stockBase.getClose() != null ? stockBase.getClose() : 0D;

        StockMin60 matched = null;
        int from = Math.max(bars.size() - 5, 0);
        for (int i = from; i < bars.size(); i++) {
            if (thisTime.equals(bars.get(i).getDay())) {
                matched = bars.get(i);
                break;
            }
        }

        if (matched != null) {
            matched.setHigh(Math.max(price, matched.getHigh() != null ? matched.getHigh() : price));
            matched.setLow(Math.min(price, matched.getLow() != null ? matched.getLow() : price));
            matched.setClose(price);
            return;
        }

        StockMin60 last = bars.get(bars.size() - 1);
        StockMin60 bar = new StockMin60();
        if ("10:30:00".equals(maTime)) {
            bar.setOpen(stockBase.getOpen());
            bar.setPrevClose(stockBase.getPrevClose());
        } else {
            bar.setOpen(last.getClose());
            bar.setPrevClose(last.getClose());
        }
        bar.setCode(stockBase.getCode());
        bar.setName(stockBase.getName());
        bar.setHigh(price);
        bar.setLow(price);
        bar.setClose(price);
        bar.setDay(thisTime);
        bar.setPeriodTypeEnum(PeriodTypeEnum.MIN60);
        bar.setVolume(0L);
        copyMa(last, bar);
        bars.add(bar);
        Collections.sort(bars);
    }

    public void updateDay(StockBase stockBase) {
        List<StockDay> bars = RealtimeStockCache.dayMap.get(stockBase.getCode());
        if (CollectionUtils.isEmpty(bars)) {
            return;
        }

        String thisDay = DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd");
        StockDay last = bars.get(bars.size() - 1);

        if (last.getDay().equals(thisDay)) {
            last.setClose(stockBase.getClose());
            if (stockBase.getHigh() != null) {
                last.setHigh(Math.max(stockBase.getHigh(), last.getHigh() != null ? last.getHigh() : stockBase.getHigh()));
            }
            if (stockBase.getLow() != null) {
                last.setLow(Math.min(stockBase.getLow(), last.getLow() != null ? last.getLow() : stockBase.getLow()));
            }
            last.setVolume(stockBase.getVolume());
            last.setAmount(stockBase.getAmount());
            return;
        }

        StockDay bar = new StockDay();
        bar.setCode(stockBase.getCode());
        bar.setName(stockBase.getName());
        bar.setClose(stockBase.getClose());
        bar.setOpen(stockBase.getOpen());
        bar.setHigh(stockBase.getHigh());
        bar.setLow(stockBase.getLow());
        bar.setPrevClose(stockBase.getPrevClose());
        bar.setVolume(stockBase.getVolume());
        bar.setAmount(stockBase.getAmount());
        copyMa(last, bar);
        bar.setPeriodTypeEnum(PeriodTypeEnum.DAY);
        bar.setDay(thisDay);
        bars.add(bar);
    }

    public void updateWeek(StockBase stockBase) {
        List<StockWeek> bars = RealtimeStockCache.weekMap.get(stockBase.getCode());
        if (CollectionUtils.isEmpty(bars)) {
            return;
        }

        String thisWeek = DateUtil.getTodayWeek();
        StockWeek last = bars.get(bars.size() - 1);

        if (last.getDay().equals(thisWeek)) {
            last.setClose(stockBase.getClose());
            if (stockBase.getHigh() != null) {
                last.setHigh(Math.max(stockBase.getHigh(), last.getHigh() != null ? last.getHigh() : stockBase.getHigh()));
            }
            if (stockBase.getLow() != null) {
                last.setLow(Math.min(stockBase.getLow(), last.getLow() != null ? last.getLow() : stockBase.getLow()));
            }
            return;
        }

        StockWeek bar = newStockWeek(stockBase, last, thisWeek);
        bars.add(bar);
    }

    public void updateMonth(StockBase stockBase) {
        List<StockMonth> bars = RealtimeStockCache.monthMap.get(stockBase.getCode());
        if (CollectionUtils.isEmpty(bars)) {
            return;
        }

        String thisMonth = DateUtil.getMonthLastDay();
        StockMonth last = bars.get(bars.size() - 1);

        if (last.getDay().equals(thisMonth)) {
            last.setClose(stockBase.getClose());
            if (stockBase.getHigh() != null) {
                last.setHigh(Math.max(stockBase.getHigh(), last.getHigh() != null ? last.getHigh() : stockBase.getHigh()));
            }
            if (stockBase.getLow() != null) {
                last.setLow(Math.min(stockBase.getLow(), last.getLow() != null ? last.getLow() : stockBase.getLow()));
            }
            return;
        }

        StockMonth bar = new StockMonth();
        fillBar(stockBase, last, bar, thisMonth, PeriodTypeEnum.MONTH);
        bars.add(bar);
    }

    public void updateYear(StockBase stockBase) {
        List<StockYear> bars = RealtimeStockCache.yearMap.get(stockBase.getCode());
        if (CollectionUtils.isEmpty(bars)) {
            return;
        }

        String thisYear = DateUtil.getYearLastDay();
        StockYear last = bars.get(bars.size() - 1);

        if (last.getDay().equals(thisYear)) {
            last.setClose(stockBase.getClose());
            if (stockBase.getHigh() != null) {
                last.setHigh(Math.max(stockBase.getHigh(), last.getHigh() != null ? last.getHigh() : stockBase.getHigh()));
            }
            if (stockBase.getLow() != null) {
                last.setLow(Math.min(stockBase.getLow(), last.getLow() != null ? last.getLow() : stockBase.getLow()));
            }
            return;
        }

        StockYear bar = new StockYear();
        fillBar(stockBase, last, bar, thisYear, PeriodTypeEnum.YEAR);
        bars.add(bar);
    }

    private static StockWeek newStockWeek(StockBase stockBase, StockWeek last, String day) {
        StockWeek bar = new StockWeek();
        fillBar(stockBase, last, bar, day, PeriodTypeEnum.WEEK);
        return bar;
    }

    private static void fillBar(StockBase stockBase, StockBase last, StockBase bar, String day,
                                PeriodTypeEnum period) {
        bar.setCode(stockBase.getCode());
        bar.setName(stockBase.getName());
        bar.setClose(stockBase.getClose());
        bar.setOpen(stockBase.getOpen());
        bar.setHigh(stockBase.getHigh());
        bar.setLow(stockBase.getLow());
        bar.setPrevClose(stockBase.getPrevClose());
        bar.setVolume(stockBase.getVolume());
        bar.setAmount(stockBase.getAmount());
        copyMa(last, bar);
        bar.setPeriodTypeEnum(period);
        bar.setDay(day);
    }

    private static void copyMa(StockBase from, StockBase to) {
        to.setMa5(from.getMa5());
        to.setMa10(from.getMa10());
        to.setMa20(from.getMa20());
        to.setMa30(from.getMa30());
        to.setMa60(from.getMa60());
    }
}
