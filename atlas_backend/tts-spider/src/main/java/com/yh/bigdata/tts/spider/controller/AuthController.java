package com.yh.bigdata.tts.spider.controller;

import com.yh.bigdata.tts.common.param.base.Response;
import com.yh.bigdata.tts.common.param.base.ResponseUtil;
import com.yh.bigdata.tts.spider.service.AtlasAuthService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AtlasAuthService atlasAuthService;

    @PostMapping("/wx/login")
    public Response<Map<String, Object>> wxLogin(@RequestBody WxLoginRequest request) {
        return ResponseUtil.success(atlasAuthService.wxLogin(request.getCode()));
    }

    @Data
    public static class WxLoginRequest {
        private String code;
    }
}
