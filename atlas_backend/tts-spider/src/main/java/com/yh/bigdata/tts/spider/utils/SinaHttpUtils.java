package com.yh.bigdata.tts.spider.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.StockFenshi;
import com.yh.bigdata.tts.common.utils.DateUtil;

public class SinaHttpUtils {
	private static Logger logger = LoggerFactory.getLogger(SinaHttpUtils.class);
	
	
	public static StockFenshi parseStockFenshi(String line) {
		
		try {
			
			String data = line.substring(line.indexOf("=") + 2, line.lastIndexOf(";"));
			String[] split = data.split(",");
			
			String code = line.substring(line.indexOf("hq_str_") + "hq_str_".length(), line.indexOf("="));
			String name = split[0];
			double open = Double.parseDouble(split[1]);
			double last_trade = Double.parseDouble(split[2]);
			double trade = Double.parseDouble(split[3]);
			double high = Double.parseDouble(split[4]);
			double low = Double.parseDouble(split[5]);
//			double buy1Price = Double.parseDouble(split[6]);			//?
//			double sell1Price = Double.parseDouble(split[7]);			//?
			long volume = Long.parseLong(split[8]);
			double amount = Double.parseDouble(split[9]);
			long buy1Volume = Long.parseLong(split[10]);
			double buy1Price = Double.parseDouble(split[11]);
			long buy2Volume = Long.parseLong(split[12]);
			double buy2Price = Double.parseDouble(split[13]);
			long buy3Volume = Long.parseLong(split[14]);
			double buy3Price = Double.parseDouble(split[15]);
			long buy4Volume = Long.parseLong(split[16]);
			double buy4Price = Double.parseDouble(split[17]);
			long buy5Volume = Long.parseLong(split[18]);
			double buy5Price = Double.parseDouble(split[19]);
			long sell1Volume = Long.parseLong(split[20]);
			double sell1Price = Double.parseDouble(split[21]);
			long sell2Volume = Long.parseLong(split[22]);
			double sell2Price = Double.parseDouble(split[23]);
			long sell3Volume = Long.parseLong(split[24]);
			double sell3Price = Double.parseDouble(split[25]);
			long sell4Volume = Long.parseLong(split[26]);
			double sell4Price = Double.parseDouble(split[27]);
			long sell5Volume = Long.parseLong(split[28]);
			double sell5Price = Double.parseDouble(split[29]);
			String day = split[30];
			String time = split[31];
			
			StockFenshi stockFenshi = new StockFenshi();
			stockFenshi.setCode(code);
			stockFenshi.setName(name);
			stockFenshi.setDay(day);
			stockFenshi.setTime(time);
			stockFenshi.setOpen(open);
			stockFenshi.setTrade(trade);
			stockFenshi.setHigh(high);
			stockFenshi.setLow(low);
			stockFenshi.setLastTrade(last_trade);
			stockFenshi.setAmount(amount);
			stockFenshi.setVolume(volume);
			stockFenshi.setBuy1Price(buy1Price);
			stockFenshi.setBuy1Volume(buy1Volume);
			stockFenshi.setBuy2Price(buy2Price);
			stockFenshi.setBuy2Volume(buy2Volume);
			stockFenshi.setBuy3Price(buy3Price);
			stockFenshi.setBuy3Volume(buy3Volume);
			stockFenshi.setBuy4Price(buy4Price);
			stockFenshi.setBuy4Volume(buy4Volume);
			stockFenshi.setBuy5Price(buy5Price);
			stockFenshi.setBuy5Volume(buy5Volume);
			stockFenshi.setSell1Price(sell1Price);
			stockFenshi.setSell1Volume(sell1Volume);
			stockFenshi.setSell2Price(sell2Price);
			stockFenshi.setSell2Volume(sell2Volume);
			stockFenshi.setSell3Price(sell3Price);
			stockFenshi.setSell3Volume(sell3Volume);
			stockFenshi.setSell4Price(sell4Price);
			stockFenshi.setSell4Volume(sell4Volume);
			stockFenshi.setSell5Price(sell5Price);
			stockFenshi.setSell5Volume(sell5Volume);
			
			return stockFenshi;
			
		} catch (Exception e) {
			logger.error("parseStockFenshi exception...line_data = " + line, e);
		}
		
		return null;
	}
	
	
	public static StockBase parseStockFinance(String line) {
		StockBase stockBase = new StockBase();
		
		try {
			
			String data = line.substring(line.indexOf("=") + 2, line.lastIndexOf(";"));
			String[] split = data.split(",");
			
			String code = line.substring(line.indexOf("hq_str_") + "hq_str_".length(), line.indexOf("_i="));
			Double mgsy = Double.parseDouble(split[4]);
			Double mgjzc = Double.parseDouble(split[5]);
			
			stockBase.setCode(code);
//			stockBase.setMgsy(mgsy);
//			stockBase.setMgjzc(mgjzc);
			
			return stockBase;
			
		}catch (Exception e) {
			logger.error("parseStockFenshi exception...line_data = " + line, e);
		}
		return stockBase;
	}
	
	public static Boolean isTradeOfCurrentDay() {
		String url = "https://hq.sinajs.cn/etag.php?list=sh000001";
		try {
			String ret_renshi = Request.Get(url)
					.addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sh000001/nc.shtml")			
					.execute().returnContent().asString();
			StockFenshi stockFenshi = parseStockFenshi(ret_renshi);
			return DateUtil.isSameDay(stockFenshi.getDay());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static List<StockDay> getTradeDays(int days) {
		String url = "https://quotes.sina.cn/cn/api/jsonp_v2.php=/CN_MarketDataService.getKLineData?symbol=sh000001&scale=240&ma=1&datalen="+days;
		try {
			String ret_day = Request.Get(url)
					.addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sh000001/nc.shtml")			
					.execute().returnContent().asString();
			
			if (StringUtils.isNotBlank(ret_day) && !ret_day.equals("null")) {
				return JSON.parseArray(ret_day, StockDay.class);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	
	}
	
	public static void main(String [] args) {
		String url = "http://hq.sinajs.cn/list=sh603958";
		try {
			String ret_renshi = Request.Get(url)
					.addHeader("Referer", "https://finance.sina.com.cn/realstock/company/sh000001/nc.shtml")			
					.execute().returnContent().asString();
			
			StockFenshi stockFenshi = parseStockFenshi(ret_renshi);	
			
			System.out.println(JSON.toJSONString(stockFenshi));
			
			if (new BigDecimal(stockFenshi.getSell5Price()).compareTo(BigDecimal.ZERO) == 0) {
				System.out.println("===");
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
