package com.yh.bigdata.tts.common.indicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.DateUtil;
import org.apache.http.client.fluent.Request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.yh.bigdata.tts.common.constants.Constants;

public class XueQiuUtils {

    public static List<Trade> getXueQiuJson(String code, String period) throws IOException {
    	
    	String param = "&symbol=" + code.toUpperCase() + "&period=" + period + "&begin=" + System.currentTimeMillis();
    	String url = "https://stock.xueqiu.com/v5/stock/chart/kline.json?type=before&count=-100&indicator=kline,pe,pb,ps,pcf,market_capital,agt,ggt,balance,ma,macd" + param;
        String jsonStr = Request.Get(url)
                .addHeader("Cookie", Constants.COOKIE_XUEQIU)
                .execute().returnContent().asString();

        JSONArray jsonArray = JSON.parseObject(jsonStr).getJSONObject("data").getJSONArray("item");
        List<Trade> trades = new ArrayList<>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            trades.add(parseStockTrade(jsonArray.getJSONArray(i)));
        }

        return trades;
    }


    private static Trade parseStockTrade(JSONArray itemObject) {
        Trade trade = new Trade();

        Long dateTime = itemObject.getLong(0);
        Long volume = itemObject.getLong(1);
        double open = itemObject.getDouble(2);
        double high = itemObject.getDouble(3);
        double low = itemObject.getDouble(4);
        double close = itemObject.getDouble(5);
        //变化值
        double change = itemObject.getDouble(6);
        //变化率
        double percent = itemObject.getDouble(7);
        //换手率
        double turnoverRate = itemObject.getDouble(8);
        //成交额
        double amount = itemObject.getDouble(9);

        //MA
        double ma5 = itemObject.getDoubleValue(12);
        double ma10 = itemObject.getDoubleValue(13);
        double ma20 = itemObject.getDoubleValue(14);
        double ma30 = itemObject.getDoubleValue(15);

        //macd
        double dea = itemObject.getDoubleValue(16);
        double dif = itemObject.getDoubleValue(17);
        double macd = itemObject.getDoubleValue(18);

//        trade.setCode(stockBase.getCode());
//        trade.setName(stockBase.getName());
        trade.setDay(DateUtil.parseTime(dateTime));

        trade.setOpen(open);
        trade.setHigh(high);
        trade.setLow(low);
        trade.setClose(close);
        trade.setLastTrade(close - change);
        trade.setVolume(volume);
        trade.setAmount(amount);
        trade.setTurnoverRate(turnoverRate);
        trade.setPercent(percent);
        trade.setMa5(ma5);
        trade.setMa10(ma10);
        trade.setMa20(ma20);
        trade.setMa30(ma30);
        trade.setDea(dea);
        trade.setDif(dif);
        trade.setMacd(macd);

        return trade;
    }

}
