package com.binance.client.utils;

import com.binance.client.websocket.HttpClientSingleton;
import com.binance.client.websocket.ProxyAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * HTTP/HTTPS 代理：优先 JVM 参数 / 环境变量，可在 BootServer 启动时显式配置。
 */
public final class NetworkProxySupport {

    private static final Logger log = LoggerFactory.getLogger(NetworkProxySupport.class);
    private static final int CONNECT_TIMEOUT_SEC = 30;
    private static final int READ_TIMEOUT_SEC = 60;
    private static final int WRITE_TIMEOUT_SEC = 30;

    private static volatile ProxyAuth proxyAuth;
    private static volatile boolean configured;

    private NetworkProxySupport() {
    }

    public static void configure(String proxyUrl, boolean enabled) {
        if (!enabled || proxyUrl == null || proxyUrl.trim().isEmpty()) {
            proxyAuth = null;
            configured = true;
            HttpClientSingleton.reset();
            log.info("Binance HTTP client: direct connection (no proxy)");
            return;
        }
        proxyAuth = toProxyAuth(parseUrl(proxyUrl.trim()));
        configured = true;
        HttpClientSingleton.reset();
        log.info("Binance HTTP client: proxy enabled -> {}", proxyUrl.trim());
    }

    public static ProxyAuth getProxyAuth() {
        if (!configured) {
            configure(resolveFromEnv(), resolveFromEnv() != null);
        }
        return proxyAuth;
    }

    public static int connectTimeoutSec() {
        return CONNECT_TIMEOUT_SEC;
    }

    public static int readTimeoutSec() {
        return READ_TIMEOUT_SEC;
    }

    public static int writeTimeoutSec() {
        return WRITE_TIMEOUT_SEC;
    }

    private static String resolveFromEnv() {
        String fromProp = firstNonBlank(
                System.getProperty("binance.http.proxy"),
                System.getProperty("http.proxyHost") != null
                        ? "http://" + System.getProperty("http.proxyHost") + ":"
                        + System.getProperty("http.proxyPort", "80")
                        : null
        );
        if (fromProp != null) {
            return fromProp;
        }
        return firstNonBlank(System.getenv("HTTPS_PROXY"), System.getenv("HTTP_PROXY"));
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static URI parseUrl(String proxyUrl) {
        try {
            URI uri = URI.create(proxyUrl.contains("://") ? proxyUrl : "http://" + proxyUrl);
            if (uri.getHost() == null) {
                throw new IllegalArgumentException("invalid proxy host");
            }
            return uri;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid proxy url: " + proxyUrl, ex);
        }
    }

    private static ProxyAuth toProxyAuth(URI uri) {
        String scheme = uri.getScheme() == null ? "http" : uri.getScheme().toLowerCase();
        Proxy.Type type = "socks".equals(scheme) || "socks5".equals(scheme)
                ? Proxy.Type.SOCKS
                : Proxy.Type.HTTP;
        int port = uri.getPort() > 0 ? uri.getPort() : ("https".equals(scheme) ? 443 : 8890);
        Proxy proxy = new Proxy(type, new InetSocketAddress(uri.getHost(), port));
        return new ProxyAuth(proxy, null);
    }
}
