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

    /** 单边趋势 · 日 K 平台回溯根数 */
    private Integer uDayLookback;
    /** 单边趋势 · 强阳阈值（%，如 3 表示 3%） */
    private Double uStrongYangPct;
    /** 单边趋势 · 周 K 环境最少满足项数 1-3 */
    private Integer uWeekContextMin;
    /** 单边趋势 · 最低日均成交额（万） */
    private Integer uMinAmountWan;
    private Boolean uEnableModeB;
    private Boolean uEnableModeA;
    private Boolean uEnableModeBWeak;
    /** 单边趋势 · 最低档位 ALL/S/A/B/C */
    private String uTierMin;

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

    /** 多周期强势 · 最少共振周期数 1-3 */
    private Integer mMinResonancePeriods;
    /** 多周期强势 · 最低日均成交额（万） */
    private Integer mMinAmountWan;
    private Boolean mEnableModeA;
    private Boolean mEnableModeB;
    private Boolean mEnableModeC;
    private Boolean mEnableWeakContext;
    private Boolean mEnableWeakSingle;
    /** 多周期强势 · 最低档位 ALL/S/A/B/C */
    private String mTierMin;

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
        return StrategyTypeEnum.getByCode(this.strategy);
    }

    public UnilateralStrategyParams toUnilateralParams() {
        UnilateralStrategyParams.UnilateralStrategyParamsBuilder b = UnilateralStrategyParams.builder();
        if (uDayLookback != null) {
            b.dayPlatformLookback(uDayLookback);
        }
        if (uStrongYangPct != null) {
            b.strongYangRate(uStrongYangPct / 100.0);
        }
        if (uWeekContextMin != null) {
            b.weekContextMin(uWeekContextMin);
        }
        if (uMinAmountWan != null) {
            b.minAvgAmount(uMinAmountWan * 10_000D);
        }
        if (uEnableModeB != null) {
            b.enableModeB(uEnableModeB);
        }
        if (uEnableModeA != null) {
            b.enableModeA(uEnableModeA);
        }
        if (uEnableModeBWeak != null) {
            b.enableModeBWeak(uEnableModeBWeak);
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

    public MultiStrategyParams toMultiParams() {
        MultiStrategyParams.MultiStrategyParamsBuilder b = MultiStrategyParams.builder();
        if (mMinResonancePeriods != null) {
            b.minResonancePeriods(mMinResonancePeriods);
        }
        if (mMinAmountWan != null) {
            b.minAvgAmount(mMinAmountWan * 10_000D);
        }
        if (mEnableModeA != null) {
            b.enableModeA(mEnableModeA);
        }
        if (mEnableModeB != null) {
            b.enableModeB(mEnableModeB);
        }
        if (mEnableModeC != null) {
            b.enableModeC(mEnableModeC);
        }
        if (mEnableWeakContext != null) {
            b.enableWeakContext(mEnableWeakContext);
        }
        if (mEnableWeakSingle != null) {
            b.enableWeakSinglePeriod(mEnableWeakSingle);
        }
        if (mTierMin != null && !mTierMin.isEmpty()) {
            b.tierMin(mTierMin);
        }
        return MultiStrategyParams.merge(b.build());
    }
	
}