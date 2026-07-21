package com.yh.bigdata.tts.spider.auth;

import lombok.Data;

@Data
public class WxQrLoginSession {

    public enum Status {
        PENDING, SUCCESS, EXPIRED
    }

    private String state;
    private Status status;
    private long expireAt;
    private String token;
    private String openid;
    private String nickname;
}
