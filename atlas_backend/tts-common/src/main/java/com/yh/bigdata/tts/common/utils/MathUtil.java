package com.yh.bigdata.tts.common.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.yh.bigdata.tts.common.model.Trade;
import org.apache.commons.lang3.tuple.Pair;

public final class MathUtil {
	
	public static Double max(Double... value) {
		return Arrays.asList(value).stream().filter(x-> Objects.nonNull(x)).max(Comparator.comparing(Double::doubleValue)).orElse(null);
	}
	
	public static Double max(List<Double> values) {
		return values.stream().filter(x-> Objects.nonNull(x)).max(Comparator.comparing(Double::doubleValue)).orElse(null);
	}
	
	
	public static Long max(Long... value) {
		return Arrays.asList(value).stream().filter(x-> Objects.nonNull(x)).max(Comparator.comparing(Long::longValue)).orElse(null);
	}

	public static Double min(Double... value) {
		return Arrays.asList(value).stream().filter(x -> Objects.nonNull(x)).min(Comparator.comparing(Double::doubleValue)).orElse(null);
	}
	
	public static int max(Integer... value) {
		return Arrays.asList(value).stream().filter(x -> Objects.nonNull(x)).max(Comparator.comparing(Integer::intValue)).orElse(null);
	}

	public static int min(Integer... value) {
		return Arrays.asList(value).stream().filter(x -> Objects.nonNull(x)).min(Comparator.comparing(Integer::intValue)).orElse(null);
	}

    public static BigDecimal max(BigDecimal... value) {
        return Arrays.asList(value).stream().filter(x -> Objects.nonNull(x)).max(Comparator.comparing(BigDecimal::doubleValue)).orElse(null);
    }

    public static BigDecimal min(BigDecimal... value) {
        return Arrays.asList(value).stream().filter(x -> Objects.nonNull(x)).min(Comparator.comparing(BigDecimal::doubleValue)).orElse(null);
    }

    public static Double lowMax(Trade... obj) {
        return Arrays.asList(obj).stream().filter(Objects::nonNull).map(Trade::getLow).max(Comparator.comparing(Double::doubleValue)).orElse(null);
    }

    public static Double highMin(Trade... obj) {
        return Arrays.asList(obj).stream().filter(Objects::nonNull).map(Trade::getHigh).min(Comparator.comparing(Double::doubleValue)).orElse(null);
    }

    public static Double shitiMax(Trade... obj) {
        return Arrays.asList(obj).stream().filter(Objects::nonNull).map(Trade::getShitiMax).min(Comparator.comparing(Double::doubleValue)).orElse(null);
    }

    public static Double highMax(Trade... obj) {
        return Arrays.asList(obj).stream().filter(Objects::nonNull).map(Trade::getHigh).max(Comparator.comparing(Double::doubleValue)).orElse(null);
    }

    public static Double mid(Double... value) {
		return Arrays.asList(value).stream().filter(x -> Objects.nonNull(x)).collect(Collectors.averagingDouble(Double::doubleValue));
	}
	
	public static Double min(List<Double> values) {
		return values.stream().filter(x -> Objects.nonNull(x)).min(Comparator.comparing(Double::doubleValue)).orElse(null);
	}

	public static Double mid(List<Double> values) {
		return values.stream().filter(x -> Objects.nonNull(x)).collect(Collectors.averagingDouble(Double::doubleValue));
	}

    public static boolean isGreaterThanZero(double... values) {
        return Arrays.stream(values).allMatch(x -> x > 0);
    }

    public static boolean isGreaterOrThanZero(double... values) {
        return Arrays.stream(values).anyMatch(x -> x > 0);
    }
	
	/**
	 * 计算直线相交的纵坐标
	 * @param A1
	 * @param A2
	 * @param B1
	 * @param B2
	 * @return
	 */
	public static Double calcuateCrossY(Pair<Double, Double> A1, Pair<Double, Double> A2
			, Pair<Double, Double> B1, Pair<Double, Double> B2) {
		
		double numerator = (B2.getLeft() - B1.getLeft()) * (A1.getRight() - B1.getRight()) - (B2.getRight() - B1.getRight()) * (A1.getLeft() - B1.getLeft());
		double demoninator = (B2.getRight() - B1.getRight()) * (A2.getLeft() - A1.getLeft()) - (B2.getLeft() - B1.getLeft()) * (A2.getRight() - A1.getRight());
		
		if(demoninator == 0) {
			return 0.0;
		}
		
		double ua = numerator / demoninator;
		
		return new BigDecimal(A1.getRight() + ua * (A2.getRight() - A1.getRight())).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
		
	}
	

	public static String formatMoney(Double value) {
		return new BigDecimal(value).divide(new BigDecimal(1_0000_0000), 2, BigDecimal.ROUND_UP).toPlainString();
	}
	
	public static Double getLowSpacePrice(double low, double high, double lowRate){
		return  low + lowRate * (high - low);
	}
	
	public static void main(String args[]) {
		System.out.println(mid(31.60, 33.90));
		System.out.println(getLowSpacePrice(67.10, 76.41, 0.8));
	
	}
	}
