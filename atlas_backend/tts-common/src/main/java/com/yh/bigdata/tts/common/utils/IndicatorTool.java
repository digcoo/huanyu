package com.yh.bigdata.tts.common.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils.MACDPoint;

public final class IndicatorTool {
	
	public static Pair<Double, Double> lowAndHigh(MACDPoint leftMACDPoint, MACDPoint rightMACDPoint) {
		List<Double> prices = new ArrayList<Double>();
		if (leftMACDPoint != null) {
			prices.add(leftMACDPoint.getTicker().getLow());
			prices.add(leftMACDPoint.getTicker().getHigh());
		}
		
		if (rightMACDPoint != null) {
			prices.add(rightMACDPoint.getTicker().getLow());
			prices.add(rightMACDPoint.getTicker().getHigh());
		}
		
		Double max = MathUtil.max(prices);
		Double min = MathUtil.min(prices);
		
		return Pair.of(min, max);
		
	}
	

	public static List<Double> mergeBoxPrices(Pair<MACDPoint, MACDPoint> glodMACDPoint) {
		List<Double> prices = new ArrayList<Double>();
		if (glodMACDPoint != null) {
			prices.add(glodMACDPoint.getLeft().getTicker().getLow());
			prices.add(glodMACDPoint.getLeft().getTicker().getHigh());
			prices.add(glodMACDPoint.getRight().getTicker().getLow());
			prices.add(glodMACDPoint.getRight().getTicker().getHigh());
		}
		return prices;
		
	}
	
}
