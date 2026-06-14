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

    /** 超短线 · 最低日均成交额（万） */
    private Integer lMinAmountWan;
    private String lTierMin;
    /** 30m 大阳线/大涨幅下限（0~1） */
    private Double lMin30BodyPct;
    /** 向前几个交易日（不含当日，1~2） */
    private Integer lMin30PrevDays;
    /** 每交易日最多几根 30m */
    private Integer lMin30BarsPerDay;

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
            return StrategyTypeEnum.DEFAUL;
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
        if (lMin30BodyPct != null && lMin30BodyPct > 0) {
            b.min30BodyGainPct(lMin30BodyPct);
        }
        if (lMin30PrevDays != null && lMin30PrevDays >= 1) {
            b.min30PrevDays(lMin30PrevDays);
        }
        if (lMin30BarsPerDay != null && lMin30BarsPerDay > 0) {
            b.min30BarsPerDay(lMin30BarsPerDay);
        }
        return UltraLowReboundStrategyParams.merge(b.build());
    }
	
}