package com.yh.bigdata.tts.spider.strategy.tools.daymin60;

import com.yh.bigdata.tts.common.indicator.MACDIndicatorUtils;
import com.yh.bigdata.tts.common.indicator.Ticker;
import com.yh.bigdata.tts.common.indicator.XueQiuUtils;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.common.param.DayMin60ComboStrategyParams;
import com.yh.bigdata.tts.spider.strategy.tools.bodybar.BodyBarTierTools;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;
import com.yh.bigdata.tts.spider.strategy.tools.macdgcwave.MacdGcWaveBandTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutBucketTools;
import com.yh.bigdata.tts.spider.strategy.tools.ultralow.BreakoutScanWindowTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.constants.RealtimeStockCache;
import com.yh.bigdata.tts.spider.response.CheckResult;
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
 * 单票诊断：拉雪球日/Min60 复现 daymin60 命中路径。
 */
public class DayMin60ComboDiagTest {

    @After
    public void tearDown() {
        RealtimeStockCache.dayMap.remove("sz002648");
        RealtimeStockCache.min60Map.remove("sz002648");
    }

    @Test
    public void diagnoseSz002648() throws Exception {
        String code = "sz002648";
        DayMin60ComboStrategyParams p = DayMin60ComboStrategyParams.defaults();

        List<Trade> dayBars = XueQiuUtils.getXueQiuJson(code, "day");
        Assert.assertFalse("no day bars", dayBars.isEmpty());
        List<MACDIndicatorUtils.MACDPoint> dayPoints =
                MACDIndicatorUtils.calculateMACD(Ticker.from(dayBars));

        System.out.println("=== " + code + " DAY gate ===");
        explainDayGate(dayBars, dayPoints);

        List<Trade> min60Bars = XueQiuUtils.getXueQiuJson(code, "60m");
        Assert.assertFalse("no min60 bars", min60Bars.isEmpty());
        List<MACDIndicatorUtils.MACDPoint> min60Points =
                MACDIndicatorUtils.calculateMACD(Ticker.from(min60Bars));

        System.out.println("\n=== " + code + " MIN60 tail ===");
        printTail(min60Bars, min60Points, 16);

        BreakoutScanWindowTools.ScanWindow window = BreakoutScanWindowTools.buildScanWindow(
                min60Bars, BreakoutBucketTools::dayKey, p.getPrevDays(), p.getMaxBarsPerDay());
        System.out.println("\nsignalUnit=" + window.getSignalUnitKey()
                + " signalBars=" + window.getSignalBars().size()
                + " priorBars=" + window.getPriorBars().size());

        DayMin60ComboTools.Hit hit = DayMin60ComboTools.findHitOnBars(min60Bars, min60Points, p);
        if (hit != null) {
            System.out.println("\nRESULT: structural hit (before optional gates)");
            System.out.println(DayMin60ComboTools.buildTrendMessage(hit));
            System.out.println(DayMin60ComboTools.buildSignalMessage(hit));
            double rise = BodyBarTierTools.risePct(hit.getSignalBar(), hit.getPrevBar());
            System.out.printf("optional risePct=%.4f (need > %.4f)%n", rise, p.getSignalRisePct());
            return;
        }

        System.out.println("\nRESULT: miss");
        explainMin60Miss(min60Bars, min60Points, p, window);
    }

    @Test
    public void countMin60Db() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/tts?useSSL=false&allowPublicKeyRetrieval=true",
                "root", "root1234");
             PreparedStatement ps = conn.prepareStatement(
                     "select count(*) c from min60k where code=?")) {
            ps.setString(1, "sz002648");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.println("min60k rows for sz002648=" + rs.getInt(1));
            }
        }
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/tts?useSSL=false&allowPublicKeyRetrieval=true",
                "root", "root1234");
             PreparedStatement ps = conn.prepareStatement(
                     "select count(distinct code) c from min60k")) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.println("min60k distinct codes=" + rs.getInt(1));
            }
        }
    }

    @Test
    public void diagnoseSz002648FullPathWithCache() throws Exception {
        String code = "sz002648";
        List<Trade> dayBars = loadRecentDayBars(code, 80);
        List<Trade> min60Bars = loadRecentMin60Bars(code, 120);
        RealtimeStockCache.dayMap.put(code, new ArrayList<>(dayBars));
        RealtimeStockCache.min60Map.put(code, new ArrayList<>(min60Bars));

        StockBase stock = new StockBase();
        stock.setCode(code);
        stock.setName("卫星化学");
        stock.setIsTrade(true);

        DayMin60ComboStrategyParams p = DayMin60ComboStrategyParams.defaults();
        System.out.println("cache day=" + RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 5).size());
        System.out.println("cache min60=" + RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.MIN60, 5).size());
        List<Trade> gateDayBars = RealtimeStockCache.getLastTrades(stock, PeriodTypeEnum.DAY, 40);
        explainDayGate(gateDayBars, MACDIndicatorUtils.calculateMACD(Ticker.from(gateDayBars)));
        System.out.println("passesDayGate=" + DayMin60ComboTools.passesDayGate(stock));
        DayMin60ComboTools.Hit hit = DayMin60ComboTools.findHit(stock, p);
        System.out.println("findHit=" + (hit != null));
        if (hit != null) {
            CheckResult cr = new CheckResult(code, 0D);
            boolean ok = DayMin60ComboTools.passesOptionalGates(stock, cr, p, hit.getSignalBar(), hit.getPrevBar());
            System.out.println("optionalGates=" + ok + " msg=" + cr.getTrendMessage());
        }
    }

    @Test
    public void diagnoseSz002648FromDb() throws Exception {
        String code = "sz002648";
        DayMin60ComboStrategyParams p = DayMin60ComboStrategyParams.defaults();
        List<Trade> dayBars = loadRecentDayBars(code, 10);
        List<Trade> min60Bars = loadRecentMin60Bars(code, 120);
        Assert.assertFalse(dayBars.isEmpty());
        Assert.assertFalse(min60Bars.isEmpty());

        StockBase stock = new StockBase();
        stock.setCode(code);
        stock.setName("卫星化学");
        stock.setIsTrade(true);

        System.out.println("=== " + code + " DB/cache simulation ===");
        explainDayGate(dayBars, MACDIndicatorUtils.calculateMACD(Ticker.from(dayBars)));
        printDayAmounts(dayBars);

        List<MACDIndicatorUtils.MACDPoint> min60Points =
                MACDIndicatorUtils.calculateMACD(Ticker.from(min60Bars));
        DayMin60ComboTools.Hit hit = DayMin60ComboTools.findHitOnBars(min60Bars, min60Points, p);
        if (hit == null) {
            System.out.println("DB RESULT: structural miss");
            explainMin60Miss(min60Bars, min60Points, p,
                    BreakoutScanWindowTools.buildScanWindow(
                            min60Bars, BreakoutBucketTools::dayKey, p.getPrevDays(), p.getMaxBarsPerDay()));
            return;
        }
        System.out.println("DB RESULT: structural hit");
        System.out.println(DayMin60ComboTools.buildSignalMessage(hit));
        boolean optional = DayMin60ComboTools.passesOptionalGates(
                stock, null, p, hit.getSignalBar(), hit.getPrevBar());
        System.out.println("optional gates pass=" + optional);
        if (!optional) {
            explainOptionalFail(dayBars, hit);
        }
    }

    private static void explainOptionalFail(List<Trade> dayBars, DayMin60ComboTools.Hit hit) {
        int n = dayBars.size();
        int from = n < 6 ? 0 : n - 6;
        double avg = dayBars.subList(from, n - 1).stream()
                .filter(t -> t.getAmount() != null && t.getAmount() > 0.1)
                .mapToDouble(Trade::getAmount)
                .average()
                .orElse(0);
        System.out.printf("optional amount avg=%.0f wan (need > 3000), countedDays=%d%n",
                avg / 10_000,
                (int) dayBars.subList(from, n - 1).stream()
                        .filter(t -> t.getAmount() != null && t.getAmount() > 0.1).count());
        double rise = BodyBarTierTools.risePct(hit.getSignalBar(), hit.getPrevBar());
        System.out.printf("optional rise=%.4f (need > 0.01)%n", rise);
    }

    private static void printDayAmounts(List<Trade> dayBars) {
        System.out.println("recent day amounts (wan):");
        int from = Math.max(0, dayBars.size() - 7);
        for (int i = from; i < dayBars.size(); i++) {
            Trade b = dayBars.get(i);
            Double amt = b.getAmount();
            System.out.printf("  %s close=%.2f amount=%s%n",
                    b.getDay(), b.getClose(), amt == null ? "null" : String.format("%.0f", amt / 10_000));
        }
    }

    private static List<Trade> loadRecentDayBars(String code, int limit) throws Exception {
        String sql = "SELECT day, open, high, low, close, amount FROM dayk "
                + "WHERE code=? ORDER BY day DESC LIMIT ?";
        return loadTrades(sql, code, limit);
    }

    private static List<Trade> loadRecentMin60Bars(String code, int limit) throws Exception {
        String sql = "SELECT day, open, high, low, close, amount, prev_close FROM min60k "
                + "WHERE code=? ORDER BY day DESC LIMIT ?";
        return loadTrades(sql, code, limit);
    }

    private static List<Trade> loadTrades(String sql, String code, int limit) throws Exception {
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
                    try {
                        t.setPrevClose(rs.getDouble("prev_close"));
                    } catch (Exception ignored) {
                        t.setPrevClose(rs.getDouble("close"));
                    }
                    rows.add(0, t);
                }
            }
        }
        return rows;
    }

    private static void explainDayGate(List<Trade> dayBars, List<MACDIndicatorUtils.MACDPoint> dayPoints) {
        int n = dayBars.size();
        Trade last = dayBars.get(n - 1);
        Trade d1 = dayBars.get(n - 2);
        Trade d2 = dayBars.get(n - 3);
        MACDIndicatorUtils.MACDPoint lastPt = dayPoints.get(n - 1);
        double floor = Math.max(d1.getLow(), d2.getLow());

        System.out.printf("lastDay=%s close=%.2f macd=%.4f gc=%s%n",
                last.getDay(), last.getClose(), lastPt.getMacd(),
                lastPt.isIfRedGoldCross() ? "Y" : "N");
        System.out.printf("d-1 low=%.2f d-2 low=%.2f floor=%.2f close>floor=%s%n",
                d1.getLow(), d2.getLow(), floor, last.getClose() > floor);

        if (lastPt.getMacd() <= 0) {
            System.out.println("DAY GATE FAIL: last day MACD <= 0");
        } else if (!DayMin60ComboTools.passesDayCloseAboveRecentLows(dayBars)) {
            System.out.println("DAY GATE FAIL: close <= max(prev1 low, prev2 low)");
        } else {
            System.out.println("DAY GATE PASS");
        }

        int from = n < 6 ? 0 : n - 6;
        double avgAmount = dayBars.subList(from, n - 1).stream()
                .filter(t -> t.getAmount() != null && t.getAmount() > 0.1)
                .mapToDouble(Trade::getAmount)
                .average()
                .orElse(0);
        System.out.printf("avgAmount(6d excl last)=%.0f wan (need > 3000)%n", avgAmount / 10_000);
    }

    private static void explainMin60Miss(List<Trade> allBars, List<MACDIndicatorUtils.MACDPoint> points,
                                         DayMin60ComboStrategyParams p,
                                         BreakoutScanWindowTools.ScanWindow window) {
        int n = allBars.size();
        MACDIndicatorUtils.MACDPoint last = points.get(n - 1);
        if (last.getMacd() <= 0) {
            System.out.println("MIN60 GATE FAIL: last bar MACD <= 0");
            return;
        }
        if (last.isIfRedGoldCross()) {
            System.out.println("MIN60 GATE FAIL: last bar is golden cross (skip entire stock)");
            return;
        }
        if (window.getSignalBars().isEmpty()) {
            System.out.println("MIN60 GATE FAIL: no signal bars in last trading day window");
            return;
        }

        int lookback = Math.max(p.getGcLookbackBars(), 5);
        int bandLookback = lookback + 40;
        for (Trade signalBar : window.getSignalBars()) {
            int signalIdx = indexOf(allBars, signalBar);
            if (signalIdx <= 0) {
                System.out.printf("signal %s: invalid index%n", signalBar.getDay());
                continue;
            }
            MACDIndicatorUtils.MACDPoint signalPt = points.get(signalIdx);
            if (signalPt != null && signalPt.isIfRedGoldCross()) {
                System.out.printf("signal %s: skip (signal bar is golden cross)%n", signalBar.getDay());
                continue;
            }
            Trade prevBar = allBars.get(signalIdx - 1);
            MacdCrossStructureTools.CrossBar cross = DayMin60ComboTools.resolveGoldenCrossBefore(
                    allBars, points, signalIdx, lookback);
            if (cross == null || cross.getKind() != MacdCrossStructureTools.CrossKind.GOLDEN) {
                System.out.printf("signal %s: no golden cross before signal in lookback=%d%n",
                        signalBar.getDay(), lookback);
                continue;
            }
            if (sameBar(cross.getBar(), signalBar)) {
                System.out.printf("signal %s: same bar as gc%n", signalBar.getDay());
                continue;
            }
            YangBandTools.CompleteYangBand band = MacdGcWaveBandTools.resolveReferenceBand(
                    allBars, cross.getBar(), bandLookback);
            if (band == null || Double.isNaN(band.getBandHigh())) {
                System.out.printf("signal %s: gc=%s but no reference yang band%n",
                        signalBar.getDay(), cross.getBar().getDay());
                continue;
            }
            double bandHigh = band.getBandHigh();
            boolean edge = DayMin60ComboTools.passesBandHighEdge(prevBar, signalBar, bandHigh);
            boolean amp = DayMin60ComboTools.passesAmplitudeExpand(signalBar, prevBar);
            double rise = BodyBarTierTools.risePct(signalBar, prevBar);
            System.out.printf("signal %s close=%.2f prev=%.2f gc=%s bandHigh=%.2f edge=%s amp=%s rise=%.4f%n",
                    signalBar.getDay(), signalBar.getClose(), prevBar.getClose(),
                    cross.getBar().getDay(), bandHigh, edge, amp, rise);
            if (!edge) {
                System.out.println("  -> edge fail: need prevClose<=bandHigh && signalClose>bandHigh");
            }
            if (!amp) {
                System.out.println("  -> amplitude fail: signal range pct must exceed prev bar");
            }
            if (edge && amp && (Double.isNaN(rise) || rise <= p.getSignalRisePct())) {
                System.out.printf("  -> would hit structurally but rise gate fail (need > %.2f%%)%n",
                        p.getSignalRisePct() * 100);
            }
        }
    }

    private static void printTail(List<Trade> bars, List<MACDIndicatorUtils.MACDPoint> points, int tail) {
        int start = Math.max(0, bars.size() - tail);
        for (int i = start; i < bars.size(); i++) {
            Trade b = bars.get(i);
            MACDIndicatorUtils.MACDPoint pt = points.get(i);
            String cross = pt.isIfRedGoldCross() ? " GOLDEN" : (pt.isIfGreenGoldCross() ? " DEATH" : "");
            System.out.printf("%3d %s o=%.2f h=%.2f l=%.2f c=%.2f macd=%.4f%s%n",
                    i, b.getDay(), b.getOpen(), b.getHigh(), b.getLow(), b.getClose(), pt.getMacd(), cross);
        }
    }

    private static int indexOf(List<Trade> bars, Trade target) {
        for (int i = 0; i < bars.size(); i++) {
            if (target.getDay().equals(bars.get(i).getDay())) {
                return i;
            }
        }
        return -1;
    }

    private static boolean sameBar(Trade a, Trade b) {
        return a != null && b != null && a.getDay() != null && a.getDay().equals(b.getDay());
    }
}
