/**
 * Backend DTO → 小程序 UI 字段映射
 */
const { MARKETS } = require('./mock');
const { formatDataUpdatedLabel } = require('./time');

const STRATEGY_API = {
  magoldbreak: { strategy: 'magoldbreak', trendPeriodTypes: 'year,quarter,month,week,day,min30', opPeriodType: 'day' },
  magoldbreakFlash: { strategy: 'magoldbreak', trendPeriodTypes: 'year,quarter,month,week,day,min30', opPeriodType: 'min30' },
  magoldbreakShort: { strategy: 'magoldbreak', trendPeriodTypes: 'year,quarter,month,week,day,min30', opPeriodType: 'day' },
  magoldbreakMedium: { strategy: 'magoldbreak', trendPeriodTypes: 'year,quarter,month,week,day,min30', opPeriodType: 'week' },
  magoldbreakLong: { strategy: 'magoldbreak', trendPeriodTypes: 'year,quarter,month,week,day,min30', opPeriodType: 'month' },
  magoldbreakQuarter: { strategy: 'magoldbreak', trendPeriodTypes: 'year,quarter,month,week,day,min30', opPeriodType: 'quarter' },
  madeathbreak: { strategy: 'madeathbreak', trendPeriodTypes: 'year,quarter,month,week,day,min30', opPeriodType: 'day' },
  madeathbreakFlash: { strategy: 'madeathbreak', trendPeriodTypes: 'year,quarter,month,week,day,min30', opPeriodType: 'min30' },
  madeathbreakShort: { strategy: 'madeathbreak', trendPeriodTypes: 'year,quarter,month,week,day,min30', opPeriodType: 'day' },
  madeathbreakMedium: { strategy: 'madeathbreak', trendPeriodTypes: 'year,quarter,month,week,day,min30', opPeriodType: 'week' },
  madeathbreakLong: { strategy: 'madeathbreak', trendPeriodTypes: 'year,quarter,month,week,day,min30', opPeriodType: 'month' },
  madeathbreakQuarter: { strategy: 'madeathbreak', trendPeriodTypes: 'year,quarter,month,week,day,min30', opPeriodType: 'quarter' }
};

function normalizeStrategyId(strategyId) {
  if (!strategyId) return 'magoldbreakFlash';
  if (strategyId === 'magoldbreak') return 'magoldbreakFlash';
  if (strategyId === 'madeathbreak') return 'madeathbreakFlash';
  // 旧下线策略本地缓存 → MA金叉点·30分
  if (/^bottombandhigh|^bottomprev2high|^maalignlift|^macrossbreak|^mabearbreakma|^mabull4m|^concavebreak|^trendretest(low|high)|^prevbandhigh|^mabreakma|^mabull3m/.test(strategyId)) {
    return 'magoldbreakFlash';
  }
  if (/^magoldbreak|^madeathbreak/.test(strategyId)) {
    return strategyId;
  }
  return 'magoldbreakFlash';
}

function normalizeCode(code) {
  if (!code) return '';
  var c = String(code).trim().toLowerCase();
  if (c.indexOf('sh') === 0 || c.indexOf('sz') === 0) return c;
  if (/^\d{6}$/.test(c)) {
    return c.indexOf('6') === 0 ? 'sh' + c : 'sz' + c;
  }
  return c;
}

function displayCode(code) {
  var c = normalizeCode(code);
  if (c.indexOf('sh') === 0) return c.slice(2);
  if (c.indexOf('sz') === 0) return c.slice(2);
  return c;
}

function makeStockId(strategy, market, code) {
  return strategy + '-' + market + '-' + normalizeCode(code);
}

function extractCode(idOrCode) {
  if (!idOrCode) return '';
  var s = String(idOrCode);
  if (s.indexOf('-') >= 0) {
    var parts = s.split('-');
    return normalizeCode(parts[parts.length - 1]);
  }
  return normalizeCode(s);
}

function extractStrategy(id) {
  if (!id || id.indexOf('-') < 0) return 'magoldbreakFlash';
  return normalizeStrategyId(id.split('-')[0] || 'magoldbreakFlash');
}

function findMarketLabel(marketId) {
  var meta = MARKETS.find(function (m) { return m.id === marketId; });
  return meta ? meta.icon + ' ' + meta.name : marketId;
}

function barsToKlines(bars) {
  if (!bars || !bars.length) return [];
  return bars.map(function (b) {
    return {
      day: b.day || '',
      open: b.open,
      high: b.high,
      low: b.low,
      close: b.close
    };
  });
}

function mapSearchItem(item, strategy) {
  strategy = strategy || 'ultra';
  var code = normalizeCode(item.code);
  return {
    id: makeStockId(strategy, item.market || 'cn', code),
    strategy: strategy,
    code: code,
    name: item.name,
    market: item.market || 'cn',
    marketLabel: findMarketLabel(item.market || 'cn'),
    price: item.price != null ? item.price : item.close,
    changePct: normalizeChangePct(item),
    signalMessage: item.signalMessage || '',
    trendMessage: item.trendMessage || ''
  };
}

function parseUnilateralTier(trendMessage) {
  if (!trendMessage) return null;
  var m = String(trendMessage).match(/\[([SABC])\]/);
  return m ? m[1] : null;
}

function parseUnilateralTrendLabel(trendMessage) {
  if (!trendMessage) return '';
  var s = String(trendMessage);
  var bracket = s.match(/\[([SABC])\]([^|,]+)/);
  if (bracket) return bracket[2].trim();
  return s.replace(/^\([^)]*:\s*/, '').replace(/\).*$/, '').trim();
}

function appendGlobalGateTags(item, tags) {
  var text = [item.trendMessage, item.signalMessage].join('|');
  if (/日周月双低支撑门|双低支撑门/.test(text)) {
    tags.push('双低门');
  }
  if (/日周月无阻力MACD门|无阻力MACD门/.test(text)) {
    tags.push('无阻力门');
  }
  if (/日周月MACD死叉高门|MACD死叉高门/.test(text)) {
    tags.push('死叉高门');
  }
  if (/日周月MACD交叉low门|MACD交叉low门/.test(text)) {
    tags.push('交叉low门');
  }
  if (/日周月年全阳门|日周月全阳门|全阳门/.test(text)) {
    tags.push('全阳门');
  }
}

function appendUltraShortTags(item, tags) {
  appendGlobalGateTags(item, tags);
  var text = [item && item.trendMessage, item && item.signalMessage].filter(Boolean).join('|');
  if (/\[ULTRA\]|30m跨日桶柱内上移|局部新高\/最近强K基准/.test(text)) {
    tags.push('基准30m');
  }
  if (/30m突破|柱内突破K,period=min30/.test(text)) {
    tags.push('超短突破');
  }
}

function buildUltraTags(item) {
  var tags = [];
  appendUltraShortTags(item, tags);
  return tags;
}

function buildTrendV2Tags(item) {
  var tags = [];
  if (item.trendMessage && /\[SHORT\]|周内局部新高/.test(item.trendMessage)) {
    tags.push('基准日K');
  }
  if (item.signalMessage && /日K突破|sigDay=/.test(item.signalMessage)) {
    tags.push('突破日K');
  }
  appendUltraShortTags(item, tags);
  if (item.trendMessage && /月MACD>0/.test(item.trendMessage)) tags.push('月MACD');
  if (item.trendMessage && /周MACD>0/.test(item.trendMessage)) tags.push('周MACD');
  if (item.trendMessage && /日MACD>0/.test(item.trendMessage)) tags.push('日MACD');
  if (item.signalMessage && item.signalMessage.indexOf('周K MACD金叉') >= 0) {
    tags.push('周K金叉');
  }
  return tags;
}

function buildMediumTags(item) {
  var tags = [];
  if (item.trendMessage && /\[MEDIUM\]|月内局部新高/.test(item.trendMessage)) {
    tags.push('基准周K');
  }
  if (item.signalMessage && /周K突破|sigDay=/.test(item.signalMessage)) {
    tags.push('突破周K');
  }
  appendUltraShortTags(item, tags);
  if (item.trendMessage && /月MACD>0/.test(item.trendMessage)) tags.push('月MACD');
  if (item.trendMessage && /年MACD>0/.test(item.trendMessage)) tags.push('年MACD');
  if (item.signalMessage && item.signalMessage.indexOf('月K MACD金叉') >= 0) {
    tags.push('月K金叉');
  }
  return tags;
}

function buildLongTags(item) {
  var tags = [];
  if (item.trendMessage && /\[LONG\]|年内局部新高/.test(item.trendMessage)) {
    tags.push('基准月K');
  }
  if (item.signalMessage && /月K突破|sigDay=/.test(item.signalMessage)) {
    tags.push('突破月K');
  }
  appendUltraShortTags(item, tags);
  if (item.trendMessage && /年MACD>0/.test(item.trendMessage)) tags.push('年MACD');
  if (item.trendMessage && /月MACD>0/.test(item.trendMessage)) tags.push('月MACD');
  if (item.signalMessage && item.signalMessage.indexOf('年K MACD金叉') >= 0) {
    tags.push('年K金叉');
  }
  return tags;
}

function buildUnilateralTags(item) {
  var tags = [];
  var tier = parseUnilateralTier(item.trendMessage);
  var label = parseUnilateralTrendLabel(item.trendMessage);
  if (tier === 'S') tags.push('短线金叉');
  else if (tier === 'B') tags.push('长线金叉');
  if (item.signalMessage && item.signalMessage.indexOf('日K MACD金叉') >= 0) {
    tags.push('日K金叉');
  }
  if (item.signalMessage && item.signalMessage.indexOf('周K MACD金叉') >= 0) {
    tags.push('周K金叉');
  }
  if (item.trendMessage && /月\/周MACD>0|月MACD>0/.test(item.trendMessage)) {
    tags.push('月/周多头');
  }
  if (item.trendMessage && /年\/月MACD>0|年MACD>0/.test(item.trendMessage)) {
    tags.push('年/月多头');
  }
  if (label && tags.indexOf(label) < 0 && label.length <= 12) {
    tags.push(label);
  }
  return tags;
}

function buildResonanceTags(item) {
  var tags = [];
  var tier = parseUnilateralTier(item.trendMessage);
  var label = parseUnilateralTrendLabel(item.trendMessage);
  if (tier === 'S') tags.push('短线共振');
  else if (tier === 'A') tags.push('中线共振');
  else if (tier === 'B') tags.push('长线共振');
  if (item.trendMessage && item.trendMessage.indexOf('非金叉') >= 0) {
    tags.push('MACD多头');
  }
  if (item.signalMessage && item.signalMessage.indexOf('日K突破') >= 0) {
    tags.push('日K突破');
  }
  if (item.signalMessage && item.signalMessage.indexOf('周K突破') >= 0) {
    tags.push('周K突破');
  }
  if (item.signalMessage && item.signalMessage.indexOf('月K突破') >= 0) {
    tags.push('月K突破');
  }
  if (label && tags.indexOf(label) < 0 && label.length <= 12) {
    tags.push(label);
  }
  return tags;
}

function buildReboundTags(item) {
  var tags = [];
  var tier = parseUnilateralTier(item.trendMessage);
  var label = parseUnilateralTrendLabel(item.trendMessage);
  var isV3 = label.indexOf('深跌反弹') >= 0
    || (item.signalMessage && /日K突破|周K突破|月K突破/.test(item.signalMessage))
    || (item.trendMessage && /MACD[<>]0/.test(item.trendMessage));
  if (tier === 'S') tags.push('短线深跌');
  else if (tier === 'A') tags.push('中线深跌');
  else if (tier === 'B') tags.push('长线深跌');
  if (item.signalMessage && item.signalMessage.indexOf('日K突破') >= 0) {
    tags.push('日K突破');
  }
  if (item.signalMessage && item.signalMessage.indexOf('周K突破') >= 0) {
    tags.push('周K突破');
  }
  if (item.signalMessage && item.signalMessage.indexOf('月K突破') >= 0) {
    tags.push('月K突破');
  }
  // v3.0 文案才追加趋势标签；旧版「恐慌释放后脱离」等不再展示
  if (isV3 && label && tags.indexOf(label) < 0 && label.length <= 12) {
    tags.push(label);
  }
  return tags;
}

function buildReboundSummary(item) {
  var signal = item.signalMessage || '';
  if (signal.indexOf('日K突破') >= 0 || signal.indexOf('周K突破') >= 0 || signal.indexOf('月K突破') >= 0) {
    return signal.split(',')[0];
  }
  var label = parseUnilateralTrendLabel(item.trendMessage);
  if (label.indexOf('深跌反弹') >= 0) return label;
  return signal.split(',')[0] || label || '';
}

function buildLadderTags(item) {
  var tags = [];
  var label = parseUnilateralTrendLabel(item.trendMessage);
  if (item.trendMessage && /前日.*新高/.test(item.trendMessage)) {
    tags.push('前日新高');
  }
  if (item.signalMessage && item.signalMessage.indexOf('high') >= 0) {
    tags.push('当日突破');
  }
  if (label && tags.indexOf(label) < 0 && label.length <= 12) {
    tags.push(label);
  }
  return tags;
}

function buildLadderSummary(item) {
  var signal = item.signalMessage || '';
  if (signal) return signal.split(',')[0];
  return parseUnilateralTrendLabel(item.trendMessage) || '';
}

function buildGc2Tags(item) {
  var tags = [];
  var tier = parseUnilateralTier(item.trendMessage);
  var label = parseUnilateralTrendLabel(item.trendMessage);
  if (tier === 'S') tags.push('日K二次突破');
  else if (tier === 'B') tags.push('周K二次突破');
  if (item.signalMessage && item.signalMessage.indexOf('refDay=') >= 0) {
    tags.push('突破金叉高点');
  }
  if (item.trendMessage && /月\/周MACD>0|月MACD>0/.test(item.trendMessage)) {
    tags.push('月/周多头');
  }
  if (item.trendMessage && /年\/月MACD>0|年MACD>0/.test(item.trendMessage)) {
    tags.push('年/月多头');
  }
  if (label && tags.indexOf(label) < 0 && label.length <= 14) {
    tags.push(label);
  }
  return tags;
}

function buildDc2Tags(item) {
  var tags = [];
  var tier = parseUnilateralTier(item.trendMessage);
  var label = parseUnilateralTrendLabel(item.trendMessage);
  if (tier === 'S') tags.push('日K死叉突破');
  else if (tier === 'B') tags.push('周K死叉突破');
  if (item.signalMessage && item.signalMessage.indexOf('refDay=') >= 0) {
    tags.push('突破死叉高点');
  }
  if (item.trendMessage && /月\/周MACD>0|月MACD>0/.test(item.trendMessage)) {
    tags.push('月/周多头');
  }
  if (item.trendMessage && /年\/月MACD>0|年MACD>0/.test(item.trendMessage)) {
    tags.push('年/月多头');
  }
  if (label && tags.indexOf(label) < 0 && label.length <= 14) {
    tags.push(label);
  }
  return tags;
}

function buildDc2Summary(item) {
  var signal = item.signalMessage || '';
  if (signal) return signal.split(',')[0];
  return parseUnilateralTrendLabel(item.trendMessage) || '';
}

function buildGc2Summary(item) {
  var signal = item.signalMessage || '';
  if (signal) return signal.split(',')[0];
  return parseUnilateralTrendLabel(item.trendMessage) || '';
}

function parseRetestModes(item) {
  var text = [item.signalMessage, item.trendMessage].join('|');
  var m = text.match(/modes=([^,|]+)/);
  if (!m) return [];
  return m[1].split(',').map(function (s) { return s.trim(); }).filter(Boolean);
}

function buildRetestTags(item) {
  var tags = [];
  var tier = parseUnilateralTier(item.trendMessage);
  var label = parseUnilateralTrendLabel(item.trendMessage);
  var modes = parseRetestModes(item);
  if (modes.indexOf('bear') >= 0) tags.push('下跌反转');
  if (modes.indexOf('bull') >= 0) tags.push('上涨中继');
  if (tier === 'S') tags.push('超短回踩');
  else if (tier === 'A') tags.push('短线回踩');
  else if (tier === 'B') tags.push('中线回踩');
  else if (tier === 'C') tags.push('长线回踩');
  if (label && tags.indexOf(label) < 0 && label.length <= 14) {
    tags.push(label);
  }
  return tags;
}

function buildRetestSummary(item) {
  var signal = item.signalMessage || '';
  if (signal) return signal.split(',')[0];
  return parseUnilateralTrendLabel(item.trendMessage) || '';
}

function buildNrfTags(item) {
  var tags = [];
  appendGlobalGateTags(item, tags);
  var text = [item.trendMessage, item.signalMessage].join('|');
  if (/柱内上移|柱内突破/.test(text)) tags.push('柱内上移');
  if (/日K跨周桶/.test(text)) tags.push('日K');
  if (/周K跨月桶/.test(text)) tags.push('周K');
  if (/月K跨年桶/.test(text)) tags.push('月K');
  if (/30m✓|30分:/.test(text)) tags.push('30m');
  if (item.signalMessage && /refDay=/.test(item.signalMessage)) {
    tags.push('柱内突破K');
  }
  return tags;
}

function buildNrfSummary(item) {
  var signal = item.signalMessage || '';
  if (signal) return signal.split(',')[0];
  return parseUnilateralTrendLabel(item.trendMessage) || '';
}

function buildMacdEdgeTags(item) {
  var tags = ['MACD边沿'];
  var text = [item.trendMessage, item.signalMessage].join('|');
  if (/signalTier=min30|refPeriod=min30|Min30基准/.test(text)) tags.push('Min30');
  if (/signalTier=day|refPeriod=day|日K基准/.test(text)) tags.push('日基准');
  if (/signalTier=week|refPeriod=week|周K基准/.test(text)) tags.push('周基准');
  if (/signalTier=month|refPeriod=month|月K基准/.test(text)) tags.push('月基准');
  if (/signalTier=year|refPeriod=year|年K基准/.test(text)) tags.push('年基准');
  if (/crossType=GC/.test(text)) tags.push('金叉K');
  if (/crossType=DC/.test(text)) tags.push('死叉K');
  appendGlobalGateTags(item, tags);
  var label = parseUnilateralTrendLabel(item.trendMessage);
  if (label && tags.indexOf(label) < 0 && label.length <= 14) {
    tags.push(label);
  }
  return tags;
}

function buildMacdEdgeSummary(item) {
  var signal = item.signalMessage || '';
  if (signal) return signal.split(',')[0];
  return parseUnilateralTrendLabel(item.trendMessage) || '';
}

function buildCascadewaveconvexTags(item) {
  var tags = ['级联凸'];
  var text = [item.trendMessage, item.signalMessage].join('|');
  if (/signalTier=day|refPeriod=day|日K级联凸/.test(text)) tags.push('日波段');
  if (/signalTier=week|refPeriod=week|周K级联凸/.test(text)) tags.push('周波段');
  if (/signalTier=month|refPeriod=month|月K级联凸/.test(text)) tags.push('月波段');
  if (/breakPath=PREV_BAND_HIGH/.test(text)) tags.push('前波段顶');
  if (/breakPath=LAST_BAND_HIGH/.test(text)) tags.push('末波段顶');
  appendGlobalGateTags(item, tags);
  var label = parseUnilateralTrendLabel(item.trendMessage);
  if (label && tags.indexOf(label) < 0 && label.length <= 14) {
    tags.push(label);
  }
  return tags;
}

function buildCascadewaveconvexSummary(item) {
  var signal = item.signalMessage || '';
  if (signal) return signal.split(',')[0];
  return parseUnilateralTrendLabel(item.trendMessage) || '';
}

function buildCascadewaveconcaveTags(item) {
  var tags = ['级联凹'];
  var text = [item.trendMessage, item.signalMessage].join('|');
  if (/signalTier=day|refPeriod=day|日K级联凹/.test(text)) tags.push('日波段');
  if (/signalTier=week|refPeriod=week|周K级联凹/.test(text)) tags.push('周波段');
  if (/signalTier=month|refPeriod=month|月K级联凹/.test(text)) tags.push('月波段');
  if (/breakPath=PREV_BAND_HIGH/.test(text)) tags.push('前波段顶');
  if (/breakPath=LAST_BAND_HIGH/.test(text)) tags.push('末波段顶');
  appendGlobalGateTags(item, tags);
  var label = parseUnilateralTrendLabel(item.trendMessage);
  if (label && tags.indexOf(label) < 0 && label.length <= 14) {
    tags.push(label);
  }
  return tags;
}

function buildCascadewaveconcaveSummary(item) {
  var signal = item.signalMessage || '';
  if (signal) return signal.split(',')[0];
  return parseUnilateralTrendLabel(item.trendMessage) || '';
}

function buildCascadewaveconvexdayTags(item) {
  var tags = ['级联凸日'];
  var text = [item.trendMessage, item.signalMessage].join('|');
  if (/signalTier=day|日K级联凸日/.test(text)) tags.push('日波段');
  if (/signalTier=week|周K级联凸日/.test(text)) tags.push('周波段');
  if (/signalTier=month|月K级联凸日/.test(text)) tags.push('月波段');
  if (/edgeMode=DAY|refPeriod=day/.test(text)) tags.push('日边沿');
  if (/breakPath=PREV_BAND_HIGH/.test(text)) tags.push('前波段顶');
  if (/breakPath=LAST_BAND_HIGH/.test(text)) tags.push('末波段顶');
  appendGlobalGateTags(item, tags);
  return tags;
}

function buildCascadewaveconvexdaySummary(item) {
  var signal = item.signalMessage || '';
  if (signal) return signal.split(',')[0];
  return parseUnilateralTrendLabel(item.trendMessage) || '';
}

function buildCascadewaveconcavedayTags(item) {
  var tags = ['级联凹日'];
  var text = [item.trendMessage, item.signalMessage].join('|');
  if (/signalTier=day|日K级联凹日/.test(text)) tags.push('日波段');
  if (/signalTier=week|周K级联凹日/.test(text)) tags.push('周波段');
  if (/signalTier=month|月K级联凹日/.test(text)) tags.push('月波段');
  if (/edgeMode=DAY|refPeriod=day/.test(text)) tags.push('日边沿');
  if (/breakPath=PREV_BAND_HIGH/.test(text)) tags.push('前波段顶');
  if (/breakPath=LAST_BAND_HIGH/.test(text)) tags.push('末波段顶');
  appendGlobalGateTags(item, tags);
  return tags;
}

function buildCascadewaveconcavedaySummary(item) {
  var signal = item.signalMessage || '';
  if (signal) return signal.split(',')[0];
  return parseUnilateralTrendLabel(item.trendMessage) || '';
}

function buildCascadeTags(item) {
  var tags = ['级联交叉'];
  var text = [item.trendMessage, item.signalMessage].join('|');
  if (/signalTier=day|refPeriod=day|日K基准/.test(text)) tags.push('日基准');
  if (/signalTier=week|refPeriod=week|周K基准/.test(text)) tags.push('周基准');
  if (/signalTier=month|refPeriod=month|月K基准/.test(text)) tags.push('月基准');
  if (/crossType=GC/.test(text)) tags.push('金叉K');
  if (/crossType=DC/.test(text)) tags.push('死叉K');
  appendGlobalGateTags(item, tags);
  var label = parseUnilateralTrendLabel(item.trendMessage);
  if (label && tags.indexOf(label) < 0 && label.length <= 14) {
    tags.push(label);
  }
  return tags;
}

function buildCascadeSummary(item) {
  var signal = item.signalMessage || '';
  if (signal) return signal.split(',')[0];
  return parseUnilateralTrendLabel(item.trendMessage) || '';
}

function mapRecommendation(item, strategyId) {
  strategyId = normalizeStrategyId(strategyId || 'magoldbreakFlash');
  var code = normalizeCode(item.code);
  var changePct = normalizeChangePct(item);
  var tags = [];
  if (item.newFlag) tags.push('今日新推');
  if (/^magoldbreak/.test(strategyId) || strategyId === 'magoldbreak') {
    tags.push('MA金叉点突破');
    if (/period=min30/.test(item.signalMessage || '')) tags.push('30分档');
    else if (/period=week/.test(item.signalMessage || '')) tags.push('周档');
    else if (/period=month/.test(item.signalMessage || '')) tags.push('月档');
    else if (/period=quarter/.test(item.signalMessage || '')) tags.push('季档');
    else tags.push('日档');
  } else if (/^madeathbreak/.test(strategyId) || strategyId === 'madeathbreak') {
    tags.push('MA死叉点突破');
    if (/period=min30/.test(item.signalMessage || '')) tags.push('30分档');
    else if (/period=week/.test(item.signalMessage || '')) tags.push('周档');
    else if (/period=month/.test(item.signalMessage || '')) tags.push('月档');
    else if (/period=quarter/.test(item.signalMessage || '')) tags.push('季档');
    else tags.push('日档');
  } else if (item.signalMessage) {
    tags.push('信号');
  } else if (item.trendMessage) {
    tags.push('趋势');
  }
  tags = tags.filter(function (t, i) { return tags.indexOf(t) === i; });

  var summaryParts = [
    parseUnilateralTrendLabel(item.trendMessage),
    item.signalMessage,
    item.mainBusiness,
    item.summary
  ].filter(function (s) { return s && String(s).trim(); });
  return {
    id: makeStockId(strategyId, 'cn', code),
    strategy: strategyId,
    code: code,
    name: item.name,
    market: 'cn',
    price: item.close != null ? item.close : item.price,
    changePct: changePct,
    tags: tags,
    summary: summaryParts.length ? summaryParts[0] : '',
    signalMessage: item.signalMessage || '',
    trendMessage: item.trendMessage || '',
    resonance: changePct > 2 ? 'strong' : changePct > 0 ? 'medium' : 'weak',
    dataDay: item.day || '',
    dataUpdatedLabel: formatDataUpdatedLabel(item.day)
  };
}

function normalizeChangePct(item) {
  if (item.changePct != null) return +Number(item.changePct).toFixed(2);
  if (item.changeRate != null) return +(item.changeRate * 100).toFixed(2);
  return 0;
}

function normalizeProfile(profile) {
  if (!profile) return null;
  return {
    businessOneLiner: profile.businessOneLiner || '',
    industryPosition: profile.industryPosition || '',
    strengths: profile.strengths || [],
    risks: profile.risks || [],
    dimensions: profile.dimensions || [],
    businessScope: profile.businessScope || '',
    briefSource: profile.briefSource || ''
  };
}

function radarToPoints(values, maxValues) {
  if (!values || !values.length) return [];
  var count = values.length;
  var points = [];
  for (var i = 0; i < count; i++) {
    var angle = (Math.PI * 2 * i / count) - Math.PI / 2;
    var max = maxValues[i] || 1;
    var ratio = max <= 0 ? 0 : values[i] / max;
    ratio = Math.min(ratio, 1.15);
    var r = ratio * 42;
    points.push({
      x: (50 + r * Math.cos(angle)).toFixed(2),
      y: (50 + r * Math.sin(angle)).toFixed(2)
    });
  }
  return points;
}

function hasRadarPointShape(points) {
  return points && points.length && points[0] && points[0].x != null && points[0].y != null;
}

/** 兼容后端 radar：补全极坐标点位 */
function normalizeRadar(radar) {
  if (!radar || !radar.dimensions) return radar;
  var company = radar.company || [];
  var industry = radar.industry || [];
  var maxRadar = radar.dimensions.map(function (_, i) {
    return Math.max(company[i] || 0, industry[i] || 0) * 1.2 + 0.01;
  });
  return Object.assign({}, radar, {
    companyPoints: hasRadarPointShape(radar.companyPoints)
      ? radar.companyPoints
      : radarToPoints(company, maxRadar),
    industryPoints: hasRadarPointShape(radar.industryPoints)
      ? radar.industryPoints
      : radarToPoints(industry, maxRadar)
  });
}

function mapCompass(raw) {
  if (!raw) return null;
  return {
    financial: raw.financial,
    operation: raw.operation,
    chain: raw.chain,
    capital: raw.capital
  };
}

function findMockIndexKlines(code, displayCode, period) {
  var cn = require('./mock').MARKET_INDICES.cn || [];
  var key = String(code || '').toLowerCase();
  var display = String(displayCode || '');
  var mock = cn.find(function (item) {
    return item.code === display || item.code === key.replace(/^sh|^sz/, '');
  });
  if (!mock || !mock.klines || !mock.klines[period]) return [];
  return mock.klines[period].slice();
}

function mapMarketIndex(item, period) {
  if (!item) return null;
  period = period || 'week';
  var bars = barsToKlines(item.klines || []);
  if (!bars.length) {
    bars = findMockIndexKlines(item.code, item.displayCode, period);
  }
  var klines = {};
  klines[period] = bars;
  var price = item.price;
  var changePct = item.changePct;
  return {
    name: item.name,
    code: item.displayCode || item.code,
    price: price != null ? Number(price).toFixed(2) : '--',
    changePct: changePct != null ? Number(Number(changePct).toFixed(2)) : 0,
    klines: klines
  };
}

function mapMarketIndices(list, period) {
  return (list || []).map(function (item) {
    return mapMarketIndex(item, period);
  }).filter(Boolean);
}

function getStrategyApiParams(strategyId) {
  return STRATEGY_API[normalizeStrategyId(strategyId)] || STRATEGY_API.magoldbreakFlash;
}

module.exports = {
  STRATEGY_API: STRATEGY_API,
  normalizeCode: normalizeCode,
  displayCode: displayCode,
  makeStockId: makeStockId,
  extractCode: extractCode,
  extractStrategy: extractStrategy,
  barsToKlines: barsToKlines,
  mapSearchItem: mapSearchItem,
  mapRecommendation: mapRecommendation,
  normalizeProfile: normalizeProfile,
  normalizeRadar: normalizeRadar,
  mapCompass: mapCompass,
  mapMarketIndex: mapMarketIndex,
  mapMarketIndices: mapMarketIndices,
  normalizeStrategyId: normalizeStrategyId,
  getStrategyApiParams: getStrategyApiParams,
  findMarketLabel: findMarketLabel
};
