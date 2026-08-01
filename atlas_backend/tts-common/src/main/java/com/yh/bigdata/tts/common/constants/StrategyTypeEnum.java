package com.yh.bigdata.tts.common.constants;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StrategyTypeEnum {

    /** 超短线策略（当前主策略） */
    ULTRA_SHORT("ultra", "超短线策略", 1),
    /** 短线策略 */
    TREND_V2("trend", "短线策略", 2),
    /** 中线策略 */
    MEDIUM("medium", "中线策略", 3),
    /** 长线策略 */
    LONG("long", "长线策略", 4),
    /** 跨周期内梯子上移 */
    FRICTIONLESS_LADDER("nrf", "跨周期内梯子上移", 5),
    /** 柱子内上移 */
    PILLAR_BREAKOUT("pillar", "柱子内上移", 7),
    /** 级联交叉突破 */
    CASCADE_BREAKOUT("cascade", "级联交叉突破", 8),
    /** 级联梯子探底回升 */
    LADDER_DIP("ldip", "级联梯子探底回升", 10),
    /** MACD 交叉边沿突破 */
    MACD_EDGE_BREAKOUT("macedge", "MACD交叉边沿突破", 11),
    /** @deprecated 波段末阳中位数突破已下线 */
    WAVE_BREAKOUT("wavebreak", "波段突破", 12),
    /** @deprecated 波段同档末阳中位数突破已下线 */
    WAVE_BREAKOUT_TIER("wavetier", "波段同档突破", 13),
    /** @deprecated 波段末阳底突破已下线 */
    WAVE_LOW("wavelow", "波段末阳底突破", 14),
    /** @deprecated 波段同档末阳底突破已下线 */
    WAVE_LOW_TIER("wavetierlow", "波段同档末阳底突破", 15),
    /** 凸波段突破 */
    WAVE_CONVEX("waveconvex", "凸波段突破", 16),
    /** 凹波段突破 */
    WAVE_CONCAVE("waveconcave", "凹波段突破", 17),
    /** 凸波段日突破 */
    WAVE_CONVEX_DAY("waveconvexday", "凸波段日突破", 18),
    /** 凹波段日突破 */
    WAVE_CONCAVE_DAY("waveconcaveday", "凹波段日突破", 19),
    /** 级联 MACD 凸波段同档突破 */
    CASCADE_WAVE_CONVEX("cascadewaveconvex", "级联MACD凸波段同档突破", 20),
    /** 级联 MACD 凹波段同档突破 */
    CASCADE_WAVE_CONCAVE("cascadewaveconcave", "级联MACD凹波段同档突破", 21),
    /** 级联 MACD 凸波段日突破 */
    CASCADE_WAVE_CONVEX_DAY("cascadewaveconvexday", "级联MACD凸波段日突破", 22),
    /** 级联 MACD 凹波段日突破 */
    CASCADE_WAVE_CONCAVE_DAY("cascadewaveconcaveday", "级联MACD凹波段日突破", 23),
    /** 波段突破 · 凸 */
    WAVE_BAND_CONVEX("wavebandconvex", "波段突破凸", 27),
    /** 波段突破 · 凹 */
    WAVE_BAND_CONCAVE("wavebandconcave", "波段突破凹", 28),
    /** @deprecated 波段策略（月周形态门 + 日收阳） */
    WAVE_BAND("waveband", "波段策略", 35),
    /** 波段策略 · 短线（周形态门 + 日收阳） */
    WAVE_BAND_SHORT("wavebandShort", "波段策略短线", 36),
    /** 波段策略 · 中线（月形态门 + 日收阳） */
    WAVE_BAND_MEDIUM("wavebandMedium", "波段策略中线", 37),
    /** 多周期波段形态门（周月年 + 日 K 边沿） */
    WAVE_PERIOD_GATE("waveperiodgate", "多周期波段形态门", 38),
    /** 同档 MACD 交叉突破（日/周/月单档） */
    MACD_CROSS_TIER("macdcrosstier", "同档MACD交叉突破", 39),
    /** 凸波段上移（日凸 + 周/月/年 close&gt;前K high） */
    CONVEX_LIFT_TIER("convexlifttier", "凸波段上移", 40),
    /** 柱子策略（日/周/月单档实体柱突破） */
    BODY_BAR_TIER("bodybar", "柱子策略", 41),
    /** MACD金叉（日/周/月单档末 K MACD 金叉） */
    MACD_GOLDEN_CROSS("macdgc", "MACD金叉", 42),
    /** MACD金叉波段High突破（日/周/月单档） */
    MACD_GC_WAVE_HIGH("macdgcwh", "MACD金叉波段High突破", 43),
    /** MACD金叉波段High回踩（日/周/月单档） */
    MACD_GC_WAVE_HIGH_RETEST("macdgcwhr", "MACD金叉波段High回踩", 44),
    /** MACD金叉波段High上移（日/周/月单档） */
    MACD_GC_WAVE_HIGH_LIFT("macdgcwhu", "MACD金叉波段High上移", 45),
    /** MACD死叉突破（日/周/月单档） */
    MACD_DC_BREAKOUT("macddcb", "MACD死叉突破", 46),
    /** 超短线 · MACD金叉K突破（Min30） */
    ULTRA_GC_BREAKOUT("ultragc", "MACD金叉K突破", 47),
    /** 凹凸突破（日/周/月单档凸凹边沿破波段 High） */
    WAVE_CC_BREAKOUT("waveccbreak", "凹凸突破", 48),
    /** 日小时组合（日 MACD&gt;0 + Min60 金叉波段 High 突破） */
    DAY_MIN60_COMBO("daymin60", "日小时组合", 49),
    /** 小时周组合（周 MACD&gt;0 + Min60 金叉波段 High 突破） */
    WEEK_MIN60_COMBO("weekmin60", "小时周组合", 50),
    /** 日周组合（周+日 MACD&gt;0 + 日金叉波段 High 突破，信号限末自然周） */
    DAY_WEEK_COMBO("dayweek", "日周组合", 51),
    /** 日月组合（月+日 MACD&gt;0 + 日金叉波段 High 突破，信号限末自然月） */
    DAY_MONTH_COMBO("daymonth", "日月组合", 52),
    /** 分时凹凸突破（Min60/日/周 MACD≥2&gt;0 + 末根 Min60 凸凹边沿突破） */
    MIN60_WAVE_CC_BREAKOUT("min60wavecc", "分时凹凸突破", 53),
    /** 日凹凸突破（日/周/月 MACD≥2&gt;0 + 末根日 K 凸凹边沿突破） */
    DAY_WAVE_CC_BREAKOUT("daywavecc", "日凹凸突破", 54),
    /** @deprecated 已下线 */
    WEEK_WAVE_CC_BREAKOUT("weekwavecc", "周凹凸突破", 55),
    /** @deprecated 已下线 */
    MONTH_WAVE_CC_BREAKOUT("monthwavecc", "月凹凸突破", 56),
    /** 趋势内凹凸突破（趋势 MACD&gt;0 + 信号周期凸凹边沿突破） */
    TREND_WAVE_CC_BREAKOUT("trendwavecc", "趋势内凹凸突破", 57),
    /** 趋势上移（日/周/月 MACD 全 &gt;0 + 日 close &gt; 前一日 low） */
    TREND_LIFT("trendlift", "趋势上移", 58),
    /** 趋势MA（日/周/月 MACD&gt;0 且 close&gt;max(MA5~30) 且收阳，至少 2 档） */
    TREND_MA("trendma", "趋势MA", 59),
    /** 趋势回踩破 Low */
    TREND_RETEST_LOW("trendretestlow", "趋势回踩破Low", 61),
    /** 趋势回踩破 High */
    TREND_RETEST_HIGH("trendretesthigh", "趋势回踩破High", 62),
    /** 趋势中转二阳 */
    TREND_RELAY_2YANG("trendrelay2yang", "趋势中转二阳", 63),
    /** 趋势中转突破前 High */
    TREND_RELAY_PREV_HIGH("trendrelayprevhigh", "趋势中转破前High", 64),
    /** 趋势中转突破末波段 High */
    TREND_RELAY_BAND_HIGH("trendrelaybandhigh", "趋势中转破末High", 65),
    /** 底部波段突破 */
    BOTTOM_BAND_HIGH("bottombandhigh", "底部波段突破", 66),
    /** 底部 High 突破（末前 2 根 K max high） */
    BOTTOM_PREV2_HIGH("bottomprev2high", "底部High突破", 67),
    /** 均线多头突破（MA10&gt;MA20&gt;MA30 + 边沿或开盘突破均线MAX） */
    MA_ALIGN_LIFT("maalignlift", "均线多头突破", 68),
    /** 均线空头突破（MA10&lt;MA20&lt;MA30 + 边沿或开盘突破均线MAX） */
    MA_BEAR_BREAK("mabearbreak", "均线空头突破", 69),
    /** @deprecated 凹波分档突破 · 短线 */
    WAVE_CONCAVE_TIER_SHORT("waveconcavetierShort", "凹波突破短线", 29),
    /** 凹波分档突破 · 中线 */
    /** @deprecated 凹波分档突破 · 中线 */
    WAVE_CONCAVE_TIER_MEDIUM("waveconcavetierMedium", "凹波突破中线", 30),
    /** @deprecated 凹波分档突破 · 长线 */
    WAVE_CONCAVE_TIER_LONG("waveconcavetierLong", "凹波突破长线", 31),
    /** @deprecated 凸波分档突破 · 短线 */
    WAVE_CONVEX_TIER_SHORT("waveconvextierShort", "凸波突破短线", 32),
    /** @deprecated 凸波分档突破 · 中线 */
    WAVE_CONVEX_TIER_MEDIUM("waveconvextierMedium", "凸波突破中线", 33),
    /** @deprecated 凸波分档突破 · 长线 */
    WAVE_CONVEX_TIER_LONG("waveconvextierLong", "凸波突破长线", 34),

    // --- v1 已下线，保留枚举供历史 stock_target 数据兼容 ---
    /** @deprecated 级联梯子突破已下线 */
    CASCADE_LADDER("cladder", "级联梯子突破", 9),
    /** @deprecated 突破 MACD 交叉已下线 */
    BOGO_BREAKOUT("bogo", "突破MACD交叉", 6),
    /** @deprecated 三周期交叉上破已下线 */
    TRENDM("trendm", "三周期交叉上破", 110),
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
        return this == TREND_RETEST_LOW || this == TREND_RETEST_HIGH || this == TREND_RELAY_2YANG
                || this == TREND_RELAY_PREV_HIGH || this == TREND_RELAY_BAND_HIGH
                || this == BOTTOM_BAND_HIGH || this == BOTTOM_PREV2_HIGH
                || this == MA_ALIGN_LIFT || this == MA_BEAR_BREAK;
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

