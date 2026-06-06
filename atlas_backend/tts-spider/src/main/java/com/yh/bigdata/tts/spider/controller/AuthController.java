package com.yh.bigdata.tts.spider.controller;

import com.yh.bigdata.tts.common.param.base.Response;
import com.yh.bigdata.tts.common.param.base.ResponseUtil;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/wx/login")
    public Response<Map<String, Object>> wxLogin(@RequestBody WxLoginRequest request) {
        Map<String, Object> user = new HashMap<>();
        user.put("nickname", "Atlas用户");

        Map<String, Object> data = new HashMap<>();
        data.put("token", "dev-" + UUID.randomUUID().toString().replace("-", ""));
        data.put("openid", "dev-openid-" + (request.getCode() == null ? "mock"
                : request.getCode().substring(0, Math.min(8, request.getCode().length()))));
        data.put("user", user);
        return ResponseUtil.success(data);
    }

    @Data
    public static class WxLoginRequest {
        private String code;
    }
}
