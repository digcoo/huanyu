package com.yh.bigdata.tts.spider.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.yh.bigdata.tts.common.model.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.yh.bigdata.tts.common.constants.Constants;
import com.yh.bigdata.tts.common.utils.DateUtil;

public class XueQiuHttpUtils {
	
	private static Logger logger = LoggerFactory.getLogger(XueQiuHttpUtils.class);

//	String base_url = "https://stock.xueqiu.com/v5/stock/chart/kline.json?symbol=SZ000721&begin=1698935762607&period=60m&type=before&count=-8&indicator=kline,pe,pb,ps,pcf,market_capital,agt,ggt,balance,ma,macd";
    public final static String base_url = "https://stock.xueqiu.com/v5/stock/chart/kline.json?symbol=%s&begin=%s&period=%s&type=before&count=%s&indicator=kline,ma,macd";


    public static String getData(String url) throws ClientProtocolException, IOException {
		String result = Request.Get(url)
				.addHeader("Cookie", Constants.COOKIE_XUEQIU)
				.execute().returnContent().asString();
        return result;
	}


    public static <T extends Trade> List<T> parseStockTrades(String json, StockBase stockBase, Class<T> clazz) {

        try {
            List<T> trades = new ArrayList<T>();
            JSONArray jsonArray = JSON.parseObject(json).getJSONObject("data").getJSONArray("item");
            if (!jsonArray.isEmpty()) {

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONArray itemObject = jsonArray.getJSONArray(i);

                    T trade = clazz.getDeclaredConstructor().newInstance();
                    parseStockTrade(itemObject, stockBase, trade);

                    trades.add(trade);

                }
            }

            return trades;

        } catch (Exception e) {
            logger.error("parseStockTrades exception...json_data = " + json, e);
        }

        return null;
    }

    private static void parseStockTrade(JSONArray itemObject, StockBase stockBase, Trade trade) {
        Long dateTime = itemObject.getLong(0);
        Long volume = itemObject.getLong(1);
        Double open = itemObject.getDouble(2);
        Double high = itemObject.getDouble(3);
        Double low = itemObject.getDouble(4);
        Double close = itemObject.getDouble(5);
        //变化值
        Double change = itemObject.getDouble(6);
        //变化率
        Double percent = itemObject.getDouble(7);
        //换手率
        Double turnoverRate = itemObject.getDouble(8);
        //成交额
        Double amount = itemObject.getDouble(9);

        //MA
        Double ma5 = itemObject.getDouble(12);
        Double ma10 = itemObject.getDouble(13);
        Double ma20 = itemObject.getDouble(14);
        Double ma30 = itemObject.getDouble(15);

        //macd
        Double dea = itemObject.getDouble(16);
        Double dif = itemObject.getDouble(17);
        Double macd = itemObject.getDouble(18);

        trade.setCode(stockBase.getCode());
        trade.setName(stockBase.getName());
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
    }


	public static void main(String [] args) throws ClientProtocolException, IOException {
		System.out.println(getData("https://stock.xueqiu.com/v5/stock/chart/kline.json?symbol=SH600009&begin=1700706667493&period=30m&type=before&count=-284&indicator=kline,pe,pb,ps,pcf,market_capital,agt,ggt,balance"));
	}
}
