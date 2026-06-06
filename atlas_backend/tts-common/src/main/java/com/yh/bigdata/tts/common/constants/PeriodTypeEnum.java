package com.yh.bigdata.tts.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author duyp
 * 
 * @date 2019/01/17
 * 
 * @comment
 */

@AllArgsConstructor
@Getter
public enum PeriodTypeEnum {
	DAY("day", "1d", "日", 0.02, 0.025,0.015, 0.02, 0.01),
	WEEK("week", "1w", "周", 0.025, 0.05, 0.015, 0.02, 0.01),
	MONTH("month", "1M", "月", 0.045, 0.08, 0.015, 0.02, 0.01),
	QUARTER("quarter", "3M","季", 0.045,0.15, 0.015, 0.02, 0.01),
	YEAR("year", "1Y", "年", 0.045,0.3,0.015, 0.02, 0.01),
	;

    private final String code;
    private final String cryptoCode;
    private final String desc;
    private final double bandShiTiRate;
    private final double bandShockRate;
    private final double bandChangeRate;

    private final double crossMaxHighRate;
    private final double crossMinHighRate;

    public static PeriodTypeEnum getByCode(String code) {
    	for (PeriodTypeEnum periodTypeEnum : values()) {
    		if (periodTypeEnum.code.equals(code)) {
    			return periodTypeEnum;
    		}
    	}
    	return null;
    }

}
