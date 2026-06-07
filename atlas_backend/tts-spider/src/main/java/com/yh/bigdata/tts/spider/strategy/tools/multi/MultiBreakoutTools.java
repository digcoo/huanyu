package com.yh.bigdata.tts.spider.strategy.tools.multi;

import com.yh.bigdata.tts.common.constants.PeriodTypeEnum;
import com.yh.bigdata.tts.common.dto.CheckResponse;
import com.yh.bigdata.tts.common.model.StockBase;
import com.yh.bigdata.tts.common.param.MultiStrategyParams;
import com.yh.bigdata.tts.spider.strategy.group.band.SignalBandTools;
import com.yh.bigdata.tts.spider.strategy.tools.SignalTools;

import java.util.Arrays;
import java.util.List;

/**
 * 多周期强势 · 波段/梯子突破 Trigger
 */
public final class MultiBreakoutTools {

    private static final List<PeriodTypeEnum> SIGNAL_PERIODS =
            Arrays.asList(PeriodTypeEnum.DAY, PeriodTypeEnum.WEEK, PeriodTypeEnum.MONTH);

    private MultiBreakoutTools() {
    }

    public static BreakoutSnapshot evaluate(StockBase stock, MultiStrategyParams params) {
        MultiStrategyParams p = params != null ? params : MultiStrategyParams.defaults();
        PeriodTypeEnum op = PeriodTypeEnum.DAY;
        BreakoutSnapshot snap = new BreakoutSnapshot();

        for (PeriodTypeEnum period : SIGNAL_PERIODS) {
            boolean periodHit = false;
            if (p.isEnableModeA()) {
                CheckResponse band = SignalBandTools.checkCrossBandHighSignal(stock, period, op);
                if (band.isSuccess()) {
                    snap.bandHits++;
                    snap.addBandPeriod(period, band.getMessage());
                    periodHit = true;
                }
            }
            if (p.isEnableModeB()) {
                CheckResponse revert = SignalBandTools.checkRevertBandHighSignal(stock, period, op);
                if (revert.isSuccess()) {
                    snap.revertHits++;
                    snap.addRevertPeriod(period, revert.getMessage());
                    periodHit = true;
                }
            }
            if (p.isEnableModeC()) {
                CheckResponse tizi = SignalTools.checkCrossTiZiHighSignal(stock, period, op);
                if (tizi.isSuccess()) {
                    snap.tiziHits++;
                    snap.addTiziPeriod(period, tizi.getMessage());
                    periodHit = true;
                }
            }
            if (periodHit) {
                snap.signalPeriodCount++;
            }
        }
        return snap;
    }

    public static final class BreakoutSnapshot {
        public int bandHits;
        public int revertHits;
        public int tiziHits;
        public int signalPeriodCount;
        public StringBuilder bandDetail = new StringBuilder();
        public StringBuilder revertDetail = new StringBuilder();
        public StringBuilder tiziDetail = new StringBuilder();

        void addBandPeriod(PeriodTypeEnum period, String msg) {
            appendDetail(bandDetail, period, msg);
        }

        void addRevertPeriod(PeriodTypeEnum period, String msg) {
            appendDetail(revertDetail, period, msg);
        }

        void addTiziPeriod(PeriodTypeEnum period, String msg) {
            appendDetail(tiziDetail, period, msg);
        }

        private void appendDetail(StringBuilder sb, PeriodTypeEnum period, String msg) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(period.getDesc()).append('-').append(msg);
        }

        public boolean hasTrigger() {
            return bandHits > 0 || revertHits > 0 || tiziHits > 0;
        }

        public int triggerKindCount() {
            int n = 0;
            if (bandHits > 0) {
                n++;
            }
            if (revertHits > 0) {
                n++;
            }
            if (tiziHits > 0) {
                n++;
            }
            return n;
        }

        public String buildSignalMessage() {
            StringBuilder sb = new StringBuilder();
            if (bandDetail.length() > 0) {
                sb.append("趋势波段:").append(bandDetail);
            }
            if (revertDetail.length() > 0) {
                if (sb.length() > 0) {
                    sb.append('|');
                }
                sb.append("反转波段:").append(revertDetail);
            }
            if (tiziDetail.length() > 0) {
                if (sb.length() > 0) {
                    sb.append('|');
                }
                sb.append("梯子突破:").append(tiziDetail);
            }
            return sb.toString();
        }
    }
}
