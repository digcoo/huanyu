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

    public static QueryContextParam empty() {
        return QueryContextParam.builder()
                .lianBanDays(1)
                .build();
    }
	
}