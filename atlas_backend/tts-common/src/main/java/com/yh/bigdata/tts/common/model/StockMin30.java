package com.yh.bigdata.tts.common.model;

import org.springframework.beans.BeanUtils;

public class StockMin30 extends Trade {

	public Trade copy() {
		StockMin30 stockMin30 = new StockMin30();
		BeanUtils.copyProperties(this, stockMin30);
		return stockMin30;
	}
}
