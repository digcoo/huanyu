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

@Getter
@AllArgsConstructor
public enum SourceTypeEnum {
	
	MIN(1, "分时计算"),
	DAY(2, "日计算"),
	WEEK(3, "周拉取"),
	MONTH(4, "月拉取");

	private final int code;
	private final String desc;

}
