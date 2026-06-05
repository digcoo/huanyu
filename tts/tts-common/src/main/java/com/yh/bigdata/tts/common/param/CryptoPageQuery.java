package com.yh.bigdata.tts.common.param;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.param.base.PageQuery;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;

@Data
@NoArgsConstructor
public class CryptoPageQuery extends PageQuery {

    private String symbol;

	private Boolean isSelectMode;		//是否选股模式

	private Boolean isFilterMode;		//是否过滤模式

	private String day;

	private String strategy;

	private boolean all = false;

    private String periodType;

    public CryptoPageQuery(Integer page, Integer size) {
		super(page, size);
	}

	public Boolean getIsSelectMode() {
		if (isSelectMode == null) {
			Calendar calendar = Calendar.getInstance();
			return calendar.get(Calendar.HOUR_OF_DAY) < 15;
		}
		return isSelectMode;
	}

    public PeriodTypeEnum getPeriodTypeEnum() {
        return PeriodTypeEnum.getByCode(periodType);
    }

	
}