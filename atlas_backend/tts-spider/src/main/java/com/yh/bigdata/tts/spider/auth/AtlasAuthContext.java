package com.yh.bigdata.tts.spider.auth;

public final class AtlasAuthContext {

    private static final ThreadLocal<String> OPENID = new ThreadLocal<>();

    private AtlasAuthContext() {
    }

    public static void setOpenid(String openid) {
        OPENID.set(openid);
    }

    public static String getOpenid() {
        return OPENID.get();
    }

    public static void clear() {
        OPENID.remove();
    }
}
