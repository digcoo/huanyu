package com.yh.bigdata.tts.spider.controller;

import com.yh.bigdata.tts.common.param.base.Response;
import com.yh.bigdata.tts.common.param.base.ResponseUtil;
import com.yh.bigdata.tts.spider.service.AtlasAuthService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AtlasAuthService atlasAuthService;

    @Value("${atlas.auth.wx.web-front-url:http://localhost:5173}")
    private String webFrontUrl;

    /** 小程序 code 登录 */
    @PostMapping("/wx/login")
    public Response<Map<String, Object>> wxLogin(@RequestBody WxLoginRequest request) {
        return ResponseUtil.success(atlasAuthService.wxLogin(request.getCode()));
    }

    /** Web 扫码：创建会话，返回 state 与二维码内容 */
    @PostMapping("/wx/qr/create")
    public Response<Map<String, Object>> createWxQr() {
        return ResponseUtil.success(atlasAuthService.createWxQrSession());
    }

    /** Web 扫码：轮询登录结果 */
    @GetMapping("/wx/qr/poll")
    public Response<Map<String, Object>> pollWxQr(@RequestParam("state") String state) {
        return ResponseUtil.success(atlasAuthService.pollWxQrSession(state));
    }

    /** 开发环境：模拟用户已扫码确认 */
    @PostMapping("/wx/qr/dev-confirm")
    public Response<Map<String, Object>> devConfirmWxQr(@RequestBody WxQrStateRequest request) {
        return ResponseUtil.success(atlasAuthService.devConfirmWxQrSession(request.getState()));
    }

    /**
     * 微信开放平台 OAuth 回调（网站应用扫码）
     * 配置 redirect-uri 指向此地址，例如 http://your-domain/tts/auth/wx/callback
     */
    @GetMapping("/wx/callback")
    public void wxCallback(@RequestParam(value = "code", required = false) String code,
                           @RequestParam(value = "state", required = false) String state,
                           HttpServletResponse response) throws IOException {
        String base = webFrontUrl.replaceAll("/$", "");
        try {
            if (code == null || state == null) {
                response.sendRedirect(base + "/login?error=missing_code");
                return;
            }
            Map<String, Object> result = atlasAuthService.completeWxWebLogin(code, state);
            String token = result.get("token") != null ? result.get("token").toString() : "";
            String redirect = base + "/login/callback?state=" + URLEncoder.encode(state, "UTF-8")
                    + "&token=" + URLEncoder.encode(token, "UTF-8");
            response.sendRedirect(redirect);
        } catch (Exception e) {
            response.sendRedirect(base + "/login?error=" + URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    @Data
    public static class WxLoginRequest {
        private String code;
    }

    @Data
    public static class WxQrStateRequest {
        private String state;
    }
}
