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

    /** 深坑反弹 · 最低日均成交额（万） */
    private Integer rMinAmountWan;
    /** 深坑反弹 · 日 K 恐慌大跌阈值（%） */
    private Double rCapitulationDayPct;
    /** 深坑反弹 · 周 K 恐慌大跌阈值（%） */
    private Double rCapitulationWeekPct;
    /** 深坑反弹 · 相对 52 周高最小回撤（%） */
    private Double rMinDrawdownPct;
    /** 深坑反弹 · 恐慌日 K 回溯根数 */
    private Integer rCapitulationLookbackDays;
    /** 深坑反弹 · 恐慌周 K 回溯根数 */
    private Integer rCapitulationLookbackWeeks;
    private Boolean rEnableModeA;
    private Boolean rEnableModeB;
    private Boolean rEnableModeC;
    /** 深坑反弹 · 最低档位 ALL/S/A/B/C */
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
        if (rCapitulationDayPct != null) {
            incoming.setCapitulationDayRate(rCapitulationDayPct / 100.0);
        }
        if (rCapitulationWeekPct != null) {
            incoming.setCapitulationWeekRate(rCapitulationWeekPct / 100.0);
        }
        if (rMinDrawdownPct != null) {
            incoming.setMinDrawdownFrom52w(rMinDrawdownPct / 100.0);
        }
        if (rCapitulationLookbackDays != null) {
            incoming.setCapitulationLookbackDays(rCapitulationLookbackDays);
        }
        if (rCapitulationLookbackWeeks != null) {
            incoming.setCapitulationLookbackWeeks(rCapitulationLookbackWeeks);
        }
        if (rEnableModeA != null) {
            incoming.setEnableModeA(rEnableModeA);
        }
        if (rEnableModeB != null) {
            incoming.setEnableModeB(rEnableModeB);
        }
        if (rEnableModeC != null) {
            incoming.setEnableModeC(rEnableModeC);
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
	
}