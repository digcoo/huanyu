package com.yh.bigdata.tts.common.param;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import lombok.Data;

import com.yh.bigdata.tts.common.param.base.PageQuery;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StockPageQuery extends PageQuery {

    private String code;

	private Boolean isSelectMode;		//是否选股模式
	
	private Boolean isFilterMode;		//是否过滤模式
	
	private String day;
	
	private String strategy;

	private boolean all = false;

    private String trendPeriodType;

    private String opPeriodType;

    private Integer lianBanDays;
    private String trendPeriodTypes;

    /** 金叉策略 · 最低日均成交额（万） */
    private Integer uMinAmountWan;
    /** 金叉策略 · 启用短线（周 MACD&gt;0 + 日金叉） */
    private Boolean uEnableShort;
    /** 金叉策略 · 启用中线（月 MACD&gt;0 + 周金叉） */
    private Boolean uEnableMedium;
    /** 金叉策略 · 启用长线（年 MACD&gt;0 + 月金叉） */
    private Boolean uEnableLong;
    /** 金叉策略 · 最低档位 ALL/S/A/B（S=短线 A=中线 B=长线） */
    private String uTierMin;

    /** @deprecated v3.0 忽略 */
    private Integer uDayLookback;
    /** @deprecated v3.0 忽略 */
    private Double uStrongYangPct;
    /** @deprecated v3.0 忽略 */
    private Integer uWeekContextMin;
    /** @deprecated v3.0 忽略 */
    private Boolean uEnableModeB;
    /** @deprecated v3.0 忽略 */
    private Boolean uEnableModeA;
    /** @deprecated v3.0 忽略 */
    private Boolean uEnableModeBWeak;

    /** 深跌反弹 · 最低日均成交额（万） */
    private Integer rMinAmountWan;
    private Boolean rEnableShort;
    private Boolean rEnableMedium;
    private Boolean rEnableLong;
    /** 深跌反弹 · 最低档位 ALL/S/A/B */
    private String rTierMin;

    /** 预判金叉 · 最低日均成交额（万） */
    private Integer pMinAmountWan;
    private Boolean pEnableShort;
    private Boolean pEnableMedium;
    private Boolean pEnableLong;
    /** 预判金叉 · 最低档位 ALL/S/A/B */
    private String pTierMin;

    /** 周期共振 · 最低日均成交额（万） */
    private Integer cMinAmountWan;
    private Boolean cEnableShort;
    private Boolean cEnableMedium;
    private Boolean cEnableLong;
    /** 周期共振 · 最低档位 ALL/S/A/B */
    private String cTierMin;

    /** 梯子突破 · 最低日均成交额（万） */
    private Integer lMinAmountWan;
    private String lTierMin;
    private Boolean lEnableUltra;
    private Boolean lEnableShort;
    private Boolean lEnableMedium;
    private Boolean lEnableLong;
    /** 30m 大阳线/大涨幅下限（0~1） */
    private Double lMin30BodyPct;
    /** 向前几个交易日（不含当日，1~2） */
    private Integer lMin30PrevDays;
    /** 每交易日最多几根 30m */
    private Integer lMin30BarsPerDay;
    private Double lDayBodyPct;
    private Integer lDayPrevWeeks;
    private Integer lDayBarsPerWeek;
    private Double lWeekBodyPct;
    private Integer lWeekPrevMonths;
    private Integer lWeekBarsPerMonth;
    private Double lMonthBodyPct;
    private Integer lMonthPrevYears;
    private Integer lMonthBarsPerYear;
    /** 长线现价过滤 1=启用 0=关闭 */
    private Integer lLongPriceFilter;

    /** 回踩抬升 · 最低日均成交额（万） */
    private Integer tMinAmountWan;
    private String tTierMin;
    private Boolean tEnableBear;
    private Boolean tEnableBull;
    private Boolean tEnableUltra;
    private Boolean tEnableShort;
    private Boolean tEnableMedium;
    private Boolean tEnableLong;

    /** 金叉二次突破 · 最低日均成交额（万） */
    private Integer g2MinAmountWan;
    private String g2TierMin;
    private Boolean g2EnableShort;
    private Boolean g2EnableMedium;
    private Boolean g2EnableLong;
    private Integer g2LookbackShort;
    private Integer g2LookbackMedium;
    private Integer g2LookbackLong;

    /** 死叉突破 · 最低日均成交额（万） */
    private Integer d2MinAmountWan;
    private String d2TierMin;
    private Boolean d2EnableShort;
    private Boolean d2EnableLong;
    private Integer d2LookbackShort;
    private Integer d2LookbackLong;

    /** 超短线 · 最低日均成交额（万） */
    private Integer ulMinAmountWan;
    /** 超短线 · 月 MACD&gt;0 */
    private Boolean ulRequireMonthMacd;
    /** 超短线 · 周 MACD&gt;0 */
    private Boolean ulRequireWeekMacd;
    /** 超短线 · 日 MACD&gt;0 */
    private Boolean ulRequireDayMacd;
    /** 超短线 · 日 MACD&lt;0 */
    private Boolean ulRequireDayMacdNegative;
    /** 超短线 · 周 MACD&lt;0 */
    private Boolean ulRequireWeekMacdNegative;
    /** 超短线 · 月 MACD&lt;0 */
    private Boolean ulRequireMonthMacdNegative;
    /** 超短线 · 须当前K为突破K（1=是 0=当日有突破K即可） */
    private Boolean ulRequireCurrentBreakout;

    /** 短线 · 最低日均成交额（万） */
    private Integer trMinAmountWan;
    /** 短线 · 基准背景周数（自然周，默认 2） */
    private Integer trPrevWeeks;
    /** 短线 · 须当前日K为突破K */
    private Boolean trRequireCurrentBreakout;
    /** 短线 · 月 MACD&gt;0 */
    private Boolean trRequireMonthMacd;
    /** 短线 · 周 MACD&gt;0 */
    private Boolean trRequireWeekMacd;
    /** 短线 · 日 MACD&gt;0 */
    private Boolean trRequireDayMacd;
    /** 短线 · 周K MACD 金叉 */
    private Boolean trRequireWeekGoldenCross;
    /** 短线 · 须同时满足超短 30m 突破 */
    private Boolean trRequireUltra;

    /** 中线 · 最低日均成交额（万） */
    private Integer mdMinAmountWan;
    /** 中线 · 基准背景月数（自然月，默认 2） */
    private Integer mdPrevMonths;
    /** 中线 · 须当前周K为突破K */
    private Boolean mdRequireCurrentBreakout;
    /** 中线 · 月 MACD&gt;0 */
    private Boolean mdRequireMonthMacd;
    /** 中线 · 年 MACD&gt;0 */
    private Boolean mdRequireYearMacd;
    /** 中线 · 月K MACD 金叉 */
    private Boolean mdRequireMonthGoldenCross;
    /** 中线 · 须同时满足超短 30m 突破 */
    private Boolean mdRequireUltra;

    /** 长线 · 最低日均成交额（万） */
    private Integer lgMinAmountWan;
    /** 长线 · 基准背景年数（自然年，默认 2） */
    private Integer lgPrevYears;
    /** 长线 · 须当前月K为突破K */
    private Boolean lgRequireCurrentBreakout;
    /** 长线 · 年 MACD&gt;0 */
    private Boolean lgRequireYearMacd;
    /** 长线 · 月 MACD&gt;0 */
    private Boolean lgRequireMonthMacd;
    /** 长线 · 年K MACD 金叉 */
    private Boolean lgRequireYearGoldenCross;
    /** 长线 · 须同时满足超短 30m 突破 */
    private Boolean lgRequireUltra;

    /** 跨周期内柱子上移 · 档位 short/medium/long（同梯子策略） */
    private String nrfActiveTier;

    /** 柱子内上移 · 启用日 K */
    private Boolean piEnableDay;
    /** 柱子内上移 · 启用周 K */
    private Boolean piEnableWeek;
    /** 柱子内上移 · 启用月 K */
    private Boolean piEnableMonth;
    /** 柱子内上移 · 日 K lookback */
    private Integer piLookbackDay;
    /** 柱子内上移 · 周 K lookback */
    private Integer piLookbackWeek;
    /** 柱子内上移 · 月 K lookback */
    private Integer piLookbackMonth;
    /** 柱子内上移 · 须 min30 梯子 */
    private Boolean piRequireUltra;
    /** 柱子内上移 · 最低成交额（万） */
    private Integer piMinAmountWan;

    /** 级联交叉突破 · 双低支撑门 */
    private Boolean caEnableDualLowGate;
    /** 级联交叉突破 · 无阻力 MACD 门 */
    private Boolean caEnableMacdGate;
    /** 级联交叉突破 · MACD 死叉高门 */
    private Boolean caEnableMacdDcHighGate;
    /** 级联交叉突破 · MACD 交叉 low 门 */
    private Boolean caEnableCrossLowGate;
    /** 级联交叉突破 · 高点递进门 */
    private Boolean caEnableBarHighGate;
    /** 级联交叉突破 · 全阳门 */
    private Boolean caEnableAllYangGate;
    /** 级联交叉突破 · 突破日基准 */
    private Boolean caEnableDay;
    /** 级联交叉突破 · 突破周基准 */
    private Boolean caEnableWeek;
    /** 级联交叉突破 · 突破月基准 */
    private Boolean caEnableMonth;
    /** 级联交叉突破 · 日 K lookback */
    private Integer caLookbackDay;
    /** 级联交叉突破 · 周 K lookback */
    private Integer caLookbackWeek;
    /** 级联交叉突破 · 月 K lookback */
    private Integer caLookbackMonth;
    /** 级联交叉突破 · 备选突破路径（前 K high 边沿 + 当前档 close &gt; 基准 low，与基准 high 并集） */
    private Boolean caEnableAltBreakout;
    /** 级联交叉突破 · 须 min30 跨日桶突破 */
    private Boolean caRequireUltra;
    /** 级联交叉突破 · 最低成交额（万） */
    private Integer caMinAmountWan;

    /** 级联梯子探底回升 · 双低支撑门 */
    private Boolean ldEnableDualLowGate;
    /** 级联梯子探底回升 · 无阻力 MACD 门 */
    private Boolean ldEnableMacdGate;
    /** 级联梯子探底回升 · MACD 死叉高门 */
    private Boolean ldEnableMacdDcHighGate;
    /** 级联梯子探底回升 · MACD 交叉 low 门 */
    private Boolean ldEnableCrossLowGate;
    /** 级联梯子探底回升 · 高点递进门 */
    private Boolean ldEnableBarHighGate;
    /** 级联梯子探底回升 · 全阳门 */
    private Boolean ldEnableAllYangGate;
    /** 级联梯子探底回升 · 日档 */
    private Boolean ldEnableDay;
    /** 级联梯子探底回升 · 周档 */
    private Boolean ldEnableWeek;
    /** 级联梯子探底回升 · 月档 */
    private Boolean ldEnableMonth;
    /** 级联梯子探底回升 · 日 K lookback */
    private Integer ldLookbackDay;
    /** 级联梯子探底回升 · 周 K lookback */
    private Integer ldLookbackWeek;
    /** 级联梯子探底回升 · 月 K lookback */
    private Integer ldLookbackMonth;
    /** 级联梯子探底回升 · 须 min30 跨日桶突破 */
    private Boolean ldRequireUltra;
    /** 级联梯子探底回升 · 最低成交额（万） */
    private Integer ldMinAmountWan;

    /** MACD 交叉边沿突破 · 双低支撑门 */
    private Boolean meEnableDualLowGate;
    /** MACD 交叉边沿突破 · 无阻力 MACD 门 */
    private Boolean meEnableMacdGate;
    /** MACD 交叉边沿突破 · MACD 死叉高门 */
    private Boolean meEnableMacdDcHighGate;
    /** MACD 交叉边沿突破 · MACD 交叉 low 门 */
    private Boolean meEnableCrossLowGate;
    /** MACD 交叉边沿突破 · 高点递进门 */
    private Boolean meEnableBarHighGate;
    /** MACD 交叉边沿突破 · 全阳门 */
    private Boolean meEnableAllYangGate;
    /** MACD 交叉边沿突破 · Min30 基准 */
    private Boolean meEnableMin30;
    /** MACD 交叉边沿突破 · 日基准 */
    private Boolean meEnableDay;
    /** MACD 交叉边沿突破 · 周基准 */
    private Boolean meEnableWeek;
    /** MACD 交叉边沿突破 · 月基准 */
    private Boolean meEnableMonth;
    /** MACD 交叉边沿突破 · 年基准 */
    private Boolean meEnableYear;
    /** MACD 交叉边沿突破 · Min30 lookback */
    private Integer meLookbackMin30;
    /** MACD 交叉边沿突破 · 日 K lookback */
    private Integer meLookbackDay;
    /** MACD 交叉边沿突破 · 周 K lookback */
    private Integer meLookbackWeek;
    /** MACD 交叉边沿突破 · 月 K lookback */
    private Integer meLookbackMonth;
    /** MACD 交叉边沿突破 · 年 K lookback */
    private Integer meLookbackYear;
    /** MACD 交叉边沿突破 · 最低成交额（万） */
    private Integer meMinAmountWan;

    /** 凸波段突破 · 全阳门 */
    private Boolean wcvEnableAllYangGate;
    /** 凸波段突破 · Min30 突破门 */
    private Boolean wcvEnableMin30Gate;
    /** 凸波段突破 · 末阳顶突破 */
    private Boolean wcvEnableLastHighBreak;
    /** 凸波段突破 · 末阳中位突破 */
    private Boolean wcvEnableLastMedianBreak;
    /** 凸波段突破 · 末阳底突破 */
    private Boolean wcvEnableLastLowBreak;
    /** 凸波段突破 · 日档 */
    private Boolean wcvEnableDay;
    /** 凸波段突破 · 周档 */
    private Boolean wcvEnableWeek;
    /** 凸波段突破 · 月档 */
    private Boolean wcvEnableMonth;
    /** 凸波段突破 · 日 K lookback */
    private Integer wcvLookbackDay;
    /** 凸波段突破 · 周 K lookback */
    private Integer wcvLookbackWeek;
    /** 凸波段突破 · 月 K lookback */
    private Integer wcvLookbackMonth;
    /** 凸波段突破 · 最低成交额（万） */
    private Integer wcvMinAmountWan;

    /** 凹波段突破 · 全阳门 */
    private Boolean wccEnableAllYangGate;
    /** 凹波段突破 · Min30 突破门 */
    private Boolean wccEnableMin30Gate;
    /** 凹波段突破 · 末阳顶突破 */
    private Boolean wccEnableLastHighBreak;
    /** 凹波段突破 · 末阳中位突破 */
    private Boolean wccEnableLastMedianBreak;
    /** 凹波段突破 · 末阳底突破 */
    private Boolean wccEnableLastLowBreak;
    /** 凹波段突破 · 日档 */
    private Boolean wccEnableDay;
    /** 凹波段突破 · 周档 */
    private Boolean wccEnableWeek;
    /** 凹波段突破 · 月档 */
    private Boolean wccEnableMonth;
    /** 凹波段突破 · 日 K lookback */
    private Integer wccLookbackDay;
    /** 凹波段突破 · 周 K lookback */
    private Integer wccLookbackWeek;
    /** 凹波段突破 · 月 K lookback */
    private Integer wccLookbackMonth;
    /** 凹波段突破 · 最低成交额（万） */
    private Integer wccMinAmountWan;

    /** 凸波段日突破 · 全阳门 */
    private Boolean wcvdEnableAllYangGate;
    /** 凸波段日突破 · Min30 突破门 */
    private Boolean wcvdEnableMin30Gate;
    /** 凸波段日突破 · 末阳顶突破 */
    private Boolean wcvdEnableLastHighBreak;
    /** 凸波段日突破 · 末阳中位突破 */
    private Boolean wcvdEnableLastMedianBreak;
    /** 凸波段日突破 · 末阳底突破 */
    private Boolean wcvdEnableLastLowBreak;
    /** 凸波段日突破 · 日档 */
    private Boolean wcvdEnableDay;
    /** 凸波段日突破 · 周档 */
    private Boolean wcvdEnableWeek;
    /** 凸波段日突破 · 月档 */
    private Boolean wcvdEnableMonth;
    /** 凸波段日突破 · 日 K lookback */
    private Integer wcvdLookbackDay;
    /** 凸波段日突破 · 周 K lookback */
    private Integer wcvdLookbackWeek;
    /** 凸波段日突破 · 月 K lookback */
    private Integer wcvdLookbackMonth;
    /** 凸波段日突破 · 最低成交额（万） */
    private Integer wcvdMinAmountWan;

    /** 凹波段日突破 · 全阳门 */
    private Boolean wccdEnableAllYangGate;
    /** 凹波段日突破 · Min30 突破门 */
    private Boolean wccdEnableMin30Gate;
    /** 凹波段日突破 · 末阳顶突破 */
    private Boolean wccdEnableLastHighBreak;
    /** 凹波段日突破 · 末阳中位突破 */
    private Boolean wccdEnableLastMedianBreak;
    /** 凹波段日突破 · 末阳底突破 */
    private Boolean wccdEnableLastLowBreak;
    /** 凹波段日突破 · 日档 */
    private Boolean wccdEnableDay;
    /** 凹波段日突破 · 周档 */
    private Boolean wccdEnableWeek;
    /** 凹波段日突破 · 月档 */
    private Boolean wccdEnableMonth;
    /** 凹波段日突破 · 日 K lookback */
    private Integer wccdLookbackDay;
    /** 凹波段日突破 · 周 K lookback */
    private Integer wccdLookbackWeek;
    /** 凹波段日突破 · 月 K lookback */
    private Integer wccdLookbackMonth;
    /** 凹波段日突破 · 最低成交额（万） */
    private Integer wccdMinAmountWan;

    /** 级联 MACD 凸波段 · 全阳门 */
    private Boolean cwcvEnableAllYangGate;
    /** 级联 MACD 凸波段 · Min30 突破门 */
    private Boolean cwcvEnableMin30Gate;
    /** 级联 MACD 凸波段 · 末阳低门（日周月收盘 &gt; 末阳 low） */
    private Boolean cwcvEnableBandLastYangLowGate;
    /** 级联 MACD 凸波段 · 日周月趋势门 */
    private Boolean cwcvEnableYangBandTrendGate;
    /** 级联 MACD 凸波段 · 前波段末阳 high 突破 */
    private Boolean cwcvEnablePrevBandBreak;
    /** 级联 MACD 凸波段 · 日档 */
    private Boolean cwcvEnableDay;
    /** 级联 MACD 凸波段 · 周档 */
    private Boolean cwcvEnableWeek;
    /** 级联 MACD 凸波段 · 月档 */
    private Boolean cwcvEnableMonth;
    /** 级联 MACD 凸波段 · 日 K lookback */
    private Integer cwcvLookbackDay;
    /** 级联 MACD 凸波段 · 周 K lookback */
    private Integer cwcvLookbackWeek;
    /** 级联 MACD 凸波段 · 月 K lookback */
    private Integer cwcvLookbackMonth;
    /** 级联 MACD 凸波段 · 年 K lookback（月档级联年 MACD） */
    private Integer cwcvLookbackYear;
    /** 级联 MACD 凸波段 · 最低成交额（万） */
    private Integer cwcvMinAmountWan;

    /** 级联 MACD 凹波段 · 全阳门 */
    private Boolean cwcavEnableAllYangGate;
    /** 级联 MACD 凹波段 · Min30 突破门 */
    private Boolean cwcavEnableMin30Gate;
    /** 级联 MACD 凹波段 · 末阳低门 */
    private Boolean cwcavEnableBandLastYangLowGate;
    /** 级联 MACD 凹波段 · 日周月趋势门 */
    private Boolean cwcavEnableYangBandTrendGate;
    /** 级联 MACD 凹波段 · 前波段末阳 high 突破 */
    private Boolean cwcavEnablePrevBandBreak;
    /** 级联 MACD 凹波段 · 日档 */
    private Boolean cwcavEnableDay;
    /** 级联 MACD 凹波段 · 周档 */
    private Boolean cwcavEnableWeek;
    /** 级联 MACD 凹波段 · 月档 */
    private Boolean cwcavEnableMonth;
    /** 级联 MACD 凹波段 · 日 K lookback */
    private Integer cwcavLookbackDay;
    /** 级联 MACD 凹波段 · 周 K lookback */
    private Integer cwcavLookbackWeek;
    /** 级联 MACD 凹波段 · 月 K lookback */
    private Integer cwcavLookbackMonth;
    /** 级联 MACD 凹波段 · 年 K lookback */
    private Integer cwcavLookbackYear;
    /** 级联 MACD 凹波段 · 最低成交额（万） */
    private Integer cwcavMinAmountWan;

    /** 级联 MACD 凸波段日 · 全阳门 */
    private Boolean cwcvdEnableAllYangGate;
    /** 级联 MACD 凸波段日 · Min30 门 */
    private Boolean cwcvdEnableMin30Gate;
    /** 级联 MACD 凸波段日 · 末阳低门 */
    private Boolean cwcvdEnableBandLastYangLowGate;
    /** 级联 MACD 凸波段日 · 日周月趋势门 */
    private Boolean cwcvdEnableYangBandTrendGate;
    /** 级联 MACD 凸波段日 · 前波段末阳顶突破 */
    private Boolean cwcvdEnablePrevBandBreak;
    /** 级联 MACD 凸波段日 · 日 K 波段 */
    private Boolean cwcvdEnableDay;
    /** 级联 MACD 凸波段日 · 周 K 波段 */
    private Boolean cwcvdEnableWeek;
    /** 级联 MACD 凸波段日 · 月 K 波段 */
    private Boolean cwcvdEnableMonth;
    /** 级联 MACD 凸波段日 · 日 K lookback */
    private Integer cwcvdLookbackDay;
    /** 级联 MACD 凸波段日 · 周 K lookback */
    private Integer cwcvdLookbackWeek;
    /** 级联 MACD 凸波段日 · 月 K lookback */
    private Integer cwcvdLookbackMonth;
    /** 级联 MACD 凸波段日 · 年 K lookback */
    private Integer cwcvdLookbackYear;
    /** 级联 MACD 凸波段日 · 最低成交额（万） */
    private Integer cwcvdMinAmountWan;

    /** 级联 MACD 凹波段日 · 全阳门 */
    private Boolean cwcadEnableAllYangGate;
    /** 级联 MACD 凹波段日 · Min30 门 */
    private Boolean cwcadEnableMin30Gate;
    /** 级联 MACD 凹波段日 · 末阳低门 */
    private Boolean cwcadEnableBandLastYangLowGate;
    /** 级联 MACD 凹波段日 · 日周月趋势门 */
    private Boolean cwcadEnableYangBandTrendGate;
    /** 级联 MACD 凹波段日 · 前波段末阳顶突破 */
    private Boolean cwcadEnablePrevBandBreak;
    /** 级联 MACD 凹波段日 · 日 K 波段 */
    private Boolean cwcadEnableDay;
    /** 级联 MACD 凹波段日 · 周 K 波段 */
    private Boolean cwcadEnableWeek;
    /** 级联 MACD 凹波段日 · 月 K 波段 */
    private Boolean cwcadEnableMonth;
    /** 级联 MACD 凹波段日 · 日 K lookback */
    private Integer cwcadLookbackDay;
    /** 级联 MACD 凹波段日 · 周 K lookback */
    private Integer cwcadLookbackWeek;
    /** 级联 MACD 凹波段日 · 月 K lookback */
    private Integer cwcadLookbackMonth;
    /** 级联 MACD 凹波段日 · 年 K lookback */
    private Integer cwcadLookbackYear;
    /** 级联 MACD 凹波段日 · 最低成交额（万） */
    private Integer cwcadMinAmountWan;

    /** 波段策略 · 日 K lookback */
    private Integer wbLookbackDay;
    /** 波段策略 · 周 K lookback */
    private Integer wbLookbackWeek;
    /** 波段策略 · 月 K lookback */
    private Integer wbLookbackMonth;
    /** 波段策略 · 最低成交额（万） */
    private Integer wbMinAmountWan;

    /** 波段策略短线 · 日 K lookback */
    private Integer wbsLookbackDay;
    /** 波段策略短线 · 周 K lookback */
    private Integer wbsLookbackWeek;
    /** 波段策略短线 · 最低成交额（万） */
    private Integer wbsMinAmountWan;

    /** 波段策略中线 · 日 K lookback */
    private Integer wbmLookbackDay;
    /** 波段策略中线 · 月 K lookback */
    private Integer wbmLookbackMonth;
    /** 波段策略中线 · 最低成交额（万） */
    private Integer wbmMinAmountWan;

    /** 形态门 · 档位 day/week/month */
    private String wpgTier;
    /** 形态门 · 日 K lookback */
    private Integer wpgLookbackDay;
    /** 形态门 · 周 K lookback */
    private Integer wpgLookbackWeek;
    /** 形态门 · 月 K lookback */
    private Integer wpgLookbackMonth;
    /** 形态门 · 启用成交额过滤 */
    private Boolean wpgEnableMinAmountFilter;
    /** 形态门 · close &gt; max(末/次波段 low) */
    private Boolean wpgEnableMaxBandLowGate;
    /** 形态门 · 凹波段突破末波段 high */
    private Boolean wpgEnableConcaveBreakout;
    /** 形态门 · 凸波段突破前 K high */
    private Boolean wpgEnableConvexBreakout;
    /** 形态门 · 上级周期 min 波段 low 门（日→周、周→月、月→年） */
    private Boolean wpgEnableUpperPeriodMinBandLowGate;
    /** 形态门 · 本档 MACD 柱 > 0 */
    private Boolean wpgEnableTierMacdPositive;
    /** 形态门 · 周/月凹凸形态门 */
    private Boolean wpgEnableWeekMonthBandShapeGate;
    /** 形态门 · 周/月收盘价 > 末波段底 */
    private Boolean wpgEnableWeekMonthBandLowGate;
    /** 形态门 · 年/周/月须全部收阳 */
    private Boolean wpgEnableYearWeekMonthYangGate;
    /** 形态门 · 最低成交额（万，启用过滤时生效） */
    private Integer wpgMinAmountWan;
    /** 形态门 · 年 K lookback（月档上级周期门） */
    private Integer wpgLookbackYear;
    /** 同档 MACD 交叉突破 · 档位 day/week/month */
    private String mctTier;
    /** 同档 MACD 交叉突破 · 日 K lookback */
    private Integer mctLookbackDay;
    /** 同档 MACD 交叉突破 · 周 K lookback */
    private Integer mctLookbackWeek;
    /** 同档 MACD 交叉突破 · 月 K lookback */
    private Integer mctLookbackMonth;
    /** 同档 MACD 交叉突破 · 基准交叉 K：金叉 */
    private Boolean mctEnableGoldenCross;
    /** 同档 MACD 交叉突破 · 基准交叉 K：死叉 */
    private Boolean mctEnableDeathCross;
    /** 同档 MACD 交叉突破 · 当前金叉+上涨率路径 */
    private Boolean mctEnableGoldenCrossRiseGate;
    /** 凸波段上移 · 档位 week/month/year */
    private String cltTier;
    /** 凸波段上移 · 日 K lookback */
    private Integer cltLookbackDay;
    /** 柱子策略 · 档位 day/week/month */
    private String bbtTier;
    /** 柱子策略 · 向前检视 K 根数（不含信号 K） */
    private Integer bbtLookbackBars;
    /** MACD金叉 · 档位 day/week/month */
    private String mgcTier;
    /** MACD金叉 · 启用近6日日均成交额门 */
    private Boolean mgcEnableMinAmountFilter;
    /** MACD金叉 · 最低日均成交额（万） */
    private Integer mgcMinAmountWan;
    /** MACD金叉 · 启用末 K 上涨率门 */
    private Boolean mgcEnableSignalRiseGate;
    /** MACD金叉 · 末 K 上涨率阈值（%，如 3 表示 3%） */
    private Double mgcSignalRisePct;
    /** MACD金叉 · 启用近 N 根历史上涨率门 */
    private Boolean mgcEnableHistoryRiseGate;
    /** MACD金叉 · 历史检视 K 根数（不含末 K） */
    private Integer mgcHistoryLookbackBars;
    /** MACD金叉 · 历史上涨率阈值（%，如 3 表示 3%） */
    private Double mgcHistoryRisePct;
    /** MACD金叉波段High突破 · 档位 day/week/month */
    private String mgcwhTier;
    /** MACD金叉波段High突破 · 日 K lookback */
    private Integer mgcwhLookbackDay;
    /** MACD金叉波段High突破 · 周 K lookback */
    private Integer mgcwhLookbackWeek;
    /** MACD金叉波段High突破 · 月 K lookback */
    private Integer mgcwhLookbackMonth;
    /** MACD金叉波段High突破 · 启用近6日日均成交额门 */
    private Boolean mgcwhEnableMinAmountFilter;
    /** MACD金叉波段High突破 · 最低日均成交额（万） */
    private Integer mgcwhMinAmountWan;
    /** MACD金叉波段High突破 · 启用末 K 上涨率门 */
    private Boolean mgcwhEnableSignalRiseGate;
    /** MACD金叉波段High突破 · 末 K 上涨率阈值（%，如 3 表示 3%） */
    private Double mgcwhSignalRisePct;
    /** MACD金叉波段High回踩 · 档位 day/week/month */
    private String mgcwhrTier;
    /** MACD金叉波段High回踩 · 日 K lookback */
    private Integer mgcwhrLookbackDay;
    /** MACD金叉波段High回踩 · 周 K lookback */
    private Integer mgcwhrLookbackWeek;
    /** MACD金叉波段High回踩 · 月 K lookback */
    private Integer mgcwhrLookbackMonth;
    /** MACD金叉波段High回踩 · 启用近6日日均成交额门 */
    private Boolean mgcwhrEnableMinAmountFilter;
    /** MACD金叉波段High回踩 · 最低日均成交额（万） */
    private Integer mgcwhrMinAmountWan;
    /** MACD金叉波段High上移 · 档位 day/week/month */
    private String mgcwhuTier;
    /** MACD金叉波段High上移 · 日 K lookback */
    private Integer mgcwhuLookbackDay;
    /** MACD金叉波段High上移 · 周 K lookback */
    private Integer mgcwhuLookbackWeek;
    /** MACD金叉波段High上移 · 月 K lookback */
    private Integer mgcwhuLookbackMonth;
    /** MACD金叉波段High上移 · 启用近6日日均成交额门 */
    private Boolean mgcwhuEnableMinAmountFilter;
    /** MACD金叉波段High上移 · 最低日均成交额（万） */
    private Integer mgcwhuMinAmountWan;
    /** MACD金叉波段High上移 · 启用末 K 上涨率门 */
    private Boolean mgcwhuEnableSignalRiseGate;
    /** MACD金叉波段High上移 · 末 K 上涨率阈值（%，如 3 表示 3%） */
    private Double mgcwhuSignalRisePct;
    /** MACD死叉突破 · 档位 day/week/month */
    private String mdcbTier;
    /** MACD死叉突破 · 日 K lookback */
    private Integer mdcbLookbackDay;
    /** MACD死叉突破 · 周 K lookback */
    private Integer mdcbLookbackWeek;
    /** MACD死叉突破 · 月 K lookback */
    private Integer mdcbLookbackMonth;
    /** MACD死叉突破 · 启用近6日日均成交额门 */
    private Boolean mdcbEnableMinAmountFilter;
    /** MACD死叉突破 · 最低日均成交额（万） */
    private Integer mdcbMinAmountWan;
    /** MACD死叉突破 · 启用末 K 上涨率门 */
    private Boolean mdcbEnableSignalRiseGate;
    /** MACD死叉突破 · 末 K 上涨率阈值（%，如 3 表示 3%） */
    private Double mdcbSignalRisePct;
    /** 凹凸突破 · 档位 day/week/month */
    private String wccbTier;
    /** 凹凸突破 · 日 K lookback */
    private Integer wccbLookbackDay;
    /** 凹凸突破 · 周 K lookback */
    private Integer wccbLookbackWeek;
    /** 凹凸突破 · 月 K lookback */
    private Integer wccbLookbackMonth;
    /** 凹凸突破 · 启用近6日日均成交额门 */
    private Boolean wccbEnableMinAmountFilter;
    /** 凹凸突破 · 最低日均成交额（万） */
    private Integer wccbMinAmountWan;
    /** 凹凸突破 · 启用末 K 上涨率门 */
    private Boolean wccbEnableSignalRiseGate;
    /** 凹凸突破 · 末 K 上涨率阈值（%，如 3 表示 3%） */
    private Double wccbSignalRisePct;
    /** 多周期 MACD&gt;0 门 · 日档 */
    private Boolean mgRequireDayMacd;
    /** 多周期 MACD&gt;0 门 · 周档 */
    private Boolean mgRequireWeekMacd;
    /** 多周期 MACD&gt;0 门 · 月档 */
    private Boolean mgRequireMonthMacd;
    /** 多周期 MACD&gt;0 门 · Min60 档（macdgcwhu） */
    private Boolean mgRequireMin60Macd;
    /** MACD金叉K突破 · 信号日前 N 个交易日 */
    private Integer ulgcPrevDays;
    /** MACD金叉K突破 · 每日最多 Min30 根 */
    private Integer ulgcMaxBarsPerDay;
    /** MACD金叉K突破 · 金叉回溯根数 */
    private Integer ulgcGcLookbackBars;
    /** MACD金叉K突破 · 信号 Min30 涨幅阈值（%，如 1 表示 1%） */
    private Double ulgcSignalRisePct;
    /** 日小时组合 · 信号日前 N 个交易日（Min60 背景） */
    private Integer dm60PrevDays;
    /** 日小时组合 · 每日最多 Min60 根 */
    private Integer dm60MaxBarsPerDay;
    /** 日小时组合 · 金叉回溯根数 */
    private Integer dm60GcLookbackBars;
    /** 日小时组合 · 启用近6日日均成交额门 */
    private Boolean dm60EnableMinAmountFilter;
    /** 日小时组合 · 最低日均成交额（万） */
    private Integer dm60MinAmountWan;
    /** 日小时组合 · 启用突破 K 涨幅门 */
    private Boolean dm60EnableSignalRiseGate;
    /** 日小时组合 · 突破 K 涨幅阈值（%，如 1 表示 1%） */
    private Double dm60SignalRisePct;
    /** 小时周组合 · 信号周前 N 个自然周（Min60 背景） */
    private Integer wm60PrevWeeks;
    /** 小时周组合 · 每周最多 Min60 根 */
    private Integer wm60MaxBarsPerWeek;
    /** 小时周组合 · 金叉回溯根数 */
    private Integer wm60GcLookbackBars;
    /** 小时周组合 · 启用近6日日均成交额门 */
    private Boolean wm60EnableMinAmountFilter;
    /** 小时周组合 · 最低日均成交额（万） */
    private Integer wm60MinAmountWan;
    /** 小时周组合 · 启用突破 K 涨幅门 */
    private Boolean wm60EnableSignalRiseGate;
    /** 小时周组合 · 突破 K 涨幅阈值（%，如 1 表示 1%） */
    private Double wm60SignalRisePct;
    /** 日周组合 · 信号周前 N 个自然周（日 K 背景） */
    private Integer dwPrevWeeks;
    /** 日周组合 · 每自然周最多日 K 根 */
    private Integer dwMaxBarsPerWeek;
    /** 日周组合 · 金叉回溯根数 */
    private Integer dwGcLookbackBars;
    /** 日周组合 · 启用近6日日均成交额门 */
    private Boolean dwEnableMinAmountFilter;
    /** 日周组合 · 最低日均成交额（万） */
    private Integer dwMinAmountWan;
    /** 日周组合 · 启用突破 K 涨幅门 */
    private Boolean dwEnableSignalRiseGate;
    /** 日周组合 · 突破 K 涨幅阈值（%，如 1 表示 1%） */
    private Double dwSignalRisePct;
    /** 日月组合 · 信号月前 N 个自然月（日 K 背景） */
    private Integer dmonPrevMonths;
    /** 日月组合 · 每自然月最多日 K 根 */
    private Integer dmonMaxBarsPerMonth;
    /** 日月组合 · 金叉回溯根数 */
    private Integer dmonGcLookbackBars;
    /** 日月组合 · 启用近6日日均成交额门 */
    private Boolean dmonEnableMinAmountFilter;
    /** 日月组合 · 最低日均成交额（万） */
    private Integer dmonMinAmountWan;
    /** 日月组合 · 启用突破 K 涨幅门 */
    private Boolean dmonEnableSignalRiseGate;
    /** 日月组合 · 突破 K 涨幅阈值（%，如 1 表示 1%） */
    private Double dmonSignalRisePct;
    /** 小时凹凸突破 · 信号日前 N 个交易日（Min60 背景） */
    private Integer m60wccbPrevDays;
    /** 小时凹凸突破 · 每日最多 Min60 根 */
    private Integer m60wccbMaxBarsPerDay;
    /** 小时凹凸突破 · Min60 波段回溯根数 */
    private Integer m60wccbLookbackBars;
    /** 小时凹凸突破 · 启用近6日日均成交额门 */
    private Boolean m60wccbEnableMinAmountFilter;
    /** 小时凹凸突破 · 最低日均成交额（万） */
    private Integer m60wccbMinAmountWan;
    /** 小时凹凸突破 · 启用突破 K 涨幅门 */
    private Boolean m60wccbEnableSignalRiseGate;
    /** 小时凹凸突破 · 突破 K 涨幅阈值（%，如 1 表示 1%） */
    private Double m60wccbSignalRisePct;
    /** 小时凹凸突破 · 须末根 Min60 K 为突破 K（0=当日任一根满足即可） */
    private Boolean m60wccbRequireCurrentBreakout;
    /** 日凹凸突破 · 信号日前 N 个交易日（Min60 背景） */
    private Integer dwccbPrevDays;
    /** 日凹凸突破 · 每日最多 Min60 根 */
    private Integer dwccbMaxBarsPerDay;
    /** 日凹凸突破 · Min60 波段回溯根数 */
    private Integer dwccbLookbackBars;
    /** 日凹凸突破 · 启用近6日日均成交额门 */
    private Boolean dwccbEnableMinAmountFilter;
    /** 日凹凸突破 · 最低日均成交额（万） */
    private Integer dwccbMinAmountWan;
    /** 日凹凸突破 · 启用突破 K 涨幅门 */
    private Boolean dwccbEnableSignalRiseGate;
    /** 日凹凸突破 · 突破 K 涨幅阈值（%，如 1 表示 1%） */
    private Double dwccbSignalRisePct;
    /** 日凹凸突破 · 须末根 Min60 K 为突破 K（0=当日任一根满足即可） */
    private Boolean dwccbRequireCurrentBreakout;
    /** 周凹凸突破 · 周 K 波段回溯根数 */
    private Integer wwccbLookbackBars;
    /** 周凹凸突破 · 启用近6日日均成交额门 */
    private Boolean wwccbEnableMinAmountFilter;
    /** 周凹凸突破 · 最低日均成交额（万） */
    private Integer wwccbMinAmountWan;
    /** 周凹凸突破 · 启用突破 K 涨幅门 */
    private Boolean wwccbEnableSignalRiseGate;
    /** 周凹凸突破 · 突破 K 涨幅阈值（%，如 1 表示 1%） */
    private Double wwccbSignalRisePct;
    /** 周凹凸突破 · 回踩 low 与次波段 High 最大相对差值（%，如 1 表示 1%） */
    private Double wwccbMaxRetestGapPct;
    /** 月凹凸突破 · 月 K 波段回溯根数 */
    private Integer mwccbLookbackBars;
    /** 月凹凸突破 · 启用近6日日均成交额门 */
    private Boolean mwccbEnableMinAmountFilter;
    /** 月凹凸突破 · 最低日均成交额（万） */
    private Integer mwccbMinAmountWan;
    /** 月凹凸突破 · 启用突破 K 涨幅门 */
    private Boolean mwccbEnableSignalRiseGate;
    /** 月凹凸突破 · 突破 K 涨幅阈值（%，如 1 表示 1%） */
    private Double mwccbSignalRisePct;
    /** 月凹凸突破 · 回踩 low 与次波段 High 最大相对差值（%，如 1 表示 1%） */
    private Double mwccbMaxRetestGapPct;
    /** 趋势内凹凸突破 · 信号周期 min60/day/week */
    private String twccbSignalPeriod;
    /** 趋势内凹凸突破 · Min60 信号日前 N 个交易日 */
    private Integer twccbPrevDays;
    /** 趋势内凹凸突破 · Min60 每日最多根数 */
    private Integer twccbMaxBarsPerDay;
    /** 趋势内凹凸突破 · 波段回溯根数 */
    private Integer twccbLookbackBars;
    /** 趋势内凹凸突破 · 启用近6日日均成交额门 */
    private Boolean twccbEnableMinAmountFilter;
    /** 趋势内凹凸突破 · 最低日均成交额（万） */
    private Integer twccbMinAmountWan;
    /** 趋势内凹凸突破 · 启用信号 K 涨幅门 */
    private Boolean twccbEnableSignalRiseGate;
    /** 趋势内凹凸突破 · 信号 K 涨幅阈值（%） */
    private Double twccbSignalRisePct;
    /** 趋势内凹凸突破 · 趋势门 日 MACD&gt;0 */
    private Boolean twccbRequireDayMacd;
    /** 趋势内凹凸突破 · 趋势门 周 MACD&gt;0 */
    private Boolean twccbRequireWeekMacd;
    /** 趋势内凹凸突破 · 趋势门 月 MACD&gt;0 */
    private Boolean twccbRequireMonthMacd;
    /** 趋势上移 · 启用近6日日均成交额门 */
    private Boolean tlEnableMinAmountFilter;
    /** 趋势上移 · 最低日均成交额（万） */
    private Integer tlMinAmountWan;
    /** 趋势MA · 启用近6日日均成交额门 */
    private Boolean tmaEnableMinAmountFilter;
    /** 趋势MA · 最低日均成交额（万） */
    private Integer tmaMinAmountWan;
    /** 趋势回踩破 Low · 档位 day/week/month */
    private String trlTier;
    /** 趋势回踩破 Low · 启用成交额门 */
    private Boolean trlEnableMinAmountFilter;
    /** 趋势回踩破 Low · 最低日均成交额（万） */
    private Integer trlMinAmountWan;
    /** 趋势回踩破 High · 档位 day/week/month */
    private String trhTier;
    /** 趋势回踩破 High · 启用成交额门 */
    private Boolean trhEnableMinAmountFilter;
    /** 趋势回踩破 High · 最低日均成交额（万） */
    private Integer trhMinAmountWan;
    /** 底部波段突破 · 档位 */
    private String bbhTier;
    /** 底部波段突破 · 启用成交额门 */
    private Boolean bbhEnableMinAmountFilter;
    /** 底部波段突破 · 最低日均成交额（万） */
    private Integer bbhMinAmountWan;
    /** 底部 High 突破 · 档位 */
    private String bp2hTier;
    /** 底部 High 突破 · 启用成交额门 */
    private Boolean bp2hEnableMinAmountFilter;
    /** 底部 High 突破 · 最低日均成交额（万） */
    private Integer bp2hMinAmountWan;
    /** MA多头3M突破 · 档位 min60/day/week/month */
    private String m3mTier;
    /** MA多头3M突破 · 启用成交额门 */
    private Boolean m3mEnableMinAmountFilter;
    /** MA多头3M突破 · 最低日均成交额（万） */
    private Integer m3mMinAmountWan;
    /** MA多头破MA · 档位 min30/day/week/month */
    private String mbmTier;
    /** MA多头破MA · 启用成交额门 */
    private Boolean mbmEnableMinAmountFilter;
    /** MA多头破MA · 最低日均成交额（万） */
    private Integer mbmMinAmountWan;
    /** MA多头破MA · 额外要求30分也破MAX */
    private Boolean mbmEnableMin30BreakFilter;
    /** MA多头破MA · 要求日线3M多头 */
    private Boolean mbmRequireDayAlign;
    /** MA多头破MA · 要求周线3M多头 */
    private Boolean mbmRequireWeekAlign;
    /** MA多头破MA · 要求月线3M多头 */
    private Boolean mbmRequireMonthAlign;
    /** MA金叉点突破 · 档位 */
    private String mgbTier;
    /** MA金叉点突破 · 启用成交额门 */
    private Boolean mgbEnableMinAmountFilter;
    /** MA金叉点突破 · 最低日均成交额（万） */
    private Integer mgbMinAmountWan;
    /** MA金叉点突破 · 右侧趋势 MA5&gt;MA60 */
    private Boolean mgbEnableRightTrend;
    /** MA死叉点突破 · 档位 */
    private String mdbTier;
    /** MA死叉点突破 · 启用成交额门 */
    private Boolean mdbEnableMinAmountFilter;
    /** MA死叉点突破 · 最低日均成交额（万） */
    private Integer mdbMinAmountWan;
    /** MA死叉点突破 · 右侧趋势 MA5&gt;MA60 */
    private Boolean mdbEnableRightTrend;
    /** 突破前波段High · 档位 */
    private String pbhTier;
    /** 突破前波段High · 启用成交额门 */
    private Boolean pbhEnableMinAmountFilter;
    /** 突破前波段High · 最低日均成交额（万） */
    private Integer pbhMinAmountWan;
    /** 突破前波段High · 右侧趋势 MA5&gt;MA60 */
    private Boolean pbhEnableRightTrend;
    /** MA空头破MA · 启用成交额门 */
    private Boolean mbbmEnableMinAmountFilter;
    /** MA空头破MA · 最低日均成交额（万） */
    private Integer mbbmMinAmountWan;
    /** MA多头4M排列 · 档位 min30/day/week/month */
    private String m4mTier;
    /** MA多头4M排列 · 启用成交额门 */
    private Boolean m4mEnableMinAmountFilter;
    /** MA多头4M排列 · 最低日均成交额（万） */
    private Integer m4mMinAmountWan;
    /** @deprecated */
    private Boolean wpgEnableWeekGate;
    /** @deprecated */
    private Boolean wpgEnableMonthGate;
    /** @deprecated */
    private Boolean wpgEnableYearGate;
    /** @deprecated */
    private Boolean wpgEnableYearBandGate;

    public StockPageQuery(Integer page, Integer size) {
		super(page, size);
	}

	public Boolean getIsSelectMode() {
		if (isSelectMode == null) {
			Calendar calendar = Calendar.getInstance();
			return calendar.get(Calendar.HOUR_OF_DAY) < 15;
		}
		return isSelectMode;
	}

    public PeriodTypeEnum getTrendPeriodTypeEnum() {
        return PeriodTypeEnum.getByCode(trendPeriodType);
    }
    public List<PeriodTypeEnum> getTrendPeriodTypesEnum() {
        return Objects.nonNull(this.trendPeriodTypes)? Stream.of(trendPeriodTypes.split(",")).map(PeriodTypeEnum::getByCode).collect(Collectors.toList()) : Collections.emptyList();
    }

    public PeriodTypeEnum getOpPeriodTypeEnum() {
        return PeriodTypeEnum.getByCode(opPeriodType);
    }

    public StrategyTypeEnum getStrategyTypeEnum() {
        if (strategy == null || strategy.isEmpty()) {
            return StrategyTypeEnum.ULTRA_SHORT;
        }
        if ("multi".equalsIgnoreCase(strategy)) {
            return null;
        }
        for (StrategyTypeEnum type : StrategyTypeEnum.values()) {
            if (type.getCode().equals(strategy)) {
                return type;
            }
        }
        return null;
    }

    public UnilateralStrategyParams toUnilateralParams() {
        UnilateralStrategyParams.UnilateralStrategyParamsBuilder b = UnilateralStrategyParams.builder();
        if (uMinAmountWan != null) {
            b.minAvgAmount(uMinAmountWan * 10_000D);
        }
        if (uEnableShort != null) {
            b.enableShort(uEnableShort);
        } else if (uEnableModeB != null) {
            b.enableShort(uEnableModeB);
        }
        if (uEnableMedium != null) {
            b.enableMedium(uEnableMedium);
        } else if (uEnableModeA != null) {
            b.enableMedium(uEnableModeA);
        }
        if (uEnableLong != null) {
            b.enableLong(uEnableLong);
        }
        if (uTierMin != null && !uTierMin.isEmpty()) {
            b.tierMin(uTierMin);
        }
        return UnilateralStrategyParams.merge(b.build());
    }

    public ReboundStrategyParams toReboundParams() {
        ReboundStrategyParams incoming = new ReboundStrategyParams();
        if (rMinAmountWan != null) {
            incoming.setMinAvgAmount(rMinAmountWan * 10_000D);
        }
        if (rEnableShort != null) {
            incoming.setEnableShort(rEnableShort);
        }
        if (rEnableMedium != null) {
            incoming.setEnableMedium(rEnableMedium);
        }
        if (rEnableLong != null) {
            incoming.setEnableLong(rEnableLong);
        }
        if (rTierMin != null && !rTierMin.isEmpty()) {
            incoming.setTierMin(rTierMin);
        }
        return ReboundStrategyParams.merge(incoming);
    }

    public PreGoldenStrategyParams toPreGoldenParams() {
        PreGoldenStrategyParams.PreGoldenStrategyParamsBuilder b = PreGoldenStrategyParams.builder();
        if (pMinAmountWan != null) {
            b.minAvgAmount(pMinAmountWan * 10_000D);
        }
        if (pEnableShort != null) {
            b.enableShort(pEnableShort);
        }
        if (pEnableMedium != null) {
            b.enableMedium(pEnableMedium);
        }
        if (pEnableLong != null) {
            b.enableLong(pEnableLong);
        }
        if (pTierMin != null && !pTierMin.isEmpty()) {
            b.tierMin(pTierMin);
        }
        return PreGoldenStrategyParams.merge(b.build());
    }

    public ResonanceStrategyParams toResonanceParams() {
        ResonanceStrategyParams.ResonanceStrategyParamsBuilder b = ResonanceStrategyParams.builder();
        if (cMinAmountWan != null) {
            b.minAvgAmount(cMinAmountWan * 10_000D);
        }
        if (cEnableShort != null) {
            b.enableShort(cEnableShort);
        }
        if (cEnableMedium != null) {
            b.enableMedium(cEnableMedium);
        }
        if (cEnableLong != null) {
            b.enableLong(cEnableLong);
        }
        if (cTierMin != null && !cTierMin.isEmpty()) {
            b.tierMin(cTierMin);
        }
        return ResonanceStrategyParams.merge(b.build());
    }

    public UltraLowReboundStrategyParams toUltraLowParams() {
        UltraLowReboundStrategyParams.UltraLowReboundStrategyParamsBuilder b =
                UltraLowReboundStrategyParams.builder();
        if (lMinAmountWan != null) {
            b.minAvgAmount(lMinAmountWan * 10_000D);
        }
        if (lTierMin != null && !lTierMin.isEmpty()) {
            b.tierMin(lTierMin);
        }
        if (lEnableUltra != null) {
            b.enableUltra(lEnableUltra);
        }
        if (lEnableShort != null) {
            b.enableShort(lEnableShort);
        }
        if (lEnableMedium != null) {
            b.enableMedium(lEnableMedium);
        }
        if (lEnableLong != null) {
            b.enableLong(lEnableLong);
        }
        if (lMin30BodyPct != null && lMin30BodyPct > 0) {
            b.min30BodyGainPct(lMin30BodyPct);
        }
        if (lMin30PrevDays != null && lMin30PrevDays >= 1) {
            b.min30PrevDays(lMin30PrevDays);
        }
        if (lMin30BarsPerDay != null && lMin30BarsPerDay > 0) {
            b.min30BarsPerDay(lMin30BarsPerDay);
        }
        if (lDayBodyPct != null && lDayBodyPct > 0) {
            b.dayBodyGainPct(lDayBodyPct);
        }
        if (lDayPrevWeeks != null && lDayPrevWeeks >= 1) {
            b.dayPrevWeeks(lDayPrevWeeks);
        }
        if (lDayBarsPerWeek != null && lDayBarsPerWeek > 0) {
            b.dayBarsPerWeek(lDayBarsPerWeek);
        }
        if (lWeekBodyPct != null && lWeekBodyPct > 0) {
            b.weekBodyGainPct(lWeekBodyPct);
        }
        if (lWeekPrevMonths != null && lWeekPrevMonths >= 1) {
            b.weekPrevMonths(lWeekPrevMonths);
        }
        if (lWeekBarsPerMonth != null && lWeekBarsPerMonth > 0) {
            b.weekBarsPerMonth(lWeekBarsPerMonth);
        }
        if (lMonthBodyPct != null && lMonthBodyPct > 0) {
            b.monthBodyGainPct(lMonthBodyPct);
        }
        if (lMonthPrevYears != null && lMonthPrevYears >= 1) {
            b.monthPrevYears(lMonthPrevYears);
        }
        if (lMonthBarsPerYear != null && lMonthBarsPerYear > 0) {
            b.monthBarsPerYear(lMonthBarsPerYear);
        }
        if (lLongPriceFilter != null) {
            b.enableLongPriceFilter(lLongPriceFilter != 0);
        }
        return UltraLowReboundStrategyParams.merge(b.build());
    }

    public RetestStrategyParams toRetestParams() {
        RetestStrategyParams.RetestStrategyParamsBuilder b = RetestStrategyParams.builder();
        if (tMinAmountWan != null) {
            b.minAvgAmount(tMinAmountWan * 10_000D);
        }
        if (tTierMin != null && !tTierMin.isEmpty()) {
            b.tierMin(tTierMin);
        }
        if (tEnableBear != null) {
            b.enableBear(tEnableBear);
        }
        if (tEnableBull != null) {
            b.enableBull(tEnableBull);
        }
        if (tEnableUltra != null) {
            b.enableUltra(tEnableUltra);
        }
        if (tEnableShort != null) {
            b.enableShort(tEnableShort);
        }
        if (tEnableMedium != null) {
            b.enableMedium(tEnableMedium);
        }
        if (tEnableLong != null) {
            b.enableLong(tEnableLong);
        }
        return RetestStrategyParams.merge(b.build());
    }

    public Gc2StrategyParams toGc2Params() {
        Gc2StrategyParams.Gc2StrategyParamsBuilder b = Gc2StrategyParams.builder();
        if (g2MinAmountWan != null) {
            b.minAvgAmount(g2MinAmountWan * 10_000D);
        }
        if (g2TierMin != null && !g2TierMin.isEmpty()) {
            b.tierMin(g2TierMin);
        }
        if (g2EnableShort != null) {
            b.enableShort(g2EnableShort);
        }
        if (g2EnableMedium != null) {
            b.enableMedium(g2EnableMedium);
        }
        if (g2EnableLong != null) {
            b.enableLong(g2EnableLong);
        }
        if (g2LookbackShort != null && g2LookbackShort >= 10) {
            b.lookbackShort(g2LookbackShort);
        }
        if (g2LookbackMedium != null && g2LookbackMedium >= 10) {
            b.lookbackMedium(g2LookbackMedium);
        }
        if (g2LookbackLong != null && g2LookbackLong >= 6) {
            b.lookbackLong(g2LookbackLong);
        }
        return Gc2StrategyParams.merge(b.build());
    }

    public Dc2StrategyParams toDc2Params() {
        Dc2StrategyParams.Dc2StrategyParamsBuilder b = Dc2StrategyParams.builder();
        if (d2MinAmountWan != null) {
            b.minAvgAmount(d2MinAmountWan * 10_000D);
        }
        if (d2TierMin != null && !d2TierMin.isEmpty()) {
            b.tierMin(d2TierMin);
        }
        if (d2EnableShort != null) {
            b.enableShort(d2EnableShort);
        }
        if (d2EnableLong != null) {
            b.enableLong(d2EnableLong);
        }
        if (d2LookbackShort != null && d2LookbackShort >= 10) {
            b.lookbackShort(d2LookbackShort);
        }
        if (d2LookbackLong != null && d2LookbackLong >= 10) {
            b.lookbackLong(d2LookbackLong);
        }
        return Dc2StrategyParams.merge(b.build());
    }

    public UltraShortStrategyParams toUltraShortParams() {
        UltraShortStrategyParams.UltraShortStrategyParamsBuilder b = UltraShortStrategyParams.builder();
        if (ulMinAmountWan != null) {
            b.minAvgAmount(ulMinAmountWan * 10_000D);
        }
        if (ulRequireDayMacdNegative != null) {
            b.requireDayMacdNegative(ulRequireDayMacdNegative);
        }
        if (ulRequireWeekMacdNegative != null) {
            b.requireWeekMacdNegative(ulRequireWeekMacdNegative);
        }
        if (ulRequireMonthMacdNegative != null) {
            b.requireMonthMacdNegative(ulRequireMonthMacdNegative);
        }
        if (ulRequireCurrentBreakout != null) {
            b.requireCurrentBreakout(ulRequireCurrentBreakout);
        }
        return UltraShortStrategyParams.merge(b.build());
    }

    public TrendV2StrategyParams toTrendV2Params() {
        TrendV2StrategyParams.TrendV2StrategyParamsBuilder b = TrendV2StrategyParams.builder();
        if (trMinAmountWan != null) {
            b.minAvgAmount(trMinAmountWan * 10_000D);
        }
        if (trPrevWeeks != null && trPrevWeeks >= 1) {
            b.prevWeeks(trPrevWeeks);
        }
        if (trRequireCurrentBreakout != null) {
            b.requireCurrentBreakout(trRequireCurrentBreakout);
        }
        if (trRequireMonthMacd != null) {
            b.requireMonthMacd(trRequireMonthMacd);
        }
        if (trRequireWeekMacd != null) {
            b.requireWeekMacd(trRequireWeekMacd);
        }
        if (trRequireDayMacd != null) {
            b.requireDayMacd(trRequireDayMacd);
        }
        if (trRequireWeekGoldenCross != null) {
            b.requireWeekGoldenCross(trRequireWeekGoldenCross);
        }
        if (trRequireUltra != null) {
            b.requireUltra(trRequireUltra);
        }
        return TrendV2StrategyParams.merge(b.build());
    }

    public MediumStrategyParams toMediumParams() {
        MediumStrategyParams.MediumStrategyParamsBuilder b = MediumStrategyParams.builder();
        if (mdMinAmountWan != null) {
            b.minAvgAmount(mdMinAmountWan * 10_000D);
        }
        if (mdPrevMonths != null && mdPrevMonths >= 1) {
            b.prevMonths(mdPrevMonths);
        }
        if (mdRequireCurrentBreakout != null) {
            b.requireCurrentBreakout(mdRequireCurrentBreakout);
        }
        if (mdRequireMonthMacd != null) {
            b.requireMonthMacd(mdRequireMonthMacd);
        }
        if (mdRequireYearMacd != null) {
            b.requireYearMacd(mdRequireYearMacd);
        }
        if (mdRequireMonthGoldenCross != null) {
            b.requireMonthGoldenCross(mdRequireMonthGoldenCross);
        }
        if (mdRequireUltra != null) {
            b.requireUltra(mdRequireUltra);
        }
        return MediumStrategyParams.merge(b.build());
    }

    public LongStrategyParams toLongParams() {
        LongStrategyParams.LongStrategyParamsBuilder b = LongStrategyParams.builder();
        if (lgMinAmountWan != null) {
            b.minAvgAmount(lgMinAmountWan * 10_000D);
        }
        if (lgPrevYears != null && lgPrevYears >= 1) {
            b.prevYears(lgPrevYears);
        }
        if (lgRequireCurrentBreakout != null) {
            b.requireCurrentBreakout(lgRequireCurrentBreakout);
        }
        if (lgRequireYearMacd != null) {
            b.requireYearMacd(lgRequireYearMacd);
        }
        if (lgRequireMonthMacd != null) {
            b.requireMonthMacd(lgRequireMonthMacd);
        }
        if (lgRequireYearGoldenCross != null) {
            b.requireYearGoldenCross(lgRequireYearGoldenCross);
        }
        if (lgRequireUltra != null) {
            b.requireUltra(lgRequireUltra);
        }
        return LongStrategyParams.merge(b.build());
    }

    public FrictionlessLadderStrategyParams toFrictionlessParams() {
        FrictionlessLadderStrategyParams incoming = new FrictionlessLadderStrategyParams();
        if (nrfActiveTier != null && !nrfActiveTier.isEmpty()) {
            incoming.setActiveTier(FrictionlessLadderStrategyParams.parseActiveTier(nrfActiveTier));
        }
        return FrictionlessLadderStrategyParams.merge(incoming);
    }

    public PillarStrategyParams toPillarParams() {
        PillarStrategyParams.PillarStrategyParamsBuilder b = PillarStrategyParams.builder();
        if (piEnableDay != null) {
            b.enableDay(piEnableDay);
        }
        if (piEnableWeek != null) {
            b.enableWeek(piEnableWeek);
        }
        if (piEnableMonth != null) {
            b.enableMonth(piEnableMonth);
        }
        if (piLookbackDay != null && piLookbackDay >= 10) {
            b.lookbackDay(piLookbackDay);
        }
        if (piLookbackWeek != null && piLookbackWeek >= 10) {
            b.lookbackWeek(piLookbackWeek);
        }
        if (piLookbackMonth != null && piLookbackMonth >= 6) {
            b.lookbackMonth(piLookbackMonth);
        }
        if (piRequireUltra != null) {
            b.requireUltra(piRequireUltra);
        }
        if (piMinAmountWan != null && piMinAmountWan >= 0) {
            b.minAvgAmount(piMinAmountWan * 10_000D);
        }
        return PillarStrategyParams.merge(b.build());
    }

    public CascadeStrategyParams toCascadeParams() {
        CascadeStrategyParams.CascadeStrategyParamsBuilder b = CascadeStrategyParams.builder();
        if (caEnableDualLowGate != null) {
            b.enableDualLowGate(caEnableDualLowGate);
        }
        if (caEnableMacdGate != null) {
            b.enableMacdGate(caEnableMacdGate);
        }
        if (caEnableMacdDcHighGate != null) {
            b.enableMacdDcHighGate(caEnableMacdDcHighGate);
        }
        if (caEnableCrossLowGate != null) {
            b.enableCrossLowGate(caEnableCrossLowGate);
        }
        if (caEnableBarHighGate != null) {
            b.enableBarHighGate(caEnableBarHighGate);
        }
        if (caEnableAllYangGate != null) {
            b.enableAllYangGate(caEnableAllYangGate);
        }
        if (caEnableDay != null) {
            b.enableDay(caEnableDay);
        }
        if (caEnableWeek != null) {
            b.enableWeek(caEnableWeek);
        }
        if (caEnableMonth != null) {
            b.enableMonth(caEnableMonth);
        }
        if (caLookbackDay != null && caLookbackDay >= 10) {
            b.lookbackDay(caLookbackDay);
        }
        if (caLookbackWeek != null && caLookbackWeek >= 10) {
            b.lookbackWeek(caLookbackWeek);
        }
        if (caLookbackMonth != null && caLookbackMonth >= 6) {
            b.lookbackMonth(caLookbackMonth);
        }
        if (caEnableAltBreakout != null) {
            b.enableAltBreakout(caEnableAltBreakout);
        }
        if (caRequireUltra != null) {
            b.requireUltra(caRequireUltra);
        }
        if (caMinAmountWan != null && caMinAmountWan >= 0) {
            b.minAvgAmount(caMinAmountWan * 10_000D);
        }
        return CascadeStrategyParams.merge(b.build());
    }

    public LadderDipStrategyParams toLadderDipParams() {
        LadderDipStrategyParams.LadderDipStrategyParamsBuilder b = LadderDipStrategyParams.builder();
        if (ldEnableDualLowGate != null) {
            b.enableDualLowGate(ldEnableDualLowGate);
        }
        if (ldEnableMacdGate != null) {
            b.enableMacdGate(ldEnableMacdGate);
        }
        if (ldEnableMacdDcHighGate != null) {
            b.enableMacdDcHighGate(ldEnableMacdDcHighGate);
        }
        if (ldEnableCrossLowGate != null) {
            b.enableCrossLowGate(ldEnableCrossLowGate);
        }
        if (ldEnableBarHighGate != null) {
            b.enableBarHighGate(ldEnableBarHighGate);
        }
        if (ldEnableAllYangGate != null) {
            b.enableAllYangGate(ldEnableAllYangGate);
        }
        if (ldEnableDay != null) {
            b.enableDay(ldEnableDay);
        }
        if (ldEnableWeek != null) {
            b.enableWeek(ldEnableWeek);
        }
        if (ldEnableMonth != null) {
            b.enableMonth(ldEnableMonth);
        }
        if (ldLookbackDay != null && ldLookbackDay >= 10) {
            b.lookbackDay(ldLookbackDay);
        }
        if (ldLookbackWeek != null && ldLookbackWeek >= 10) {
            b.lookbackWeek(ldLookbackWeek);
        }
        if (ldLookbackMonth != null && ldLookbackMonth >= 6) {
            b.lookbackMonth(ldLookbackMonth);
        }
        if (ldRequireUltra != null) {
            b.requireUltra(ldRequireUltra);
        }
        if (ldMinAmountWan != null && ldMinAmountWan >= 0) {
            b.minAvgAmount(ldMinAmountWan * 10_000D);
        }
        return LadderDipStrategyParams.merge(b.build());
    }

    public MacdEdgeStrategyParams toMacdEdgeParams() {
        MacdEdgeStrategyParams.MacdEdgeStrategyParamsBuilder b = MacdEdgeStrategyParams.builder();
        if (meEnableDualLowGate != null) {
            b.enableDualLowGate(meEnableDualLowGate);
        }
        if (meEnableMacdGate != null) {
            b.enableMacdGate(meEnableMacdGate);
        }
        if (meEnableMacdDcHighGate != null) {
            b.enableMacdDcHighGate(meEnableMacdDcHighGate);
        }
        if (meEnableCrossLowGate != null) {
            b.enableCrossLowGate(meEnableCrossLowGate);
        }
        if (meEnableBarHighGate != null) {
            b.enableBarHighGate(meEnableBarHighGate);
        }
        if (meEnableAllYangGate != null) {
            b.enableAllYangGate(meEnableAllYangGate);
        }
        if (meEnableMin30 != null) {
            b.enableMin30(meEnableMin30);
        }
        if (meEnableDay != null) {
            b.enableDay(meEnableDay);
        }
        if (meEnableWeek != null) {
            b.enableWeek(meEnableWeek);
        }
        if (meEnableMonth != null) {
            b.enableMonth(meEnableMonth);
        }
        if (meEnableYear != null) {
            b.enableYear(meEnableYear);
        }
        if (meLookbackMin30 != null && meLookbackMin30 >= 10) {
            b.lookbackMin30(meLookbackMin30);
        }
        if (meLookbackDay != null && meLookbackDay >= 10) {
            b.lookbackDay(meLookbackDay);
        }
        if (meLookbackWeek != null && meLookbackWeek >= 10) {
            b.lookbackWeek(meLookbackWeek);
        }
        if (meLookbackMonth != null && meLookbackMonth >= 6) {
            b.lookbackMonth(meLookbackMonth);
        }
        if (meLookbackYear != null && meLookbackYear >= 4) {
            b.lookbackYear(meLookbackYear);
        }
        if (meMinAmountWan != null && meMinAmountWan >= 0) {
            b.minAvgAmount(meMinAmountWan * 10_000D);
        }
        return MacdEdgeStrategyParams.merge(b.build());
    }

    public WaveConvexStrategyParams toWaveConvexParams() {
        WaveConvexStrategyParams.WaveConvexStrategyParamsBuilder b = WaveConvexStrategyParams.builder();
        if (wcvEnableAllYangGate != null) {
            b.enableAllYangGate(wcvEnableAllYangGate);
        }
        if (wcvEnableMin30Gate != null) {
            b.enableMin30Gate(wcvEnableMin30Gate);
        }
        if (wcvEnableLastHighBreak != null) {
            b.enableLastHighBreak(wcvEnableLastHighBreak);
        }
        if (wcvEnableLastMedianBreak != null) {
            b.enableLastMedianBreak(wcvEnableLastMedianBreak);
        }
        if (wcvEnableLastLowBreak != null) {
            b.enableLastLowBreak(wcvEnableLastLowBreak);
        }
        if (wcvEnableDay != null) {
            b.enableDay(wcvEnableDay);
        }
        if (wcvEnableWeek != null) {
            b.enableWeek(wcvEnableWeek);
        }
        if (wcvEnableMonth != null) {
            b.enableMonth(wcvEnableMonth);
        }
        if (wcvLookbackDay != null && wcvLookbackDay >= 10) {
            b.lookbackDay(wcvLookbackDay);
        }
        if (wcvLookbackWeek != null && wcvLookbackWeek >= 10) {
            b.lookbackWeek(wcvLookbackWeek);
        }
        if (wcvLookbackMonth != null && wcvLookbackMonth >= 6) {
            b.lookbackMonth(wcvLookbackMonth);
        }
        if (wcvMinAmountWan != null && wcvMinAmountWan >= 0) {
            b.minAvgAmount(wcvMinAmountWan * 10_000D);
        }
        return WaveConvexStrategyParams.merge(b.build());
    }

    public WaveConcaveStrategyParams toWaveConcaveParams() {
        WaveConcaveStrategyParams.WaveConcaveStrategyParamsBuilder b = WaveConcaveStrategyParams.builder();
        if (wccEnableAllYangGate != null) {
            b.enableAllYangGate(wccEnableAllYangGate);
        }
        if (wccEnableMin30Gate != null) {
            b.enableMin30Gate(wccEnableMin30Gate);
        }
        if (wccEnableLastHighBreak != null) {
            b.enableLastHighBreak(wccEnableLastHighBreak);
        }
        if (wccEnableLastMedianBreak != null) {
            b.enableLastMedianBreak(wccEnableLastMedianBreak);
        }
        if (wccEnableLastLowBreak != null) {
            b.enableLastLowBreak(wccEnableLastLowBreak);
        }
        if (wccEnableDay != null) {
            b.enableDay(wccEnableDay);
        }
        if (wccEnableWeek != null) {
            b.enableWeek(wccEnableWeek);
        }
        if (wccEnableMonth != null) {
            b.enableMonth(wccEnableMonth);
        }
        if (wccLookbackDay != null && wccLookbackDay >= 10) {
            b.lookbackDay(wccLookbackDay);
        }
        if (wccLookbackWeek != null && wccLookbackWeek >= 10) {
            b.lookbackWeek(wccLookbackWeek);
        }
        if (wccLookbackMonth != null && wccLookbackMonth >= 6) {
            b.lookbackMonth(wccLookbackMonth);
        }
        if (wccMinAmountWan != null && wccMinAmountWan >= 0) {
            b.minAvgAmount(wccMinAmountWan * 10_000D);
        }
        return WaveConcaveStrategyParams.merge(b.build());
    }

    public WaveConvexDayStrategyParams toWaveConvexDayParams() {
        WaveConvexDayStrategyParams.WaveConvexDayStrategyParamsBuilder b = WaveConvexDayStrategyParams.builder();
        if (wcvdEnableAllYangGate != null) {
            b.enableAllYangGate(wcvdEnableAllYangGate);
        }
        if (wcvdEnableMin30Gate != null) {
            b.enableMin30Gate(wcvdEnableMin30Gate);
        }
        if (wcvdEnableLastHighBreak != null) {
            b.enableLastHighBreak(wcvdEnableLastHighBreak);
        }
        if (wcvdEnableLastMedianBreak != null) {
            b.enableLastMedianBreak(wcvdEnableLastMedianBreak);
        }
        if (wcvdEnableLastLowBreak != null) {
            b.enableLastLowBreak(wcvdEnableLastLowBreak);
        }
        if (wcvdEnableDay != null) {
            b.enableDay(wcvdEnableDay);
        }
        if (wcvdEnableWeek != null) {
            b.enableWeek(wcvdEnableWeek);
        }
        if (wcvdEnableMonth != null) {
            b.enableMonth(wcvdEnableMonth);
        }
        if (wcvdLookbackDay != null && wcvdLookbackDay >= 10) {
            b.lookbackDay(wcvdLookbackDay);
        }
        if (wcvdLookbackWeek != null && wcvdLookbackWeek >= 10) {
            b.lookbackWeek(wcvdLookbackWeek);
        }
        if (wcvdLookbackMonth != null && wcvdLookbackMonth >= 6) {
            b.lookbackMonth(wcvdLookbackMonth);
        }
        if (wcvdMinAmountWan != null && wcvdMinAmountWan >= 0) {
            b.minAvgAmount(wcvdMinAmountWan * 10_000D);
        }
        return WaveConvexDayStrategyParams.merge(b.build());
    }

    public WaveConcaveDayStrategyParams toWaveConcaveDayParams() {
        WaveConcaveDayStrategyParams.WaveConcaveDayStrategyParamsBuilder b = WaveConcaveDayStrategyParams.builder();
        if (wccdEnableAllYangGate != null) {
            b.enableAllYangGate(wccdEnableAllYangGate);
        }
        if (wccdEnableMin30Gate != null) {
            b.enableMin30Gate(wccdEnableMin30Gate);
        }
        if (wccdEnableLastHighBreak != null) {
            b.enableLastHighBreak(wccdEnableLastHighBreak);
        }
        if (wccdEnableLastMedianBreak != null) {
            b.enableLastMedianBreak(wccdEnableLastMedianBreak);
        }
        if (wccdEnableLastLowBreak != null) {
            b.enableLastLowBreak(wccdEnableLastLowBreak);
        }
        if (wccdEnableDay != null) {
            b.enableDay(wccdEnableDay);
        }
        if (wccdEnableWeek != null) {
            b.enableWeek(wccdEnableWeek);
        }
        if (wccdEnableMonth != null) {
            b.enableMonth(wccdEnableMonth);
        }
        if (wccdLookbackDay != null && wccdLookbackDay >= 10) {
            b.lookbackDay(wccdLookbackDay);
        }
        if (wccdLookbackWeek != null && wccdLookbackWeek >= 10) {
            b.lookbackWeek(wccdLookbackWeek);
        }
        if (wccdLookbackMonth != null && wccdLookbackMonth >= 6) {
            b.lookbackMonth(wccdLookbackMonth);
        }
        if (wccdMinAmountWan != null && wccdMinAmountWan >= 0) {
            b.minAvgAmount(wccdMinAmountWan * 10_000D);
        }
        return WaveConcaveDayStrategyParams.merge(b.build());
    }

    public CascadeWaveConvexStrategyParams toCascadeWaveConvexParams() {
        CascadeWaveConvexStrategyParams.CascadeWaveConvexStrategyParamsBuilder b =
                CascadeWaveConvexStrategyParams.builder();
        if (cwcvEnableAllYangGate != null) {
            b.enableAllYangGate(cwcvEnableAllYangGate);
        }
        if (cwcvEnableMin30Gate != null) {
            b.enableMin30Gate(cwcvEnableMin30Gate);
        }
        if (cwcvEnableBandLastYangLowGate != null) {
            b.enableBandLastYangLowGate(cwcvEnableBandLastYangLowGate);
        }
        if (cwcvEnableYangBandTrendGate != null) {
            b.enableYangBandTrendGate(cwcvEnableYangBandTrendGate);
        }
        if (cwcvEnablePrevBandBreak != null) {
            b.enablePrevBandBreak(cwcvEnablePrevBandBreak);
        }
        if (cwcvEnableDay != null) {
            b.enableDay(cwcvEnableDay);
        }
        if (cwcvEnableWeek != null) {
            b.enableWeek(cwcvEnableWeek);
        }
        if (cwcvEnableMonth != null) {
            b.enableMonth(cwcvEnableMonth);
        }
        if (cwcvLookbackDay != null && cwcvLookbackDay >= 10) {
            b.lookbackDay(cwcvLookbackDay);
        }
        if (cwcvLookbackWeek != null && cwcvLookbackWeek >= 10) {
            b.lookbackWeek(cwcvLookbackWeek);
        }
        if (cwcvLookbackMonth != null && cwcvLookbackMonth >= 6) {
            b.lookbackMonth(cwcvLookbackMonth);
        }
        if (cwcvLookbackYear != null && cwcvLookbackYear >= 4) {
            b.lookbackYear(cwcvLookbackYear);
        }
        if (cwcvMinAmountWan != null && cwcvMinAmountWan >= 0) {
            b.minAvgAmount(cwcvMinAmountWan * 10_000D);
        }
        return CascadeWaveConvexStrategyParams.merge(b.build());
    }

    public CascadeWaveConcaveStrategyParams toCascadeWaveConcaveParams() {
        CascadeWaveConcaveStrategyParams.CascadeWaveConcaveStrategyParamsBuilder b =
                CascadeWaveConcaveStrategyParams.builder();
        if (cwcavEnableAllYangGate != null) {
            b.enableAllYangGate(cwcavEnableAllYangGate);
        }
        if (cwcavEnableMin30Gate != null) {
            b.enableMin30Gate(cwcavEnableMin30Gate);
        }
        if (cwcavEnableBandLastYangLowGate != null) {
            b.enableBandLastYangLowGate(cwcavEnableBandLastYangLowGate);
        }
        if (cwcavEnableYangBandTrendGate != null) {
            b.enableYangBandTrendGate(cwcavEnableYangBandTrendGate);
        }
        if (cwcavEnablePrevBandBreak != null) {
            b.enablePrevBandBreak(cwcavEnablePrevBandBreak);
        }
        if (cwcavEnableDay != null) {
            b.enableDay(cwcavEnableDay);
        }
        if (cwcavEnableWeek != null) {
            b.enableWeek(cwcavEnableWeek);
        }
        if (cwcavEnableMonth != null) {
            b.enableMonth(cwcavEnableMonth);
        }
        if (cwcavLookbackDay != null && cwcavLookbackDay >= 10) {
            b.lookbackDay(cwcavLookbackDay);
        }
        if (cwcavLookbackWeek != null && cwcavLookbackWeek >= 10) {
            b.lookbackWeek(cwcavLookbackWeek);
        }
        if (cwcavLookbackMonth != null && cwcavLookbackMonth >= 6) {
            b.lookbackMonth(cwcavLookbackMonth);
        }
        if (cwcavLookbackYear != null && cwcavLookbackYear >= 4) {
            b.lookbackYear(cwcavLookbackYear);
        }
        if (cwcavMinAmountWan != null && cwcavMinAmountWan >= 0) {
            b.minAvgAmount(cwcavMinAmountWan * 10_000D);
        }
        return CascadeWaveConcaveStrategyParams.merge(b.build());
    }

    public CascadeWaveConvexDayStrategyParams toCascadeWaveConvexDayParams() {
        CascadeWaveConvexDayStrategyParams.CascadeWaveConvexDayStrategyParamsBuilder b =
                CascadeWaveConvexDayStrategyParams.builder();
        if (cwcvdEnableAllYangGate != null) {
            b.enableAllYangGate(cwcvdEnableAllYangGate);
        }
        if (cwcvdEnableMin30Gate != null) {
            b.enableMin30Gate(cwcvdEnableMin30Gate);
        }
        if (cwcvdEnableBandLastYangLowGate != null) {
            b.enableBandLastYangLowGate(cwcvdEnableBandLastYangLowGate);
        }
        if (cwcvdEnableYangBandTrendGate != null) {
            b.enableYangBandTrendGate(cwcvdEnableYangBandTrendGate);
        }
        if (cwcvdEnablePrevBandBreak != null) {
            b.enablePrevBandBreak(cwcvdEnablePrevBandBreak);
        }
        if (cwcvdEnableDay != null) {
            b.enableDay(cwcvdEnableDay);
        }
        if (cwcvdEnableWeek != null) {
            b.enableWeek(cwcvdEnableWeek);
        }
        if (cwcvdEnableMonth != null) {
            b.enableMonth(cwcvdEnableMonth);
        }
        if (cwcvdLookbackDay != null && cwcvdLookbackDay >= 10) {
            b.lookbackDay(cwcvdLookbackDay);
        }
        if (cwcvdLookbackWeek != null && cwcvdLookbackWeek >= 10) {
            b.lookbackWeek(cwcvdLookbackWeek);
        }
        if (cwcvdLookbackMonth != null && cwcvdLookbackMonth >= 6) {
            b.lookbackMonth(cwcvdLookbackMonth);
        }
        if (cwcvdLookbackYear != null && cwcvdLookbackYear >= 4) {
            b.lookbackYear(cwcvdLookbackYear);
        }
        if (cwcvdMinAmountWan != null && cwcvdMinAmountWan >= 0) {
            b.minAvgAmount(cwcvdMinAmountWan * 10_000D);
        }
        return CascadeWaveConvexDayStrategyParams.merge(b.build());
    }

    public CascadeWaveConcaveDayStrategyParams toCascadeWaveConcaveDayParams() {
        CascadeWaveConcaveDayStrategyParams.CascadeWaveConcaveDayStrategyParamsBuilder b =
                CascadeWaveConcaveDayStrategyParams.builder();
        if (cwcadEnableAllYangGate != null) {
            b.enableAllYangGate(cwcadEnableAllYangGate);
        }
        if (cwcadEnableMin30Gate != null) {
            b.enableMin30Gate(cwcadEnableMin30Gate);
        }
        if (cwcadEnableBandLastYangLowGate != null) {
            b.enableBandLastYangLowGate(cwcadEnableBandLastYangLowGate);
        }
        if (cwcadEnableYangBandTrendGate != null) {
            b.enableYangBandTrendGate(cwcadEnableYangBandTrendGate);
        }
        if (cwcadEnablePrevBandBreak != null) {
            b.enablePrevBandBreak(cwcadEnablePrevBandBreak);
        }
        if (cwcadEnableDay != null) {
            b.enableDay(cwcadEnableDay);
        }
        if (cwcadEnableWeek != null) {
            b.enableWeek(cwcadEnableWeek);
        }
        if (cwcadEnableMonth != null) {
            b.enableMonth(cwcadEnableMonth);
        }
        if (cwcadLookbackDay != null && cwcadLookbackDay >= 10) {
            b.lookbackDay(cwcadLookbackDay);
        }
        if (cwcadLookbackWeek != null && cwcadLookbackWeek >= 10) {
            b.lookbackWeek(cwcadLookbackWeek);
        }
        if (cwcadLookbackMonth != null && cwcadLookbackMonth >= 6) {
            b.lookbackMonth(cwcadLookbackMonth);
        }
        if (cwcadLookbackYear != null && cwcadLookbackYear >= 4) {
            b.lookbackYear(cwcadLookbackYear);
        }
        if (cwcadMinAmountWan != null && cwcadMinAmountWan >= 0) {
            b.minAvgAmount(cwcadMinAmountWan * 10_000D);
        }
        return CascadeWaveConcaveDayStrategyParams.merge(b.build());
    }

    public WaveBandStrategyParams toWaveBandShortParams() {
        WaveBandStrategyParams.WaveBandStrategyParamsBuilder b = WaveBandStrategyParams.builder();
        if (wbsLookbackDay != null && wbsLookbackDay >= 10) {
            b.lookbackDay(wbsLookbackDay);
        } else if (wbLookbackDay != null && wbLookbackDay >= 10) {
            b.lookbackDay(wbLookbackDay);
        }
        if (wbsLookbackWeek != null && wbsLookbackWeek >= 10) {
            b.lookbackWeek(wbsLookbackWeek);
        } else if (wbLookbackWeek != null && wbLookbackWeek >= 10) {
            b.lookbackWeek(wbLookbackWeek);
        }
        if (wbsMinAmountWan != null && wbsMinAmountWan >= 0) {
            b.minAvgAmount(wbsMinAmountWan * 10_000D);
        } else if (wbMinAmountWan != null && wbMinAmountWan >= 0) {
            b.minAvgAmount(wbMinAmountWan * 10_000D);
        }
        return WaveBandStrategyParams.merge(b.build());
    }

    public WaveBandStrategyParams toWaveBandMediumParams() {
        WaveBandStrategyParams.WaveBandStrategyParamsBuilder b = WaveBandStrategyParams.builder();
        if (wbmLookbackDay != null && wbmLookbackDay >= 10) {
            b.lookbackDay(wbmLookbackDay);
        } else if (wbLookbackDay != null && wbLookbackDay >= 10) {
            b.lookbackDay(wbLookbackDay);
        }
        if (wbmLookbackMonth != null && wbmLookbackMonth >= 6) {
            b.lookbackMonth(wbmLookbackMonth);
        } else if (wbLookbackMonth != null && wbLookbackMonth >= 6) {
            b.lookbackMonth(wbLookbackMonth);
        }
        if (wbmMinAmountWan != null && wbmMinAmountWan >= 0) {
            b.minAvgAmount(wbmMinAmountWan * 10_000D);
        } else if (wbMinAmountWan != null && wbMinAmountWan >= 0) {
            b.minAvgAmount(wbMinAmountWan * 10_000D);
        }
        return WaveBandStrategyParams.merge(b.build());
    }

    public WavePeriodGateStrategyParams toWavePeriodGateParams() {
        WavePeriodGateStrategyParams.WavePeriodGateStrategyParamsBuilder b =
                WavePeriodGateStrategyParams.builder();
        if (wpgTier != null && !wpgTier.trim().isEmpty()) {
            b.tier(WavePeriodGateStrategyParams.parseTier(wpgTier));
        } else if (Boolean.TRUE.equals(wpgEnableMonthGate) || Boolean.TRUE.equals(wpgEnableYearGate)
                || Boolean.TRUE.equals(wpgEnableYearBandGate)) {
            b.tier(WavePeriodGateStrategyParams.Tier.MONTH);
        } else if (Boolean.FALSE.equals(wpgEnableWeekGate) && Boolean.TRUE.equals(wpgEnableMonthGate)) {
            b.tier(WavePeriodGateStrategyParams.Tier.MONTH);
        } else if (Boolean.TRUE.equals(wpgEnableWeekGate)) {
            b.tier(WavePeriodGateStrategyParams.Tier.WEEK);
        }
        if (wpgLookbackDay != null && wpgLookbackDay >= 20) {
            b.lookbackDay(wpgLookbackDay);
        }
        if (wpgLookbackWeek != null && wpgLookbackWeek >= 10) {
            b.lookbackWeek(wpgLookbackWeek);
        }
        if (wpgLookbackMonth != null && wpgLookbackMonth >= 6) {
            b.lookbackMonth(wpgLookbackMonth);
        }
        if (wpgEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(wpgEnableMinAmountFilter);
        } else if (wpgMinAmountWan != null && wpgMinAmountWan > 0) {
            b.enableMinAmountFilter(true);
        }
        if (wpgEnableMaxBandLowGate != null) {
            b.enableMaxBandLowGate(wpgEnableMaxBandLowGate);
        }
        if (wpgEnableConcaveBreakout != null) {
            b.enableConcaveBreakout(wpgEnableConcaveBreakout);
        }
        if (wpgEnableConvexBreakout != null) {
            b.enableConvexBreakout(wpgEnableConvexBreakout);
        }
        if (wpgEnableUpperPeriodMinBandLowGate != null) {
            b.enableUpperPeriodMinBandLowGate(wpgEnableUpperPeriodMinBandLowGate);
        }
        if (wpgEnableTierMacdPositive != null) {
            b.enableTierMacdPositiveGate(wpgEnableTierMacdPositive);
        }
        if (wpgEnableWeekMonthBandShapeGate != null) {
            b.enableWeekMonthBandShapeGate(wpgEnableWeekMonthBandShapeGate);
        }
        if (wpgEnableWeekMonthBandLowGate != null) {
            b.enableWeekMonthBandLowGate(wpgEnableWeekMonthBandLowGate);
        }
        if (wpgEnableYearWeekMonthYangGate != null) {
            b.enableYearWeekMonthYangGate(wpgEnableYearWeekMonthYangGate);
        }
        if (wpgLookbackYear != null && wpgLookbackYear >= 4) {
            b.lookbackYear(wpgLookbackYear);
        }
        if (wpgMinAmountWan != null && wpgMinAmountWan >= 0) {
            b.minAvgAmount(wpgMinAmountWan * 10_000D);
        }
        return WavePeriodGateStrategyParams.merge(b.build());
    }

    public MacdCrossTierStrategyParams toMacdCrossTierParams() {
        MacdCrossTierStrategyParams.MacdCrossTierStrategyParamsBuilder b =
                MacdCrossTierStrategyParams.builder();
        if (mctTier != null && !mctTier.trim().isEmpty()) {
            b.tier(MacdCrossTierStrategyParams.parseTier(mctTier));
        }
        if (mctLookbackDay != null && mctLookbackDay >= 10) {
            b.lookbackDay(mctLookbackDay);
        }
        if (mctLookbackWeek != null && mctLookbackWeek >= 10) {
            b.lookbackWeek(mctLookbackWeek);
        }
        if (mctLookbackMonth != null && mctLookbackMonth >= 6) {
            b.lookbackMonth(mctLookbackMonth);
        }
        if (mctEnableGoldenCross != null || mctEnableDeathCross != null
                || mctEnableGoldenCrossRiseGate != null) {
            b.enableGoldenCross(Boolean.TRUE.equals(mctEnableGoldenCross));
            b.enableDeathCross(Boolean.TRUE.equals(mctEnableDeathCross));
            b.enableGoldenCrossRiseGate(Boolean.TRUE.equals(mctEnableGoldenCrossRiseGate));
        }
        return MacdCrossTierStrategyParams.merge(b.build());
    }

    public ConvexLiftTierStrategyParams toConvexLiftTierParams() {
        ConvexLiftTierStrategyParams.ConvexLiftTierStrategyParamsBuilder b =
                ConvexLiftTierStrategyParams.builder();
        if (cltTier != null && !cltTier.trim().isEmpty()) {
            b.tier(ConvexLiftTierStrategyParams.parseTier(cltTier));
        }
        if (cltLookbackDay != null && cltLookbackDay >= 20) {
            b.lookbackDay(cltLookbackDay);
        }
        return ConvexLiftTierStrategyParams.merge(b.build());
    }

    public BodyBarTierStrategyParams toBodyBarTierParams() {
        BodyBarTierStrategyParams.BodyBarTierStrategyParamsBuilder b =
                BodyBarTierStrategyParams.builder();
        if (bbtTier != null && !bbtTier.trim().isEmpty()) {
            b.tier(BodyBarTierStrategyParams.parseTier(bbtTier));
        }
        if (bbtLookbackBars != null && bbtLookbackBars >= 3) {
            b.lookbackBars(bbtLookbackBars);
        }
        return BodyBarTierStrategyParams.merge(b.build());
    }

    public MacdGoldenCrossStrategyParams toMacdGoldenCrossParams() {
        MacdGoldenCrossStrategyParams.MacdGoldenCrossStrategyParamsBuilder b =
                MacdGoldenCrossStrategyParams.builder();
        if (mgcTier != null && !mgcTier.trim().isEmpty()) {
            b.tier(MacdGoldenCrossStrategyParams.parseTier(mgcTier));
        }
        if (mgcEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(mgcEnableMinAmountFilter);
        }
        if (mgcMinAmountWan != null && mgcMinAmountWan >= 0) {
            b.minAvgAmount(mgcMinAmountWan * 10_000D);
        }
        if (mgcEnableSignalRiseGate != null) {
            b.enableSignalRiseGate(mgcEnableSignalRiseGate);
        }
        if (mgcSignalRisePct != null && mgcSignalRisePct > 0) {
            b.signalRisePct(mgcSignalRisePct / 100D);
        }
        if (mgcEnableHistoryRiseGate != null) {
            b.enableHistoryRiseGate(mgcEnableHistoryRiseGate);
        }
        if (mgcHistoryLookbackBars != null && mgcHistoryLookbackBars >= 1) {
            b.historyLookbackBars(mgcHistoryLookbackBars);
        }
        if (mgcHistoryRisePct != null && mgcHistoryRisePct > 0) {
            b.historyRisePct(mgcHistoryRisePct / 100D);
        }
        return MacdGoldenCrossStrategyParams.merge(b.build());
    }

    public MacdGcWaveHighStrategyParams toMacdGcWaveHighParams() {
        MacdGcWaveHighStrategyParams.MacdGcWaveHighStrategyParamsBuilder b =
                MacdGcWaveHighStrategyParams.builder();
        if (mgcwhTier != null && !mgcwhTier.trim().isEmpty()) {
            b.tier(MacdGcWaveHighStrategyParams.parseTier(mgcwhTier));
        }
        if (mgcwhLookbackDay != null && mgcwhLookbackDay >= 10) {
            b.lookbackDay(mgcwhLookbackDay);
        }
        if (mgcwhLookbackWeek != null && mgcwhLookbackWeek >= 10) {
            b.lookbackWeek(mgcwhLookbackWeek);
        }
        if (mgcwhLookbackMonth != null && mgcwhLookbackMonth >= 6) {
            b.lookbackMonth(mgcwhLookbackMonth);
        }
        if (mgcwhEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(mgcwhEnableMinAmountFilter);
        }
        if (mgcwhMinAmountWan != null && mgcwhMinAmountWan >= 0) {
            b.minAvgAmount(mgcwhMinAmountWan * 10_000D);
        }
        if (mgcwhEnableSignalRiseGate != null) {
            b.enableSignalRiseGate(mgcwhEnableSignalRiseGate);
        }
        if (mgcwhSignalRisePct != null && mgcwhSignalRisePct > 0) {
            b.signalRisePct(mgcwhSignalRisePct / 100D);
        }
        return MacdGcWaveHighStrategyParams.merge(b.build());
    }

    public MacdGcWaveHighRetestStrategyParams toMacdGcWaveHighRetestParams() {
        MacdGcWaveHighRetestStrategyParams.MacdGcWaveHighRetestStrategyParamsBuilder b =
                MacdGcWaveHighRetestStrategyParams.builder();
        if (mgcwhrTier != null && !mgcwhrTier.trim().isEmpty()) {
            b.tier(MacdGcWaveHighRetestStrategyParams.parseTier(mgcwhrTier));
        }
        if (mgcwhrLookbackDay != null && mgcwhrLookbackDay >= 10) {
            b.lookbackDay(mgcwhrLookbackDay);
        }
        if (mgcwhrLookbackWeek != null && mgcwhrLookbackWeek >= 10) {
            b.lookbackWeek(mgcwhrLookbackWeek);
        }
        if (mgcwhrLookbackMonth != null && mgcwhrLookbackMonth >= 6) {
            b.lookbackMonth(mgcwhrLookbackMonth);
        }
        if (mgcwhrEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(mgcwhrEnableMinAmountFilter);
        }
        if (mgcwhrMinAmountWan != null && mgcwhrMinAmountWan >= 0) {
            b.minAvgAmount(mgcwhrMinAmountWan * 10_000D);
        }
        return MacdGcWaveHighRetestStrategyParams.merge(b.build());
    }

    public MacdGcWaveHighLiftStrategyParams toMacdGcWaveHighLiftParams() {
        MacdGcWaveHighLiftStrategyParams.MacdGcWaveHighLiftStrategyParamsBuilder b =
                MacdGcWaveHighLiftStrategyParams.builder();
        if (mgcwhuTier != null && !mgcwhuTier.trim().isEmpty()) {
            b.tier(MacdGcWaveHighLiftStrategyParams.parseTier(mgcwhuTier));
        }
        if (mgcwhuLookbackDay != null && mgcwhuLookbackDay >= 10) {
            b.lookbackDay(mgcwhuLookbackDay);
        }
        if (mgcwhuLookbackWeek != null && mgcwhuLookbackWeek >= 10) {
            b.lookbackWeek(mgcwhuLookbackWeek);
        }
        if (mgcwhuLookbackMonth != null && mgcwhuLookbackMonth >= 6) {
            b.lookbackMonth(mgcwhuLookbackMonth);
        }
        if (mgcwhuEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(mgcwhuEnableMinAmountFilter);
        }
        if (mgcwhuMinAmountWan != null && mgcwhuMinAmountWan >= 0) {
            b.minAvgAmount(mgcwhuMinAmountWan * 10_000D);
        }
        if (mgcwhuEnableSignalRiseGate != null) {
            b.enableSignalRiseGate(mgcwhuEnableSignalRiseGate);
        }
        if (mgcwhuSignalRisePct != null && mgcwhuSignalRisePct > 0) {
            b.signalRisePct(mgcwhuSignalRisePct / 100D);
        }
        if (mgRequireDayMacd != null) {
            b.requireDayMacd(mgRequireDayMacd);
        }
        if (mgRequireWeekMacd != null) {
            b.requireWeekMacd(mgRequireWeekMacd);
        }
        if (mgRequireMonthMacd != null) {
            b.requireMonthMacd(mgRequireMonthMacd);
        }
        if (mgRequireMin60Macd != null) {
            b.requireMin60Macd(mgRequireMin60Macd);
        }
        return MacdGcWaveHighLiftStrategyParams.merge(b.build());
    }

    public MacdDcBreakoutStrategyParams toMacdDcBreakoutParams() {
        MacdDcBreakoutStrategyParams.MacdDcBreakoutStrategyParamsBuilder b =
                MacdDcBreakoutStrategyParams.builder();
        if (mdcbTier != null && !mdcbTier.trim().isEmpty()) {
            b.tier(MacdDcBreakoutStrategyParams.parseTier(mdcbTier));
        }
        if (mdcbLookbackDay != null && mdcbLookbackDay >= 10) {
            b.lookbackDay(mdcbLookbackDay);
        }
        if (mdcbLookbackWeek != null && mdcbLookbackWeek >= 10) {
            b.lookbackWeek(mdcbLookbackWeek);
        }
        if (mdcbLookbackMonth != null && mdcbLookbackMonth >= 6) {
            b.lookbackMonth(mdcbLookbackMonth);
        }
        if (mdcbEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(mdcbEnableMinAmountFilter);
        }
        if (mdcbMinAmountWan != null && mdcbMinAmountWan >= 0) {
            b.minAvgAmount(mdcbMinAmountWan * 10_000D);
        }
        if (mdcbEnableSignalRiseGate != null) {
            b.enableSignalRiseGate(mdcbEnableSignalRiseGate);
        }
        if (mdcbSignalRisePct != null && mdcbSignalRisePct > 0) {
            b.signalRisePct(mdcbSignalRisePct / 100D);
        }
        return MacdDcBreakoutStrategyParams.merge(b.build());
    }

    public WaveCcBreakoutStrategyParams toWaveCcBreakoutParams() {
        WaveCcBreakoutStrategyParams.WaveCcBreakoutStrategyParamsBuilder b =
                WaveCcBreakoutStrategyParams.builder();
        if (wccbTier != null && !wccbTier.trim().isEmpty()) {
            b.tier(WaveCcBreakoutStrategyParams.parseTier(wccbTier));
        }
        if (wccbLookbackDay != null && wccbLookbackDay >= 10) {
            b.lookbackDay(wccbLookbackDay);
        }
        if (wccbLookbackWeek != null && wccbLookbackWeek >= 10) {
            b.lookbackWeek(wccbLookbackWeek);
        }
        if (wccbLookbackMonth != null && wccbLookbackMonth >= 6) {
            b.lookbackMonth(wccbLookbackMonth);
        }
        if (wccbEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(wccbEnableMinAmountFilter);
        }
        if (wccbMinAmountWan != null && wccbMinAmountWan >= 0) {
            b.minAvgAmount(wccbMinAmountWan * 10_000D);
        }
        if (wccbEnableSignalRiseGate != null) {
            b.enableSignalRiseGate(wccbEnableSignalRiseGate);
        }
        if (wccbSignalRisePct != null && wccbSignalRisePct > 0) {
            b.signalRisePct(wccbSignalRisePct / 100D);
        }
        return WaveCcBreakoutStrategyParams.merge(b.build());
    }

    public MacdPositiveGateParams toMacdPositiveGateParams() {
        MacdPositiveGateParams.MacdPositiveGateParamsBuilder b = MacdPositiveGateParams.builder();
        if (mgRequireDayMacd != null) {
            b.requireDayMacd(mgRequireDayMacd);
        } else if (ulRequireDayMacd != null) {
            b.requireDayMacd(ulRequireDayMacd);
        }
        if (mgRequireWeekMacd != null) {
            b.requireWeekMacd(mgRequireWeekMacd);
        } else if (ulRequireWeekMacd != null) {
            b.requireWeekMacd(ulRequireWeekMacd);
        }
        if (mgRequireMonthMacd != null) {
            b.requireMonthMacd(mgRequireMonthMacd);
        } else if (ulRequireMonthMacd != null) {
            b.requireMonthMacd(ulRequireMonthMacd);
        }
        return MacdPositiveGateParams.merge(b.build());
    }

    public UltraGcBreakoutStrategyParams toUltraGcBreakoutParams() {
        UltraGcBreakoutStrategyParams.UltraGcBreakoutStrategyParamsBuilder b =
                UltraGcBreakoutStrategyParams.builder();
        if (ulgcPrevDays != null && ulgcPrevDays >= 0) {
            b.prevDays(ulgcPrevDays);
        }
        if (ulgcMaxBarsPerDay != null && ulgcMaxBarsPerDay >= 1) {
            b.maxBarsPerDay(ulgcMaxBarsPerDay);
        }
        if (ulgcGcLookbackBars != null && ulgcGcLookbackBars >= 5) {
            b.gcLookbackBars(ulgcGcLookbackBars);
        }
        if (ulgcSignalRisePct != null && ulgcSignalRisePct > 0) {
            b.signalRisePct(ulgcSignalRisePct / 100D);
        }
        return UltraGcBreakoutStrategyParams.merge(b.build());
    }

    public DayMin60ComboStrategyParams toDayMin60ComboParams() {
        DayMin60ComboStrategyParams.DayMin60ComboStrategyParamsBuilder b =
                DayMin60ComboStrategyParams.builder();
        if (dm60PrevDays != null && dm60PrevDays >= 0) {
            b.prevDays(dm60PrevDays);
        }
        if (dm60MaxBarsPerDay != null && dm60MaxBarsPerDay >= 1) {
            b.maxBarsPerDay(dm60MaxBarsPerDay);
        }
        if (dm60GcLookbackBars != null && dm60GcLookbackBars >= 5) {
            b.gcLookbackBars(dm60GcLookbackBars);
        }
        if (dm60EnableMinAmountFilter != null) {
            b.enableMinAmountFilter(dm60EnableMinAmountFilter);
        }
        if (dm60MinAmountWan != null && dm60MinAmountWan >= 0) {
            b.minAvgAmount(dm60MinAmountWan * 10_000D);
        }
        if (dm60EnableSignalRiseGate != null) {
            b.enableSignalRiseGate(dm60EnableSignalRiseGate);
        }
        if (dm60SignalRisePct != null && dm60SignalRisePct > 0) {
            b.signalRisePct(dm60SignalRisePct / 100D);
        }
        return DayMin60ComboStrategyParams.merge(b.build());
    }

    public WeekMin60ComboStrategyParams toWeekMin60ComboParams() {
        WeekMin60ComboStrategyParams.WeekMin60ComboStrategyParamsBuilder b =
                WeekMin60ComboStrategyParams.builder();
        if (wm60PrevWeeks != null && wm60PrevWeeks >= 0) {
            b.prevWeeks(wm60PrevWeeks);
        }
        if (wm60MaxBarsPerWeek != null && wm60MaxBarsPerWeek >= 1) {
            b.maxBarsPerWeek(wm60MaxBarsPerWeek);
        }
        if (wm60GcLookbackBars != null && wm60GcLookbackBars >= 5) {
            b.gcLookbackBars(wm60GcLookbackBars);
        }
        if (wm60EnableMinAmountFilter != null) {
            b.enableMinAmountFilter(wm60EnableMinAmountFilter);
        }
        if (wm60MinAmountWan != null && wm60MinAmountWan >= 0) {
            b.minAvgAmount(wm60MinAmountWan * 10_000D);
        }
        if (wm60EnableSignalRiseGate != null) {
            b.enableSignalRiseGate(wm60EnableSignalRiseGate);
        }
        if (wm60SignalRisePct != null && wm60SignalRisePct > 0) {
            b.signalRisePct(wm60SignalRisePct / 100D);
        }
        return WeekMin60ComboStrategyParams.merge(b.build());
    }

    public DayWeekComboStrategyParams toDayWeekComboParams() {
        DayWeekComboStrategyParams.DayWeekComboStrategyParamsBuilder b =
                DayWeekComboStrategyParams.builder();
        if (dwPrevWeeks != null && dwPrevWeeks >= 0) {
            b.prevWeeks(dwPrevWeeks);
        }
        if (dwMaxBarsPerWeek != null && dwMaxBarsPerWeek >= 1) {
            b.maxBarsPerWeek(dwMaxBarsPerWeek);
        }
        if (dwGcLookbackBars != null && dwGcLookbackBars >= 5) {
            b.gcLookbackBars(dwGcLookbackBars);
        }
        if (dwEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(dwEnableMinAmountFilter);
        }
        if (dwMinAmountWan != null && dwMinAmountWan >= 0) {
            b.minAvgAmount(dwMinAmountWan * 10_000D);
        }
        if (dwEnableSignalRiseGate != null) {
            b.enableSignalRiseGate(dwEnableSignalRiseGate);
        }
        if (dwSignalRisePct != null && dwSignalRisePct > 0) {
            b.signalRisePct(dwSignalRisePct / 100D);
        }
        return DayWeekComboStrategyParams.merge(b.build());
    }

    public DayMonthComboStrategyParams toDayMonthComboParams() {
        DayMonthComboStrategyParams.DayMonthComboStrategyParamsBuilder b =
                DayMonthComboStrategyParams.builder();
        if (dmonPrevMonths != null && dmonPrevMonths >= 0) {
            b.prevMonths(dmonPrevMonths);
        }
        if (dmonMaxBarsPerMonth != null && dmonMaxBarsPerMonth >= 1) {
            b.maxBarsPerMonth(dmonMaxBarsPerMonth);
        }
        if (dmonGcLookbackBars != null && dmonGcLookbackBars >= 5) {
            b.gcLookbackBars(dmonGcLookbackBars);
        }
        if (dmonEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(dmonEnableMinAmountFilter);
        }
        if (dmonMinAmountWan != null && dmonMinAmountWan >= 0) {
            b.minAvgAmount(dmonMinAmountWan * 10_000D);
        }
        if (dmonEnableSignalRiseGate != null) {
            b.enableSignalRiseGate(dmonEnableSignalRiseGate);
        }
        if (dmonSignalRisePct != null && dmonSignalRisePct > 0) {
            b.signalRisePct(dmonSignalRisePct / 100D);
        }
        return DayMonthComboStrategyParams.merge(b.build());
    }

    public Min60WaveCcBreakoutStrategyParams toMin60WaveCcBreakoutParams() {
        Min60WaveCcBreakoutStrategyParams.Min60WaveCcBreakoutStrategyParamsBuilder b =
                Min60WaveCcBreakoutStrategyParams.builder();
        if (m60wccbPrevDays != null && m60wccbPrevDays >= 0) {
            b.prevDays(m60wccbPrevDays);
        }
        if (m60wccbMaxBarsPerDay != null && m60wccbMaxBarsPerDay >= 1) {
            b.maxBarsPerDay(m60wccbMaxBarsPerDay);
        }
        if (m60wccbLookbackBars != null && m60wccbLookbackBars >= 10) {
            b.lookbackBars(m60wccbLookbackBars);
        }
        if (m60wccbEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(m60wccbEnableMinAmountFilter);
        }
        if (m60wccbMinAmountWan != null && m60wccbMinAmountWan >= 0) {
            b.minAvgAmount(m60wccbMinAmountWan * 10_000D);
        }
        if (m60wccbEnableSignalRiseGate != null) {
            b.enableSignalRiseGate(m60wccbEnableSignalRiseGate);
        }
        if (m60wccbSignalRisePct != null && m60wccbSignalRisePct > 0) {
            b.signalRisePct(m60wccbSignalRisePct / 100D);
        }
        if (m60wccbRequireCurrentBreakout != null) {
            b.requireCurrentBreakout(m60wccbRequireCurrentBreakout);
        }
        return Min60WaveCcBreakoutStrategyParams.merge(b.build());
    }

    public DayWaveCcBreakoutStrategyParams toDayWaveCcBreakoutParams() {
        DayWaveCcBreakoutStrategyParams.DayWaveCcBreakoutStrategyParamsBuilder b =
                DayWaveCcBreakoutStrategyParams.builder();
        if (dwccbPrevDays != null && dwccbPrevDays >= 0) {
            b.prevDays(dwccbPrevDays);
        }
        if (dwccbMaxBarsPerDay != null && dwccbMaxBarsPerDay >= 1) {
            b.maxBarsPerDay(dwccbMaxBarsPerDay);
        }
        if (dwccbLookbackBars != null && dwccbLookbackBars >= 10) {
            b.lookbackBars(dwccbLookbackBars);
        }
        if (dwccbEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(dwccbEnableMinAmountFilter);
        }
        if (dwccbMinAmountWan != null && dwccbMinAmountWan >= 0) {
            b.minAvgAmount(dwccbMinAmountWan * 10_000D);
        }
        if (dwccbEnableSignalRiseGate != null) {
            b.enableSignalRiseGate(dwccbEnableSignalRiseGate);
        }
        if (dwccbSignalRisePct != null && dwccbSignalRisePct > 0) {
            b.signalRisePct(dwccbSignalRisePct / 100D);
        }
        if (dwccbRequireCurrentBreakout != null) {
            b.requireCurrentBreakout(dwccbRequireCurrentBreakout);
        }
        return DayWaveCcBreakoutStrategyParams.merge(b.build());
    }

    public WeekWaveCcBreakoutStrategyParams toWeekWaveCcBreakoutParams() {
        WeekWaveCcBreakoutStrategyParams.WeekWaveCcBreakoutStrategyParamsBuilder b =
                WeekWaveCcBreakoutStrategyParams.builder();
        if (wwccbLookbackBars != null && wwccbLookbackBars >= 10) {
            b.lookbackBars(wwccbLookbackBars);
        }
        if (wwccbEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(wwccbEnableMinAmountFilter);
        }
        if (wwccbMinAmountWan != null && wwccbMinAmountWan >= 0) {
            b.minAvgAmount(wwccbMinAmountWan * 10_000D);
        }
        if (wwccbEnableSignalRiseGate != null) {
            b.enableSignalRiseGate(wwccbEnableSignalRiseGate);
        }
        if (wwccbSignalRisePct != null && wwccbSignalRisePct > 0) {
            b.signalRisePct(wwccbSignalRisePct / 100D);
        }
        if (wwccbMaxRetestGapPct != null && wwccbMaxRetestGapPct > 0) {
            b.maxRetestGapPct(wwccbMaxRetestGapPct / 100D);
        }
        return WeekWaveCcBreakoutStrategyParams.merge(b.build());
    }

    public MonthWaveCcBreakoutStrategyParams toMonthWaveCcBreakoutParams() {
        MonthWaveCcBreakoutStrategyParams.MonthWaveCcBreakoutStrategyParamsBuilder b =
                MonthWaveCcBreakoutStrategyParams.builder();
        if (mwccbLookbackBars != null && mwccbLookbackBars >= 6) {
            b.lookbackBars(mwccbLookbackBars);
        }
        if (mwccbEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(mwccbEnableMinAmountFilter);
        }
        if (mwccbMinAmountWan != null && mwccbMinAmountWan >= 0) {
            b.minAvgAmount(mwccbMinAmountWan * 10_000D);
        }
        if (mwccbEnableSignalRiseGate != null) {
            b.enableSignalRiseGate(mwccbEnableSignalRiseGate);
        }
        if (mwccbSignalRisePct != null && mwccbSignalRisePct > 0) {
            b.signalRisePct(mwccbSignalRisePct / 100D);
        }
        if (mwccbMaxRetestGapPct != null && mwccbMaxRetestGapPct > 0) {
            b.maxRetestGapPct(mwccbMaxRetestGapPct / 100D);
        }
        return MonthWaveCcBreakoutStrategyParams.merge(b.build());
    }

    public TrendWaveCcBreakoutStrategyParams toTrendWaveCcBreakoutParams() {
        TrendWaveCcBreakoutStrategyParams.TrendWaveCcBreakoutStrategyParamsBuilder b =
                TrendWaveCcBreakoutStrategyParams.builder();
        if (twccbSignalPeriod != null && !twccbSignalPeriod.isEmpty()) {
            b.signalPeriod(twccbSignalPeriod);
        }
        if (twccbPrevDays != null && twccbPrevDays >= 0) {
            b.prevDays(twccbPrevDays);
        }
        if (twccbMaxBarsPerDay != null && twccbMaxBarsPerDay >= 1) {
            b.maxBarsPerDay(twccbMaxBarsPerDay);
        }
        if (twccbLookbackBars != null && twccbLookbackBars >= 10) {
            b.lookbackBars(twccbLookbackBars);
        }
        if (twccbEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(twccbEnableMinAmountFilter);
        }
        if (twccbMinAmountWan != null && twccbMinAmountWan >= 0) {
            b.minAvgAmount(twccbMinAmountWan * 10_000D);
        }
        if (twccbEnableSignalRiseGate != null) {
            b.enableSignalRiseGate(twccbEnableSignalRiseGate);
        }
        if (twccbSignalRisePct != null && twccbSignalRisePct > 0) {
            b.signalRisePct(twccbSignalRisePct / 100D);
        }
        if (twccbRequireDayMacd != null) {
            b.requireDayMacd(twccbRequireDayMacd);
        }
        if (twccbRequireWeekMacd != null) {
            b.requireWeekMacd(twccbRequireWeekMacd);
        }
        if (twccbRequireMonthMacd != null) {
            b.requireMonthMacd(twccbRequireMonthMacd);
        }
        return TrendWaveCcBreakoutStrategyParams.merge(b.build());
    }

    public TrendLiftStrategyParams toTrendLiftParams() {
        TrendLiftStrategyParams.TrendLiftStrategyParamsBuilder b = TrendLiftStrategyParams.builder();
        if (tlEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(tlEnableMinAmountFilter);
        }
        if (tlMinAmountWan != null && tlMinAmountWan >= 0) {
            b.minAvgAmount(tlMinAmountWan * 10_000D);
        }
        return TrendLiftStrategyParams.merge(b.build());
    }

    public TrendMaStrategyParams toTrendMaParams() {
        TrendMaStrategyParams.TrendMaStrategyParamsBuilder b = TrendMaStrategyParams.builder();
        if (tmaEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(tmaEnableMinAmountFilter);
        }
        if (tmaMinAmountWan != null && tmaMinAmountWan >= 0) {
            b.minAvgAmount(tmaMinAmountWan * 10_000D);
        }
        return TrendMaStrategyParams.merge(b.build());
    }

    public TrendRetestLowStrategyParams toTrendRetestLowParams() {
        TrendRetestLowStrategyParams.TrendRetestLowStrategyParamsBuilder b =
                TrendRetestLowStrategyParams.builder();
        if (trlTier != null && !trlTier.trim().isEmpty()) {
            b.tier(TrendRetestLowStrategyParams.parseTier(trlTier));
        }
        if (trlEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(trlEnableMinAmountFilter);
        }
        if (trlMinAmountWan != null && trlMinAmountWan >= 0) {
            b.minAvgAmount(trlMinAmountWan * 10_000D);
        }
        return TrendRetestLowStrategyParams.merge(b.build());
    }

    public TrendRetestHighStrategyParams toTrendRetestHighParams() {
        TrendRetestHighStrategyParams.TrendRetestHighStrategyParamsBuilder b =
                TrendRetestHighStrategyParams.builder();
        if (trhTier != null && !trhTier.trim().isEmpty()) {
            b.tier(TrendRetestHighStrategyParams.parseTier(trhTier));
        }
        if (trhEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(trhEnableMinAmountFilter);
        }
        if (trhMinAmountWan != null && trhMinAmountWan >= 0) {
            b.minAvgAmount(trhMinAmountWan * 10_000D);
        }
        return TrendRetestHighStrategyParams.merge(b.build());
    }

    public BottomBandHighStrategyParams toBottomBandHighParams() {
        BottomBandHighStrategyParams.BottomBandHighStrategyParamsBuilder b =
                BottomBandHighStrategyParams.builder();
        if (bbhTier != null && !bbhTier.trim().isEmpty()) {
            b.tier(BottomBandHighStrategyParams.parseTier(bbhTier));
        }
        if (bbhEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(bbhEnableMinAmountFilter);
        }
        if (bbhMinAmountWan != null && bbhMinAmountWan >= 0) {
            b.minAvgAmount(bbhMinAmountWan * 10_000D);
        }
        return BottomBandHighStrategyParams.merge(b.build());
    }

    public BottomPrev2HighStrategyParams toBottomPrev2HighParams() {
        BottomPrev2HighStrategyParams.BottomPrev2HighStrategyParamsBuilder b =
                BottomPrev2HighStrategyParams.builder();
        if (bp2hTier != null && !bp2hTier.trim().isEmpty()) {
            b.tier(BottomPrev2HighStrategyParams.parseTier(bp2hTier));
        }
        if (bp2hEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(bp2hEnableMinAmountFilter);
        }
        if (bp2hMinAmountWan != null && bp2hMinAmountWan >= 0) {
            b.minAvgAmount(bp2hMinAmountWan * 10_000D);
        }
        return BottomPrev2HighStrategyParams.merge(b.build());
    }

    public MaBull3mStrategyParams toMaBull3mParams() {
        MaBull3mStrategyParams.MaBull3mStrategyParamsBuilder b =
                MaBull3mStrategyParams.builder();
        if (m3mTier != null && !m3mTier.trim().isEmpty()) {
            b.tier(MaBull3mStrategyParams.parseTier(m3mTier));
        }
        if (m3mEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(m3mEnableMinAmountFilter);
        }
        if (m3mMinAmountWan != null && m3mMinAmountWan >= 0) {
            b.minAvgAmount(m3mMinAmountWan * 10_000D);
        }
        return MaBull3mStrategyParams.merge(b.build());
    }

    public MaBreakMaStrategyParams toMaBreakMaParams() {
        MaBreakMaStrategyParams.MaBreakMaStrategyParamsBuilder b =
                MaBreakMaStrategyParams.builder();
        if (mbmTier != null && !mbmTier.trim().isEmpty()) {
            b.tier(MaBreakMaStrategyParams.parseTier(mbmTier));
        }
        if (mbmEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(mbmEnableMinAmountFilter);
        }
        if (mbmMinAmountWan != null && mbmMinAmountWan >= 0) {
            b.minAvgAmount(mbmMinAmountWan * 10_000D);
        }
        if (mbmEnableMin30BreakFilter != null) {
            b.enableMin30BreakFilter(mbmEnableMin30BreakFilter);
        }
        if (mbmRequireDayAlign != null) {
            b.requireDayAlign(mbmRequireDayAlign);
        }
        if (mbmRequireWeekAlign != null) {
            b.requireWeekAlign(mbmRequireWeekAlign);
        }
        if (mbmRequireMonthAlign != null) {
            b.requireMonthAlign(mbmRequireMonthAlign);
        }
        return MaBreakMaStrategyParams.merge(b.build());
    }

    public MaCrossPointStrategyParams toMaGoldBreakParams() {
        return toMaCrossPointParams(mgbTier, mgbEnableMinAmountFilter, mgbMinAmountWan, mgbEnableRightTrend);
    }

    public MaCrossPointStrategyParams toMaDeathBreakParams() {
        return toMaCrossPointParams(mdbTier, mdbEnableMinAmountFilter, mdbMinAmountWan, mdbEnableRightTrend);
    }

    public MaCrossPointStrategyParams toPrevBandHighParams() {
        return toMaCrossPointParams(pbhTier, pbhEnableMinAmountFilter, pbhMinAmountWan, pbhEnableRightTrend);
    }

    private MaCrossPointStrategyParams toMaCrossPointParams(String tier, Boolean enableMinAmountFilter,
                                                           Integer minAmountWan, Boolean enableRightTrend) {
        MaCrossPointStrategyParams.MaCrossPointStrategyParamsBuilder b =
                MaCrossPointStrategyParams.builder();
        if (tier != null && !tier.trim().isEmpty()) {
            b.tier(MaCrossPointStrategyParams.parseTier(tier));
        }
        if (enableMinAmountFilter != null) {
            b.enableMinAmountFilter(enableMinAmountFilter);
        }
        if (minAmountWan != null && minAmountWan >= 0) {
            b.minAvgAmount(minAmountWan * 10_000D);
        }
        if (enableRightTrend != null) {
            b.enableRightTrend(enableRightTrend);
        }
        return MaCrossPointStrategyParams.merge(b.build());
    }

    public MaBearBreakMaStrategyParams toMaBearBreakMaParams() {
        MaBearBreakMaStrategyParams.MaBearBreakMaStrategyParamsBuilder b =
                MaBearBreakMaStrategyParams.builder();
        if (mbbmEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(mbbmEnableMinAmountFilter);
        }
        if (mbbmMinAmountWan != null && mbbmMinAmountWan >= 0) {
            b.minAvgAmount(mbbmMinAmountWan * 10_000D);
        }
        return MaBearBreakMaStrategyParams.merge(b.build());
    }

    public MaBull4mStrategyParams toMaBull4mParams() {
        MaBull4mStrategyParams.MaBull4mStrategyParamsBuilder b =
                MaBull4mStrategyParams.builder();
        if (m4mTier != null && !m4mTier.trim().isEmpty()) {
            b.tier(MaBull4mStrategyParams.parseTier(m4mTier));
        }
        if (m4mEnableMinAmountFilter != null) {
            b.enableMinAmountFilter(m4mEnableMinAmountFilter);
        }
        if (m4mMinAmountWan != null && m4mMinAmountWan >= 0) {
            b.minAvgAmount(m4mMinAmountWan * 10_000D);
        }
        return MaBull4mStrategyParams.merge(b.build());
    }

    /** @deprecated 兼容旧 waveband 参数 */
    public WaveBandStrategyParams toWaveBandParams() {
        WaveBandStrategyParams.WaveBandStrategyParamsBuilder b = WaveBandStrategyParams.builder();
        if (wbLookbackDay != null && wbLookbackDay >= 10) {
            b.lookbackDay(wbLookbackDay);
        }
        if (wbLookbackWeek != null && wbLookbackWeek >= 10) {
            b.lookbackWeek(wbLookbackWeek);
        }
        if (wbLookbackMonth != null && wbLookbackMonth >= 6) {
            b.lookbackMonth(wbLookbackMonth);
        }
        if (wbMinAmountWan != null && wbMinAmountWan >= 0) {
            b.minAvgAmount(wbMinAmountWan * 10_000D);
        }
        return WaveBandStrategyParams.merge(b.build());
    }

}