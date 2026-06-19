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
        return StrategyTypeEnum.getByCode(this.strategy);
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
        return LongStrategyParams.merge(b.build());
    }

}