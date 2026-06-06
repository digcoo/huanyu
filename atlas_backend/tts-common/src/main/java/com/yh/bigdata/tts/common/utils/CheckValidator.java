package com.yh.bigdata.tts.common.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.yh.bigdata.tts.common.model.StockDay;
import com.yh.bigdata.tts.common.model.StockMonth;
import com.yh.bigdata.tts.common.model.StockWeek;
import com.yh.bigdata.tts.common.model.Trade;

public final class CheckValidator {

	public static boolean checkShunshi(List<Trade> trades, boolean upHigh) {
		Trade trade0 = trades.get(trades.size() - 1);
		if (trades.size() <= 1) {
			return trade0.getShitiRate() > 0;
		}

		Trade trade1 = trades.get(trades.size() - 2);
		if (upHigh) {
			if (trade1.getShitiRate() <= 0) {
				return trade0.getClose() > trade1.getShitiMax();
			} else {
//				return trade0.getClose() > trade1.getShitiMin();
				return trade0.getClose() > trade1.getLow();
			}
		} else {
			return trade0.getClose() > trade1.getLow();
		}
	}

	public static boolean checkShangyi(List<Trade> trades) {

		Trade trade0 = trades.get(trades.size() - 1);
		// 如果只有1个K线，则实体为正
		if (trades.size() <= 1) {
			return trade0.getShitiRate() > 0;
		}
		// 否则看最后2个K线反包
		Trade trade1 = trades.get(trades.size() - 2);
		if (trade0.getHigh() >= trade1.getHigh() && trade0.getClose() > trade1.getShitiMax()) {
			return true;
		}

//		if(trades.size() <= 2) {
//			return false;
//		}
//		
//		//否则看倒数第2个K线反包
//		Trade trade2 = trades.get(trades.size() - 3);
//		if(trade1.getHigh() >= trade2.getHigh() 
//				&& trade1.getClose() > trade2.getClose()
//				&& trade0.getClose() > trade1.getLow()
//				&& trade0.getClose() > trade2.getLow()
//				) {
//			return true;
//		}
//		
		// 否则看最后3个K线斜率突破((t3 - t2) > (t2 -t1) ) => t3 + t1 < 2t2
//		if(trade2.getHigh() + trade0.getClose() > 2 * trade1.getHigh()
//				&& trade0.getClose() > trade1.getLow()
//				&& trade0.getClose() > trade2.getLow()
//				) {
//			return true;
//		}

		return false;
	}

	
	public static boolean checkCommon(List<Trade> trades, double minZhenfuRate, double minVolAmount) {
		Trade trade0 = trades.get(trades.size() - 1);
		Trade trade1 = trades.size() > 1 ? trades.get(trades.size() - 2) : null;
		String code = trade0.getCode();

		int zhengfuPeriod = 3;
		boolean zhengfuFlag = false;
		List<Trade> zhengfuLatestPeriodTrades = CommonUtil.subReverseList(trades, zhengfuPeriod);
		for (Trade trade : zhengfuLatestPeriodTrades) {
			if (trade.getHighLastRate() >= minZhenfuRate || trade.getChangeRate() >= minZhenfuRate) {
				zhengfuFlag = true;
				break;
			}
		}
		if (!zhengfuFlag) {
			return false;
		}

		int volPeriod = 3;
		boolean volFlag = false;
		List<Trade> volLatestPeriodTrades = CommonUtil.subReverseList(trades, volPeriod);
		for (Trade trade : volLatestPeriodTrades) {
			if (trade.getAmount() >= minVolAmount) {
				volFlag = true;
				break;
			}
		}
		if (!volFlag) {
			return false;
		}

//		Double ma5Value0 = IndicatorCaculater.calMA(trades, 5);
//		Double ma10Value0 = IndicatorCaculater.calMA(trades, 10);
//		Double ma20Value0 = IndicatorCaculater.calMA(trades, 20);
//		Double ma30Value0 = IndicatorCaculater.calMA(trades, 30);
//
//		if (trade0 instanceof StockYear) {
//
//			if ((trade1 != null && trade0.getClose() < trade1.getLow())) {
//				return false;
//			}
//
//		} else if (trade0 instanceof StockMonth) {
//			
//			if ((trade1 != null && trade0.getClose() < trade1.getLow())) {
//				return false;
//			}
//			
//			if (!(ma5Value0 > ma10Value0 && ma10Value0 > ma20Value0)) {
//				return false;
//			}
//
//		} else if (trade0 instanceof StockWeek) {
//
//			if ((trade1 != null && trade0.getClose() < trade1.getOpen())) {
//				return false;
//			}
//			
//		} else if(trade0 instanceof StockDay){
//			
//			if ((trade1 != null && trade0.getClose() < trade1.getShitiMax())) {
//				return false;
//			}
//
//		}

		return true;
	}

	public static boolean checkCrossMAMaxAndMin(List<Trade> trades, int lastestPeriodNum, List<Integer> MAs) {

		boolean crossMAMax = false;
		boolean crossMAMin = false;

		for (int i = trades.size() - 1; i >= trades.size() - lastestPeriodNum; i--) {
			Trade tmpTrade = trades.get(i);
			List<Trade> tmpTrades = trades.subList(0, i + 1);
			if (tmpTrade.getClose() >= IndicatorCaculater.calMAMax(tmpTrades, MAs)) {
				crossMAMax = true;
			}

			if (tmpTrade.getLow() <= IndicatorCaculater.calMAMin(tmpTrades, MAs)) {
				crossMAMin = true;
			}
			if (crossMAMax && crossMAMin) {
				break;
			}
		}
		return crossMAMax && crossMAMin;
	}

	public static boolean checkOverMA(List<Trade> trades, List<Integer> MAs, boolean andOr) {
		Trade trade0 = trades.get(trades.size() - 1);
		boolean flag = true;
		if (andOr) {
			boolean tmpFlag = true;
			for (Integer MA : MAs) {
				tmpFlag = tmpFlag && trade0.getClose() >= IndicatorCaculater.calMA(trades, MA);
			}
			flag = tmpFlag;
		} else {
			boolean tmpFlag = false;
			for (Integer MA : MAs) {
				if (trade0.getClose() >= IndicatorCaculater.calMA(trades, MA)) {
					tmpFlag = true;
					break;
				}
			}
			flag = tmpFlag;
		}
		return flag;
	}
	

	public static boolean checkOverMA(List<Trade> trades, List<Integer> MAs, int nk) {
		Trade trade0 = trades.get(trades.size() - 1);

		List<Double> MAValues0 = MAs.stream()
				.map(x -> IndicatorCaculater.calMA(trades, x))
				.sorted(Comparator.comparing(x -> x)).collect(Collectors.toList())
				.subList(0, nk);

		return trade0.getClose() > MathUtil.max(MAValues0);
	}
	

	public static boolean checkDownMA(List<Trade> trades, List<Integer> MAs, int nk) {
		Trade trade0 = trades.get(trades.size() - 1);

		List<Double> MAValues0 = MAs.stream()
				.map(x -> IndicatorCaculater.calMA(trades, x))
				.sorted(Comparator.comparing(Double::doubleValue)).collect(Collectors.toList())
				.subList(0, nk);

		return trade0.getClose() < MathUtil.max(MAValues0);
	}
	

	public static boolean checkUnderMA(List<Trade> trades, List<Integer> MAs, boolean andOr) {
		Trade trade0 = trades.get(trades.size() - 1);
		boolean flag = true;
		if (andOr) {
			boolean tmpFlag = true;
			for (Integer MA : MAs) {
				tmpFlag = tmpFlag && trade0.getClose() <= IndicatorCaculater.calMA(trades, MA);
			}
			flag = tmpFlag;
		} else {
			boolean tmpFlag = false;
			for (Integer MA : MAs) {
				if (trade0.getClose() <= IndicatorCaculater.calMA(trades, MA)) {
					tmpFlag = true;
					break;
				}
			}
			flag = tmpFlag;
		}
		return flag;
	}

	/**
	 * 
	 * <pre>
	 * MA20 > max(MA5, MA10) : close > max(MA5, MA10)
	 * MA20 < min(MA5, MA10) : close > min(MA5, MA10)
	 * </pre>
	 * 
	 * @param trades
	 * @return
	 */
	public static boolean checkOverMA(List<Trade> trades, double spaceRate, boolean flag) {
		Trade trade0 = trades.get(trades.size() - 1);
		Double ma5Value = IndicatorCaculater.calMA(trades, 5);
		Double ma20Value = IndicatorCaculater.calMA(trades, 20);

		if (ma20Value < ma5Value) {
			// 主升浪趋势：再突
			if (trade0.getClose() >= ma5Value) {
				return true;
			}

		}

		if (ma20Value >= ma5Value) {
			// 调整下穿MA20后企稳
			if (trade0.getClose() >= ma5Value && trade0.getClose() < ma20Value
					&& (Math.abs(ma5Value - ma20Value) / ma20Value) >= spaceRate) {
				return true;
			}

			if (trade0.getClose() > ma20Value) {
				return true;
			}

		}

		return false;

	}

	public static boolean checkMAOverMA(List<Trade> trades, int headMA, int floorMA) {
		Double headMAValue = IndicatorCaculater.calMA(trades, headMA);
		Double floorMAValue = IndicatorCaculater.calMA(trades, floorMA);
		return headMAValue > floorMAValue;
	}

	public static boolean checkMACrossMA(List<Trade> trades, int crossMA, int baseMA, double crossRate)
			throws ParseException {
		Trade trade0 = trades.get(trades.size() - 1);

		List<Trade> lastTrades = trades.subList(0, trades.size() - 1);
		double lastBaseMA = IndicatorCaculater.calMA(lastTrades, baseMA);
		double lastCrossMA = IndicatorCaculater.calMA(lastTrades, crossMA);

		double todayBaseMA = IndicatorCaculater.calMA(trades, baseMA);
		double todayCrossMA = IndicatorCaculater.calMA(trades, crossMA);

		return lastCrossMA < lastBaseMA && todayCrossMA > todayBaseMA
				&& (trade0.getShitiRate() > 0 || trade0.getChangeRate() > 0);
	}

	public static boolean checkMAGongzhen(List<Trade> trades, List<Integer> MATypes) {
		Trade trade0 = trades.get(trades.size() - 1);

		boolean flag = true;
		List<Double> MAValues = MATypes.stream().map(x -> IndicatorCaculater.calMA(trades, x))
				.collect(Collectors.toList());
		for (int i = 0; i < MAValues.size() - 1; i++) {
			Double maVal1 = MAValues.get(i);
			Double maVal2 = MAValues.get(i + 1);
			if (maVal1 < maVal2) {
				flag = false;
				break;
			}
		}
		return flag && trade0.getClose() > MathUtil.min(MAValues)
				&& (trade0.getShitiRate() > 0 || trade0.getChangeRate() > 0);
	}

	/**
	 * 底部上穿或非底部cross
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkCrossMutiMAsOverAll(List<Trade> trades, List<Integer> MATypes, double crossRate,
			int nk) {
		Trade trade0 = trades.get(trades.size() - 1);

		List<Double> MAValues1 = MATypes.stream()
				.map(x -> IndicatorCaculater.calMA(trades.subList(0, trades.size() - 1), x))
				.sorted(Comparator.comparing(x -> x)).collect(Collectors.toList())
				.subList(MATypes.size() - nk, MATypes.size());
		Double MAMin1 = MathUtil.min(MAValues1);

		List<Double> MAValues0 = MATypes.stream().map(x -> IndicatorCaculater.calMA(trades, x))
				.sorted(Comparator.comparing(x -> x)).collect(Collectors.toList())
				.subList(MATypes.size() - nk, MATypes.size());
		Double MAMin0 = MathUtil.min(MAValues0);
		Double MAMax0 = MathUtil.max(MAValues0);
//		double low = MathUtil.min(trade0.getLastTrade(), trade0.getLow());		

		boolean crossMA = false;
		if ((trade0.getLastTrade() <= MAMin1 /* || trade0.getLow() <= MAMin0 */) && trade0.getClose() > MAMax0) {
			crossMA = true;
		}
		return crossMA && (trade0.getShitiRate() > crossRate || trade0.getChangeRate() > crossRate);
	}

	/**
	 * 非底部上穿
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkCrossMutiMAsWithoutOverAll(List<Trade> trades, List<Integer> MATypes, double crossRate,
			int nk) {

		Trade trade0 = trades.get(trades.size() - 1);
		double low = MathUtil.min(trade0.getLastTrade(), trade0.getLow());
		boolean crossMA = false;
		List<Double> MAValues0 = MATypes.stream().map(x -> IndicatorCaculater.calMA(trades, x))
				.sorted(Comparator.comparing(x -> x)).collect(Collectors.toList());

//		List<Double> MAValues1 = MATypes.stream().map(x -> IndicatorCaculater.calMA(trades.subList(0, trades.size() - 1), x))
//				.sorted(Comparator.comparing(x -> x))
//				.collect(Collectors.toList())
//				.subList(MATypes.size() - nk, MATypes.size());
//		Double MAMin1 = MathUtil.min(MAValues1);

		for (int i = 0; i <= MAValues0.size() - nk; i++) {
			if (low <= MAValues0.get(i) && trade0.getClose() >= MAValues0.get(i + nk - 1)) {
				crossMA = true;
				break;
			}
		}

		return crossMA && (trade0.getShitiRate() > crossRate || trade0.getChangeRate() > crossRate);
	}

	/**
	 * 底部上穿
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkDownUpCrossMutiMAsOverAll(List<Trade> trades, List<Integer> MATypes, double crossRate,
			int nk) {
		Trade trade0 = trades.get(trades.size() - 1);
		
		if ("sz300960".equals(trade0.getCode())) {
			System.out.println("====");
		}

		List<Double> MAValues1 = MATypes.stream()
				.map(x -> IndicatorCaculater.calMA(trades.subList(0, trades.size() - 1), x))
				.sorted(Comparator.comparing(x -> x))
				.collect(Collectors.toList())
//				.subList(MATypes.size() - nk, MATypes.size());
				.subList(0, nk);
		Double MAMax1 = MathUtil.max(MAValues1);
//		Double MAMin1 = MathUtil.min(MAValues1);

		List<Double> MAValues0 = MATypes.stream().map(x -> IndicatorCaculater.calMA(trades, x))
				.sorted(Comparator.comparing(x -> x)).collect(Collectors.toList())
//				.subList(MATypes.size() - nk, MATypes.size());
				.subList(0, nk);
		
		Double MAMax0 = MathUtil.max(MAValues0);
//		Double MAMin0 = MathUtil.min(MAValues0);

		boolean crossMA = false;
		if ((trade0.getLastTrade() <= MAMax1) && trade0.getClose() > MAMax0) {
			crossMA = true;
		}
		return crossMA && (trade0.getShitiRate() > crossRate || trade0.getChangeRate() > crossRate);
	}
//
//	/**
//	 * 底部上穿
//	 *
//	 * @return
//	 */
//	public static boolean checkDownUpCrossMA(List<Trade> bigTrades, List<Trade> dayTrades, Integer MAType,
//			double crossRate) {
//
//		Trade bigTrade0 = bigTrades.get(bigTrades.size() - 1);
//
//		Trade dayTrade0 = dayTrades.get(dayTrades.size() - 1);
//		Trade dayTrade1 = dayTrades.size() > 1 ? dayTrades.get(dayTrades.size() - 2) : null;
//
//		List<Trade> lastBigTrades = new ArrayList(bigTrades.subList(0, bigTrades.size() - 1));
//		Trade newWeekTrade0 = bigTrade0.copy();
//		newWeekTrade0.setClose(dayTrade1.getClose());
//		lastBigTrades.add(newWeekTrade0);
//
//		Double MAValue1 = IndicatorCaculater.calMA(lastBigTrades, MAType);
//		Double MAValue0 = IndicatorCaculater.calMA(bigTrades, MAType);
//
//		String code = dayTrade0.getCode();
//
////
////		if ("sz002537".equals(code)) {
////			System.out.println("===");
////		}
//
//		return dayTrade1.getClose() <= MAValue1 && dayTrade0.getClose() > MAValue0
//				&& (dayTrade0.getShitiRate() > crossRate || dayTrade0.getChangeRate() > crossRate);
//
//	}

	/**
	 * 底部上穿
	 * 
	 * @return
	 */
	public static boolean checkDownUpCrossPreShitiMax(List<Trade> bigTrades, List<Trade> dayTrades, double crossRate) {
		Trade bigTrade0 = bigTrades.get(bigTrades.size() - 1);
		Trade bigTrade1 = bigTrades.size() > 1 ? bigTrades.get(bigTrades.size() - 2) : null;

		Trade dayTrade0 = dayTrades.get(dayTrades.size() - 1);
		Trade dayTrade1 = dayTrades.size() > 1 ? dayTrades.get(dayTrades.size() - 2) : null;

		String code = dayTrade0.getCode();

//		
//		if ("sz002537".equals(code)) {
//			System.out.println("===");
//		}

		if (bigTrade1 == null) {
			return false;
		}

		return dayTrade1.getClose() <= bigTrade1.getShitiMax() && dayTrade0.getClose() > bigTrade1.getShitiMax()
				&& (dayTrade0.getShitiRate() > crossRate || dayTrade0.getChangeRate() > crossRate);

	}

	/**
	 * 阳包阴
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkDownUpCrossFanbao(List<Trade> bigTrades, List<Trade> dayTrades, double crossRate) {
		Trade bigTrade0 = bigTrades.get(bigTrades.size() - 1);
		Trade bigTrade1 = bigTrades.size() > 1 ? bigTrades.get(bigTrades.size() - 2) : null;

		Trade dayTrade0 = dayTrades.get(dayTrades.size() - 1);
		Trade dayTrade1 = dayTrades.size() > 1 ? dayTrades.get(dayTrades.size() - 2) : null;

		String code = dayTrade0.getCode();

//		
//		if ("sz002537".equals(code)) {
//			System.out.println("===");
//		}

		if (bigTrade1 == null) {
			return false;
		}

		return bigTrade1.getShitiRate() < 0 && dayTrade1.getClose() <= bigTrade1.getShitiMax()
				&& dayTrade0.getClose() > bigTrade1.getShitiMax()
				&& (dayTrade0.getShitiRate() > crossRate || dayTrade0.getChangeRate() > crossRate);

	}

	/**
	 * 阳包阴
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkDownUpCrossFanbaoHigh(List<Trade> bigTrades, List<Trade> dayTrades, double crossRate) {
		Trade bigTrade0 = bigTrades.get(bigTrades.size() - 1);
		Trade bigTrade1 = bigTrades.size() > 1 ? bigTrades.get(bigTrades.size() - 2) : null;

		Trade dayTrade0 = dayTrades.get(dayTrades.size() - 1);
		Trade dayTrade1 = dayTrades.size() > 1 ? dayTrades.get(dayTrades.size() - 2) : null;

		String code = dayTrade0.getCode();

//		
//		if ("sz002537".equals(code)) {
//			System.out.println("===");
//		}

		if (bigTrade1 == null) {
			return false;
		}

		return bigTrade1.getShitiRate() < 0 && dayTrade1.getClose() <= bigTrade1.getHigh()
				&& dayTrade0.getClose() > bigTrade1.getHigh()
				&& (dayTrade0.getShitiRate() > crossRate || dayTrade0.getChangeRate() > crossRate);

	}

	/**
	 * 阳包阴
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkFanbao(List<Trade> trades, double crossRate) {
		Trade trade0 = trades.get(trades.size() - 1);
		Trade trade1 = trades.size() > 1 ? trades.get(trades.size() - 2) : null;
		Trade trade2 = trades.size() > 2 ? trades.get(trades.size() - 3) : null;

		String code = trade0.getCode();

//		
//		if ("sz002537".equals(code)) {
//			System.out.println("===");
//		}

		if (trade1 == null) {
			return false;
		}

		return trade1.getShitiRate() < 0 && trade0.getShitiRate() > 0
				&& (trade0.getShitiRate() > crossRate || trade0.getChangeRate() > crossRate);

	}

	/**
	 * 阳包阴
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkLianZhang(List<Trade> trades, int nPeriods) {

		List<Trade> subTrades = trades.subList(trades.size() < nPeriods ? 0 : trades.size() - nPeriods, trades.size());

		return subTrades.stream().allMatch(x -> x.getShitiRate() > 0 && x.getClose() > x.getLastTrade());

	}

	/**
	 * 阳包阴
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkLianZhang2(List<Trade> trades, int nPeriods) {

		List<Trade> subTrades = trades.subList(trades.size() < nPeriods + 1 ? 0 : trades.size() - nPeriods - 1,
				trades.size() - 1);

		List<Trade> subTrades2 = trades.subList(trades.size() < nPeriods ? 0 : trades.size() - nPeriods, trades.size());

		return subTrades.stream().allMatch(x -> x.getShitiRate() >= 0)

				|| subTrades2.stream().allMatch(x -> x.getShitiRate() >= 0);

	}

	/**
	 * 底部上穿
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkDownUpCrossPreShitiMin(List<Trade> bigTrades, List<Trade> dayTrades, double crossRate) {
		Trade bigTrade0 = bigTrades.get(bigTrades.size() - 1);
		Trade bigTrade1 = bigTrades.size() > 1 ? bigTrades.get(bigTrades.size() - 2) : null;

		Trade dayTrade0 = dayTrades.get(dayTrades.size() - 1);
		Trade dayTrade1 = dayTrades.size() > 1 ? dayTrades.get(dayTrades.size() - 2) : null;

		String code = dayTrade0.getCode();

//		
//		if ("sz002537".equals(code)) {
//			System.out.println("===");
//		}

		if (bigTrade1 == null) {
			return false;
		}

		return dayTrade1.getClose() <= bigTrade1.getShitiMin() && dayTrade0.getClose() > bigTrade1.getShitiMin()
				&& (dayTrade0.getShitiRate() > crossRate || dayTrade0.getChangeRate() > crossRate);

	}

	/**
	 * 底部上穿
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkDownUpCrossPreLow(List<Trade> bigTrades, List<Trade> dayTrades, double crossRate) {
		Trade bigTrade0 = bigTrades.get(bigTrades.size() - 1);
		Trade bigTrade1 = bigTrades.size() > 1 ? bigTrades.get(bigTrades.size() - 2) : null;

		Trade dayTrade0 = dayTrades.get(dayTrades.size() - 1);
		Trade dayTrade1 = dayTrades.size() > 1 ? dayTrades.get(dayTrades.size() - 2) : null;

		String code = dayTrade0.getCode();

//		
//		if ("sz002537".equals(code)) {
//			System.out.println("===");
//		}

		if (bigTrade1 == null) {
			return false;
		}

		return dayTrade1.getClose() <= bigTrade1.getLow() && dayTrade0.getClose() > bigTrade1.getLow()
				&& (dayTrade0.getShitiRate() > crossRate || dayTrade0.getChangeRate() > crossRate);

	}

	/**
	 * 底部上穿
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkDownUpCrossOpen(List<Trade> bigTrades, List<Trade> dayTrades, double crossRate) {
		Trade bigTrade0 = bigTrades.get(bigTrades.size() - 1);

		Trade dayTrade0 = dayTrades.get(dayTrades.size() - 1);
		Trade dayTrade1 = dayTrades.size() > 1 ? dayTrades.get(dayTrades.size() - 2) : null;

		String code = dayTrade0.getCode();

//		if ("sz002537".equals(code)) {
//			System.out.println("===");
//		}

		return dayTrade1.getClose() <= bigTrade0.getOpen() && dayTrade0.getClose() > bigTrade0.getOpen()
				&& (dayTrade0.getShitiRate() > crossRate || dayTrade0.getChangeRate() > crossRate);

	}

	/**
	 * 底部上穿
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkDownUpCrossShunShiRed(List<Trade> bigTrades, List<Trade> dayTrades, double crossRate) {
		Trade bigTrade0 = bigTrades.get(bigTrades.size() - 1);

		Trade dayTrade0 = dayTrades.get(dayTrades.size() - 1);
		Trade dayTrade1 = dayTrades.size() > 1 ? dayTrades.get(dayTrades.size() - 2) : null;

		String code = dayTrade0.getCode();

//		if ("sz002537".equals(code)) {
//			System.out.println("===");
//		}

		return dayTrade1.getClose() <= bigTrade0.getOpen() && dayTrade0.getClose() > bigTrade0.getOpen()
				&& (dayTrade0.getShitiRate() > crossRate || dayTrade0.getChangeRate() > crossRate);

	}

	/**
	 * 底部上穿
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkDownUpCrossMutiMAsWithoutOverAll(List<Trade> trades, List<Integer> MATypes,
			double crossRate) {

		Trade trade0 = trades.get(trades.size() - 1);

		Map<Integer, Double> MAValueMap0 = new HashMap<Integer, Double>(MATypes.size());
		for (Integer MAType : MATypes) {
			MAValueMap0.put(MAType, IndicatorCaculater.calMA(trades, MAType));
		}

		Map<Integer, Double> MAValueMap1 = new HashMap<Integer, Double>(MATypes.size());
		List<Trade> lastTrades1 = trades.subList(0, trades.size() - 1);
		for (Integer MAType : MATypes) {
			MAValueMap1.put(MAType, IndicatorCaculater.calMA(lastTrades1, MAType));
		}

		boolean crossMA = false;
		for (Map.Entry<Integer, Double> entry : MAValueMap0.entrySet()) {
			if (trade0.getLastTrade() <= MAValueMap1.get(entry.getKey())
					&& trade0.getClose() >= MAValueMap0.get(entry.getKey())) {
				crossMA = true;
				break;
			}
		}

		return crossMA && (trade0.getShitiRate() > crossRate || trade0.getChangeRate() > crossRate);
	}

	public static boolean checkTupoLatestHigh(List<Trade> trades, int latestPeriods) {
		Trade trade0 = trades.get(trades.size() - 1);
		Trade trade1 = trades.get(trades.size() - 2);

		// 去除当日
		trades = trades.subList(Math.max(0, trades.size() - latestPeriods - 1), trades.size() - 1);
		Double highMax = trades.stream()
//				.filter(x -> x.getClose() > x.getOpen() || x.getChangeRate() > 0)
				.map(Trade::getHigh).max(Comparator.comparing(x -> x)).orElse(trade1.getHigh());

		return trade0.getClose() > highMax;
	}

	/**
	 * 顺势(长周期、脱离、上移)：尾盘
	 * 
	 * <pre>
	 * 脱离
	 * 
	 * </pre>
	 * 
	 * @param trades
	 * @return
	 */
	public static boolean checkShunShi(List<Trade> dayTrades, List<Trade> weekTrades, List<Trade> monthTrades) {
		Trade dayTrade0 = dayTrades.get(dayTrades.size() - 1);
		Trade weekTrade0 = weekTrades.get(weekTrades.size() - 1);
		Trade monthTrade0 = monthTrades.get(monthTrades.size() - 1);

		// 大趋势下：企稳
		boolean flag1 =
//				dayTrade0.getShitiRate() > 0
//				&& 
				CheckValidator.checkOverMA(dayTrades, Arrays.asList(20), true)

						&& (
//						weekTrade0.getShitiRate() > 0
//						&&
						(CheckValidator.checkMAGongzhen(weekTrades, Arrays.asList(5, 30))
								|| CheckValidator.checkMAGongzhen(weekTrades, Arrays.asList(10, 30)))

								// 或者
								|| (
//							monthTrade0.getShitiRate() > 0 && weekTrade0.getShitiRate() > 0
//							&& 
								(CheckValidator.checkMAGongzhen(monthTrades, Arrays.asList(5, 20))
										|| CheckValidator.checkMAGongzhen(monthTrades, Arrays.asList(10, 20)))));

		// 调整待突破
		Double ma5Value = IndicatorCaculater.calMA(dayTrades, 5);
		Double ma10Value = IndicatorCaculater.calMA(dayTrades, 10);
		Double ma20Value = IndicatorCaculater.calMA(dayTrades, 20);
		Double ma30Value = IndicatorCaculater.calMA(dayTrades, 30);

		boolean flag2 = dayTrade0.getShitiRate() > 0 && dayTrade0.getClose() > MathUtil.max(ma20Value)
				&& ma5Value > ma20Value && ma10Value > ma20Value
//				&& CheckValidator.checkMAGongzhen(dayTrades, Arrays.asList(5, 10, 20))
				&& CheckValidator.checkDownUpCrossMutiMAsWithoutOverAll(dayTrades, Arrays.asList(5, 10), 0.015);

		return flag2;

	}

	/**
	 * 超低选股(深坑、梯子、空间、不追高)：尾盘
	 * 
	 * <pre>
	 * 特征：深坑、梯子、空间
	 * 避开：不追高（非箱顶）
	 * 优选：严格的空头排列：M5 < M10 < M20，且散列
	 * 
	 * 
	 * </pre>
	 * 
	 * @param trades
	 * @return
	 */
	public static boolean checkDownUpCrossMA(List<Trade> trades, double crossRate) {
		Trade trade0 = trades.get(trades.size() - 1);

		List<Trade> trades1 = trades.subList(0, trades.size() - 1);
		Double ma5Value1 = IndicatorCaculater.calMA(trades1, 5);
		Double ma10Value1 = IndicatorCaculater.calMA(trades1, 10);
		Double ma20Value1 = IndicatorCaculater.calMA(trades1, 20);
		Double ma30Value1 = IndicatorCaculater.calMA(trades1, 30);

		Double ma5Value0 = IndicatorCaculater.calMA(trades, 5);
		Double ma10Value0 = IndicatorCaculater.calMA(trades, 10);
		Double ma20Value0 = IndicatorCaculater.calMA(trades, 20);
		Double ma30Value0 = IndicatorCaculater.calMA(trades, 30);

		// 上穿M5、M10
		if (ma5Value1 < ma20Value1 && ma5Value1 < ma20Value1) {
			if (trade0.getClose() > MathUtil.max(ma5Value0, ma10Value0) && trade0.getClose() < ma20Value0
			// 底部上穿
					&& CheckValidator.checkDownUpCrossMutiMAsOverAll(trades, Arrays.asList(5, 10), crossRate, 1)) {
				return true;
			}
		}

		// 上穿M20
		if (ma5Value1 < ma20Value1 && ma10Value1 < ma20Value1) {
			if (trade0.getClose() > ma20Value0
					// 底部上穿
					&& CheckValidator.checkDownUpCrossMutiMAsOverAll(trades, Arrays.asList(20), crossRate, 1)) {
				return true;
			}
		}

		// 上穿M30
		if (ma5Value1 < ma30Value1 && ma10Value1 < ma30Value1 && ma20Value1 < ma30Value1) {
			if (trade0.getClose() > ma30Value0
					// 底部上穿
					&& CheckValidator.checkDownUpCrossMutiMAsOverAll(trades, Arrays.asList(30), crossRate, 1)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 底部上穿
	 * 
	 * @param trades
	 * @param MATypes
	 * @param crossRate
	 * @param nk
	 * @return
	 */
	public static boolean checkGongzhenQiangshi(List<Trade> bigTrades, List<Trade> dayTrades, double crossRate) {

		Trade dayTrade0 = dayTrades.get(dayTrades.size() - 1);

		String code = dayTrade0.getCode();

		return checkMAGongzhen(bigTrades, Arrays.asList(5, 10, 20))
				&& (dayTrade0.getShitiRate() > crossRate || dayTrade0.getChangeRate() > crossRate);

	}

	public static boolean checkGongZhen(List<Trade> trades) {
		
		Trade trade0 = trades.get(trades.size() - 1);
		Trade trade1 = trades.size() > 1? trades.get(trades.size() - 2) : null;

		
//		if ("sh603030".equals(trade0.getCode())) {
//			System.out.println("===");
//		}
		
		Double ma5Value0 = IndicatorCaculater.calMA(trades, 5);
		Double ma10Value0 = IndicatorCaculater.calMA(trades, 10);
		Double ma20Value0 = IndicatorCaculater.calMA(trades, 20);
		Double ma30Value0 = IndicatorCaculater.calMA(trades, 30);

//		List<Trade> lastTrades = trades.subList(0, trades.size() - 1);
//		Double lastMa5Value0 = IndicatorCaculater.calMA(lastTrades, 5);
//		Double lastMa10Value0 = IndicatorCaculater.calMA(lastTrades, 10);
//		Double lastMa20Value0 = IndicatorCaculater.calMA(lastTrades, 20);
		
		if (trade0 instanceof StockMonth) {
			
			if ((ma5Value0 > ma20Value0 || ma10Value0 > ma20Value0)
					&& MathUtil.min(ma5Value0, ma10Value0) > ma30Value0
					
//					&& (lastMa5Value0 > lastMa20Value0 || lastMa10Value0 > lastMa20Value0)
					&& (trade1 == null || trade0.getClose() > trade1.getLow())
					&& (trade0.getClose() > ma5Value0)
					&& trade0.getShitiRate() > 0
					) {
				return true;
			}
			
		} else if (trade0 instanceof StockWeek) {
			
			if ((ma5Value0 > ma20Value0 || ma10Value0 > ma20Value0 )
					&& MathUtil.min(ma5Value0, ma10Value0) > ma30Value0
					
//					&& (lastMa5Value0 > lastMa20Value0 || lastMa10Value0 > lastMa20Value0)
					&& (trade1 == null || trade0.getClose() > trade1.getLow())
					&& (trade0.getClose() > ma5Value0)
					&& trade0.getShitiRate() > 0
					) {
				return true;
			}
			
		}else if (trade0 instanceof StockDay) {
			
			if ((ma5Value0 > ma20Value0 || ma10Value0 > ma20Value0 )
					&& MathUtil.min(ma5Value0, ma10Value0) > ma30Value0
					
//					&& (lastMa5Value0 > lastMa20Value0 || lastMa10Value0 > lastMa20Value0)
					&& (trade1 == null || trade0.getClose() > trade1.getLow())
					&& (trade0.getClose() > ma5Value0)
					&& trade0.getShitiRate() > 0
					) {
				return true;
			}
			
		}
		
		return false;
	}
	

	/**
	 * 底部大阳线(反包)
	 * @param stockBase
	 * @return
	 */
	public static boolean checkBottomShiti(List<Trade> trades, Double shitiRate, int periodNum) {

		Trade trade0 = trades.get(trades.size() - 1);
		Trade trade1 = trades.size() > 1? trades.get(trades.size() - 2) : null;

		Trade maxTrade = null;

		trades = trades.subList(trades.size() > periodNum? trades.size() - periodNum : 0, trades.size());
		
		for (int i = trades.size() - 2; i >= 0; i--) {
			Trade tmpTrade = trades.get(i);
			if (tmpTrade.getShitiRate() > shitiRate) {
				if (maxTrade == null || tmpTrade.getShitiRate() > maxTrade.getShitiRate()) {
					maxTrade = tmpTrade;
				}
			}
		}

		if (trade0 instanceof StockMonth) {

			if (maxTrade != null 
					
					&& trade0.getClose() > maxTrade.getOpen()

					&& trade0.getClose() > trade1.getClose() 
					
					&& trade0.getShitiRate() >= 0
					
					) {
				return true;
			}
			
		}else if (trade0 instanceof StockWeek) {

			if (maxTrade != null 
					
					&& trade0.getClose() > maxTrade.getOpen()

					&& trade0.getClose() > trade1.getOpen() 
					
					&& trade0.getShitiRate() >= 0
					
					) {
				return true;
			}
			
		}else {
			
			if (maxTrade != null 
					
					&& trade0.getClose() > maxTrade.getLow()

					&& trade0.getClose() > trade1.getClose() 
					
					&& trade0.getShitiRate() >= 0
					
					) {
				return true;
			}
		}
		return false;
	}

	public static Trade checkExistShitiDaZhang(List<Trade> trades, int latestN, double rate) {
		for (int i = trades.size() - latestN; i >= 0; i--) {
			Trade trade = trades.get(i);
			if(trade.getShitiRate() > rate) {
				return trade;
			}
		}
		return null;
	}
	
	
	public static boolean checkHasChangeRateDaZhang(List<Trade> trades, int latestN, double rate) {
		for (int i = trades.size() - latestN; i >= 0; i--) {
			Trade trade = trades.get(i);
			if(trade.getChangeRate() > rate || trade.getShitiRate() > rate) {
				return true;
			}
		}
		return false;
		
	}
	
}