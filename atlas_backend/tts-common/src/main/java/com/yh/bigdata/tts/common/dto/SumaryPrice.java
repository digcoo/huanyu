package com.yh.bigdata.tts.common.dto;

import com.yh.bigdata.tts.common.model.Trade;
import lombok.Data;

@Data
public class SumaryPrice {
//	private Double min = 0.0;
//	private Double max = 0.0;
	private Double shitiAvg = 0.0;
	private Trade maxRedTrade;
	private Trade minRedTrade;
	private Trade mergeZaopanTrade;

}
