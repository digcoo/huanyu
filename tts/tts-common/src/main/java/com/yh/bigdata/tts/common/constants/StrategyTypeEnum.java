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
public enum StrategyTypeEnum {

    TREND_NEW("qsn", "趋势", 1),
//    OVER_ALL_MACD("over_all_macd", "MACD全红 ", 1),
//    OVER_ALL_PRESSURE("over_all_pressure", "K线无阻力 ", 1),
//
//    //打板策略
//    DA_BAN_1("db1", "打板1", 4),
//    DA_BAN_3("db3", "打板3", 4),
//    LIAN_BAN("lb", "连板", 4),

    DEFAUL("default", "右侧(脱离)*梯子*深坑*上移(底上移、顶上移)：周期", 9),

//
//    BETWEEN_TIZI_MACD("between_tizi_macd", "梯子之间(macd>0)", 9),
//    CROSS_ALL_MA("cross_all_ma", "MA穿3剑", 9),
//    CROSS_GOLD_MACD("cross_gold_macd", "macd金叉", 9),


    // 趋势上升策略
    CROSS_BAND_PRESSURE("cross_band_pressure", "突破波段: 关键阻力位", 9),

    ;

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
