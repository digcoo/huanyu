package com.yh.bigdata.tts.common.model;

import org.apache.commons.lang3.tuple.Pair;

import com.yh.bigdata.tts.common.constants.MATypeEnum;
import com.yh.bigdata.tts.common.utils.MathUtil;

public class MACross {
	MALine maLine1;
	MALine maLine2;
	Double crossPrice;
	
	public MACross() {
	}
	
	public MACross(MALine maLine1, MALine maLine2) {
		this.maLine1 = maLine1;
		this.maLine2 = maLine2;
		this.crossPrice = calCrossPrice();
	}
	
	private Double calCrossPrice() {
		return MathUtil.calcuateCrossY(Pair.of(1.0, maLine1.getMALine().getLeft())
				, Pair.of(2.0, maLine1.getMALine().getRight())
				, Pair.of(1.0, maLine2.getMALine().getLeft())
				, Pair.of(2.0, maLine2.getMALine().getRight()));
	}
	
	public boolean isContain(MATypeEnum maTypeEnum) {
		return maLine1.getMAType().equals(maTypeEnum) || maLine2.getMAType().equals(maTypeEnum);
	}

	public Double getCrossPrice() {
		return crossPrice;
	}

	public MALine getMaLine1() {
		return maLine1;
	}

	public MALine getMaLine2() {
		return maLine2;
	}
	
}