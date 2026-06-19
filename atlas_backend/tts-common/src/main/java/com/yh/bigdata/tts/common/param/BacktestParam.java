package com.yh.bigdata.tts.common.param;

import lombok.Data;

/**
 * 策略回测请求参数
 */
@Data
public class BacktestParam {

    /** 策略 code：ultra / trend 等 */
    private String strategy = "ultra";

    /** 回测扫描窗口（交易日近似，按日 K 根数） */
    private Integer days = 365;

    /** 持有天数（信号日后 N 根日 K 收盘结算） */
    private Integer holdDays = 10;

    /** 最多扫描股票数（全市场时限制，避免超时） */
    private Integer maxStocks = 200;

    /** 可选：逗号分隔股票代码，如 600519,000001 */
    private String codes;

    /** 判定盈利的最低收益率 %，默认 0 */
    private Double winThreshold = 0.0;

    /** 同一股票两次信号最小间隔（根日 K），默认等于 holdDays */
    private Integer signalCooldown;

    /** 策略自定义参数（与 rescan 一致） */
    private QueryContextParam params;

    public int effectiveCooldown() {
        if (signalCooldown != null && signalCooldown > 0) {
            return signalCooldown;
        }
        return holdDays != null && holdDays > 0 ? holdDays : 10;
    }

    public double effectiveWinThreshold() {
        return winThreshold != null ? winThreshold : 0.0;
    }
}
