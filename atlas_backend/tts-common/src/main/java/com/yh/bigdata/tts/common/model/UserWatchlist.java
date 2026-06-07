package com.yh.bigdata.tts.common.model;

import lombok.Data;

@Data
public class UserWatchlist {

    private Long id;

    private String openid;

    private String stockId;

    private String code;

    private String name;

    private String market;

    private String strategy;

    private Double entryPrice;

    private String resonance;

    private String summary;

    private String tagsJson;

    private Long addedAt;
}
