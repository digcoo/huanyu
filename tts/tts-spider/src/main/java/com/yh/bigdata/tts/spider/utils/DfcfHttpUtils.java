package com.yh.bigdata.tts.spider.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockCapital;
import com.yh.bigdata.tts.common.model.StockTop;
import com.yh.bigdata.tts.common.utils.DateUtil;

public class DfcfHttpUtils {
	private static Logger logger = LoggerFactory.getLogger(DfcfHttpUtils.class);
	
	
	public static List<StockCapital> parseStockCapitalHis(StockBase stockBase, String json) {
		
		try {
			
			List<StockCapital> stockCapitals = Lists.newArrayList();
			
			
			JSONObject jsonObject = JSON.parseObject(json);
			JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("klines");
			for (int i = 0; i < jsonArray.size(); i++) {
				
				StockCapital stockCapital = new StockCapital();
				
				String[] split = jsonArray.getString(i).split(",");
				String day = split[0];
				Double zhuli = Double.parseDouble(split[1]);
				Double small = Double.parseDouble(split[2]);
				Double middle = Double.parseDouble(split[3]);
				Double large = Double.parseDouble(split[4]);
				Double extraLarge = Double.parseDouble(split[5]);
				Double zhuliRatio = Double.parseDouble(split[6]);
				Double smallRatio = Double.parseDouble(split[7]);
				Double middleRatio = Double.parseDouble(split[8]);
				Double largeRatio = Double.parseDouble(split[9]);
				Double extraLargeRatio = Double.parseDouble(split[10]);
				Double trade = Double.parseDouble(split[11]);
				Double changeRatio = Double.parseDouble(split[12]);
				
				stockCapital.setCode(stockBase.getCode());
				stockCapital.setName(stockBase.getName());
				stockCapital.setDay(day);
				stockCapital.setZhuli(zhuli);
				stockCapital.setZhuliRatio(zhuliRatio);
				stockCapital.setExtraLarge(extraLarge);
				stockCapital.setExtraLargeRatio(extraLargeRatio);
				stockCapital.setLarge(large);
				stockCapital.setLargeRatio(largeRatio);
				stockCapital.setMiddle(middle);
				stockCapital.setMiddleRatio(middleRatio);
				stockCapital.setSmall(small);
				stockCapital.setSmallRatio(smallRatio);
				
				stockCapital.setTrade(trade);
				stockCapital.setChangeRadio(changeRatio);
				
				stockCapitals.add(stockCapital);
				
			}
			
			return stockCapitals;
			
		} catch (Exception e) {
			logger.error("parseStockCapitalHis exception...json_data = " + json, e);
		}
		
		return null;
	}
	

	public static List<StockCapital> parseStockCapitalRealtime(String json) {
		List<StockCapital> stockCapitals = Lists.newArrayList();

		try {
			JSONObject dataObject = JSON.parseObject(json).getJSONObject("data");
			if(dataObject == null) {
				return stockCapitals;
			}
			JSONArray jsonArray = dataObject.getJSONArray("diff");
			for (int i = 0; i < jsonArray.size(); i++) {
				
				StockCapital stockCapital = new StockCapital();
				
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String code = jsonObject.getString("f12").startsWith("6")?"sh"+jsonObject.getString("f12"):"sz"+jsonObject.getString("f12");
				String name = jsonObject.getString("f14");;
				
				if(jsonObject.getString("f84").equals("-")) {
					continue;
				}
				
				Double zhuli = jsonObject.getDouble("f62");
				Double zhuliRatio = jsonObject.getDouble("f184");
				Double small = jsonObject.getDouble("f84");
				Double smallRatio = jsonObject.getDouble("f87");
				Double middle = jsonObject.getDouble("f78");
				Double middleRatio = jsonObject.getDouble("f81");
				Double large = jsonObject.getDouble("f72");
				Double largeRatio = jsonObject.getDouble("f75");
				Double extraLarge = jsonObject.getDouble("f66");
				Double extraLargeRatio = jsonObject.getDouble("f69");
				Double trade = jsonObject.getDouble("f2");
				Double changeRatio = jsonObject.getDouble("f3");
				
				stockCapital.setCode(code);
				stockCapital.setName(name);
				stockCapital.setDay(DateUtil.getCurrentDay());
				stockCapital.setZhuli(zhuli);
				stockCapital.setZhuliRatio(zhuliRatio);
				stockCapital.setExtraLarge(extraLarge);
				stockCapital.setExtraLargeRatio(extraLargeRatio);
				stockCapital.setLarge(large);
				stockCapital.setLargeRatio(largeRatio);
				stockCapital.setMiddle(middle);
				stockCapital.setMiddleRatio(middleRatio);
				stockCapital.setSmall(small);
				stockCapital.setSmallRatio(smallRatio);
				
				stockCapital.setTrade(trade);
				stockCapital.setChangeRadio(changeRatio);
				
				stockCapitals.add(stockCapital);
				
			}
			
			return stockCapitals;
			
		} catch (Exception e) {
			logger.error("parseStockCapital exception...json_data = " + json, e);
		}
		
		return stockCapitals;
	}

	

	public static List<StockTop> parseStockTopList(String json) {
		
		try {
			
			List<StockTop> stockTops = Lists.newArrayList();
			
			
			JSONObject sourceObject = JSON.parseObject(json);
			JSONArray jsonArray = sourceObject.getJSONObject("result").getJSONArray("data");
			
			
			for (int i = 0; i < jsonArray.size(); i++) {
				
				StockTop stockTop = new StockTop();
				
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String code = reverse(jsonObject.getString("SECUCODE"));		//002229.SZ
				String name = jsonObject.getString("SECURITY_NAME_ABBR");	//
				String day = jsonObject.getString("LATEST_TDATE").split(" ")[0];	//2023-06-14 00:00:00
				
				
				stockTop.setCode(code);
				stockTop.setName(name);
				stockTop.setDay(day);
				
				stockTops.add(stockTop);
				
			}
			
			return stockTops;
			
		} catch (Exception e) {
			logger.error("parseStockTopList exception...json_data = " + json, e);
		}
		
		return null;
	}
	


	public static int parseStockTopPageTotal(String json) {
		
		try {
			
			JSONObject jsonObject = JSON.parseObject(json);
			return jsonObject.getJSONObject("result").getIntValue("pages");
			
		} catch (Exception e) {
			logger.error("parseStockTopPageTotal exception...json_data = " + json, e);
		}
		
		return 1;
	}
	
	private static String reverse(String text) {
		String [] arr = text.split("\\.");
		StringBuilder sb = new StringBuilder();
		for (int i = arr.length - 1; i >= 0; i--) {
			sb.append(arr[i]);
		}
		return sb.toString().toLowerCase();
	}
	
	
	public static void main(String [] args) {
		String text = "002229.SZ";
		System.out.println(reverse(text));	
	}
}
