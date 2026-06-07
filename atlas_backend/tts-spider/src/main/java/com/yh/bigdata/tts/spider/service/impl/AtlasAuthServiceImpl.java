package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.dao.AtlasUserMapper;
import com.yh.bigdata.tts.common.dao.UserSessionMapper;
import com.yh.bigdata.tts.common.model.AtlasUser;
import com.yh.bigdata.tts.common.model.UserSession;
import com.yh.bigdata.tts.spider.service.AtlasAuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AtlasAuthServiceImpl implements AtlasAuthService {

    private static final int SESSION_DAYS = 30;

    @Autowired
    private AtlasUserMapper atlasUserMapper;

    @Autowired
    private UserSessionMapper userSessionMapper;

    @Override
    public Map<String, Object> wxLogin(String code) {
        String openid = "dev-openid-" + (StringUtils.isBlank(code) ? "mock"
                : code.substring(0, Math.min(8, code.length())));
        String token = "dev-" + UUID.randomUUID().toString().replace("-", "");

        AtlasUser user = new AtlasUser();
        user.setOpenid(openid);
        user.setNickname("Atlas用户");
        atlasUserMapper.upsert(user);

        UserSession session = new UserSession();
        session.setToken(token);
        session.setOpenid(openid);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, SESSION_DAYS);
        session.setExpireTime(cal.getTime());
        userSessionMapper.insert(session);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("nickname", user.getNickname());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("openid", openid);
        data.put("user", userMap);
        return data;
    }
}
