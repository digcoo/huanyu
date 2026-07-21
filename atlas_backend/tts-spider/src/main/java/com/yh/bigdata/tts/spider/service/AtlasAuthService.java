package com.yh.bigdata.tts.spider.service;

import java.util.Map;

public interface AtlasAuthService {

    Map<String, Object> wxLogin(String code);

    /** 创建 Web 扫码登录会话 */
    Map<String, Object> createWxQrSession();

    /** 轮询扫码状态 */
    Map<String, Object> pollWxQrSession(String state);

    /** 开发环境模拟确认扫码 */
    Map<String, Object> devConfirmWxQrSession(String state);

    /** 微信 OAuth 回调：用 code 完成登录 */
    Map<String, Object> completeWxWebLogin(String code, String state);
}
