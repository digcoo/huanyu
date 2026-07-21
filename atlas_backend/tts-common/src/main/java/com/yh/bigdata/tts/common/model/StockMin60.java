package com.yh.bigdata.tts.common.model;

import org.springframework.beans.BeanUtils;

public class StockMin60 extends Trade {

	public Trade copy() {
		StockMin60 stockMin60 = new StockMin60();
		BeanUtils.copyProperties(this, stockMin60);
		return stockMin60;
	}
}
