package com.yh.bigdata.tts.spider.auth;

import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WxQrLoginStore {

    private static final long TTL_MS = 5 * 60 * 1000L;

    private final Map<String, WxQrLoginSession> sessions = new ConcurrentHashMap<>();

    public WxQrLoginSession create() {
        purgeExpired();
        WxQrLoginSession session = new WxQrLoginSession();
        session.setState(UUID.randomUUID().toString().replace("-", ""));
        session.setStatus(WxQrLoginSession.Status.PENDING);
        session.setExpireAt(System.currentTimeMillis() + TTL_MS);
        sessions.put(session.getState(), session);
        return session;
    }

    public WxQrLoginSession get(String state) {
        if (state == null) {
            return null;
        }
        WxQrLoginSession session = sessions.get(state);
        if (session == null) {
            return null;
        }
        if (session.getStatus() == WxQrLoginSession.Status.PENDING
                && session.getExpireAt() < System.currentTimeMillis()) {
            session.setStatus(WxQrLoginSession.Status.EXPIRED);
        }
        return session;
    }

    public void markSuccess(String state, String token, String openid, String nickname) {
        WxQrLoginSession session = get(state);
        if (session == null) {
            return;
        }
        session.setStatus(WxQrLoginSession.Status.SUCCESS);
        session.setToken(token);
        session.setOpenid(openid);
        session.setNickname(nickname);
    }

    private void purgeExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, WxQrLoginSession>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, WxQrLoginSession> entry = it.next();
            WxQrLoginSession s = entry.getValue();
            if (s.getExpireAt() < now && s.getStatus() != WxQrLoginSession.Status.SUCCESS) {
                it.remove();
            }
        }
    }
}
