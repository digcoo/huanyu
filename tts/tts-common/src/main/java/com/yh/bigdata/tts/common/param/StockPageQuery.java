package com.yh.bigdata.tts.common.param;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import lombok.Data;

import com.yh.bigdata.tts.common.param.base.PageQuery;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StockPageQuery extends PageQuery {

    private String code;

	private Boolean isSelectMode;		//是否选股模式
	
	private Boolean isFilterMode;		//是否过滤模式
	
	private String day;
	
	private String strategy;

	private boolean all = false;

    private String trendPeriodType;

    private String opPeriodType;

    private Integer lianBanDays;
    private String trendPeriodTypes;

    public StockPageQuery(Integer page, Integer size) {
		super(page, size);
	}

	public Boolean getIsSelectMode() {
		if (isSelectMode == null) {
			Calendar calendar = Calendar.getInstance();
			return calendar.get(Calendar.HOUR_OF_DAY) < 15;
		}
		return isSelectMode;
	}

    public PeriodTypeEnum getTrendPeriodTypeEnum() {
        return PeriodTypeEnum.getByCode(trendPeriodType);
    }
    public List<PeriodTypeEnum> getTrendPeriodTypesEnum() {
        return Objects.nonNull(this.trendPeriodTypes)? Stream.of(trendPeriodTypes.split(",")).map(PeriodTypeEnum::getByCode).collect(Collectors.toList()) : Collections.emptyList();
    }

    public PeriodTypeEnum getOpPeriodTypeEnum() {
        return PeriodTypeEnum.getByCode(opPeriodType);
    }

    public StrategyTypeEnum getStrategyTypeEnum() {
        return StrategyTypeEnum.getByCode(this.strategy);
    }
	
}