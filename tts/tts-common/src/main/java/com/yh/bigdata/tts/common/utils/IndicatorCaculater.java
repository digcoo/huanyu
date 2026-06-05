package com.yh.bigdata.tts.common.utils;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import com.yh.bigdata.tts.common.model.Trade;

public final class IndicatorCaculater {
	
	public static boolean checkOverMACD(List<Trade> trades) throws ParseException {
		String code = trades.get(0).getCode();
		BaseBarSeries barSeries = new BaseBarSeries(code);
		

		
		for (Trade trade : trades) {
			ZonedDateTime dateZone = DateUtil.parseDate(trade.getDay()).toInstant().atZone(ZoneId.systemDefault());
			barSeries.addBar(dateZone, trade.getOpen(), trade.getHigh(), trade.getLow(), trade.getClose());
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
		MACDIndicator macdIndicator = new MACDIndicator(closePrice, 12, 26);
		EMAIndicator emaIndicator = new EMAIndicator(macdIndicator, 9);
		
		
		//计算macd
		double macdValue = macdIndicator.getValue(barSeries.getBarCount() - 1).doubleValue();
		double sinalValue = emaIndicator.getValue(barSeries.getBarCount() - 1).doubleValue();
		
		return macdValue >= sinalValue;
	}
	
	private static double calEMA(double preEMA, double price, int period) {
		double multiplier = 2.0 / (period + 1);
		return (price - preEMA) * multiplier + preEMA;
	}
	public static List<Double> calMACD(List<Double> prices) {
		if (prices == null || prices.size() < 26) {
//			throw new IllegalArgumentException("数据点不足，至少需要26个数据点来计算 MACD");
			return Collections.EMPTY_LIST;
		}
		double ema12 = prices.get(0);
		double ema26 = prices.get(0);
		double dea = 0;
		List<Double> macdValues = new ArrayList<Double>();
		
		for (int i = 0; i < prices.size(); i++) {
			double price = prices.get(i);
			
			if (i == 0) {
				ema12 = price;
				ema26 = price;
			}else {
				ema12 = calEMA(ema12, price, 12);
				ema26 = calEMA(ema26, price, 26);
			}
			
			double dif = ema12 - ema26;
			dea = calEMA(dea, dif, 9);
			double macd = 2 * (dif - dea);
			macdValues.add(macd);
		}
		
		return macdValues.subList(macdValues.size() - 2, macdValues.size());
	}
	

	public static boolean checkRSI(List<Trade> trades) throws ParseException {
		String code = trades.get(0).getCode();
		BaseBarSeries barSeries = new BaseBarSeries(code);
		

		
		for (Trade trade : trades) {
			ZonedDateTime dateZone = DateUtil.parseDate(trade.getDay()).toInstant().atZone(ZoneId.systemDefault());
			barSeries.addBar(dateZone, trade.getOpen(), trade.getHigh(), trade.getLow(), trade.getClose());
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
		RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 6);
		
		//计算RSI
		double rsiValue = rsiIndicator.getValue(barSeries.getBarCount() - 1).doubleValue();
		
		return rsiValue < 70;
	}

	public static double calMAMax(List<Trade> trades, List<Integer> MAs) {
		String code = trades.get(0).getCode();		
		BaseBarSeries barSeries = new BaseBarSeries(code);
		
		for (Trade trade : trades) {
			ZonedDateTime dateZone;
			dateZone = DateUtil.parseDate(trade.getDay()).toInstant().atZone(ZoneId.systemDefault());
			barSeries.addBar(dateZone, trade.getOpen(), trade.getHigh(), trade.getLow(), trade.getClose());
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
		List<Double> maValues = new ArrayList<Double>();
		for (int MA : MAs) {
			maValues.add(new SMAIndicator(closePrice, MA).getValue(barSeries.getEndIndex()).doubleValue());
		}

		return MathUtil.max(maValues);
	}
	

	public static double calMAMin(List<Trade> trades, List<Integer> MAs) {
		String code = trades.get(0).getCode();		
		BaseBarSeries barSeries = new BaseBarSeries(code);
		
		for (Trade trade : trades) {
			ZonedDateTime dateZone;
			dateZone = DateUtil.parseDate(trade.getDay()).toInstant().atZone(ZoneId.systemDefault());
			barSeries.addBar(dateZone, trade.getOpen(), trade.getHigh(), trade.getLow(), trade.getClose());
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
		List<Double> maValues = new ArrayList<Double>();
		for (int MA : MAs) {
			maValues.add(new SMAIndicator(closePrice, MA).getValue(barSeries.getEndIndex()).doubleValue());
		}
		
		return MathUtil.min(maValues);
	}
	

	public static double calMA(List<Trade> trades, int MA) {
		if (CollectionUtils.isEmpty(trades)) {
			return 0;
		}
		String code = trades.get(0).getCode();
		BaseBarSeries barSeries = new BaseBarSeries(code);
		
		for (Trade trade : trades) {
			ZonedDateTime dateZone;
			try {
				dateZone = DateUtil.parseDate(trade.getDay()).toInstant().atZone(ZoneId.systemDefault());
				barSeries.addBar(dateZone, trade.getOpen(), trade.getHigh(), trade.getLow(), trade.getClose());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
		return new SMAIndicator(closePrice, MA).getValue(barSeries.getEndIndex()).doubleValue();
	}
}