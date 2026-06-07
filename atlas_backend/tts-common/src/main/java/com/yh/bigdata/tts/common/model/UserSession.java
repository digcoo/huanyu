package com.yh.bigdata.tts.common.model;

import lombok.Data;

import java.util.Date;

@Data
public class UserSession {

    private String token;

    private String openid;

    private Date expireTime;
}
