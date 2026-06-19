package com.yh.bigdata.tts.common.constants;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StrategyTypeEnum {

    /** 超短线策略（当前主策略） */
    ULTRA_SHORT("ultra", "超短线策略", 1),
    /** 趋势策略（待实现） */
    TREND_V2("trend", "趋势策略", 2),

    // --- v1 已下线，保留枚举供历史 stock_target 数据兼容 ---
    TREND_NEW("qsn", "金叉策略", 101),
    PRE_GOLD_CROSS("preqsn", "预判金叉", 102),
    PERIOD_RESONANCE("reson", "周期共振", 103),
    DEFAUL("default", "深跌反弹", 104),
    LADDER_BREAKOUT("ladder", "梯子突破", 105),
    RETEST("retest", "回踩抬升", 106),
    GC2_BREAKOUT("gc2", "金叉二次突破", 107),
    DC2_BREAKOUT("dc2", "死叉突破", 108),
    CROSS_BAND_PRESSURE("cross_band_pressure", "突破波段: 关键阻力位", 109);

	private final String code;
	private final String desc;
    private final int groupOrder;

    public boolean isActive() {
        return this == ULTRA_SHORT || this == TREND_V2;
    }

	public static StrategyTypeEnum getByCode(String code){
		if (code == null || code.isEmpty()) {
            return ULTRA_SHORT;
        }
		if ("ulow".equals(code)) {
			code = "ladder";
		}
		for (StrategyTypeEnum type : StrategyTypeEnum.values()) {
			if (type.getCode().equals(code)) {
				return type;
			}
		}
		return ULTRA_SHORT;
	}
}
