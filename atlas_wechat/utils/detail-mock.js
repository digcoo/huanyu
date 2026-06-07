/**
 * 详情页 Mock 数据 — 基于首页标的扩展，使用种子随机保证刷新一致
 */

const { buildStrategyRecommendations } = require('./mock');
const adapter = require('./adapter');

const STAGE_MAP = {
  expansion: { id: 'expansion', label: '快速扩张', desc: '营收高增长，资本开支加大，现金流紧但结构健康', color: '#0ecb81' },
  stable: { id: 'stable', label: '稳定守业', desc: '营收增速回落，现金流充沛，分红回报提升', color: '#f0b90b' },
  shrink: { id: 'shrink', label: '收缩聚焦', desc: '营收下滑但利润短期回升，降本增效特征明显', color: '#ff9500' },
  decline: { id: 'decline', label: '萎缩淘汰', desc: '量价齐跌，连续承压，现金流失血风险', color: '#f6465d' }
};

const INDUSTRY_MAP = {
  cn: { '600519': '白酒', '000858': '白酒', '300750': '锂电池', '601318': '保险', '688981': '半导体', '601012': '光伏' },
  hk: { '00700': '互联网', '09988': '电商', '01810': '消费电子', '03690': '电商', '09992': '潮玩' },
  us: { NVDA: '半导体', AAPL: '消费电子', TSLA: '新能源汽车', MSFT: '互联网', BABA: '电商' },
  crypto: { BTC: '加密货币', ETH: '加密货币', SOL: '公链' },
  futures: { AU2506: '贵金属', SC2506: '能源' },
  forex: { USDCNY: '外汇' },
  bond: { CN10Y: '国债', US10Y: '国债' }
};

const STAGE_HINTS = {
  expansion: '处于快速扩张期 → 关注营收增速与资本开支效率，而非短期估值。',
  stable: '处于稳定守业期 → 适合看现金流、分红与护城河，而非爆发式增长。',
  shrink: '处于收缩聚焦期 → 重点看降本增效能否持续、现金流能否修复。',
  decline: '处于萎缩承压期 → 优先评估偿债能力与核心业务是否还有拐点。'
};

const RADAR_META = {
  'ROE': { higherBetter: true, winText: '股东回报优于同业', loseText: '资本回报落后于行业' },
  '毛利率': { higherBetter: true, winText: '定价权与盈利空间较强', loseText: '盈利空间弱于同业' },
  '营收增速': { higherBetter: true, winText: '成长动能领先行业', loseText: '营收增长落后于同业' },
  '资产负债率': { higherBetter: false, winText: '杠杆水平相对可控', loseText: '负债压力高于行业' },
  '研发占比': { higherBetter: true, winText: '研发投入力度较大', loseText: '研发强度低于同业' }
};

const COMPANY_PROFILES = {
  '600519': {
    businessOneLiner: '高端白酒龙头，以茅台酒为核心大单品，经销与直销渠道并行。',
    industryPosition: 'A股白酒市值第一，品牌溢价与渠道管控力处于行业顶尖。',
    strengths: ['国民级品牌壁垒，定价权强劲', '现金流充沛，预收款模式优异', '直营比例提升，渠道利润回收'],
    risks: ['消费场景集中，政策与舆情敏感', '估值长期处于行业高位', '年轻消费结构变化需跟踪']
  },
  '688981': {
    businessOneLiner: '中国大陆规模最大、技术最先进的晶圆代工企业之一。',
    industryPosition: '国产半导体制造核心标的，先进制程与成熟制程并重。',
    strengths: ['国产替代战略地位突出', '产能利用率与规模效应', '政策与产业基金支持'],
    risks: ['先进制程受外部约束', '资本开支大、折旧压力高', '行业周期波动明显']
  },
  '300750': {
    businessOneLiner: '全球动力电池龙头，覆盖动力与储能两大电池系统。',
    industryPosition: '全球动力电池装机量领先，产业链垂直整合能力强。',
    strengths: ['规模与成本优势领先', '客户覆盖全球主流车企', '储能第二曲线增长'],
    risks: ['原材料价格波动', '行业竞争加剧压缩毛利', '海外贸易与地缘风险']
  },
  '00700': {
    businessOneLiner: '以社交+游戏为基石，延伸至金融科技、云与企业服务的互联网巨头。',
    industryPosition: '港股互联网市值龙头，微信生态具备稀缺流量入口。',
    strengths: ['微信/QQ 超级入口与网络效应', '游戏与广告现金流稳定', '回购与股东回报积极'],
    risks: ['游戏版号与监管政策', '广告需求随宏观波动', '云与 ToB 业务盈利仍在爬坡']
  },
  '01810': {
    businessOneLiner: '智能手机×AIoT×智能电动汽车「人车家全生态」布局。',
    industryPosition: '全球智能手机出货前列，汽车业务为新增量曲线。',
    strengths: ['IoT 生态与用户粘性', '供应链效率与性价比', '汽车交付量快速爬坡'],
    risks: ['手机市场增长放缓', '汽车业务仍处投入期', '海外拓展面临竞争']
  },
  'NVDA': {
    businessOneLiner: 'GPU 与 AI 算力基础设施龙头，数据中心业务为核心增长引擎。',
    industryPosition: '全球 AI 训练芯片市占率领先，CUDA 生态护城河深厚。',
    strengths: ['AI 算力需求爆发受益', '软硬件一体化生态', '数据中心营收高增'],
    risks: ['估值与预期较高', '大客户自研芯片替代', '出口管制与地缘风险']
  },
  'AAPL': {
    businessOneLiner: 'iPhone 为核心，服务、可穿戴与 Mac 组成硬件+服务生态。',
    industryPosition: '全球市值最大消费电子公司，品牌与用户粘性极强。',
    strengths: ['生态系统锁定与高 ARPU', '服务业务毛利率高', '现金流与回购力度大'],
    risks: ['iPhone 销售周期波动', '中国市场竞争加剧', '创新节奏与监管审查']
  },
  'MSFT': {
    businessOneLiner: 'Azure 云 + Office 365 + Copilot AI 的企业软件与云计算巨头。',
    industryPosition: '全球企业云与生产力软件领导者，AI 商业化进展领先。',
    strengths: ['Azure 与 Copilot 双轮驱动', '企业客户粘性强', '盈利与现金流稳健'],
    risks: ['云增速边际放缓', 'AI 投入期成本上升', '反垄断与数据监管']
  },
  '09992': {
    businessOneLiner: '以 IP 运营为核心的潮流玩具公司，通过盲盒、手办、MEGA 收藏及主题乐园变现。',
    industryPosition: '中国潮玩龙头，全球门店与 IP 矩阵领先，Labubu 等现象级 IP 破圈出海。',
    strengths: [
      '自有 IP 占比超 85%，Molly/Labubu 等头部 IP 持续放量',
      '全渠道覆盖：直营店+机器人+加盟+电商，海外扩张提速',
      '会员体系与社群运营带来高复购与收藏属性'
    ],
    risks: [
      '单 IP 生命周期依赖，新 IP 孵化存在不确定性',
      '海外本地化与渠道管理成本上升',
      '盲盒监管趋严、二手价波动影响品牌热度'
    ],
    dimensions: [
      {
        key: 'model',
        label: '商业模式',
        text: '签约/自研艺术家 IP → 盲盒与手办产品化 → 线下门店、机器人、电商、主题乐园全渠道销售，会员复购驱动'
      },
      {
        key: 'revenue',
        label: '收入结构',
        text: '自有 IP 约 85%+（Labubu、Molly、SKULLPANDA 等），第三方 IP 授权约 15%；海外收入占比快速提升，大中华区仍为核心基本盘'
      },
      {
        key: 'customer',
        label: '客群画像',
        text: '18–35 岁都市女性及 Z 世代为主，收藏、社交、晒图属性强；从「买玩具」转向「买 IP 情感连接」，复购率显著高于传统玩具'
      },
      {
        key: 'moat',
        label: '护城河',
        text: '头部艺术家签约壁垒 + 自有 IP 矩阵 + 高密度渠道 + 会员私域，形成「内容—产品—渠道—社群」闭环，竞品难以短期复制'
      },
      {
        key: 'growth',
        label: '增长驱动',
        text: '海外门店与本地化运营、MEGA/大娃高端线、主题乐园、游戏与数字化延伸、新 IP 持续孵化'
      },
      {
        key: 'watch',
        label: '投资关注点',
        text: '单 IP 热度能否延续、海外同店增长、毛利率与库存周转、新 IP 成功率、盲盒政策与二手市场价格'
      }
    ]
  },
  'BTC': {
    businessOneLiner: '去中心化数字黄金，总量 2100 万枚，通过 PoW 共识保障安全。',
    industryPosition: '加密货币市值第一，机构配置与 ETF 通道逐步完善。',
    strengths: ['品牌与网络效应最强', '减半周期与稀缺叙事', 'ETF 等合规通道拓展'],
    risks: ['价格波动极大', '监管政策不确定', '能源消耗舆论压力']
  }
};

const INDUSTRY_PROFILE_DEFAULT = {
  '白酒': {
    businessOneLiner: '{name}主营白酒生产与销售，品牌与渠道是核心竞争要素。',
    industryPosition: '{name}处于{name}所在白酒赛道，需关注品牌力与渠道管控。',
    strengths: ['品牌与渠道构成主要壁垒', '高端化趋势下毛利率较高', '现金流通常优于制造业'],
    risks: ['消费政策与舆情风险', '行业集中度提升竞争加剧', '库存与批价波动']
  },
  '半导体': {
    businessOneLiner: '{name}从事半导体设计/制造/设备，受国产替代与周期双重驱动。',
    industryPosition: '{name}在半导体产业链中占据关键环节，行业地位取决于技术与产能。',
    strengths: ['国产替代长期逻辑', '高壁垒带来一定定价权', '政策与产业资本支持'],
    risks: ['资本开支大、周期性强', '技术迭代与外部约束', '库存与价格下行风险']
  },
  '锂电池': {
    businessOneLiner: '{name}布局动力/储能电池或材料，绑定新能源产业链。',
    industryPosition: '{name}在锂电产业链中竞争格局激烈，规模与成本决定地位。',
    strengths: ['新能源渗透率长期向上', '龙头规模效应显著', '储能打开第二增长曲线'],
    risks: ['原材料与价格竞争', '产能过剩隐忧', '技术路线变更风险']
  },
  '互联网': {
    businessOneLiner: '{name}以流量与平台为核心，变现方式包括广告、游戏、金融科技等。',
    industryPosition: '{name}在互联网细分赛道中竞争，用户规模与留存是关键。',
    strengths: ['网络效应与平台壁垒', '轻资产、边际成本递减', '多元变现路径'],
    risks: ['监管与合规要求', '流量红利见顶', '宏观影响广告与消费']
  },
  '消费电子': {
    businessOneLiner: '{name}面向消费者的硬件与生态产品，依赖创新与供应链效率。',
    industryPosition: '{name}在消费电子赛道中，品牌与渠道决定市场份额。',
    strengths: ['品牌与用户生态', '供应链管理与成本控制', '产品迭代带来换机周期'],
    risks: ['需求波动与库存风险', '竞争压缩利润率', '创新不及预期']
  },
  '新能源汽车': {
    businessOneLiner: '{name}从事整车或核心零部件，电动化与智能化是主线。',
    industryPosition: '{name}在新能源车赛道中，交付量与毛利率是核心指标。',
    strengths: ['电动化渗透率提升', '智能化差异化空间', '政策与基础设施支持'],
    risks: ['价格战压缩利润', '补贴退坡与竞争加剧', '供应链与召回风险']
  },
  '加密货币': {
    businessOneLiner: '{name}为区块链原生资产，价格由供需、叙事与流动性驱动。',
    industryPosition: '{name}在加密市场中流动性与共识决定地位。',
    strengths: ['去中心化与全球流通', '减半/升级等叙事催化', '机构配置通道拓宽'],
    risks: ['极高波动与监管风险', '安全与黑客事件', '宏观流动性敏感']
  },
  '电商': {
    businessOneLiner: '{name}以电商平台为核心，变现依赖 GMV、广告与云计算等。',
    industryPosition: '{name}在电商/本地生活赛道中，用户规模与履约效率是关键。',
    strengths: ['平台规模与数据资产', '多元变现与生态协同', '下沉市场与出海空间'],
    risks: ['竞争与补贴压力', '监管与反垄断', '宏观消费疲软']
  },
  '潮玩': {
    businessOneLiner: '{name}以 IP 运营为核心，通过盲盒、手办等产品在全渠道变现。',
    industryPosition: '{name}在潮玩赛道中，IP 矩阵与渠道密度决定市场份额。',
    strengths: ['IP 情感连接带来高溢价', '收藏属性支撑复购', '全渠道触达年轻客群'],
    risks: ['单 IP 生命周期风险', '盲盒监管与舆论', '竞争加剧与库存波动']
  },
  '保险': {
    businessOneLiner: '{name}以寿险/财险为主，投资收益与承保利润双轮驱动。',
    industryPosition: '{name}在保险行业中，渠道与负债成本决定竞争力。',
    strengths: ['长期保单现金流稳定', '品牌与代理人渠道', '投资端弹性'],
    risks: ['利率下行压制利差', '赔付与退保波动', '监管与资本要求']
  },
  '光伏': {
    businessOneLiner: '{name}布局硅片/组件/电站，受装机量与价格周期影响大。',
    industryPosition: '{name}在光伏产业链中，成本与一体化程度决定地位。',
    strengths: ['碳中和长期需求', '龙头成本曲线领先', '技术迭代带来效率提升'],
    risks: ['产能过剩与价格战', '贸易壁垒', '上游硅料价格波动']
  },
  '能源': {
    businessOneLiner: '{name}为原油/天然气等能源衍生品，价格受供需与地缘驱动。',
    industryPosition: '{name}在能源市场中，宏观与 OPEC 政策是核心变量。',
    strengths: ['通胀与地缘避险属性', '供需缺口阶段性支撑', '产业链定价基准'],
    risks: ['宏观衰退压制需求', '政策与储备释放', '高波动风险']
  },
  '外汇': {
    businessOneLiner: '{name}反映两国货币相对强弱，受利差、贸易与政策影响。',
    industryPosition: '{name}在汇率市场中，央行政策与跨境资本流动是主线。',
    strengths: ['流动性极好', '宏观指标可跟踪', '对冲与配置工具'],
    risks: ['政策干预风险', '单边波动', '杠杆放大损失']
  },
  '国债': {
    businessOneLiner: '{name}代表无风险利率基准，价格与收益率反向变动。',
    industryPosition: '{name}在债券市场中作为定价锚，受央行与通胀预期驱动。',
    strengths: ['信用风险极低', '避险与配置价值', '宏观政策敏感可交易'],
    risks: ['利率上行带来价格下跌', '通胀超预期', '流动性阶段性收紧']
  },
  '公链': {
    businessOneLiner: '{name}为公链原生代币，价值与网络活跃度和生态绑定。',
    industryPosition: '{name}在公链竞争中，TPS、开发者与 TVL 是核心指标。',
    strengths: ['生态应用快速增长', '网络效应与社区', '高 Beta 弹性'],
    risks: ['竞争链分流', '安全与升级风险', '监管不确定性']
  },
  '综合': {
    businessOneLiner: '{name}为跨行业或综合类标的，需结合具体业务线理解。',
    industryPosition: '{name}在{industry}领域经营，行业地位需结合财报与竞品判断。',
    strengths: ['业务多元化分散风险', '具备一定区域或品类优势', '估值修复空间需个案分析'],
    risks: ['业务复杂度高、透明度低', '宏观与政策波动', '竞争格局变化']
  }
};

function fillProfileTemplate(tpl, name, industry) {
  return tpl
    .replace(/\{name\}/g, name)
    .replace(/\{industry\}/g, industry);
}

function getCompanyProfile(code, name, industry) {
  if (COMPANY_PROFILES[code]) {
    const src = COMPANY_PROFILES[code];
    const profile = {
      businessOneLiner: src.businessOneLiner,
      industryPosition: src.industryPosition,
      strengths: src.strengths.slice(),
      risks: src.risks.slice()
    };
    if (src.dimensions) {
      profile.dimensions = src.dimensions.map(function (d) {
        return { key: d.key, label: d.label, text: d.text };
      });
    }
    return profile;
  }
  const fallback = INDUSTRY_PROFILE_DEFAULT[industry] || INDUSTRY_PROFILE_DEFAULT['综合'];
  return {
    businessOneLiner: fillProfileTemplate(fallback.businessOneLiner, name, industry),
    industryPosition: fillProfileTemplate(fallback.industryPosition, name, industry),
    strengths: fallback.strengths.slice(),
    risks: fallback.risks.slice()
  };
}

function buildRadarInsights(radar) {
  return radar.dimensions.map(function (dim, i) {
    const meta = RADAR_META[dim] || { higherBetter: true, winText: '优于行业', loseText: '低于行业' };
    const company = radar.company[i];
    const industry = radar.industry[i];
    const unit = radar.unit ? radar.unit[i] : '';
    const win = meta.higherBetter ? company >= industry : company <= industry;
    const cmp = win ? '高于' : '低于';
    return {
      dim: dim,
      company: company,
      industry: industry,
      unit: unit,
      win: win,
      tag: win ? '优势' : '关注',
      text: dim + ' ' + company + unit + '，' + cmp + '行业 ' + industry + unit + '，' + (win ? meta.winText : meta.loseText)
    };
  });
}

function buildPortraitDimensions(stage, healthBreakdown, radarInsights) {
  const profitInsight = radarInsights[0] || {};
  const marginInsight = radarInsights[1] || {};
  return [
    { key: 'profit', label: '盈利能力', score: healthBreakdown.profit, text: profitInsight.text || '盈利指标待补充' },
    { key: 'growth', label: '成长动能', score: healthBreakdown.growth, text: stage.label + '阶段，' + stage.desc },
    { key: 'operation', label: '运营效率', score: healthBreakdown.operation, text: '人均创收与周转效率综合评估' },
    { key: 'debt', label: '财务安全', score: healthBreakdown.debt, text: (radarInsights[3] && radarInsights[3].text) || '偿债与杠杆综合评估' }
  ];
}

const STAGE_INSIGHTS = {
  expansion: {
    financial: '营收保持双位数增长，资本开支同步扩大，经营现金流覆盖投资支出约 85%，扩张结构整体健康。',
    operation: '员工规模温和扩张，人均创收稳步提升；存货周转天数下降，运营效率改善。',
    chain: '毛利率稳定高于行业均值，预收账款占比提升，对上游议价能力增强。',
    capital: '资产负债率处于合理区间，有息负债率可控，货币资金对短期借款覆盖倍数 > 1.5。'
  },
  stable: {
    financial: '营收增速回落至个位数，但净利润率稳定，经营现金流持续为正，分红比例提升。',
    operation: '人员结构优化，人均创收维持高位；费用率控制良好，运营杠杆稳定。',
    chain: '行业地位稳固，渠道议价能力保持，毛利率波动收窄。',
    capital: '负债结构保守，现金储备充裕，资本回报稳定，适合长期持有。'
  },
  shrink: {
    financial: '营收短期承压，但降本增效推动利润率回升，资本开支收缩，现金流逐步修复。',
    operation: '人员精简，人均效率提升；库存去化加速，周转天数明显改善。',
    chain: '市场份额阶段性收缩，但核心产品线毛利率仍高于行业。',
    capital: '主动去杠杆，有息负债下降，财务风险可控。'
  },
  decline: {
    financial: '营收与利润双降，经营现金流转负，需关注应收账款与存货减值风险。',
    operation: '人效下滑，费用刚性偏高，运营效率弱于同业。',
    chain: '议价能力减弱，毛利率低于行业均值，渠道库存偏高。',
    capital: '负债率攀升，短期偿债压力加大，需警惕再融资风险。'
  }
};

function createSeededRandom(seed) {
  let s = seed % 2147483647;
  if (s <= 0) s += 2147483646;
  return function () {
    s = (s * 16807) % 2147483647;
    return (s - 1) / 2147483646;
  };
}

function genYearSeries(rng, startVal, growth, years, jitter) {
  jitter = jitter || 0.03;
  const res = [];
  let val = startVal;
  const y0 = new Date().getFullYear() - years + 1;
  for (let i = 0; i < years; i++) {
    const noise = 1 + (rng() - 0.5) * jitter;
    val = val * (1 + growth + (rng() - 0.5) * 0.04) * noise;
    res.push({ year: String(y0 + i), value: +val.toFixed(2) });
  }
  return res;
}

function genPercentSeries(rng, start, trend, years) {
  const res = [];
  const y0 = new Date().getFullYear() - years + 1;
  for (let i = 0; i < years; i++) {
    const v = start + trend * i + (rng() - 0.5) * 2;
    res.push({ year: String(y0 + i), value: +Math.max(0, v).toFixed(1) });
  }
  return res;
}

/** 基于企业序列生成行业均值对比线（略低/更平，保持同周期） */
function buildIndustryBenchmark(rng, companyData, ratio) {
  ratio = ratio || 0.85;
  return companyData.map(function (d) {
    var jitter = (rng() - 0.5) * 0.06;
    var scale = ratio + jitter;
    return {
      year: d.year,
      value: +(d.value * scale).toFixed(2)
    };
  });
}

function withIndustryCompare(rng, chart, ratio) {
  return Object.assign({}, chart, {
    industryData: buildIndustryBenchmark(rng, chart.data, ratio)
  });
}

function findBaseItem(id) {
  const all = buildStrategyRecommendations();
  for (const strategyId in all) {
    const byMarket = all[strategyId];
    for (const market in byMarket) {
      const found = byMarket[market].find(item => item.id === id);
      if (found) return found;
    }
  }
  return null;
}

function pickStage(resonance, changePct) {
  if (resonance === 'strong' && changePct > 0) return STAGE_MAP.expansion;
  if (resonance === 'weak' || (changePct < -1 && changePct > -2)) return STAGE_MAP.shrink;
  if (changePct <= -2) return STAGE_MAP.decline;
  if (resonance === 'medium') return STAGE_MAP.stable;
  return STAGE_MAP.expansion;
}

function buildRadar(seed, stageId) {
  const rng = createSeededRandom(seed + 100);
  const stageBoost = { expansion: 8, stable: 3, shrink: -2, decline: -10 };
  const boost = stageBoost[stageId] || 0;

  const company = [
    +(16 + boost + rng() * 12).toFixed(1),
    +(38 + boost * 0.8 + rng() * 10).toFixed(1),
    +(6 + boost * 0.5 + rng() * 8).toFixed(1),
    +(30 + rng() * 18).toFixed(1),
    +(1.8 + rng() * 4).toFixed(1)
  ];

  const industry = [18.2, 41.5, 6.3, 47.8, 2.4];

  return {
    dimensions: ['ROE', '毛利率', '营收增速', '资产负债率', '研发占比'],
    unit: ['%', '%', '%', '%', '%'],
    company,
    industry,
    industryAvg: industry
  };
}

function radarToPoints(values, maxValues) {
  const count = values.length;
  const points = [];
  for (let i = 0; i < count; i++) {
    const angle = (Math.PI * 2 * i / count) - Math.PI / 2;
    const ratio = values[i] / (maxValues[i] || 1);
    const r = Math.min(ratio, 1.15) * 42;
    points.push({
      x: (50 + r * Math.cos(angle)).toFixed(2),
      y: (50 + r * Math.sin(angle)).toFixed(2)
    });
  }
  return points;
}

function buildCompass(rng, base, stageId) {
  const insights = STAGE_INSIGHTS[stageId] || STAGE_INSIGHTS.expansion;
  const rev = genYearSeries(rng, base * 0.4, 0.12, 8, 0.05);
  const profit = genYearSeries(rng, base * 0.08, 0.15, 8, 0.08);
  const ocf = genYearSeries(rng, base * 0.06, 0.1, 8, 0.1);
  const capex = genYearSeries(rng, base * 0.05, 0.08, 8, 0.12);
  const employees = genYearSeries(rng, 8000, 0.06, 8, 0.02).map(d => ({
    year: d.year,
    value: Math.round(d.value)
  }));
  const revPerEmp = rev.map((r, i) => ({
    year: r.year,
    value: +(r.value * 1000 / employees[i].value).toFixed(1)
  }));

  return {
    financial: {
      title: '财务动能',
      color: '#0ecb81',
      insight: insights.financial,
      charts: [
        { name: '营收', unit: '亿', color: '#0ecb81', data: rev },
        { name: '净利润', unit: '亿', color: '#f0b90b', data: profit },
        { name: '经营现金流', unit: '亿', color: '#848e9c', data: ocf },
        { name: '资本开支', unit: '亿', color: '#f6465d', data: capex }
      ]
    },
    operation: {
      title: '运营人效',
      color: '#f0b90b',
      insight: insights.operation,
      charts: (function () {
        var invDays = genPercentSeries(rng, 85, -3, 8);
        var arDays = genPercentSeries(rng, 42, -1.5, 8);
        return [
          { name: '员工数', unit: '人', color: '#848e9c', data: employees },
          withIndustryCompare(rng, { name: '人均创收', unit: '万', color: '#0ecb81', data: revPerEmp }, 0.72),
          withIndustryCompare(rng, { name: '存货周转天数', unit: '天', color: '#ff9500', data: invDays }, 1.08),
          withIndustryCompare(rng, { name: '应收周转天数', unit: '天', color: '#a78bfa', data: arDays }, 1.05)
        ];
      })()
    },
    chain: {
      title: '产业链地位',
      color: '#a78bfa',
      insight: insights.chain,
      compareIndustry: true,
      charts: (function () {
        var gross = genPercentSeries(rng, 38, 1.2, 8);
        var net = genPercentSeries(rng, 22, 0.8, 8);
        var prepay = genPercentSeries(rng, 8, 0.6, 8);
        var apAr = genYearSeries(rng, 1.2, 0.02, 8).map(function (d) {
          return { year: d.year, value: +d.value.toFixed(2) };
        });
        return [
          withIndustryCompare(rng, { name: '毛利率', unit: '%', color: '#0ecb81', data: gross }, 0.82),
          withIndustryCompare(rng, { name: '净利率', unit: '%', color: '#f0b90b', data: net }, 0.78),
          withIndustryCompare(rng, { name: '预收占比', unit: '%', color: '#848e9c', data: prepay }, 0.65),
          withIndustryCompare(rng, { name: '应付/应收比', unit: 'x', color: '#ff9500', data: apAr }, 0.88)
        ];
      })()
    },
    capital: {
      title: '资本结构',
      color: '#f6465d',
      insight: insights.capital,
      charts: (function () {
        var debtRatio = genPercentSeries(rng, 32, 0.5, 8);
        var ibDebtRatio = genPercentSeries(rng, 12, 0.2, 8);
        return [
          withIndustryCompare(rng, { name: '资产负债率', unit: '%', color: '#f6465d', data: debtRatio }, 1.02),
          withIndustryCompare(rng, { name: '有息负债率', unit: '%', color: '#ff9500', data: ibDebtRatio }, 0.92),
          { name: '货币资金', unit: '亿', color: '#0ecb81', data: genYearSeries(rng, base * 0.15, 0.08, 8) },
          { name: '短期借款', unit: '亿', color: '#848e9c', data: genYearSeries(rng, base * 0.06, 0.03, 8) }
        ];
      })()
    }
  };
}

const COMPANY_KEY_METRICS = {
  '09992': [
    { label: '市盈率 TTM', value: '28x' },
    { label: '市销率', value: '8.2x' },
    { label: '毛利率', value: '62.3%' },
    { label: '自有 IP 占比', value: '85%+' },
    { label: '海外收入占比', value: '~39%' },
    { label: '52周高', value: '88.50' }
  ]
};

function buildKeyMetrics(base, seed, radar) {
  if (COMPANY_KEY_METRICS[base.code]) {
    return COMPANY_KEY_METRICS[base.code].slice();
  }
  const priceBase = typeof base.price === 'number' ? base.price : 100;
  return [
    { label: '市盈率 TTM', value: (15 + seed % 25) + 'x' },
    { label: '市净率', value: (2 + (seed % 10) * 0.3).toFixed(1) + 'x' },
    { label: 'ROE', value: radar.company[0] + '%' },
    { label: '毛利率', value: radar.company[1] + '%' },
    { label: '股息率', value: (1 + (seed % 5) * 0.4).toFixed(1) + '%' },
    { label: '52周高', value: (priceBase * 1.18).toFixed(2) }
  ];
}

function getDetailById(id) {
  const base = findBaseItem(id);
  if (!base) return null;
  return buildDetailFromStock(base, {});
}

function buildDetailFromStock(base, overrides) {
  overrides = overrides || {};
  const id = base.id || ('trend-cn-' + adapter.normalizeCode(base.code));
  const seed = String(base.code || id).split('').reduce(function (s, c) {
    return s + c.charCodeAt(0);
  }, 0);
  const industry = overrides.industry
    || ((INDUSTRY_MAP[base.market] && INDUSTRY_MAP[base.market][adapter.displayCode(base.code)])
      || INDUSTRY_MAP[base.market] && INDUSTRY_MAP[base.market][base.code])
    || '综合';
  const stage = overrides.stage || pickStage(base.resonance, base.changePct);
  const healthScore = overrides.healthScore != null ? overrides.healthScore : Math.min(95, 58 + (seed % 38));
  const radar = overrides.radar || buildRadar(seed, stage.id);
  const maxRadar = radar.dimensions.map(function (_, i) {
    return Math.max(radar.company[i], radar.industry[i]) * 1.2;
  });

  const priceBase = typeof base.price === 'number' ? base.price : 100;
  const compass = overrides.compass || buildCompass(createSeededRandom(seed + 200), priceBase, stage.id);

  const profile = overrides.profile || getCompanyProfile(base.code, base.name, industry);
  const radarInsights = radar.insights || buildRadarInsights(radar);
  const healthBreakdown = overrides.healthBreakdown || {
    profit: Math.min(99, healthScore + 5),
    growth: Math.min(99, healthScore - 3),
    debt: Math.min(99, healthScore + 8),
    operation: Math.min(99, healthScore - 1)
  };

  return Object.assign({}, base, {
    id: id,
    industry: industry,
    stage: stage,
    stageHint: overrides.stageHint || STAGE_HINTS[stage.id] || '',
    healthScore: healthScore,
    healthRank: overrides.healthRank || ('盈利力超过' + industry + '行业 ' + healthScore + '% 的公司'),
    healthBreakdown: healthBreakdown,
    profile: profile,
    portraitDimensions: overrides.portraitDimensions || buildPortraitDimensions(stage, healthBreakdown, radarInsights),
    strategySummary: base.summary,
    radar: Object.assign({}, radar, {
      companyPoints: radar.companyPoints || radarToPoints(radar.company, maxRadar),
      industryPoints: radar.industryPoints || radarToPoints(radar.industry, maxRadar),
      gridLevels: radar.gridLevels || [20, 35, 50],
      insights: radarInsights
    }),
    compass: compass,
    compassList: [
      compass.financial,
      compass.operation,
      compass.chain,
      compass.capital
    ],
    keyMetrics: overrides.keyMetrics || buildKeyMetrics(base, seed, radar),
    competitors: overrides.competitors || []
  });
}

module.exports = {
  getDetailById: getDetailById,
  buildDetailFromStock: buildDetailFromStock,
  STAGE_MAP: STAGE_MAP
};
