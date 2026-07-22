package com.binance.client.futures;

import com.alibaba.fastjson.JSONArray;
import com.binance.client.constant.PrivateConfig;
import com.binance.client.futures.exceptions.BinanceClientException;
import com.binance.client.futures.exceptions.BinanceConnectorException;
import com.binance.client.futures.impl.UMFuturesClientImpl;
import com.binance.client.futures.req.DelegateOrderParam;
import com.binance.client.model.enums.OrderSide;
import com.binance.client.model.enums.OrderType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.enums.TimeInForce;
import com.binance.client.model.trade.Order;
import com.binance.client.model.trade.Position;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class BinanceServiceClient {

    static UMFuturesClientImpl binanceClient = new UMFuturesClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY, PrivateConfig.UM_BASE_URL);

    private static final Logger logger = LoggerFactory.getLogger(BinanceServiceClient.class);

    public boolean isHasOrder(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        String result = binanceClient.account().currentAllOpenOrders(parameters);
        List<Order> orders = JSONArray.parseArray(result, Order.class);

        return CollectionUtils.isNotEmpty(orders);
    }

    public boolean isHasPosition(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        String result = binanceClient.account().positionInformation(parameters);
        List<Position> positions = JSONArray.parseArray(result, Position.class)
                .stream()
                .filter(x -> x.getEntryPrice().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        return CollectionUtils.isNotEmpty(positions);
    }


    public boolean newOrder(DelegateOrderParam orderParam) {
        String symbol = orderParam.getSymbol();
        //该币对有委托，则不下单
        if (isHasOrder(symbol)) {
            return false;
        }
        //该币对有仓位，则不下单
        if (isHasPosition(symbol)) {
            return false;
        }

        try {

            //下单
            String result = binanceClient.account().newOrder(orderParam.toMap());
            logger.info("newOrder: {}", result);

            //设置止盈止损


        } catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }

        return true;
    }


    public boolean closeOrder(String symbol, String side, BigDecimal price, BigDecimal quantity) {

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

        UMFuturesClientImpl client = new UMFuturesClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY, PrivateConfig.UM_BASE_URL);


        parameters.put("symbol", symbol);
        parameters.put("side", "SELL");
        parameters.put("type", "LIMIT");
        parameters.put("timeInForce", "GTC");
//        parameters.put("positionSide", "LONG");
        parameters.put("positionSide", side);
        parameters.put("quantity", quantity);
        parameters.put("price", price);

        try {

            String result = client.account().newOrder(parameters);
            logger.info(result);
        } catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }

        return true;
    }

    public static void main(String [] args) {
        BinanceServiceClient client = new BinanceServiceClient();
        log.info("GUNUSDT hasOrder: {}", client.isHasOrder("GUNUSDT"));
        log.info("GUNUSDT hasPosition: {}", client.isHasPosition("GUNUSDT"));

    }
}
