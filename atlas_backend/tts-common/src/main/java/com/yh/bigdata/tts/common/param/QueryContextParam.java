package com.yh.bigdata.tts.common.param;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.common.param.base.PageQuery;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;

@Data
@Builder
public class QueryContextParam extends PageQuery {

    /**
     * 连板天数
     */
    private Integer lianBanDays = 1;

    /** 单边趋势策略自定义参数 */
    private UnilateralStrategyParams unilateral;

    /** 深坑反弹策略自定义参数 */
    private ReboundStrategyParams rebound;

    /** 预判金叉策略自定义参数 */
    private PreGoldenStrategyParams preGolden;

    /** 周期共振策略自定义参数 */
    private ResonanceStrategyParams resonance;

    /** 超低反弹策略自定义参数 */
    private UltraLowReboundStrategyParams ultraLow;

    /** 回踩抬升策略自定义参数 */
    private RetestStrategyParams retest;

    /** 金叉二次突破策略自定义参数 */
    private Gc2StrategyParams gc2;

    /** 死叉突破策略自定义参数 */
    private Dc2StrategyParams dc2;

    /** 超短线策略自定义参数 */
    private UltraShortStrategyParams ultraShort;

    /** 趋势策略 v2 自定义参数 */
    private TrendV2StrategyParams trendV2;

    /** 中线策略自定义参数 */
    private MediumStrategyParams medium;

    /** 长线策略自定义参数 */
    private LongStrategyParams longTerm;

    /** 无阻力梯子策略自定义参数 */
    private FrictionlessLadderStrategyParams frictionlessLadder;

    /** 柱子内上移策略自定义参数 */
    private PillarStrategyParams pillar;

    /** 级联交叉突破策略自定义参数 */
    private CascadeStrategyParams cascade;

    public static QueryContextParam empty() {
        return QueryContextParam.builder()
                .lianBanDays(1)
                .unilateral(UnilateralStrategyParams.defaults())
                .preGolden(PreGoldenStrategyParams.defaults())
                .resonance(ResonanceStrategyParams.defaults())
                .rebound(ReboundStrategyParams.defaults())
                .ultraLow(UltraLowReboundStrategyParams.defaults())
                .retest(RetestStrategyParams.defaults())
                .gc2(Gc2StrategyParams.defaults())
                .dc2(Dc2StrategyParams.defaults())
                .ultraShort(UltraShortStrategyParams.defaults())
                .trendV2(TrendV2StrategyParams.defaults())
                .medium(MediumStrategyParams.defaults())
                .longTerm(LongStrategyParams.defaults())
                .frictionlessLadder(FrictionlessLadderStrategyParams.defaults())
                .pillar(PillarStrategyParams.defaults())
                .cascade(CascadeStrategyParams.defaults())
                .build();
    }
	
}