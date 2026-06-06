package com.yh.bigdata.tts.common.model;

import org.apache.commons.lang3.tuple.Pair;

import com.yh.bigdata.tts.common.constants.MATypeEnum;

public class MALine {
	MATypeEnum MAType;
	Pair<Double, Double> MALine;
	
	public MALine(MATypeEnum mAType, Pair<Double, Double> mALine) {
		super();
		MAType = mAType;
		MALine = mALine;
	}
	public MATypeEnum getMAType() {
		return MAType;
	}
	public Pair<Double, Double> getMALine() {
		return MALine;
	}
	
}
