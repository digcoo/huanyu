package com.yh.bigdata.tts.common.dto;

import java.util.List;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class DynamicPeriod {
	private final PeriodTypeEnum trendPeriod;
	private final List<PeriodTypeEnum> assistTrendPeriods;
	private final PeriodTypeEnum opPeriod;
}
