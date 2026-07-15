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
    private Integer mgcSignalRisePct;
    /** MACD金叉 · 启用近 N 根历史上涨率门 */
    private Boolean mgcEnableHistoryRiseGate;
    /** MACD金叉 · 历史检视 K 根数（不含末 K） */
    private Integer mgcHistoryLookbackBars;
    /** MACD金叉 · 历史上涨率阈值（%，如 3 表示 3%） */
    private Integer mgcHistoryRisePct;
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
    private Integer mgcwhSignalRisePct;
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
    private Integer mgcwhuSignalRisePct;
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
    private Integer mdcbSignalRisePct;
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
        if (ulRequireMonthMacd != null) {
            b.requireMonthMacd(ulRequireMonthMacd);
        }
        if (ulRequireWeekMacd != null) {
            b.requireWeekMacd(ulRequireWeekMacd);
        }
        if (ulRequireDayMacd != null) {
            b.requireDayMacd(ulRequireDayMacd);
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