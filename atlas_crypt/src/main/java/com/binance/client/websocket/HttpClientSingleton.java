package com.binance.client.websocket;

import com.binance.client.utils.NetworkProxySupport;
import okhttp3.OkHttpClient;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;

public final class HttpClientSingleton {
    private static OkHttpClient httpClient = null;

    private HttpClientSingleton() {
    }

    public static OkHttpClient getHttpClient() {
        return getHttpClient(NetworkProxySupport.getProxyAuth());
    }

    public static OkHttpClient getHttpClient(ProxyAuth proxy) {
        if (httpClient == null) {
            createHttpClient(proxy);
        } else {
            verifyHttpClient(proxy);
        }
        return httpClient;
    }

    public static void reset() {
        httpClient = null;
    }

    private static void createHttpClient(ProxyAuth proxy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(NetworkProxySupport.connectTimeoutSec(), TimeUnit.SECONDS)
                .readTimeout(NetworkProxySupport.readTimeoutSec(), TimeUnit.SECONDS)
                .writeTimeout(NetworkProxySupport.writeTimeoutSec(), TimeUnit.SECONDS);
        if (proxy != null) {
            builder.proxy(proxy.getProxy());
            if (proxy.getAuth() != null) {
                builder.proxyAuthenticator(proxy.getAuth());
            }
        }
        httpClient = builder.build();
    }

    private static void verifyHttpClient(ProxyAuth proxy) {
        Proxy prevProxy = httpClient.proxy();

        if ((proxy != null && !proxy.getProxy().equals(prevProxy)) || (proxy == null && prevProxy != null)) {
            createHttpClient(proxy);
        }
    }
}
