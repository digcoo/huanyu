package com.yh.bigdata.tts.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 历史 K 线时点快照行（日/周/月/年分表存储）。
 * asOfDay = 快照截止日；barDay = K 线锚点日；day 字段映射 bar_day 供策略复用 Trade 序列。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KlineSnapshotBar extends Trade {

    /** 快照截止交易日 */
    private String asOfDay;

    /** K 线锚点日（日K=交易日，周K=周五，月K=月末，年K=年末） */
    private String barDay;

    /** 1=由日K派生的进行中 bar */
    private Boolean inProgress;

    /** 供策略引擎读取：bar 日期 */
    @Override
    public String getDay() {
        return barDay != null ? barDay : super.getDay();
    }

    public void setDay(String day) {
        this.barDay = day;
        super.setDay(day);
    }

    public static KlineSnapshotBar fromTrade(Trade trade, String asOfDay, String barDay, boolean inProgress) {
        KlineSnapshotBar row = new KlineSnapshotBar();
        row.setAsOfDay(asOfDay);
        row.setBarDay(barDay);
        row.setInProgress(inProgress);
        row.setCode(trade.getCode());
        row.setName(trade.getName());
        row.setOpen(trade.getOpen());
        row.setHigh(trade.getHigh());
        row.setLow(trade.getLow());
        row.setClose(trade.getClose());
        row.setPrevClose(trade.getPrevClose());
        row.setVolume(trade.getVolume());
        row.setAmount(trade.getAmount());
        row.setPercent(trade.getPercent());
        row.setDay(barDay);
        return row;
    }
}
