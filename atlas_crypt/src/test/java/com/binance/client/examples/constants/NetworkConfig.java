package com.binance.client.examples.constants;

/**
 * 本地网络配置。国内访问 Binance 通常需系统代理（如 Clash 8890）。
 */
public final class NetworkConfig {

    private NetworkConfig() {
    }

    /** 是否启用 HTTP 代理 */
    public static final boolean USE_HTTP_PROXY = true;

    /** 代理地址，与 huanyu 仓库 git 代理默认一致 */
    public static final String HTTP_PROXY = "http://127.0.0.1:8890";
}
