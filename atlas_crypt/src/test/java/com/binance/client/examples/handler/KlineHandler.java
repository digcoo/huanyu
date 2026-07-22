package com.binance.client.examples.handler;

import com.alibaba.fastjson.JSON;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.examples.KLine;
import com.binance.client.examples.constants.SymbolCacheData;
import com.binance.client.examples.backup.DFBundleDTO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


public class KlineHandler implements HttpHandler {

    Logger log = LoggerFactory.getLogger(this.getClass().getClass());

    SymbolCacheData symbolData = null;

    public KlineHandler(SymbolCacheData symbolData) {
        this.symbolData = symbolData;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("KlinesHandler process start.............");
        String response = "";
        try {

            log.info("klines size = {}", this.symbolData.getSymbols().size());

            List<KLine> kLines = new ArrayList();
            for (String symbol: this.symbolData.getSymbols()) {
                KLine kline = new KLine(symbol
                        , this.symbolData.getData(symbol).getOrDefault(PeriodTypeEnum.MONTH, new ArrayList<>())
                        , this.symbolData.getData(symbol).getOrDefault(PeriodTypeEnum.WEEK, new ArrayList<>())
                        , this.symbolData.getData(symbol).getOrDefault(PeriodTypeEnum.DAY1, new ArrayList<>())
                        , this.symbolData.getData(symbol).getOrDefault(PeriodTypeEnum.HOUR4, new ArrayList<>())
                        , this.symbolData.getData(symbol).getOrDefault(PeriodTypeEnum.MIN30, new ArrayList<>())
                        , null);
                kLines.add(kline);
            }

            Collections.sort(kLines, new Comparator<KLine>() {
                @Override
                public int compare(KLine p1, KLine p2) {
                    return p1.getM30Lines().get(p1.getM30Lines().size() - 1).getQuoteAssetVolume().compareTo(p2.getM30Lines().get(p2.getM30Lines().size() - 1).getQuoteAssetVolume()) > 0? -1 : 1;
                }
            });

            List<DFBundleDTO> targets = new ArrayList<>();

            for (KLine x : kLines) {
                try {
                    targets.add(new DFBundleDTO(x.getSymbol(), x.getMLines(), x.getWLines(), x.getDLines(), x.getH4Lines(), x.getM30Lines(), x.getM5Lines()));
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            targets = targets.subList(0, Math.min(targets.size(), 500));

            response = JSON.toJSONString(targets);

        } catch (Exception ex) {
            Map res = new HashMap();
            res.put("code", "500");
            response = JSON.toJSONString(res);

            ex.printStackTrace();
        }

        // 设置响应头部信息
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // 允许所有来源请求
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS"); // 允许的请求方法

        // 发送响应正文
        exchange.sendResponseHeaders(200, response.length());
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();

    }
}
