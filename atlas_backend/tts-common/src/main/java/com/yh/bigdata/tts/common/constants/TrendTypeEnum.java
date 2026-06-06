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
public enum TrendTypeEnum {
	LONG("多头"),
	SHOCK("震荡"),
	SHORT("空头"),
	;

	private final String desc;

}
