package com.yh.bigdata.tts.spider.profile;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业速览 L1：按行业模板生成可读文案（对齐小程序 detail-mock INDUSTRY_PROFILE_DEFAULT）
 */
public final class AtlasIndustryProfileTemplates {

    private AtlasIndustryProfileTemplates() {
    }

    private static final class Template {
        final String businessOneLiner;
        final String industryPosition;
        final List<String> strengths;
        final List<String> risks;

        Template(String businessOneLiner, String industryPosition, List<String> strengths, List<String> risks) {
            this.businessOneLiner = businessOneLiner;
            this.industryPosition = industryPosition;
            this.strengths = strengths;
            this.risks = risks;
        }
    }

    private static final Map<String, Template> TEMPLATES = new LinkedHashMap<>();
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        put("白酒",
                "{name}主营白酒生产与销售，品牌与渠道是核心竞争要素。",
                "{name}处于白酒赛道，需关注品牌力与渠道管控。",
                "品牌与渠道构成主要壁垒", "高端化趋势下毛利率较高", "现金流通常优于制造业",
                "消费政策与舆情风险", "行业集中度提升竞争加剧", "库存与批价波动");
        put("半导体",
                "{name}从事半导体设计/制造/设备，受国产替代与周期双重驱动。",
                "{name}在半导体产业链中占据关键环节，行业地位取决于技术与产能。",
                "国产替代长期逻辑", "高壁垒带来一定定价权", "政策与产业资本支持",
                "资本开支大、周期性强", "技术迭代与外部约束", "库存与价格下行风险");
        put("锂电池",
                "{name}布局动力/储能电池或材料，绑定新能源产业链。",
                "{name}在锂电产业链中竞争格局激烈，规模与成本决定地位。",
                "新能源渗透率长期向上", "龙头规模效应显著", "储能打开第二增长曲线",
                "原材料与价格竞争", "产能过剩隐忧", "技术路线变更风险");
        put("互联网",
                "{name}以流量与平台为核心，变现方式包括广告、游戏、金融科技等。",
                "{name}在互联网细分赛道中竞争，用户规模与留存是关键。",
                "网络效应与平台壁垒", "轻资产、边际成本递减", "多元变现路径",
                "监管与合规要求", "流量红利见顶", "宏观影响广告与消费");
        put("消费电子",
                "{name}面向消费者的硬件与生态产品，依赖创新与供应链效率。",
                "{name}在消费电子赛道中，品牌与渠道决定市场份额。",
                "品牌与用户生态", "供应链管理与成本控制", "产品迭代带来换机周期",
                "需求波动与库存风险", "竞争压缩利润率", "创新不及预期");
        put("新能源汽车",
                "{name}从事整车或核心零部件，电动化与智能化是主线。",
                "{name}在新能源车赛道中，交付量与毛利率是核心指标。",
                "电动化渗透率提升", "智能化差异化空间", "政策与基础设施支持",
                "价格战压缩利润", "补贴退坡与竞争加剧", "供应链与召回风险");
        put("电商",
                "{name}以电商平台为核心，变现依赖 GMV、广告与云计算等。",
                "{name}在电商/本地生活赛道中，用户规模与履约效率是关键。",
                "平台规模与数据资产", "多元变现与生态协同", "下沉市场与出海空间",
                "竞争与补贴压力", "监管与反垄断", "宏观消费疲软");
        put("潮玩",
                "{name}以 IP 运营为核心，通过盲盒、手办等产品在全渠道变现。",
                "{name}在潮玩赛道中，IP 矩阵与渠道密度决定市场份额。",
                "IP 情感连接带来高溢价", "收藏属性支撑复购", "全渠道触达年轻客群",
                "单 IP 生命周期风险", "盲盒监管与舆论", "竞争加剧与库存波动");
        put("保险",
                "{name}以寿险/财险为主，投资收益与承保利润双轮驱动。",
                "{name}在保险行业中，渠道与负债成本决定竞争力。",
                "长期保单现金流稳定", "品牌与代理人渠道", "投资端弹性",
                "利率下行压制利差", "赔付与退保波动", "监管与资本要求");
        put("光伏",
                "{name}布局硅片/组件/电站，受装机量与价格周期影响大。",
                "{name}在光伏产业链中，成本与一体化程度决定地位。",
                "碳中和长期需求", "龙头成本曲线领先", "技术迭代带来效率提升",
                "产能过剩与价格战", "贸易壁垒", "上游硅料价格波动");
        put("医药",
                "{name}从事创新药/仿制药/器械或 CXO，研发与集采政策是主线变量。",
                "{name}在医药赛道中，管线、渠道与成本控制能力决定竞争力。",
                "刚需属性与老龄化趋势", "创新管线带来溢价", "出海与授权拓展空间",
                "集采与医保控费压力", "研发失败与周期风险", "合规与质量监管");
        put("银行",
                "{name}以存贷业务与中间业务为主，息差与资产质量是核心。",
                "{name}在银行业中，零售能力、风控与资本充足率决定地位。",
                "牌照与网点壁垒", "低成本负债来源", "分红与估值修复空间",
                "息差收窄压力", "房地产与城投风险暴露", "宏观经济波动");
        put("证券",
                "{name}以经纪、投行、自营与资管为主，市场活跃度高度相关。",
                "{name}在券商行业中，资本实力与业务结构决定弹性。",
                "牛市 Beta 弹性大", "财富管理转型空间", "并购重组业务机会",
                "成交量下滑压制业绩", "自营波动风险", "监管政策变化");
        put("房地产",
                "{name}从事住宅/商业开发或物业，销售回款与杠杆是关注重点。",
                "{name}在地产行业中，土储、融资能力与品牌决定生存力。",
                "核心城市资源储备", "物业等轻资产业务", "政策边际改善预期",
                "销售与现金流压力", "债务与再融资风险", "行业出清竞争加剧");
        put("综合",
                "{name}为跨行业或综合类标的，需结合具体业务线理解。",
                "{name}在{industry}领域经营，行业地位需结合财报与竞品判断。",
                "业务多元化分散风险", "具备一定区域或品类优势", "估值修复空间需个案分析",
                "业务复杂度高、透明度低", "宏观与政策波动", "竞争格局变化");

        alias("酿酒", "白酒");
        alias("白酒Ⅱ", "白酒");
        alias("白酒Ⅲ", "白酒");
        alias("集成电路", "半导体");
        alias("半导体设备", "半导体");
        alias("分立器件", "半导体");
        alias("电子化学品", "半导体");
        alias("电池", "锂电池");
        alias("锂电池", "锂电池");
        alias("电池化学品", "锂电池");
        alias("软件开发", "互联网");
        alias("IT服务", "互联网");
        alias("游戏", "互联网");
        alias("数字媒体", "互联网");
        alias("通信服务", "互联网");
        alias("计算机设备", "消费电子");
        alias("通信设备", "消费电子");
        alias("光学光电子", "消费电子");
        alias("消费电子", "消费电子");
        alias("乘用车", "新能源汽车");
        alias("汽车零部件", "新能源汽车");
        alias("商用车", "新能源汽车");
        alias("汽车服务", "新能源汽车");
        alias("一般零售", "电商");
        alias("互联网电商", "电商");
        alias("专业连锁", "电商");
        alias("贸易", "电商");
        alias("文娱用品", "潮玩");
        alias("光伏设备", "光伏");
        alias("光伏辅材", "光伏");
        alias("化学制药", "医药");
        alias("生物制品", "医药");
        alias("医疗器械", "医药");
        alias("医疗服务", "医药");
        alias("中药", "医药");
        alias("医药商业", "医药");
        alias("股份制银行", "银行");
        alias("城商行", "银行");
        alias("国有大型银行", "银行");
        alias("证券", "证券");
        alias("多元金融", "证券");
        alias("房地产开发", "房地产");
        alias("房地产服务", "房地产");
        alias("保险", "保险");
    }

    private static void put(String key, String oneLiner, String position, String... strengthAndRisk) {
        int mid = strengthAndRisk.length / 2;
        List<String> strengths = Arrays.asList(Arrays.copyOfRange(strengthAndRisk, 0, mid));
        List<String> risks = Arrays.asList(Arrays.copyOfRange(strengthAndRisk, mid, strengthAndRisk.length));
        TEMPLATES.put(key, new Template(oneLiner, position, strengths, risks));
    }

    private static void alias(String from, String to) {
        ALIASES.put(from, to);
    }

    public static String resolveTemplateKey(String industry) {
        if (StringUtils.isBlank(industry) || "综合".equals(industry.trim())) {
            return "综合";
        }
        String trimmed = industry.trim();
        if (TEMPLATES.containsKey(trimmed)) {
            return trimmed;
        }
        String mapped = ALIASES.get(trimmed);
        if (mapped != null) {
            return mapped;
        }
        for (Map.Entry<String, String> e : ALIASES.entrySet()) {
            if (trimmed.contains(e.getKey()) || e.getKey().contains(trimmed)) {
                return e.getValue();
            }
        }
        for (String key : TEMPLATES.keySet()) {
            if (!"综合".equals(key) && (trimmed.contains(key) || key.contains(trimmed))) {
                return key;
            }
        }
        return "综合";
    }

    public static Map<String, Object> buildTemplateProfile(String stockName, String industry) {
        String safeName = StringUtils.defaultIfBlank(stockName, "该公司");
        String safeIndustry = StringUtils.defaultIfBlank(industry, "综合");
        String key = resolveTemplateKey(safeIndustry);
        Template template = TEMPLATES.getOrDefault(key, TEMPLATES.get("综合"));

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("businessOneLiner", fill(template.businessOneLiner, safeName, safeIndustry));
        profile.put("industryPosition", fill(template.industryPosition, safeName, safeIndustry));
        profile.put("strengths", copyList(template.strengths));
        profile.put("risks", copyList(template.risks));
        profile.put("dimensions", Collections.emptyList());
        profile.put("profileTemplateKey", key);
        return profile;
    }

    private static String fill(String tpl, String name, String industry) {
        return tpl.replace("{name}", name).replace("{industry}", industry);
    }

    private static List<String> copyList(List<String> src) {
        return new java.util.ArrayList<>(src);
    }
}
