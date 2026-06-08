package com.yh.bigdata.tts.spider.profile;

import java.util.HashMap;
import java.util.Map;

/**
 * 四维罗盘模块 insight（对齐 Demo STAGE_INSIGHTS）
 */
public final class AtlasCompassInsightTemplates {

    private AtlasCompassInsightTemplates() {
    }

    private static final Map<String, ModuleInsights> BY_STAGE = new HashMap<>();

    static {
        put("expansion",
                "营收保持双位数增长，资本开支同步扩大，经营现金流覆盖投资支出，扩张结构整体健康。",
                "员工规模温和扩张，人均创收稳步提升；存货周转天数下降，运营效率改善。",
                "毛利率稳定高于行业均值，预收与议价能力增强，产业链地位稳固。",
                "资产负债率处于合理区间，有息负债可控，货币资金对短期借款覆盖较充分。");
        put("stable",
                "营收增速回落至个位数，但净利润率稳定，经营现金流持续为正，分红比例提升。",
                "人员结构优化，人均创收维持高位；费用率控制良好，运营杠杆稳定。",
                "行业地位稳固，渠道议价能力保持，毛利率波动收窄。",
                "负债结构保守，现金储备充裕，资本回报稳定，适合长期持有。");
        put("shrink",
                "营收短期承压，但降本增效推动利润率回升，资本开支收缩，现金流逐步修复。",
                "人员精简，人均效率提升；库存去化加速，周转天数明显改善。",
                "市场份额阶段性收缩，但核心产品线毛利率仍高于行业。",
                "主动去杠杆，有息负债下降，财务风险可控。");
        put("decline",
                "营收与利润双降，经营现金流转弱，需关注应收账款与存货减值风险。",
                "人效下滑，费用刚性偏高，运营效率弱于同业。",
                "议价能力减弱，毛利率低于行业均值，渠道库存偏高。",
                "负债率攀升，短期偿债压力加大，需警惕再融资风险。");
    }

    private static void put(String stageId, String financial, String operation, String chain, String capital) {
        BY_STAGE.put(stageId, new ModuleInsights(financial, operation, chain, capital));
    }

    public static String financial(String stageId) {
        return pick(stageId).financial;
    }

    public static String operation(String stageId) {
        return pick(stageId).operation;
    }

    public static String chain(String stageId) {
        return pick(stageId).chain;
    }

    public static String capital(String stageId) {
        return pick(stageId).capital;
    }

    private static ModuleInsights pick(String stageId) {
        ModuleInsights ins = BY_STAGE.get(stageId);
        return ins != null ? ins : BY_STAGE.get("stable");
    }

    private static final class ModuleInsights {
        final String financial;
        final String operation;
        final String chain;
        final String capital;

        ModuleInsights(String financial, String operation, String chain, String capital) {
            this.financial = financial;
            this.operation = operation;
            this.chain = chain;
            this.capital = capital;
        }
    }
}
