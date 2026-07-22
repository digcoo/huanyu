package com.binance.client.examples.backup;


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
    private boolean success;
    private BigDecimal close;
    private BigDecimal changeRate;
    private PeriodTypeEnum opPeriodTypeEnum;
    private SideTypeEnum opSideTypeEnum;
    private boolean update = false;

    private TreeMap<PeriodTypeEnum, SideTypeEnum> trendPeriodMap = new TreeMap<>();
    private TreeMap<PeriodTypeEnum, SideTypeEnum> crossTrendPeriodMap = new TreeMap<>();
    //前1周期突破
    private TreeMap<PeriodTypeEnum, SideTypeEnum> shockPeriodMap = new TreeMap<>();
    //突破阻力位
    private TreeMap<PeriodTypeEnum, String> crossResistancePeriodMap = new TreeMap<>();
    //反包突破阻力位
    private TreeMap<PeriodTypeEnum, String> fanBaoCrossResistancePeriodMap = new TreeMap<>();
    //大周期掉头
    private TreeMap<PeriodTypeEnum, SideTypeEnum> turnRoundPeriodMap = new TreeMap<>();
    //大周期同向
    private TreeMap<PeriodTypeEnum, SideTypeEnum> sameDirectionPeriodMap = new TreeMap<>();

    //信号
    private TreeMap<PeriodTypeEnum, SideTypeEnum> signalPeriodMap = new TreeMap<>();

    public CheckResult(String symbol, Boolean success) {
        this.symbol = symbol;
        this.success = success;
    }

    public void addTrendPeriod(PeriodTypeEnum periodTypeEnum, SideTypeEnum sideTypeEnum) {
        if (this.trendPeriodMap == null) {
            this.trendPeriodMap = new TreeMap<>();
        }
        this.trendPeriodMap.put(periodTypeEnum, sideTypeEnum);
    }
    public void addSignalPeriod(PeriodTypeEnum periodTypeEnum, SideTypeEnum sideTypeEnum) {
        if (this.signalPeriodMap == null) {
            this.signalPeriodMap = new TreeMap<>();
        }
        this.signalPeriodMap.put(periodTypeEnum, sideTypeEnum);
    }

    public void addShockPeriod(PeriodTypeEnum periodTypeEnum, SideTypeEnum sideTypeEnum) {
        if (this.shockPeriodMap == null) {
            this.shockPeriodMap = new TreeMap<>();
        }
        this.shockPeriodMap.put(periodTypeEnum, sideTypeEnum);
    }

    public void addCrossResistancePeriod(PeriodTypeEnum periodTypeEnum, String resistancePrice) {
        if (this.crossResistancePeriodMap == null) {
            this.crossResistancePeriodMap = new TreeMap<>();
        }
        this.crossResistancePeriodMap.put(periodTypeEnum, resistancePrice);
    }

    public void addTurnRoundPeriod(PeriodTypeEnum periodTypeEnum, SideTypeEnum sideTypeEnum) {
        if (this.turnRoundPeriodMap == null) {
            this.turnRoundPeriodMap = new TreeMap<>();
        }
        this.turnRoundPeriodMap.put(periodTypeEnum, sideTypeEnum);
    }

    public void addFanBaoCrossResistancePeriod(PeriodTypeEnum periodTypeEnum, String resistancePrice) {
        if (this.fanBaoCrossResistancePeriodMap == null) {
            this.fanBaoCrossResistancePeriodMap = new TreeMap<>();
        }
        this.fanBaoCrossResistancePeriodMap.put(periodTypeEnum, resistancePrice);
    }

    public String getTrendMessage() {
        String trendMessage = "";
        if (MapUtils.isNotEmpty(this.trendPeriodMap)) {
            for (Map.Entry<PeriodTypeEnum, SideTypeEnum> entry : this.trendPeriodMap.entrySet()) {
                trendMessage += "," +entry.getKey().getDesc() + "(" + entry.getValue().getDesc() + ")";
            }
            return trendMessage.substring(1);
        }
        return trendMessage;
    }

    public String getSignalMessage() {
        String fanBaoCrossResistanceMessage = "";
        String crossResistanceMessage = "";
        if (MapUtils.isNotEmpty(this.fanBaoCrossResistancePeriodMap)) {
            fanBaoCrossResistanceMessage = "反包突破: ";
            for (Map.Entry<PeriodTypeEnum, String> entry : this.fanBaoCrossResistancePeriodMap.entrySet()) {
                fanBaoCrossResistanceMessage += entry.getKey().getDesc() + "(" + entry.getValue() + "),";
            }
            fanBaoCrossResistanceMessage = fanBaoCrossResistanceMessage.substring(0, fanBaoCrossResistanceMessage.length() - 1);
        } else if (MapUtils.isNotEmpty(this.crossResistancePeriodMap)) {
            crossResistanceMessage = "突破: ";
            for (Map.Entry<PeriodTypeEnum, String> entry : this.crossResistancePeriodMap.entrySet()) {
                crossResistanceMessage += entry.getKey().getDesc() + "(" + entry.getValue() + "),";
            }
            crossResistanceMessage = crossResistanceMessage.substring(0, crossResistanceMessage.length() - 1);
        }

        String turnRoundMessage = "";
        if (MapUtils.isNotEmpty(this.turnRoundPeriodMap)) {
            turnRoundMessage = "掉头: ";
            for (Map.Entry<PeriodTypeEnum, SideTypeEnum> entry : this.turnRoundPeriodMap.entrySet()) {
                turnRoundMessage += entry.getKey().getDesc() + "(" + entry.getValue() + "),";
            }
            turnRoundMessage = turnRoundMessage.substring(0, turnRoundMessage.length() - 1);
        }


        return Arrays.asList(fanBaoCrossResistanceMessage, crossResistanceMessage, turnRoundMessage)
                .stream()
                .filter(x -> StringUtils.isNotBlank(x))
                .collect(Collectors.joining(","));
    }

}
