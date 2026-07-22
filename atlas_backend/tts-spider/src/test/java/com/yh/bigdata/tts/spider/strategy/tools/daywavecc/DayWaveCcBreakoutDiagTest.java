package com.yh.bigdata.tts.spider.strategy.tools.daywavecc;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.indicator.XueQiuUtils;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.DayWaveCcBreakoutStrategyParams;
import com.yh.bigdata.tts.spider.response.CheckResult;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.WaveShapeTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 单票诊断：复现 daywavecc 命中路径。
 */
public class DayWaveCcBreakoutDiagTest {

    private static final String CODE = "sh600120";

    @After
    public void tearDown() {
        RealtimeStockCache.dayMap.remove(CODE);
        RealtimeStockCache.weekMap.remove(CODE);
        RealtimeStockCache.monthMap.remove(CODE);
    }

    @Test
    public void diagnoseSh600120FromXueQiu() throws Exception {
        List<Trade> dayBars = XueQiuUtils.getXueQiuJson(CODE, "day");
        List<Trade> weekBars = XueQiuUtils.getXueQiuJson(CODE, "week");
        List<Trade> monthBars = XueQiuUtils.getXueQiuJson(CODE, "month");
        Assert.assertFalse(dayBars.isEmpty());

        seedCache(dayBars, weekBars, monthBars);
        StockBase stock = stockStub();

        DayWaveCcBreakoutStrategyParams p = DayWaveCcBreakoutStrategyParams.defaults();
        System.out.println("=== " + CODE + " daywavecc (XueQiu) ===");
        explainMacdGate(stock);
        explainBands(dayBars, p);
        explainHit(stock, p);
    }

    @Test
    public void diagnoseSh600120FromDb() throws Exception {
        List<Trade> dayBars = loadRecentBars("dayk", CODE, 160);
        List<Trade> weekBars = loadRecentBars("weekk", CODE, 40);
        List<Trade> monthBars = loadRecentBars("monthk", CODE, 36);
        if (dayBars.isEmpty()) {
            System.out.println("DB empty for " + CODE + ", skip");
            return;
        }
        seedCache(dayBars, weekBars, monthBars);
        StockBase stock = stockStub();

        DayWaveCcBreakoutStrategyParams p = DayWaveCcBreakoutStrategyParams.defaults();
        System.out.println("=== " + CODE + " daywavecc (DB) ===");
        explainMacdGate(stock);
        explainBands(dayBars, p);
        explainHit(stock, p);
    }

    private static void explainHit(StockBase stock, DayWaveCcBreakoutStrategyParams p) {
        DayWaveCcBreakoutTools.Hit hit = DayWaveCcBreakoutTools.findHit(stock, p);
        if (hit == null) {
            System.out.println("RESULT: structural miss");
            return;
        }
        System.out.println("RESULT: structural hit");
        System.out.println(DayWaveCcBreakoutTools.buildTrendMessage(hit));
        System.out.println(DayWaveCcBreakoutTools.buildSignalMessage(hit));

        CheckResult cr = new CheckResult(stock.getCode(), 0D);
        boolean optional = DayWaveCcBreakoutTools.passesOptionalGates(
                stock, cr, p, hit.getSignalBar(), hit.getPrevBar());
        System.out.println("optionalGates=" + optional + " trendMsg=" + cr.getTrendMessage());
    }

    private static void explainMacdGate(StockBase stock) {
        double dayMacd = DayWaveCcBreakoutTools.lastMacd(stock, PeriodTypeEnum.DAY);
        double weekMacd = DayWaveCcBreakoutTools.lastMacd(stock, PeriodTypeEnum.WEEK);
        double monthMacd = DayWaveCcBreakoutTools.lastMacd(stock, PeriodTypeEnum.MONTH);
        int positive = 0;
        if (dayMacd > 0) positive++;
        if (weekMacd > 0) positive++;
        if (monthMacd > 0) positive++;
        System.out.printf("MACD gate: day=%.4f week=%.4f month=%.4f positive=%d pass=%s%n",
                dayMacd, weekMacd, monthMacd, positive, positive >= 2);
    }

    private static void explainBands(List<Trade> dayBars, DayWaveCcBreakoutStrategyParams p) {
        int lookback = Math.max(p.getLookbackBars(), 10);
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(dayBars, lookback);
        System.out.printf("complete bands in lookback=%d: count=%d%n", lookback, bands.size());
        if (bands.size() < 2) {
            return;
        }
        YangBandTools.CompleteYangBand prevBand = bands.get(bands.size() - 2);
        YangBandTools.CompleteYangBand lastBand = bands.get(bands.size() - 1);
        boolean convex = WaveShapeTools.isConvex(lastBand, prevBand);
        System.out.printf("prevBand first=%s last=%s term=%s bandHigh=%.2f%n",
                dayOf(prevBand.getFirstYang()), dayOf(prevBand.getLastYang()),
                dayOf(prevBand.getTerminatorBar()), prevBand.getBandHigh());
        System.out.printf("lastBand first=%s last=%s term=%s bandHigh=%.2f shape=%s%n",
                dayOf(lastBand.getFirstYang()), dayOf(lastBand.getLastYang()),
                dayOf(lastBand.getTerminatorBar()), lastBand.getBandHigh(),
                convex ? "CONVEX" : "CONCAVE");

        double breakLine = convex ? prevBand.getBandHigh() : lastBand.getBandHigh();
        Trade signalBar = dayBars.get(dayBars.size() - 1);
        Trade prevBar = dayBars.get(dayBars.size() - 2);
        Trade prevPrevBar = dayBars.size() >= 3 ? dayBars.get(dayBars.size() - 3) : null;
        System.out.printf("signal=%s close=%.2f prev=%s close=%.2f breakLine=%.2f edge=%s%n",
                dayOf(signalBar), signalBar.getClose(), dayOf(prevBar), prevBar.getClose(), breakLine,
                DayWaveCcBreakoutTools.passesBandHighEdge(prevBar, signalBar, breakLine));
        double rise = BodyBarTierTools.risePct(signalBar, prevBar);
        double sigRate = DayWaveCcBreakoutTools.barAmplitudeRate(signalBar, prevBar);
        double prevRate = DayWaveCcBreakoutTools.barAmplitudeRate(prevBar, prevPrevBar);
        System.out.printf("risePct=%.4f ampExpand=%s sigAmp=%.4f prevAmp=%.4f strength=%s%n",
                rise,
                DayWaveCcBreakoutTools.passesAmplitudeExpand(signalBar, prevBar, prevPrevBar),
                sigRate, prevRate,
                DayWaveCcBreakoutTools.passesBreakoutStrength(signalBar, prevBar, prevPrevBar));

        int from = Math.max(0, dayBars.size() - 8);
        System.out.println("recent day bars:");
        for (int i = from; i < dayBars.size(); i++) {
            Trade b = dayBars.get(i);
            String yang = YangBandTools.isStrictYang(b) ? "Y" : (YangBandTools.isStrictYin(b) ? "N" : "?");
            System.out.printf("  %s o=%.2f h=%.2f l=%.2f c=%.2f %s%n",
                    b.getDay(), b.getOpen(), b.getHigh(), b.getLow(), b.getClose(), yang);
        }
    }

    private static void seedCache(List<Trade> dayBars, List<Trade> weekBars, List<Trade> monthBars) {
        RealtimeStockCache.dayMap.put(CODE, new ArrayList<>(dayBars));
        if (weekBars != null && !weekBars.isEmpty()) {
            RealtimeStockCache.weekMap.put(CODE, new ArrayList<>(weekBars));
        }
        if (monthBars != null && !monthBars.isEmpty()) {
            RealtimeStockCache.monthMap.put(CODE, new ArrayList<>(monthBars));
        }
    }

    private static StockBase stockStub() {
        StockBase stock = new StockBase();
        stock.setCode(CODE);
        stock.setName("浙江东方");
        stock.setIsTrade(true);
        return stock;
    }

    private static List<Trade> loadRecentBars(String table, String code, int limit) throws Exception {
        String sql = "SELECT day, open, high, low, close, amount FROM " + table
                + " WHERE code=? ORDER BY day DESC LIMIT ?";
        List<Trade> rows = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/tts?userUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true",
                "root", "root1234");
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Trade t = new Trade();
                    t.setCode(code);
                    t.setDay(rs.getString("day"));
                    t.setOpen(rs.getDouble("open"));
                    t.setHigh(rs.getDouble("high"));
                    t.setLow(rs.getDouble("low"));
                    t.setClose(rs.getDouble("close"));
                    t.setAmount(rs.getDouble("amount"));
                    t.setVolume(0L);
                    rows.add(0, t);
                }
            }
        }
        return rows;
    }

    private static String dayOf(Trade bar) {
        return bar != null && bar.getDay() != null ? bar.getDay() : "";
    }
}
