package com.yh.bigdata.tts.spider.response;

import java.util.*;
import java.util.stream.Collectors;

import com.yh.bigdata.tts.common.model.Trade;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.TrendStageEnum;

import lombok.Data;


@Data
public class CheckResult {

	private String code;
	private double changeRate;
	private double sortValue = 0.0;
	
	private boolean hasTrend = false;
	private boolean hasSignal = false;
	private PeriodTypeEnum trendPeriodType;
	private PeriodTypeEnum opPeriodType;
	private TrendStageEnum trendStage;

	private TreeMap<PeriodTypeEnum, String> trendPeriodMap = new TreeMap<PeriodTypeEnum, String>();
    private TreeMap<PeriodTypeEnum, String> signalPeriodMap = new TreeMap<PeriodTypeEnum, String>();
    private TreeMap<PeriodTypeEnum, String> riskPeriodMap = new TreeMap<PeriodTypeEnum, String>();
	
	private TreeMap<PeriodTypeEnum, String> fanBaoSignalMap = new TreeMap<PeriodTypeEnum, String>();
	private TreeMap<PeriodTypeEnum, String> turnRoundSignalMap = new TreeMap<PeriodTypeEnum, String>();
	private TreeMap<PeriodTypeEnum, String> crossResistanceSignalMap = new TreeMap<PeriodTypeEnum, String>();
	private TreeMap<PeriodTypeEnum, String> crossKeyResistanceSignalMap = new TreeMap<PeriodTypeEnum, String>();
	
	//突破黄金位（金叉、死叉）
	private TreeMap<PeriodTypeEnum, String> crossGoldSignalMap = new TreeMap<PeriodTypeEnum, String>();
	
	//回踩黄金位（金叉、死叉）
	private TreeMap<PeriodTypeEnum, String> downGoldSignalMap = new TreeMap<PeriodTypeEnum, String>();

	//无阻力顺势（金叉、死叉）
	private TreeMap<PeriodTypeEnum, String> offGoldSignalMap = new TreeMap<PeriodTypeEnum, String>();

	
	public CheckResult(String code) {	
		this.code = code;
		this.hasTrend = false;
		this.hasSignal = false;
	}
	public CheckResult(String code, Double changeRate) {
		this.code = code;
		this.changeRate = changeRate;
		this.hasTrend = false;
		this.hasSignal = false;
	}

    public void addRiskPeriod(PeriodTypeEnum periodTypeEnum, String message) {
        this.riskPeriodMap.put(periodTypeEnum, message);
    }

	public void addTrendPeriod(PeriodTypeEnum periodTypeEnum, String message) {
		this.trendPeriodMap.put(periodTypeEnum, message);
	}
	
	public void addFanBao(PeriodTypeEnum periodTypeEnum, String message) {
		fanBaoSignalMap.put(periodTypeEnum, message);
	}
	
	public void addCrossResistance(PeriodTypeEnum periodTypeEnum, String message) {
		crossResistanceSignalMap.put(periodTypeEnum, message);
	}
	public void addCrossKeyResistance(PeriodTypeEnum periodTypeEnum, String message) {
		crossKeyResistanceSignalMap.put(periodTypeEnum, message);
	}
	
	public void addCrossGold(PeriodTypeEnum periodTypeEnum, String message) {
		crossGoldSignalMap.put(periodTypeEnum, message);
	}

    public void addSignal(PeriodTypeEnum periodTypeEnum, String message) {
        signalPeriodMap.put(periodTypeEnum, message);
    }
	
	public void addTurnRound(PeriodTypeEnum periodTypeEnum, String message) {
		turnRoundSignalMap.put(periodTypeEnum, message);
	}

	public void addDownGold(PeriodTypeEnum periodTypeEnum, String message) {
		downGoldSignalMap.put(periodTypeEnum, message);
	}

	public void addOffGold(PeriodTypeEnum periodTypeEnum, String message) {
		offGoldSignalMap.put(periodTypeEnum, message);
	}
	
	public boolean isSuccess() {
		return hasTrend && hasSignal;
	}
	
	public String getTrendMessage() {
		String trendMessage = trendStage != null? trendStage.getDesc(): "";
		
//		if (trendPeriodType != null && opPeriodType != null) {
//			trendMessage = trendPeriodType.getDesc() + "-" + opPeriodType.getDesc();
//		}
		
		if (!CollectionUtils.isEmpty(this.trendPeriodMap)) {
			String tmpTrendMessage = "";
			for (Map.Entry<PeriodTypeEnum, String> entry: this.trendPeriodMap.entrySet()) {
				tmpTrendMessage += ",(" + entry.getKey().getDesc() + ": " + entry.getValue() + ")";
			}
			trendMessage += tmpTrendMessage.substring(1);
		}
		
		return trendMessage;

	}
	
	public String getSignalMessage() {
		String fanBaoSignalMessage = "";
		if (this.fanBaoSignalMap.size() > 0) {
//			fanBaoSignalMessage = "反包: ";
			for (Map.Entry<PeriodTypeEnum, String> entry : this.fanBaoSignalMap.entrySet()) {
				fanBaoSignalMessage += entry.getKey().getDesc() + "-" + entry.getValue() + ",";
			}
			fanBaoSignalMessage = fanBaoSignalMessage.substring(0, fanBaoSignalMessage.length() - 1);
		}
		
		String turnRoundSignalMessage = "";
		if (this.turnRoundSignalMap.size() > 0) {
//			turnRoundSignalMessage = "调头: ";
			for (Map.Entry<PeriodTypeEnum, String> entry : this.turnRoundSignalMap.entrySet()) {
				turnRoundSignalMessage += entry.getKey().getDesc() + "-" + entry.getValue() + ",";
			}
			turnRoundSignalMessage = turnRoundSignalMessage.substring(0, turnRoundSignalMessage.length() - 1);
		}

		String crossResistanceSignalMessage = "";
		if (this.crossResistanceSignalMap.size() > 0) {
//			crossResistanceSignalMessage = "突破: ";
			for (Map.Entry<PeriodTypeEnum, String> entry : this.crossResistanceSignalMap.entrySet()) {
				crossResistanceSignalMessage += entry.getKey().getDesc() + "-" + entry.getValue() + ",";
			}
			crossResistanceSignalMessage = crossResistanceSignalMessage.substring(0, crossResistanceSignalMessage.length() - 1);
		}
		
		String crossKeyResistanceSignalMessage = "";
		if (this.crossKeyResistanceSignalMap.size() > 0) {
//			crossKeyResistanceSignalMessage = "突破关键阻力位: ";
			for (Map.Entry<PeriodTypeEnum, String> entry : this.crossKeyResistanceSignalMap.entrySet()) {
				crossKeyResistanceSignalMessage += entry.getKey().getDesc() + "-" + entry.getValue() + ",";
			}
			crossKeyResistanceSignalMessage = crossKeyResistanceSignalMessage.substring(0, crossKeyResistanceSignalMessage.length() - 1);
		}

		String crossGoldSignalMessage = "";
		if (this.crossGoldSignalMap.size() > 0) {
//			crossGoldSignalMessage = "黄金位: ";
			for (Map.Entry<PeriodTypeEnum, String> entry : this.crossGoldSignalMap.entrySet()) {
				crossGoldSignalMessage += entry.getKey().getDesc() + "-" + entry.getValue() + ",";
			}
			crossGoldSignalMessage = crossGoldSignalMessage.substring(0, crossGoldSignalMessage.length() - 1);
		}
		

		String downGoldSignalMessage = "";
		if (this.downGoldSignalMap.size() > 0) {
//			downGoldSignalMessage = "回踩黄金位: ";
			for (Map.Entry<PeriodTypeEnum, String> entry : this.downGoldSignalMap.entrySet()) {
				downGoldSignalMessage += entry.getKey().getDesc() + "-" + entry.getValue() + ",";
			}
			downGoldSignalMessage = downGoldSignalMessage.substring(0, downGoldSignalMessage.length() - 1);
		}
		


		String offGoldSignalMessage = "";
		if (this.offGoldSignalMap.size() > 0) {
//			offGoldSignalMessage = "无阻力顺势: ";
			for (Map.Entry<PeriodTypeEnum, String> entry : this.offGoldSignalMap.entrySet()) {
				offGoldSignalMessage += entry.getKey().getDesc() + "-" + entry.getValue() + ",";
			}
			offGoldSignalMessage = offGoldSignalMessage.substring(0, offGoldSignalMessage.length() - 1);
		}


        String signalMessage = "";
        if (this.signalPeriodMap.size() > 0) {
//			signalMessage = "信号: ";
            for (Map.Entry<PeriodTypeEnum, String> entry : this.signalPeriodMap.entrySet()) {
                signalMessage += entry.getKey().getDesc() + "-" + entry.getValue() + ",";
            }
            signalMessage = signalMessage.substring(0, signalMessage.length() - 1);
        }


        return  Arrays.asList(fanBaoSignalMessage, turnRoundSignalMessage
				, crossResistanceSignalMessage, crossKeyResistanceSignalMessage
				, crossGoldSignalMessage, downGoldSignalMessage, offGoldSignalMessage, signalMessage)
				.stream()
				.filter(x -> StringUtils.isNotBlank(x))
				.collect(Collectors.joining(","));
		
	}

    @Data
    public static class CalContextVo {

        //K线关键位
        private Map<PeriodTypeEnum, List<Trade>> klineKeyTradeMap = new HashMap<>();

        //梯子关键位
        private Map<PeriodTypeEnum, List<Trade>> tiziKeyTradeMap = new HashMap<>();

        //关键压力位
        private Map<PeriodTypeEnum, List<Double>> keyPressureMap = new HashMap<>();
    }
		
}
