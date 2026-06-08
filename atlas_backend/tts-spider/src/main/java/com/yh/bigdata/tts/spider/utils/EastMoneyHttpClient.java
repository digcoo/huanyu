package com.yh.bigdata.tts.spider.utils;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 东方财富 HTTP 请求（带超时）
 */
public final class EastMoneyHttpClient {

    private static final Logger log = LoggerFactory.getLogger(EastMoneyHttpClient.class);

    private static final int TIMEOUT_MS = 15000;

    private EastMoneyHttpClient() {
    }

    public static String get(String url) {
        try {
            return Request.Get(url)
                    .connectTimeout(TIMEOUT_MS)
                    .socketTimeout(TIMEOUT_MS)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .addHeader("Referer", "https://quote.eastmoney.com/")
                    .addHeader("Accept", "*/*")
                    .execute()
                    .returnContent()
                    .asString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("eastmoney http error, url={}", url, e);
            return null;
        }
    }
}
