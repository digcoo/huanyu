package com.binance.client.strategy.wavecc;

/**
 * 凸/凹波段形态判定。
 */
public final class WaveShapeTools {

    private static final double LINE_EPS = 1e-6;

    private WaveShapeTools() {
    }

    public static boolean isConvex(YangBandTools.CompleteYangBand lastBand,
                                   YangBandTools.CompleteYangBand prevBand) {
        if (lastBand == null || prevBand == null) {
            return false;
        }
        double lastHigh = lastBand.getBandHigh();
        double prevHigh = prevBand.getBandHigh();
        if (Double.isNaN(lastHigh) || Double.isNaN(prevHigh)) {
            return false;
        }
        return lastHigh > prevHigh + LINE_EPS;
    }

    public static boolean isConvexYin(YinBandTools.CompleteYinBand lastBand,
                                      YinBandTools.CompleteYinBand prevBand) {
        if (lastBand == null || prevBand == null) {
            return false;
        }
        double lastLow = lastBand.getBandLow();
        double prevLow = prevBand.getBandLow();
        if (Double.isNaN(lastLow) || Double.isNaN(prevLow)) {
            return false;
        }
        return lastLow < prevLow - LINE_EPS;
    }
}
