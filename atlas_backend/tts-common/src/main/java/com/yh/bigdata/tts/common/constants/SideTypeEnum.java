package com.yh.bigdata.tts.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.checker.units.qual.A;

/**
 * @author duyp
 * 
 * @date 2019/01/17
 * 
 * @comment
 */

@AllArgsConstructor
@Getter
public enum SideTypeEnum {
	LONG("多"),
	SHORT("空"),
	;
	
	private final String desc;

}
