package com.yh.bigdata.tts.common.constants;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StrategyTypeEnum {

    TREND_NEW("qsn", "金叉策略", 1),
    DEFAUL("default", "深坑反弹", 2),
    MULTI_STRENGTH("multi", "多周期强势", 3),
    CROSS_BAND_PRESSURE("cross_band_pressure", "突破波段: 关键阻力位", 9);

	private final String code;
	private final String desc;
    private final int groupOrder;

	public static StrategyTypeEnum getByCode(String code){
		for (StrategyTypeEnum type : StrategyTypeEnum.values()) {
			if (type.getCode().equals(code)) {
				return type;
			}
		}
		return DEFAUL;
	}
}
