package com.yh.bigdata.tts.spider.strategy;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.StrategyTypeEnum;
import com.yh.bigdata.tts.spider.strategy.tools.waveband.WaveBandTier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class WaveBandShortStrategy extends AbstractWaveBandStrategy {

    @Override
    public StrategyTypeEnum getStrategy() {
        return StrategyTypeEnum.WAVE_BAND_SHORT;
    }

    @Override
    protected WaveBandTier tier() {
        return WaveBandTier.SHORT;
    }

    @Override
    protected PeriodTypeEnum shapePeriodType() {
        return PeriodTypeEnum.WEEK;
    }

    @Override
    public List<PeriodTypeEnum> getTrendPeriodTypes() {
        return Arrays.asList(PeriodTypeEnum.WEEK, PeriodTypeEnum.DAY);
    }
}
