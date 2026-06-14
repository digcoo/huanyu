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

    public static QueryContextParam empty() {
        return QueryContextParam.builder()
                .lianBanDays(1)
                .unilateral(UnilateralStrategyParams.defaults())
                .preGolden(PreGoldenStrategyParams.defaults())
                .resonance(ResonanceStrategyParams.defaults())
                .rebound(ReboundStrategyParams.defaults())
                .build();
    }
	
}