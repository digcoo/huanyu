package com.yh.bigdata.tts.common.dto;

import com.yh.bigdata.tts.common.constants.TrendStageEnum;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.TrendTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@Data
@Builder
public class CheckResponse {

	private boolean success;
	private String message;
	private PeriodTypeEnum periodType;
	private TrendTypeEnum trendType;
	private boolean ifGoldCross;
	private double score;
	private TrendStageEnum periodStage;
    @Builder.Default
    private List<Trade> keyTrades = new ArrayList<>();
	
	public CheckResponse(boolean success, String message, PeriodTypeEnum periodType) {
		this.success = success;
		this.message = message;
		this.periodType = periodType;
		this.ifGoldCross = false;
		this.periodStage = TrendStageEnum.UNKNOWN;
	}
	
	public CheckResponse(boolean success, String message, double score, PeriodTypeEnum periodType, boolean ifGoldCross) {
		this.success = success;
		this.message = message;
		this.score = score;
		this.periodType = periodType;
		this.ifGoldCross = ifGoldCross;
		this.periodStage = TrendStageEnum.UNKNOWN;
	}
	
	public CheckResponse(boolean success, String message, double score, PeriodTypeEnum periodType, boolean ifGoldCross, TrendTypeEnum trendType) {
		this.success = success;
		this.message = message;
		this.score = score;
		this.periodType = periodType;
		this.ifGoldCross = ifGoldCross;
		this.trendType = trendType;
		this.periodStage = TrendStageEnum.UNKNOWN;
	}
	
	public CheckResponse(boolean success, String message, PeriodTypeEnum periodType, TrendStageEnum periodStage) {
		this.success = success;
		this.message = message;
		this.periodType = periodType;
		this.ifGoldCross = false;
		this.periodStage = periodStage;
	}

}
