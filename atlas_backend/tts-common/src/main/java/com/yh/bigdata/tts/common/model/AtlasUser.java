package com.yh.bigdata.tts.common.model;

import lombok.Data;

import java.util.Date;

@Data
public class AtlasUser {

    private String openid;

    private String nickname;

    private Date createTime;
}
