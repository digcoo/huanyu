/**
 * Backend DTO → 小程序 UI 字段映射
 */
const { MARKETS } = require('./mock');
const { formatDataUpdatedLabel } = require('./time');

const STRATEGY_API = {
  trend: { strategy: 'qsn', trendPeriodTypes: 'week,month', opPeriodType: 'day' },
  rebound: { strategy: 'default', trendPeriodTypes: 'week,month', opPeriodType: 'day' },
  multi: { strategy: 'multi', trendPeriodTypes: 'day,week,month', opPeriodType: 'day' }
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

function buildUnilateralTags(item) {
  var tags = [];
  var tier = parseUnilateralTier(item.trendMessage);
  var label = parseUnilateralTrendLabel(item.trendMessage);
  if (tier === 'S') tags.push('共振');
  else if (tier === 'A') tags.push('均线多头');
  else if (tier === 'B') tags.push('待确认');
  else if (tier === 'C') tags.push('月K拐点');
  if (item.signalMessage && item.signalMessage.indexOf('月K阳反阴') >= 0) {
    tags.push('月K信号');
  }
  if (item.signalMessage && (item.signalMessage.indexOf('梯子试盘') >= 0 || item.signalMessage.indexOf('均线再起') >= 0)) {
    tags.push('梯子试盘');
  }
  if (item.signalMessage && item.signalMessage.indexOf('平台突破') >= 0) {
    tags.push('突破延续');
  }
  if (label && tags.indexOf(label) < 0 && label.length <= 12) {
    tags.push(label);
  }
  return tags;
}

function buildMultiTags(item) {
  var tags = [];
  var tier = parseUnilateralTier(item.trendMessage);
  var label = parseUnilateralTrendLabel(item.trendMessage);
  if (tier === 'S') tags.push('多周期共振');
  else if (tier === 'A') tags.push('波段突破');
  else if (tier === 'B') tags.push('待突破');
  else if (tier === 'C') tags.push('单周期');
  if (item.signalMessage && item.signalMessage.indexOf('梯子突破') >= 0) {
    tags.push('梯子突破');
  }
  if (item.signalMessage && item.signalMessage.indexOf('趋势波段') >= 0) {
    tags.push('趋势波段');
  }
  if (item.signalMessage && item.signalMessage.indexOf('反转波段') >= 0) {
    tags.push('反转波段');
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
  if (tier === 'S') tags.push('共振反弹');
  else if (tier === 'A') tags.push('深坑脱离');
  else if (tier === 'B') tags.push('底上移');
  else if (tier === 'C') tags.push('底背离');
  if (item.signalMessage && item.signalMessage.indexOf('波段脱离') >= 0) {
    tags.push('脱离确认');
  }
  if (item.signalMessage && item.signalMessage.indexOf('底背离') >= 0) {
    tags.push('MACD背离');
  }
  if (item.signalMessage && item.signalMessage.indexOf('low上移') >= 0) {
    tags.push('结构修复');
  }
  if (item.signalMessage && item.signalMessage.indexOf('恐慌') >= 0) {
    tags.push('恐慌释放');
  }
  if (label && label.indexOf('坑底') >= 0 && tags.indexOf('坑底反弹') < 0) {
    tags.push('坑底反弹');
  }
  if (label && tags.indexOf(label) < 0 && label.length <= 12) {
    tags.push(label);
  }
  return tags;
}

function mapRecommendation(item, strategyId) {
  strategyId = strategyId || 'trend';
  var code = normalizeCode(item.code);
  var changePct = normalizeChangePct(item);
  var tags = [];
  if (item.newFlag) tags.push('今日新推');
  if (strategyId === 'trend') {
    tags = tags.concat(buildUnilateralTags(item));
  } else if (strategyId === 'rebound') {
    tags = tags.concat(buildReboundTags(item));
  } else if (strategyId === 'multi') {
    tags = tags.concat(buildMultiTags(item));
  } else {
    if (item.signalMessage) tags.push('信号');
    else if (item.trendMessage) tags.push('趋势');
  }
  tags = tags.filter(function (t, i) { return tags.indexOf(t) === i; });

  var summaryParts = [parseUnilateralTrendLabel(item.trendMessage), item.signalMessage, item.mainBusiness, item.summary]
    .filter(function (s) { return s && String(s).trim(); });
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
    resonance: strategyId === 'multi'
      ? (parseUnilateralTier(item.trendMessage) === 'S' ? 'strong'
        : parseUnilateralTier(item.trendMessage) === 'A' ? 'medium' : 'weak')
      : (changePct > 2 ? 'strong' : changePct > 0 ? 'medium' : 'weak'),
    mainBusiness: item.mainBusiness,
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
  mapMarketIndex: mapMarketIndex,
  mapMarketIndices: mapMarketIndices,
  getStrategyApiParams: getStrategyApiParams,
  findMarketLabel: findMarketLabel
};
