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
        return this == ULTRA_SHORT || this == TREND_V2 || this == MEDIUM || this == LONG
                || this == FRICTIONLESS_LADDER || this == PILLAR_BREAKOUT || this == CASCADE_BREAKOUT
                || this == LADDER_DIP || this == MACD_EDGE_BREAKOUT
                || this == WAVE_CONVEX || this == WAVE_CONCAVE
                || this == WAVE_CONVEX_DAY || this == WAVE_CONCAVE_DAY
                || this == CASCADE_WAVE_CONVEX || this == CASCADE_WAVE_CONCAVE
                || this == CASCADE_WAVE_CONVEX_DAY || this == CASCADE_WAVE_CONCAVE_DAY
                || this == WAVE_BAND_SHORT || this == WAVE_BAND_MEDIUM
                || this == WAVE_PERIOD_GATE || this == MACD_CROSS_TIER
                || this == CONVEX_LIFT_TIER || this == BODY_BAR_TIER
                || this == MACD_GOLDEN_CROSS || this == MACD_GC_WAVE_HIGH
                || this == MACD_GC_WAVE_HIGH_RETEST || this == MACD_GC_WAVE_HIGH_LIFT
                || this == MACD_DC_BREAKOUT;
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

