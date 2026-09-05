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

    /** 级联梯子探底回升策略自定义参数 */
    private LadderDipStrategyParams ladderDip;

    /** MACD 交叉边沿突破策略自定义参数 */
    private MacdEdgeStrategyParams macdEdge;

    /** 凸波段突破策略自定义参数 */
    private WaveConvexStrategyParams waveConvex;

    /** 凹波段突破策略自定义参数 */
    private WaveConcaveStrategyParams waveConcave;

    /** 凸波段日突破策略自定义参数 */
    private WaveConvexDayStrategyParams waveConvexDay;

    /** 凹波段日突破策略自定义参数 */
    private WaveConcaveDayStrategyParams waveConcaveDay;

    /** 级联 MACD 凸波段同档突破策略自定义参数 */
    private CascadeWaveConvexStrategyParams cascadeWaveConvex;

    /** 级联 MACD 凹波段同档突破策略自定义参数 */
    private CascadeWaveConcaveStrategyParams cascadeWaveConcave;

    /** 级联 MACD 凸波段日突破策略自定义参数 */
    private CascadeWaveConvexDayStrategyParams cascadeWaveConvexDay;

    /** 级联 MACD 凹波段日突破策略自定义参数 */
    private CascadeWaveConcaveDayStrategyParams cascadeWaveConcaveDay;

    /** @deprecated 波段策略自定义参数 */
    private WaveBandStrategyParams waveBand;

    /** 波段策略短线自定义参数 */
    private WaveBandStrategyParams waveBandShort;

    /** 波段策略中线自定义参数 */
    private WaveBandStrategyParams waveBandMedium;

    /** 多周期波段形态门自定义参数 */
    private WavePeriodGateStrategyParams wavePeriodGate;

    /** 同档 MACD 交叉突破自定义参数 */
    private MacdCrossTierStrategyParams macdCrossTier;

    /** 凸波段上移自定义参数 */
    private ConvexLiftTierStrategyParams convexLiftTier;

    /** 柱子策略自定义参数 */
    private BodyBarTierStrategyParams bodyBarTier;

    /** MACD金叉自定义参数 */
    private MacdGoldenCrossStrategyParams macdGoldenCross;

    /** MACD金叉波段High突破自定义参数 */
    private MacdGcWaveHighStrategyParams macdGcWaveHigh;

    /** MACD金叉波段High回踩自定义参数 */
    private MacdGcWaveHighRetestStrategyParams macdGcWaveHighRetest;

    /** MACD金叉波段High上移自定义参数 */
    private MacdGcWaveHighLiftStrategyParams macdGcWaveHighLift;

    /** MACD死叉突破自定义参数 */
    private MacdDcBreakoutStrategyParams macdDcBreakout;

    /** 多周期 MACD&gt;0 可选门（小程序各策略共用） */
    private MacdPositiveGateParams macdPositiveGate;

    /** 超短线 · MACD金叉K突破 */
    private UltraGcBreakoutStrategyParams ultraGcBreakout;

    /** 凹凸突破（waveccbreak） */
    private WaveCcBreakoutStrategyParams waveCcBreakout;

    /** 日小时组合（daymin60） */
    private DayMin60ComboStrategyParams dayMin60Combo;

    /** 小时周组合（weekmin60） */
    private WeekMin60ComboStrategyParams weekMin60Combo;

    /** 日周组合（dayweek） */
    private DayWeekComboStrategyParams dayWeekCombo;

    /** 日月组合（daymonth） */
    private DayMonthComboStrategyParams dayMonthCombo;

    /** 小时凹凸突破（min60wavecc） */
    private Min60WaveCcBreakoutStrategyParams min60WaveCcBreakout;

    /** 日凹凸突破（daywavecc） */
    private DayWaveCcBreakoutStrategyParams dayWaveCcBreakout;

    /** 周凹凸突破（weekwavecc） */
    private WeekWaveCcBreakoutStrategyParams weekWaveCcBreakout;

    /** 月凹凸突破（monthwavecc） */
    private MonthWaveCcBreakoutStrategyParams monthWaveCcBreakout;

    /** 趋势内凹凸突破（trendwavecc） */
    private TrendWaveCcBreakoutStrategyParams trendWaveCcBreakout;

    /** 趋势上移（trendlift） */
    private TrendLiftStrategyParams trendLift;

    /** 趋势MA（trendma） */
    private TrendMaStrategyParams trendMa;

    /** 底部波段突破（bottombandhigh） */
    private BottomBandHighStrategyParams bottomBandHigh;

    /** 底部 High 突破（bottomprev2high） */
    private BottomPrev2HighStrategyParams bottomPrev2High;

    /** MA金叉点突破（magoldbreak） */
    private MaCrossPointStrategyParams maGoldBreak;

    /** MA死叉点突破（madeathbreak） */
    private MaCrossPointStrategyParams maDeathBreak;

    /** 死叉交叉点突破（madcbreak） */
    private MaCrossPointStrategyParams maDeathCrossBreak;

    /** 金叉交叉点突破（magcbreak） */
    private MaCrossPointStrategyParams maGoldenCrossBreak;

    /** 金叉波段顶突破（maghbreak） */
    private MaCrossPointStrategyParams maGoldenHighBreak;

    /** 一阳穿多线（mayangpierce） */
    private MaCrossPointStrategyParams maYangPierce;

    /** MA空头破MA（mabearbreakma） */
    private MaBearBreakMaStrategyParams maBearBreakMa;

    /** MA多头4M排列（mabull4m） */
    private MaBull4mStrategyParams maBull4m;

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
                .ladderDip(LadderDipStrategyParams.defaults())
                .macdEdge(MacdEdgeStrategyParams.defaults())
                .waveConvex(WaveConvexStrategyParams.defaults())
                .waveConcave(WaveConcaveStrategyParams.defaults())
                .waveConvexDay(WaveConvexDayStrategyParams.defaults())
                .waveConcaveDay(WaveConcaveDayStrategyParams.defaults())
                .cascadeWaveConvex(CascadeWaveConvexStrategyParams.defaults())
                .cascadeWaveConcave(CascadeWaveConcaveStrategyParams.defaults())
                .cascadeWaveConvexDay(CascadeWaveConvexDayStrategyParams.defaults())
                .cascadeWaveConcaveDay(CascadeWaveConcaveDayStrategyParams.defaults())
                .waveBand(WaveBandStrategyParams.defaults())
                .waveBandShort(WaveBandStrategyParams.defaults())
                .waveBandMedium(WaveBandStrategyParams.defaults())
                .wavePeriodGate(WavePeriodGateStrategyParams.defaults())
                .macdCrossTier(MacdCrossTierStrategyParams.defaults())
                .convexLiftTier(ConvexLiftTierStrategyParams.defaults())
                .bodyBarTier(BodyBarTierStrategyParams.defaults())
                .macdGoldenCross(MacdGoldenCrossStrategyParams.defaults())
                .macdGcWaveHigh(MacdGcWaveHighStrategyParams.defaults())
                .macdGcWaveHighRetest(MacdGcWaveHighRetestStrategyParams.defaults())
                .macdGcWaveHighLift(MacdGcWaveHighLiftStrategyParams.defaults())
                .macdDcBreakout(MacdDcBreakoutStrategyParams.defaults())
                .macdPositiveGate(MacdPositiveGateParams.defaults())
                .ultraGcBreakout(UltraGcBreakoutStrategyParams.defaults())
                .waveCcBreakout(WaveCcBreakoutStrategyParams.defaults())
                .dayMin60Combo(DayMin60ComboStrategyParams.defaults())
                .weekMin60Combo(WeekMin60ComboStrategyParams.defaults())
                .dayWeekCombo(DayWeekComboStrategyParams.defaults())
                .dayMonthCombo(DayMonthComboStrategyParams.defaults())
                .min60WaveCcBreakout(Min60WaveCcBreakoutStrategyParams.defaults())
                .dayWaveCcBreakout(DayWaveCcBreakoutStrategyParams.defaults())
                .weekWaveCcBreakout(WeekWaveCcBreakoutStrategyParams.defaults())
                .monthWaveCcBreakout(MonthWaveCcBreakoutStrategyParams.defaults())
                .trendWaveCcBreakout(TrendWaveCcBreakoutStrategyParams.defaults())
                .trendLift(TrendLiftStrategyParams.defaults())
                .trendMa(TrendMaStrategyParams.defaults())
                .bottomBandHigh(BottomBandHighStrategyParams.defaults())
                .bottomPrev2High(BottomPrev2HighStrategyParams.defaults())
                .maGoldBreak(MaCrossPointStrategyParams.defaults())
                .maDeathBreak(MaCrossPointStrategyParams.defaults())
                .maDeathCrossBreak(MaCrossPointStrategyParams.defaults())
                .maGoldenCrossBreak(MaCrossPointStrategyParams.defaults())
                .maGoldenHighBreak(MaCrossPointStrategyParams.defaults())
                .maYangPierce(MaCrossPointStrategyParams.defaults())
                .maBearBreakMa(MaBearBreakMaStrategyParams.defaults())
                .maBull4m(MaBull4mStrategyParams.defaults())
                .build();
    }
	
}