package com.yh.bigdata.tts.spider.strategy.tools.waveband;

public final class WaveBandScoreCalculator {

    private WaveBandScoreCalculator() {
    }

    public static int computeScore() {
        return 50;
    }

    public static String buildTrendMessage(WaveBandTier tier) {
        if (tier == WaveBandTier.MEDIUM) {
            return "[WAVEBAND]波段策略中线|月形态门";
        }
        return "[WAVEBAND]波段策略短线|周形态门";
    }

    public static String buildSignalMessage(WaveBandTier tier) {
        String gate = tier == WaveBandTier.MEDIUM ? "month" : "week";
        String label = tier == WaveBandTier.MEDIUM ? "波段策略中线" : "波段策略短线";
        return label + ",strategyTag=WAVEBAND,signalTier=day,shapeGate=" + gate + ",signal=dayYang";
    }

    public static String buildTrendMessage() {
        return buildTrendMessage(WaveBandTier.SHORT);
    }

    public static String buildSignalMessage() {
        return buildSignalMessage(WaveBandTier.SHORT);
    }
}
