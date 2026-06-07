package com.yh.bigdata.tts.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yh.bigdata.tts.common.constants.Constants;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.utils.MathUtil;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class StockBase {

	private String code;

    private String exchange;

    private String name;

    private String quancheng;

    private String mainBusiness;

    private String industry;

    private String industryCsrc;

    private String orgProfile;

    private Double peTtm;

    private Double pb;

    private Double psTtm;

    private Double dividendYield;

    private Double high52w;

    private Double low52w;

    private Double totalMvYi;

    private Boolean isSt;

    private Boolean isTrade;

    /** 收盘价 */
    private Double close;

    private Double ma5;

    private Double ma10;

    private Double ma20;

    private Double ma30;

	protected Double ma60;

	protected Double ma120;

    protected double dea;

    protected double dif;

    protected double macd;
	
	/** 昨收 */
	protected Double prevClose;
	
	protected Double open;
	
	protected Double high;
	
	protected Double low;
	
	protected Long volume;
	
	protected Double amount;

	protected Double percent;

	protected String day;

    protected Double turnoverRate;

    private String periodType;

    private PeriodTypeEnum periodTypeEnum;

	protected String sortBy = "default";

    private StockBase preTrade;


	private String crossParams;

	private Double sortValue;

	private String trendMessage;

//	@Transient
	private String signalMessage;
	
	
	public StockBase() {
	}

	public StockBase(String code, String name) {
		super();
		this.code = code;
		this.name = name;
	}

	public StockBase(String code, String exchange, String name, Boolean isSt, Double closePrice) {
		super();
		this.code = code;
		this.exchange = exchange;
		this.name = name;
		this.isSt = isSt;
		this.close = closePrice;
	}

	@Deprecated
	public Double getTrade() {
		return close;
	}

	@Deprecated
	public void setTrade(double trade) {
		this.close = trade;
	}

	@Deprecated
	public Double getLastTrade() {
		return prevClose;
	}

	@Deprecated
	public void setLastTrade(Double lastTrade) {
		this.prevClose = lastTrade;
	}

	public String getDay() {
        if (this.day != null && day.contains(".")) {
            this.day = this.day.substring(0, this.day.indexOf("."));
        }
        return day;
    }
	
	public Double getPercent() {
        return getChangeRate();
	}

	
	public Boolean isTradeBetween(Double left, Double right) {
		return this.close <= MathUtil.max(left, right)
				&& this.close >= MathUtil.min(left, right);
	}

	@JSONField(serialize = false)
	@JsonIgnore
	public Double getHighLastRate(){
		return (high - prevClose) / prevClose;
	}
	
	@JSONField(serialize = false)
	@JsonIgnore
	public Double getHighLowRate(){
		return (high - low) / low;
	}

	@JSONField(serialize = false)
	@JsonIgnore
	public Double getShitiMax() {
		return MathUtil.max(this.close, this.open);
	}

	@JSONField(serialize = false)
	@JsonIgnore
	public Double getShitiAvg() {
		return (getShitiMax() + getShitiMin()) / 2;
	}

	@JSONField(serialize = false)
	@JsonIgnore
	public Double getMid() {
		return (getHigh() + getLow()) / 2;
	}

    @JSONField(serialize = false)
    @JsonIgnore
    public Double getBaseline() {
        return (high - low) * Constants.BASE_LINE_LOW_SPACE_RATE + low;
    }
	

	@JSONField(serialize = false)
	@JsonIgnore
	public Double getShitiMid() {
		return (getShitiMax() + getShitiMin()) * 0.5;
	}
	

	@JSONField(serialize = false)
	@JsonIgnore
	public Double getShitiMin() {
		return MathUtil.min(this.close, this.open);
	}
	
	@JSONField(serialize = false)
	@JsonIgnore
	public Double getMaxMA() {
		return MathUtil.max(this.ma5, this.ma10, this.ma20, this.ma30);
	}
	
	@JSONField(serialize = false)
	@JsonIgnore
	public Boolean isOverAllMA() {
		Double maxMA = getMaxMA();
		return maxMA == null || this.close > maxMA;
	}
	
	@JSONField(serialize = false)
	@JsonIgnore
	public Boolean isHighOverAllMA() {
		Double maxMA = getMaxMA();
		return maxMA == null || this.high > maxMA;
	}
	
	@JSONField(serialize = false)
	@JsonIgnore
	public Boolean isDownAllMA() {
		Double minMA = getMinMA();
		return minMA == null || this.close < minMA;
	}
	
	@JSONField(serialize = false)
	@JsonIgnore
	public Boolean isLowDownAllMA() {
		Double maxMA = getMaxMA();
		return maxMA == null || this.low > maxMA;
	}

	@JSONField(serialize = false)
	@JsonIgnore
	public Boolean isBetweenAllMA() {
		Double maxMA = getMaxMA();
		Double minMA = getMinMA();
		return maxMA == null || minMA == null 
				|| (this.close < maxMA && this.close > minMA);
	}

	@JSONField(serialize = false)
	@JsonIgnore
	public List<Double> getAllMAs() {
		return Arrays.asList(this.ma5, this.ma10, this.ma20, this.ma30).stream().filter(x-> Objects.nonNull(x)).collect(Collectors.toList());
	}
	
	@JSONField(serialize = false)
	@JsonIgnore
	public List<Double>	getAscMAs() {
		return getAllMAs().stream()
				.filter(x -> Objects.nonNull(x))
				.sorted(Comparator.comparing(Double::doubleValue))
				.collect(Collectors.toList());
	}

	@JSONField(serialize = false)
	@JsonIgnore
	public List<Double>	getDescMAs() {
		return Arrays.asList(this.ma5, this.ma10, this.ma20, this.ma30).stream()
				.filter(x -> Objects.nonNull(x))
				.sorted(Comparator.comparing(Double::doubleValue).reversed())
				.collect(Collectors.toList());
	}
	
	/**
	 * 上MA线的上下边界值
	 * @param topN
	 * @return
	 */
	public Pair<Double, Double> getUpMA(Integer topN) {
		List<Double> ascMAs = getAscMAs();
		
		if(ascMAs == null) {
			return null;
		}
		
		if(topN == null) {
			return Pair.of(MathUtil.min(ascMAs), MathUtil.max(ascMAs));
		}
		
		if(ascMAs.size() > topN) {
			ascMAs = ascMAs.subList(0, topN);
		}
		return Pair.of(MathUtil.min(ascMAs), MathUtil.max(ascMAs));
	}
	

	/**
	 * 下MA线的上下边界值
	 * @param topN
	 * @return
	 */
	public Pair<Double, Double> getDownMA(Integer topN) {
		List<Double> descMAs = getDescMAs();
		
		if(descMAs == null) {
			return null;
		}
		
		if(topN == null) {
			return Pair.of(MathUtil.min(descMAs), MathUtil.max(descMAs));
		}
		
		if(descMAs.size() > topN) {
			descMAs = descMAs.subList(0, topN);
		}
		return Pair.of(MathUtil.min(descMAs), MathUtil.max(descMAs));
	}
	
	
	public Boolean ifCrossUpMA(Integer topN) {
		Pair<Double, Double> maPair = getUpMA(topN);	
		if(maPair == null) {
			return false;
		}
		
		return (this.low <= maPair.getLeft()
					|| (preTrade!=null && preTrade.getLow() <= preTrade.getUpMA(topN).getLeft()))
				&& this.close > maPair.getRight()
				&& this.getShitiRate() > 0;
	}
	
	//反包
	public Boolean ifFanbao(boolean ifXiayi) {
		return this.getClose() > preTrade.getShitiMax()
				&& (!ifXiayi || preTrade.getShitiRate() < 0);
	}
	
	public Boolean ifCrossDownMA(Integer topN) {
		Pair<Double, Double> maPair = getDownMA(topN);	
		if(maPair == null) {
			return false;
		}
		
		return (this.low <= maPair.getLeft()
				|| (preTrade!=null && preTrade.getLow() <= preTrade.getDownMA(topN).getLeft()))
				&& this.close > maPair.getRight()
				&& this.getShitiRate() > 0;
	}
	
	@JSONField(serialize=false, deserialize = false)
	@JsonIgnore
	public Double getMinMA() {
		return MathUtil.min(this.ma5, this.ma10, this.ma20, this.ma30);
	}


	@JSONField(serialize = false)
	@JsonIgnore
	public Double getRealtimeAvg() {
		try {
			return this.amount/this.volume;
		} catch (Exception e) {
			return Double.MAX_VALUE;
		}
	}

	@JSONField(serialize = false)
	public Boolean isChongjiMA(int ma) {
		Double targetMA = null;
		switch (ma) {
		case 5:
			targetMA = this.ma5;
			break;
			
		case 10:
			targetMA = this.ma10;
			break;

		case 20:
			targetMA = this.ma20;
			break;

		case 30:
			targetMA = this.ma30;
			break;

		case 60:
			targetMA = this.ma60;
			break;

		case 120:
			targetMA = this.ma120;
			break;

		default:
			break;
		}
		
		return targetMA != null 
				&& this.low < targetMA
				&& this.high > targetMA;
	}
	

	@JsonIgnore
	@JSONField(serialize=false, deserialize = false)
	public Double getChangeRate(){
		return prevClose == null? 0 : (close - prevClose) / prevClose;
	}
	
	@JsonIgnore
	@JSONField(serialize=false, deserialize = false)
	public Double getBlockRate(){
		return (close - open) / open;
	}
	
	@JsonIgnore
	@JSONField(serialize=false, deserialize = false)
	public Double getShockRate(){
		return (high - low) / low;
	}
	
	@JsonIgnore
	@JSONField(serialize=false, deserialize = false)
	public Double getHighChangeRate(){
		return prevClose == null? 0 : (high - prevClose) / prevClose;
	}


    /**
     * 上影线占比
     */
    @JsonIgnore
    @JSONField(serialize=false, deserialize = false)
    public double getUpShadowRate() {
        return (high - getShitiMax()) / getShitiMax();
    }


    /**
     * 下影线占比
     */
    @JsonIgnore
    @JSONField(serialize=false, deserialize = false)
    public double getDownShadowRate() {
        return (getShitiMin() - low) / low;
    }
	
	@JsonIgnore
	@JSONField(serialize=false, deserialize = false)
	public Double getLowSpaceRate(){
		return (close - low) / low;
	}
	
	@JsonIgnore
	@JSONField(serialize=false, deserialize = false)
	public Double getLowSpacePrice(double lowRate){
		return  low + lowRate * (high - low);
	}
	

	@JsonIgnore
	@JSONField(serialize=false, deserialize = false)
	public Double getChangeRateAbs(){
		return Math.abs(getChangeRate());
	}

	@JSONField(serialize=false, deserialize = false)
	@JsonIgnore
	public Double getShitiRate() {
		return (close - open) / open;
	}

    @JSONField(serialize=false, deserialize = false)
    @JsonIgnore
    public boolean isRising() {
        return getShitiRate() >= 0;
    }

    @JSONField(serialize=false, deserialize = false)
    @JsonIgnore
    public boolean isDecline() {
        return getShitiRate() < 0;
    }
	
	@JSONField(serialize=false, deserialize = false)
	@JsonIgnore
	public Double getHighRate() {
		return (high - prevClose) / prevClose;
	}
	
	@JSONField(serialize=false, deserialize = false)
	@JsonIgnore
	public Double getLowRate() {
        return (close - low) / low;
    }

    /**
     * 判断股票是否涨停（考虑不同板块的涨停限制）
     */
    public boolean isZhangTing() {
        boolean isST = getName().toLowerCase().contains("st");
        double changeRate = getChangeRate();

        // 科创板和创业板涨跌幅限制为20%
        boolean isMainBoard = getCode().toLowerCase().startsWith("sh688") ||
                getCode().toLowerCase().startsWith("sz3");

        if (isST) {
            return changeRate > 0.0485; // ST股涨跌幅限制为5%
        } else if (isMainBoard) {
            return changeRate > 0.1985; // 科创板/创业板涨跌幅限制为20%
        } else {
            return changeRate > 0.0985; // 主板涨跌幅限制为10%
        }
    }

    /**
     * 判断股票是否涨停（考虑不同板块的涨停限制）
     */
    public boolean isApproachZhangTing() {
        boolean isST = getName().toLowerCase().contains("st");
        double changeRate = getChangeRate();

        // 科创板和创业板涨跌幅限制为20%
        boolean isMainBoard = getCode().toLowerCase().startsWith("sh688") ||
                getCode().toLowerCase().startsWith("sz3");

        if (isST) {
            return changeRate > 0.04; // ST股涨跌幅限制为5%
        } else if (isMainBoard) {
            return changeRate > 0.16; // 科创板/创业板涨跌幅限制为20%
        } else {
            return changeRate > 0.08; // 主板涨跌幅限制为10%
        }
    }
				
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StockBase other = (StockBase) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		return true;
	}

}