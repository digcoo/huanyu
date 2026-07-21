package com.yh.bigdata.tts.spider.strategy.tools.macdgcwave;

import com.yh.bigdata.tts.common.model.Trade;
import com.yh.bigdata.tts.spider.strategy.tools.macd.MacdCrossStructureTools;
import com.yh.bigdata.tts.spider.strategy.tools.wavecc.YangBandTools;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * MACD 金叉 K 定基准完整阳波段（macdgcwh / macdgcwhr / macdgcwhu 共用）。
 * <ul>
 *   <li>阳 K 金叉：取该阳 K 所属的完整阳波段。</li>
 *   <li>阴 K 金叉：从该阴 K 往前追溯最近一个完整阳波段（含以该阴 K 为完结 K 的波段）。</li>
 * </ul>
 */
public final class MacdGcWaveBandTools {

    private MacdGcWaveBandTools() {
    }

    public static YangBandTools.CompleteYangBand resolveReferenceBand(List<Trade> trades, Trade gcBar, int bandLookback) {
        if (gcBar == null || CollectionUtils.isEmpty(trades)) {
            return null;
        }
        List<YangBandTools.CompleteYangBand> bands = YangBandTools.findCompleteBands(trades, bandLookback);
        if (bands.isEmpty()) {
            return null;
        }
        if (YangBandTools.isStrictYang(gcBar)) {
            return findCompleteBandContainingBar(bands, trades, gcBar);
        }
        int gcIdx = MacdCrossStructureTools.indexOfBar(trades, gcBar);
        if (gcIdx < 0) {
            return null;
        }
        return findNearestCompleteBandAtOrBeforeIndex(bands, trades, gcIdx);
    }

    private static YangBandTools.CompleteYangBand findCompleteBandContainingBar(
            List<YangBandTools.CompleteYangBand> bands, List<Trade> trades, Trade bar) {
        int idx = MacdCrossStructureTools.indexOfBar(trades, bar);
        if (idx < 0) {
            return null;
        }
        for (int i = bands.size() - 1; i >= 0; i--) {
            YangBandTools.CompleteYangBand band = bands.get(i);
            int from = MacdCrossStructureTools.indexOfBar(trades, band.getFirstYang());
            int to = MacdCrossStructureTools.indexOfBar(trades, band.getLastYang());
            if (from >= 0 && to >= 0 && idx >= from && idx <= to) {
                return band;
            }
        }
        return null;
    }

    /** 阴 K 金叉：完结 K 索引 ≤ 金叉 K 索引的最近完整阳波段。 */
    private static YangBandTools.CompleteYangBand findNearestCompleteBandAtOrBeforeIndex(
            List<YangBandTools.CompleteYangBand> bands, List<Trade> trades, int barIdx) {
        YangBandTools.CompleteYangBand best = null;
        int bestTermIdx = -1;
        for (YangBandTools.CompleteYangBand band : bands) {
            int termIdx = MacdCrossStructureTools.indexOfBar(trades, band.getTerminatorBar());
            if (termIdx >= 0 && termIdx <= barIdx && termIdx > bestTermIdx) {
                bestTermIdx = termIdx;
                best = band;
            }
        }
        return best;
    }
}
