package com.binance.client.model.market;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.enums.MAType;
import com.binance.client.utils.MathUtil;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Data
@ToString
@SuperBuilder
@Slf4j
@JSONType(ignores = {"lastCandlestickMA", "afterCandlestickMA"})
public class ShortCandlestickMA extends Candlestick {

    private BigDecimal ma5;

    private BigDecimal ma10;

    private BigDecimal ma20;

    private BigDecimal ma30;


    private ShortCandlestickMA lastCandlestickMA;

    private ShortCandlestickMA afterCandlestickMA;

    private PeriodTypeEnum periodType;

    @JSONField(deserialize=false, serialize = false)
    public List<BigDecimal> getAllMAs() {
        return Arrays.asList(this.ma5, this.ma10, this.ma20, this.ma30).stream().filter(x -> Objects.nonNull(x)).collect(Collectors.toList());
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getShiTiRate(){
        return getClose().subtract(getOpen()).divide(getOpen(), getPrecision() + 1, RoundingMode.HALF_UP);
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getShiTiMax(){
        return MathUtil.max(this.getOpen(), this.getClose());
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getShiTiMin(){
        return MathUtil.min(this.getOpen(), this.getClose());
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getMinMA() {
        return MathUtil.min(getAllMAs());
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getMaxMA() {
        return MathUtil.max(getAllMAs());
    }


    @JSONField(deserialize=false, serialize = false)
    private List<MACross.MALine> getMALines() {
        List<MACross.MALine> maLines = new ArrayList<>();
        if (this.lastCandlestickMA == null) {
            return maLines;
        }
        if (this.ma5 != null && this.lastCandlestickMA.getMa5() != null) {
            maLines.add(new MACross.MALine(this.periodType, MAType.MA5, Pair.of(lastCandlestickMA.getMa5(), this.ma5), this.getOpenTime()));
        }
        if (this.ma10 != null && this.lastCandlestickMA.getMa10() != null) {
            maLines.add(new MACross.MALine(this.periodType, MAType.MA10, Pair.of(lastCandlestickMA.getMa10(), this.ma10), this.getOpenTime()));
        }
        if (this.ma20 != null && this.lastCandlestickMA.getMa20() != null) {
            maLines.add(new MACross.MALine(this.periodType, MAType.MA20, Pair.of(lastCandlestickMA.getMa20(), this.ma20), this.getOpenTime()));
        }
        if (this.ma30 != null && this.lastCandlestickMA.getMa30() != null) {
            maLines.add(new MACross.MALine(this.periodType, MAType.MA30, Pair.of(lastCandlestickMA.getMa30(), this.ma30), this.getOpenTime()));
        }
        return maLines;
    }

    @JSONField(deserialize=false, serialize = false)
    public List<MACross> getMACrosses() {
        List<MACross> maCrossList = new ArrayList<>();
        List<MACross.MALine> maLines = getMALines();
        for (int i = 0; i < maLines.size(); i++) {
            for (int j = i+1; j < maLines.size(); j++) {
                MACross.MALine maLine1 = maLines.get(i);
                MACross.MALine maLine2 = maLines.get(j);
                BigDecimal crossY = MathUtil.calCrossPointY(maLine1, maLine2);
                if (null != crossY) {
                    maCrossList.add(new MACross(this.periodType, maLine1, maLine2, crossY, this.getOpenTime()));
                }
            }
        }
        return maCrossList;
    }

    /**
     * 上MA线的上下边界值
     * @param topN
     * @return
     */
    public Pair<BigDecimal, BigDecimal> getUpMA(Integer topN) {
        List<BigDecimal> allMAs = getAllMAs();
        if (allMAs == null) {
            return null;
        }
        List<BigDecimal> mas = allMAs.stream()
                .sorted(Comparator.comparing(BigDecimal::doubleValue))
                .collect(Collectors.toList());

        if (topN == null) {
            return Pair.of(MathUtil.min(mas), MathUtil.max(mas));
        }

        if (mas.size() > topN) {
            mas = mas.subList(0, topN);
        }
        return Pair.of(MathUtil.min(mas), MathUtil.max(mas));
    }


    /**
     * 上MA线的上下边界值
     * @param topN
     * @return
     */
    public Pair<BigDecimal, BigDecimal> getDownMA(Integer topN) {
        List<BigDecimal> allMAs = getAllMAs();
        if (allMAs == null) {
            return null;
        }
        List<BigDecimal> mas = allMAs.stream()
                .sorted(Comparator.comparing(BigDecimal::doubleValue).reversed())
                .collect(Collectors.toList());

        if (topN == null) {
            return Pair.of(MathUtil.min(mas), MathUtil.max(mas));
        }

        if (mas.size() > topN) {
            mas = mas.subList(0, topN);
        }
        return Pair.of(MathUtil.min(mas), MathUtil.max(mas));
    }

    public Boolean ifCrossUpMA(int topN) {
        Pair<BigDecimal, BigDecimal> maPair = getUpMA(topN);
        if (maPair == null){
            return false;
        }
        return (this.getHigh().compareTo(maPair.getRight()) >= 0
                    || (this.lastCandlestickMA != null && this.lastCandlestickMA.getHigh().compareTo(this.lastCandlestickMA.getUpMA(topN).getRight()) >= 0))
                && this.getClose().compareTo(maPair.getLeft()) < 0
                && this.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                ;
    }

    public Boolean ifCrossDownMA(int topN) {
        Pair<BigDecimal, BigDecimal> maPair = getDownMA(topN);
        if (maPair == null){
            return false;
        }
        return (this.getHigh().compareTo(maPair.getRight()) >= 0
                    || (this.lastCandlestickMA != null && this.lastCandlestickMA.getHigh().compareTo(this.lastCandlestickMA.getDownMA(topN).getRight()) >= 0))
                && this.getClose().compareTo(maPair.getLeft()) < 0
                && this.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                ;
    }

    public static ShortCandlestickMA copyFrom(Candlestick candlestick) {
        return ShortCandlestickMA.builder()
                .symbol(candlestick.getSymbol())
                .open(candlestick.getOpen())
                .high(candlestick.getHigh())
                .low(candlestick.getLow())
                .close(candlestick.getClose())
                .openTime(candlestick.getOpenTime())
                .closeTime(candlestick.getCloseTime())
                .quoteAssetVolume(candlestick.getQuoteAssetVolume())
                .takerBuyBaseAssetVolume(candlestick.getTakerBuyBaseAssetVolume())
                .takerBuyQuoteAssetVolume(candlestick.getTakerBuyQuoteAssetVolume())
                .volume(candlestick.getVolume())
                .build();
    }
}
