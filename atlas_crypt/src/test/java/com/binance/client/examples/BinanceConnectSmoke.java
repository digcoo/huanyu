package com.binance.client.examples;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.examples.constants.NetworkConfig;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.market.ExchangeInformation;
import com.binance.client.utils.NetworkProxySupport;

/**
 * 快速验证 Binance REST 是否可达（需代理时先 configure）。
 */
public class BinanceConnectSmoke {

    public static void main(String[] args) {
        NetworkProxySupport.configure(NetworkConfig.HTTP_PROXY, NetworkConfig.USE_HTTP_PROXY);
        SyncRequestClient client = SyncRequestClient.create(
                PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY, new RequestOptions());
        ExchangeInformation info = client.getExchangeInformation();
        System.out.println("OK symbols=" + info.getSymbols().size());
    }
}
