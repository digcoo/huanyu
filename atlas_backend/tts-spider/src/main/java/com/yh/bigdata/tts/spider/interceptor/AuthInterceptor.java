package com.yh.bigdata.tts.spider.interceptor;

import com.yh.bigdata.tts.common.dao.UserSessionMapper;
import com.yh.bigdata.tts.common.model.UserSession;
import com.yh.bigdata.tts.spider.auth.AtlasAuthContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserSessionMapper userSessionMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (StringUtils.isBlank(auth) || !auth.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        String token = auth.substring("Bearer ".length()).trim();
        UserSession session = userSessionMapper.selectByToken(token);
        if (session == null || StringUtils.isBlank(session.getOpenid())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        if (session.getExpireTime() != null && session.getExpireTime().before(new Date())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        AtlasAuthContext.setOpenid(session.getOpenid());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        AtlasAuthContext.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        AtlasAuthContext.clear();
    }
}
