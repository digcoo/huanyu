package com.yh.bigdata.tts.spider.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class WxOAuthClient {

    private static final Logger log = LoggerFactory.getLogger(WxOAuthClient.class);

    @Value("${atlas.auth.wx.app-id:}")
    private String appId;

    @Value("${atlas.auth.wx.app-secret:}")
    private String appSecret;

    @Value("${atlas.auth.wx.redirect-uri:}")
    private String redirectUri;

    public boolean isConfigured() {
        return StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(appSecret)
                && StringUtils.isNotBlank(redirectUri);
    }

    public String buildQrConnectUrl(String state) {
        String encoded = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return "https://open.weixin.qq.com/connect/qrconnect?appid=" + appId
                + "&redirect_uri=" + encoded
                + "&response_type=code&scope=snsapi_login&state=" + state
                + "#wechat_redirect";
    }

    /**
     * 网站应用扫码登录：用 code 换取 openid
     */
    public String exchangeCodeForOpenid(String code) throws IOException {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId
                + "&secret=" + appSecret
                + "&code=" + code
                + "&grant_type=authorization_code";
        String body = Request.Get(url).execute().returnContent().asString(StandardCharsets.UTF_8);
        JSONObject json = JSON.parseObject(body);
        if (json == null) {
            throw new IOException("empty wx oauth response");
        }
        if (json.containsKey("errcode") && json.getIntValue("errcode") != 0) {
            throw new IOException("wx oauth error: " + json.getString("errmsg"));
        }
        String openid = json.getString("openid");
        if (StringUtils.isBlank(openid)) {
            throw new IOException("wx oauth missing openid");
        }
        return openid;
    }
}
