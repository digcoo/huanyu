/**
 * Backend DTO → 小程序 UI 字段映射
 */
const { MARKETS } = require('./mock');

const STRATEGY_API = {
  trend: { strategy: 'qsn', trendPeriodTypes: 'week,month', opPeriodType: 'day' },
  rebound: { strategy: 'default', trendPeriodTypes: 'week,month', opPeriodType: 'day' },
  multi: { strategy: 'cross_band_pressure', trendPeriodTypes: 'week,month,day', opPeriodType: 'day' }
};

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
  if (!id || id.indexOf('-') < 0) return 'trend';
  return id.split('-')[0] || 'trend';
}

function findMarketLabel(marketId) {
  var meta = MARKETS.find(function (m) { return m.id === marketId; });
  return meta ? meta.icon + ' ' + meta.name : marketId;
}

function barsToKlines(bars) {
  if (!bars || !bars.length) return [];
  return bars.map(function (b) {
    return {
      open: b.open,
      high: b.high,
      low: b.low,
      close: b.close
    };
  });
}

function mapSearchItem(item, strategy) {
  strategy = strategy || 'trend';
  var code = normalizeCode(item.code);
  return {
    id: makeStockId(strategy, item.market || 'cn', code),
    strategy: strategy,
    code: code,
    name: item.name,
    market: item.market || 'cn',
    marketLabel: findMarketLabel(item.market || 'cn'),
    price: item.price != null ? item.price : item.close,
    changePct: normalizeChangePct(item)
  };
}

function mapRecommendation(item, strategyId) {
  strategyId = strategyId || 'trend';
  var code = normalizeCode(item.code);
  var changePct = normalizeChangePct(item);
  var tags = [];
  if (item.newFlag) tags.push('今日新推');
  if (item.trendMessage) tags.push('趋势');
  if (item.signalMessage) tags.push('信号');

  return {
    id: makeStockId(strategyId, 'cn', code),
    strategy: strategyId,
    code: code,
    name: item.name,
    market: 'cn',
    price: item.close != null ? item.close : item.price,
    changePct: changePct,
    tags: tags,
    summary: item.summary || item.trendMessage || item.mainBusiness || item.signalMessage || '',
    resonance: changePct > 2 ? 'strong' : changePct > 0 ? 'medium' : 'weak',
    mainBusiness: item.mainBusiness
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
    dimensions: profile.dimensions || []
  };
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

function getStrategyApiParams(strategyId) {
  return STRATEGY_API[strategyId] || STRATEGY_API.trend;
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
  mapCompass: mapCompass,
  getStrategyApiParams: getStrategyApiParams,
  findMarketLabel: findMarketLabel
};
