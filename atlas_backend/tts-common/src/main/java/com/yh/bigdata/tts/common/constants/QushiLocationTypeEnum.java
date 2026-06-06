package com.yh.bigdata.tts.common.constants;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author duyp
 * 
 * @date 2019/01/17
 * 
 * @comment 趋势高水位
 */
@AllArgsConstructor
@Getter
public enum QushiLocationTypeEnum {
	
	FLOOR(-0.8, 0.0),
	LOW(0.0, 0.2),
	MIDDLE_LOW(0.2, 0.4),
	MIDDLE(0.4, 0.6),
	MIDDLE_HIGH(0.6, 0.8),
	HIGH(0.8, 1.0),
	TOP(1.0, 1000.0),
	OTHER(1000.0, 100000.0),
	;
	
	private final Double rangeLowRate;
	private final Double rangeHighRate;

	public static QushiLocationTypeEnum getByValue(Double value) {
		for (QushiLocationTypeEnum typeEnum: QushiLocationTypeEnum.values()) {
			if (value > typeEnum.getRangeLowRate() && value <= typeEnum.getRangeHighRate()) {
				return typeEnum;
			}
		}
		return OTHER;
	}

	public static boolean ifCrossByValue(Double value, List<QushiLocationTypeEnum> types) {
		return types.stream().anyMatch(x -> value > x.getRangeLowRate() && value <= x.getRangeHighRate());
	}
	
	public static List<QushiLocationTypeEnum> between(QushiLocationTypeEnum value1, QushiLocationTypeEnum value2) {
		return Lists.newArrayList(values()).stream()
				.filter(x -> x.ordinal() >= value1.ordinal() && x.ordinal() <= value2.ordinal())
				.collect(Collectors.toList());
	}
	
	
}
