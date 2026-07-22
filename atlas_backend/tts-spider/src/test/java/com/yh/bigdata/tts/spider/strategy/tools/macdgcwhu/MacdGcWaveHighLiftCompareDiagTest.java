package com.yh.bigdata.tts.spider.strategy.tools.macdgcwhu;

import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MacdGcWaveHighLiftStrategyParams;
import com.yh.bigdata.tts.spider.ApplicationStarter;
import com.yh.bigdata.tts.spider.response.CheckResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

/**
 * 对比日/周/月 MACD 与 Min60/周/月 MACD 门控命中差异（需 dev 数据源）。
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationStarter.class)
public class MacdGcWaveHighLiftCompareDiagTest {

    @Test
    public void compareDayWeekMonthVsMin60WeekMonth() {
        Assert.assertFalse("filterStockMap empty", RealtimeStockCache.filterStockMap.isEmpty());

        MacdGcWaveHighLiftStrategyParams dayGate = MacdGcWaveHighLiftStrategyParams.builder()
                .requireDayMacd(true)
                .requireWeekMacd(true)
                .requireMonthMacd(true)
                .requireMin60Macd(false)
                .enableMinAmountFilter(true)
                .enableSignalRiseGate(true)
                .build();
        MacdGcWaveHighLiftStrategyParams min60Gate = MacdGcWaveHighLiftStrategyParams.builder()
                .requireDayMacd(false)
                .requireWeekMacd(true)
                .requireMonthMacd(true)
                .requireMin60Macd(true)
                .enableMinAmountFilter(true)
                .enableSignalRiseGate(true)
                .build();

        Set<String> dayHits = collectHits(dayGate);
        Set<String> min60Hits = collectHits(min60Gate);

        Set<String> onlyDay = new HashSet<>(dayHits);
        onlyDay.removeAll(min60Hits);
        Set<String> onlyMin60 = new HashSet<>(min60Hits);
        onlyMin60.removeAll(dayHits);

        System.out.printf("day/week/month hits=%d%n", dayHits.size());
        System.out.printf("min60/week/month hits=%d%n", min60Hits.size());
        System.out.printf("only in day/week/month=%d %s%n", onlyDay.size(), onlyDay);
        System.out.printf("only in min60/week/month=%d %s%n", onlyMin60.size(), onlyMin60);
        System.out.printf("intersection=%d%n", dayHits.size() - onlyDay.size());
    }

    private static Set<String> collectHits(MacdGcWaveHighLiftStrategyParams params) {
        Set<String> hits = new HashSet<>();
        for (StockBase stock : RealtimeStockCache.filterStockMap.values()) {
            if (stock == null || stock.getCode() == null) {
                continue;
            }
            CheckResult cr = new CheckResult(stock.getCode(), stock.getChangeRate());
            if (MacdGcWaveHighLiftEvaluator.evaluate(stock, cr, params).isHit()) {
                hits.add(stock.getCode());
            }
        }
        return hits;
    }
}
