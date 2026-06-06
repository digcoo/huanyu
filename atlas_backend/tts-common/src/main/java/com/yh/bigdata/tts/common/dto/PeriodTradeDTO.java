package com.yh.bigdata.tts.common.dto;

import java.util.Comparator;
import java.util.List;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import lombok.Getter;

@Getter
public class PeriodTradeDTO {

	PeriodTypeEnum periodTypeEnum;
	
	List<Trade> allTrades;

	public PeriodTradeDTO(PeriodTypeEnum periodTypeEnum, List allTrades) {
		super();
		this.periodTypeEnum = periodTypeEnum;
		this.allTrades = allTrades;

		this.allTrades.sort(new Comparator<Trade>() {
			@Override
			public int compare(Trade o1, Trade o2) {
				return o1.getDay().compareTo(o2.getDay());
			}
		});
	}

}
