package com.yh.bigdata.tts.common.model;

import lombok.Data;

@Data
public class UserWatchHistory {

    private Long id;

    private String openid;

    private String recordId;

    private String stockId;

    private String code;

    private String name;

    private String market;

    private String strategy;

    private String resonance;

    private String tagsJson;

    private Double entryPrice;

    private Double exitPrice;

    private Long addedAt;

    private Long removedAt;

    private Integer holdDays;

    private Double pnlPct;

    private String removeReason;
}
