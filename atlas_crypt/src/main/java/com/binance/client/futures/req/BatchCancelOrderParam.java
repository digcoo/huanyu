package com.binance.client.futures.req;

import com.binance.client.model.enums.OrderSide;
import com.binance.client.model.enums.OrderType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.enums.TimeInForce;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;


@Data
public class BatchCancelOrderParam {
    String symbol;

    public LinkedHashMap<String, Object> toMap() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        return parameters;
    }

}
