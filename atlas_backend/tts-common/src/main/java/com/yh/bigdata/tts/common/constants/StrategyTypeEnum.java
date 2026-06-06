package com.yh.bigdata.tts.common.constants;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StrategyTypeEnum {

    TREND_NEW("qsn", "趋势", 1),
    DEFAUL("default", "右侧(脱离)*梯子*深坑*上移(底上移、顶上移)：周期", 9),
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
