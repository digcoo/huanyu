package com.binance.client.utils;


import com.alibaba.fastjson.JSON;
import org.apache.http.entity.ContentType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class MessageSenderUtil {
    final static String url_stub = "http://wxpusher.zjiecode.com/api/send/message";
    final static String APP_TOKEN = "AT_WAqUI14umKb0DhVKMTvm9h9y0Di9ysAL";
    final static String UID = "UID_bU0BVWqoo2LMKH6gw5MeS0X0o7eA";
    static HttpClient httpClient = null;

    static {
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2) // 使用 HTTP/2
                .connectTimeout(Duration.ofSeconds(10)) // 设置连接超时
                .build();
    }
    public static void sendAsync(String symbol) throws IOException, InterruptedException {
        Map data = new HashMap();
        data.put("appToken", APP_TOKEN);
        data.put("uids", Arrays.asList(UID));
        data.put("content", symbol);
        data.put("contentType", 1);
        data.put("summary", symbol);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url_stub))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(data)))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void main(String [] args) throws IOException, InterruptedException {
        MessageSenderUtil.sendAsync("BTCUSDT开多");
    }
}
