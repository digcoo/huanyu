package com.yh.bigdata.tts.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yh.bigdata.tts.common.constants.MATypeEnum;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.utils.MathUtil;
import lombok.Data;

@Data
public class CrossMADTO {
	Trade bt;			//baseTrade
	MATypeEnum bm;		//baseMA
	Double bp;			//basePrice
	

	Trade ct;			//crossTrade
	MATypeEnum cm;		//crossMA
	Double cp;			//crossPrice

	@JsonIgnore
	public Double getMinMA() {
		return MathUtil.min(this.bp, this.cp);
	}
	

	@JsonIgnore
	public Double getMaxMA() {
		return MathUtil.max(this.bp, this.cp);
	}

	@JsonIgnore
	public Double getMidMA() {
		return MathUtil.mid(this.bp, this.cp);
	}

}