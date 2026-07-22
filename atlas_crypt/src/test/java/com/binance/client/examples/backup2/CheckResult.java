package com.binance.client.examples.backup2;


import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.enums.SideTypeEnum;
import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Data
//@Builder(toBuilder = true)
public class CheckResult {

    private String symbol;
    private BigDecimal close;
    private BigDecimal changeRate;
    private SideTypeEnum sideType;
    private BigDecimal sortValue;

    private boolean hasTrend;
    private boolean hasSignal;
    private PeriodTypeEnum trendPeriodType;
    private PeriodTypeEnum opPeriodType;


    //趋势---MACD、KDJ、RSI、EMA
    private TreeMap<PeriodTypeEnum, String> trendPeriodMap = new TreeMap<>();

    //信号---反包
    private TreeMap<PeriodTypeEnum, String> fanBaoSignalMap = new TreeMap<>();
    //信号---掉头
    private TreeMap<PeriodTypeEnum, String> turnRoundSignalMap = new TreeMap<>();
    //信号---突破阻力位
    private TreeMap<PeriodTypeEnum, String> crossResistanceSignalMap = new TreeMap<>();
    //信号---突破关键阻力位
    private TreeMap<PeriodTypeEnum, String> crossKeyResistanceSignalMap = new TreeMap<>();
    //信号---反包kdj黄金位
    private TreeMap<PeriodTypeEnum, String> crossGoldSignalMap = new TreeMap<>();

    public CheckResult(String symbol) {
        this.symbol = symbol;
        this.hasTrend = false;
        this.hasSignal = false;
    }

    public boolean isSuccess() {
        return hasTrend && hasSignal;
    }

    public void addTrendPeriod(PeriodTypeEnum periodTypeEnum, String message) {
        if (this.trendPeriodMap == null) {
            this.trendPeriodMap = new TreeMap<>();
        }
        this.trendPeriodMap.put(periodTypeEnum, message);
    }

    public void addCrossResistance(PeriodTypeEnum periodTypeEnum, String message) {
        if (this.crossResistanceSignalMap == null) {
            this.crossResistanceSignalMap = new TreeMap<>();
        }
        this.crossResistanceSignalMap.put(periodTypeEnum, message);
    }

    public void addCrossKeyResistance(PeriodTypeEnum periodTypeEnum, String message) {
        if (this.crossKeyResistanceSignalMap == null) {
            this.crossKeyResistanceSignalMap = new TreeMap<>();
        }
        this.crossKeyResistanceSignalMap.put(periodTypeEnum, message);
    }

    public void addTurnRound(PeriodTypeEnum periodTypeEnum, String message) {
        if (this.turnRoundSignalMap == null) {
            this.turnRoundSignalMap = new TreeMap<>();
        }
        this.turnRoundSignalMap.put(periodTypeEnum, message);
    }

    public void addFanBao(PeriodTypeEnum periodTypeEnum, String message) {
        if (this.fanBaoSignalMap == null) {
            this.fanBaoSignalMap = new TreeMap<>();
        }
        this.fanBaoSignalMap.put(periodTypeEnum, message);
    }

    public void addCrossGold(PeriodTypeEnum periodTypeEnum, String message) {
        if (this.crossGoldSignalMap == null) {
            this.crossGoldSignalMap = new TreeMap<>();
        }
        this.crossGoldSignalMap.put(periodTypeEnum, message);
    }


    public String getTrendMessage() {
        String trendMessage = "";
        if (trendPeriodType != null && opPeriodType != null) {
            trendMessage = trendPeriodType.getDesc() + "-" + opPeriodType.getDesc();
        }
        if (MapUtils.isNotEmpty(this.trendPeriodMap)) {
            String tmpTrendMessage = "";
            for (Map.Entry<PeriodTypeEnum, String> entry : this.trendPeriodMap.entrySet()) {
                tmpTrendMessage += ",(" + entry.getKey().getDesc() + ": " + entry.getValue() + ")";
            }
            trendMessage += tmpTrendMessage.substring(1);
        }
        return trendMessage;
    }

    public String getSignalMessage() {
        String fanBaoSignalMessage = "";
        if (MapUtils.isNotEmpty(this.fanBaoSignalMap)) {
            fanBaoSignalMessage = "反包: ";
            for (Map.Entry<PeriodTypeEnum, String> entry : this.fanBaoSignalMap.entrySet()) {
                fanBaoSignalMessage += entry.getKey().getDesc() + "(" + entry.getValue() + "),";
            }
            fanBaoSignalMessage = fanBaoSignalMessage.substring(0, fanBaoSignalMessage.length() - 1);
        }

        String turnRoundMessage = "";
        if (MapUtils.isNotEmpty(this.turnRoundSignalMap)) {
            turnRoundMessage = "调头: ";
            for (Map.Entry<PeriodTypeEnum, String> entry : this.turnRoundSignalMap.entrySet()) {
                turnRoundMessage += entry.getKey().getDesc() + "(" + entry.getValue() + "),";
            }
            turnRoundMessage = turnRoundMessage.substring(0, turnRoundMessage.length() - 1);
        }

        String crossResistanceSignalMessage = "";
        if (MapUtils.isNotEmpty(this.crossResistanceSignalMap)) {
            crossResistanceSignalMessage = "突破: ";
            for (Map.Entry<PeriodTypeEnum, String> entry : this.crossResistanceSignalMap.entrySet()) {
                crossResistanceSignalMessage += entry.getKey().getDesc() + "(" + entry.getValue() + "),";
            }
            crossResistanceSignalMessage = crossResistanceSignalMessage.substring(0, crossResistanceSignalMessage.length() - 1);
        }

        String crossKeyResistanceSignalMessage = "";
        if (MapUtils.isNotEmpty(this.crossKeyResistanceSignalMap)) {
            crossKeyResistanceSignalMessage = "突破关键阻力位: ";
            for (Map.Entry<PeriodTypeEnum, String> entry : this.crossKeyResistanceSignalMap.entrySet()) {
                crossKeyResistanceSignalMessage += entry.getKey().getDesc() + "(" + entry.getValue() + "),";
            }
            crossKeyResistanceSignalMessage = crossKeyResistanceSignalMessage.substring(0, crossKeyResistanceSignalMessage.length() - 1);
        }

        String crossGoldSignalMessage = "";
        if (MapUtils.isNotEmpty(this.crossGoldSignalMap)) {
            crossGoldSignalMessage = "黄金位: ";
            for (Map.Entry<PeriodTypeEnum, String> entry : this.crossGoldSignalMap.entrySet()) {
                crossGoldSignalMessage += entry.getKey().getDesc() + "(" + entry.getValue() + "),";
            }
            crossGoldSignalMessage = crossGoldSignalMessage.substring(0, crossGoldSignalMessage.length() - 1);
        }

        return Arrays.asList(fanBaoSignalMessage, turnRoundMessage
                        , crossResistanceSignalMessage, crossKeyResistanceSignalMessage
                        , crossGoldSignalMessage)
                .stream()
                .filter(x -> StringUtils.isNotBlank(x))
                .collect(Collectors.joining(","));
    }

}
