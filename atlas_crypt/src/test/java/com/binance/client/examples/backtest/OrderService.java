package com.binance.client.examples.backtest;


import com.binance.client.futures.req.DelegateOrderParam;
import lombok.extern.slf4j.Slf4j;


/**
 * 下单服务
 *
 */
@Slf4j
public class OrderService {

    public boolean newOrder(DelegateOrderParam orderParam) {
        return true;
    }

    public boolean batchCancelOrder(DelegateOrderParam orderParam) {
        return true;
    }

}
