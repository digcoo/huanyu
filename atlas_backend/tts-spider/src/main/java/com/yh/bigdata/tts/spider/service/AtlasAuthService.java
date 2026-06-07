package com.yh.bigdata.tts.spider.service;

import com.yh.bigdata.tts.common.model.AtlasUser;

import java.util.Map;

public interface AtlasAuthService {

    Map<String, Object> wxLogin(String code);
}
