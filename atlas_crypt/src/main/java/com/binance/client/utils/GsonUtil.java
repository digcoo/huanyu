package com.binance.client.utils;

import com.google.gson.*;

import java.math.BigDecimal;

public class GsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .registerTypeAdapter(
                    BigDecimal.class,
                    (JsonSerializer<BigDecimal>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toPlainString()))
            .create();

    public static Gson gson() {
        return GSON;
    }

}
