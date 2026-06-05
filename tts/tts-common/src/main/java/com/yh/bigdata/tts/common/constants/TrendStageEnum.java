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
public enum TrendStageEnum {
	UNKNOWN(1, "假复苏"),
	FAKE_RECOVERY(1, "假复苏"),						//假复苏：红柱子区间--跌破区间
	RECOVERY_SHOCK(2, "复苏期--震荡"),				//红柱子区间
	RECOVERY_PROSPERITY(3, "复苏期--繁荣"),			//突破红柱子
	PROSPERITY_SHOCK(4, "繁荣期--震荡"),				//突破红柱子顶 -> 进入绿柱子区间
	PROSPERITY_BUBBLE(5, "繁荣期--泡沫"),				//突破绿柱子顶
	FAKE_RECESSION_SHOCK(6, "假衰退期--震荡"),			//形成死叉 -> 未跌破绿柱子
	FAKE_RECESSION_BUBBLE(7, "假衰退期--繁荣泡沫"),	//突破绿柱子顶
	RECESSION(8, "衰退期"),							//跌破绿柱子
	RECESSION_SHOCK(9, "衰退期--震荡"),				//跌破绿柱子底 -> 进入红柱子区间
	RECESSION_BUBBLE(10, "衰退期--泡沫");				//跌破红柱子底

	private final int code;
	private final String desc;
	
	/**
	 * 极端（乐观）情绪
	 * @return
	 */
	public boolean isExtremeBullish() {
		return this == PROSPERITY_BUBBLE;
	}
	
	/**
	 * 乐观情绪
	 * @return
	 */
	public boolean isBullish() {
		return this == RECOVERY_PROSPERITY
				|| this == PROSPERITY_SHOCK
				|| this == PROSPERITY_BUBBLE
				|| this == FAKE_RECESSION_SHOCK
				|| this == FAKE_RECESSION_BUBBLE
				;
	}
	
	/**
	 * 中性情绪
	 * @return
	 */
	public boolean isNeutral() {
		return this == RECOVERY_SHOCK
				|| this == FAKE_RECESSION_SHOCK;
	}
	
	public boolean isNeutralOrBullish() {
		return isNeutral() || isBullish();
	}
	
	/**
	 * 悲观情绪
	 * @return
	 */
	public boolean isBearish() {
		return this == FAKE_RECOVERY
				|| this == RECESSION
				|| this == RECESSION_SHOCK
				|| this == RECESSION_BUBBLE
				;
	}
	
	/**
	 * 极端（悲观）情绪
	 * @return
	 */
	public boolean isExtremeBearish() {
		return this == RECESSION_BUBBLE;
	}
	
}
