package com.yh.bigdata.tts.spider.service.impl;

import com.yh.bigdata.tts.common.dao.AtlasUserMapper;
import com.yh.bigdata.tts.common.dao.UserSessionMapper;
import com.yh.bigdata.tts.common.model.AtlasUser;
import com.yh.bigdata.tts.common.model.UserSession;
import com.yh.bigdata.tts.spider.auth.WxOAuthClient;
import com.yh.bigdata.tts.spider.auth.WxQrLoginSession;
import com.yh.bigdata.tts.spider.auth.WxQrLoginStore;
import com.yh.bigdata.tts.spider.service.AtlasAuthService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AtlasAuthServiceImpl implements AtlasAuthService {

    private static final Logger log = LoggerFactory.getLogger(AtlasAuthServiceImpl.class);
    private static final int SESSION_DAYS = 30;

    @Autowired
    private AtlasUserMapper atlasUserMapper;

    @Autowired
    private UserSessionMapper userSessionMapper;

    @Autowired
    private WxQrLoginStore wxQrLoginStore;

    @Autowired
    private WxOAuthClient wxOAuthClient;

    @Value("${atlas.auth.wx.dev-mode:true}")
    private boolean wxDevMode;

    @Override
    public Map<String, Object> wxLogin(String code) {
        String openid = resolveOpenidFromMiniProgramCode(code);
        return issueSession(openid, "Atlas用户");
    }

    @Override
    public Map<String, Object> createWxQrSession() {
        WxQrLoginSession session = wxQrLoginStore.create();
        boolean devMode = !wxOAuthClient.isConfigured() || wxDevMode;

        Map<String, Object> data = new HashMap<>();
        data.put("state", session.getState());
        data.put("devMode", devMode);
        data.put("expiresIn", 300);
        if (devMode) {
            data.put("qrUrl", null);
            data.put("qrContent", "atlas-dev-login:" + session.getState());
        } else {
            String qrUrl = wxOAuthClient.buildQrConnectUrl(session.getState());
            data.put("qrUrl", qrUrl);
            data.put("qrContent", qrUrl);
        }
        return data;
    }

    @Override
    public Map<String, Object> pollWxQrSession(String state) {
        WxQrLoginSession session = wxQrLoginStore.get(state);
        Map<String, Object> data = new HashMap<>();
        if (session == null) {
            data.put("status", "expired");
            return data;
        }
        if (session.getStatus() == WxQrLoginSession.Status.EXPIRED) {
            data.put("status", "expired");
            return data;
        }
        if (session.getStatus() == WxQrLoginSession.Status.SUCCESS) {
            data.put("status", "ok");
            data.put("token", session.getToken());
            data.put("openid", session.getOpenid());
            Map<String, Object> user = new HashMap<>();
            user.put("nickname", session.getNickname());
            data.put("user", user);
            return data;
        }
        data.put("status", "pending");
        return data;
    }

    @Override
    public Map<String, Object> devConfirmWxQrSession(String state) {
        if (!wxDevMode && wxOAuthClient.isConfigured()) {
            throw new IllegalStateException("dev confirm disabled");
        }
        WxQrLoginSession session = wxQrLoginStore.get(state);
        if (session == null || session.getStatus() != WxQrLoginSession.Status.PENDING) {
            throw new IllegalArgumentException("invalid or expired state");
        }
        String openid = "web-dev-openid-" + state.substring(0, Math.min(8, state.length()));
        Map<String, Object> issued = issueSession(openid, "Atlas Web用户");
        wxQrLoginStore.markSuccess(state,
                (String) issued.get("token"),
                (String) issued.get("openid"),
                "Atlas Web用户");
        return pollWxQrSession(state);
    }

    @Override
    public Map<String, Object> completeWxWebLogin(String code, String state) {
        if (StringUtils.isBlank(state)) {
            throw new IllegalArgumentException("missing state");
        }
        WxQrLoginSession session = wxQrLoginStore.get(state);
        if (session == null || session.getStatus() != WxQrLoginSession.Status.PENDING) {
            throw new IllegalArgumentException("invalid or expired state");
        }
        try {
            String openid = wxOAuthClient.exchangeCodeForOpenid(code);
            Map<String, Object> issued = issueSession(openid, "微信用户");
            wxQrLoginStore.markSuccess(state,
                    (String) issued.get("token"),
                    (String) issued.get("openid"),
                    "微信用户");
            return pollWxQrSession(state);
        } catch (Exception e) {
            log.error("wx web login failed, state={}", state, e);
            throw new IllegalStateException("wx oauth failed: " + e.getMessage());
        }
    }

    private String resolveOpenidFromMiniProgramCode(String code) {
        return "dev-openid-" + (StringUtils.isBlank(code) ? "mock"
                : code.substring(0, Math.min(8, code.length())));
    }

    private Map<String, Object> issueSession(String openid, String nickname) {
        String token = "dev-" + UUID.randomUUID().toString().replace("-", "");

        AtlasUser user = new AtlasUser();
        user.setOpenid(openid);
        user.setNickname(nickname);
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
