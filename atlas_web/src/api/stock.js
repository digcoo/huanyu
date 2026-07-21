import client from './client';

const STRATEGY_API = {
  ultra: { strategy: 'ultra', trendPeriodTypes: 'week,day,min30', opPeriodType: 'min30' },
  macdgcShort: { strategy: 'macdgc', trendPeriodTypes: 'month,week,day', opPeriodType: 'day' },
  macdgcMedium: { strategy: 'macdgc', trendPeriodTypes: 'year,month,week', opPeriodType: 'week' },
  macdgcLong: { strategy: 'macdgc', trendPeriodTypes: 'year,month', opPeriodType: 'month' },
  macdgcwhShort: { strategy: 'macdgcwh', trendPeriodTypes: 'month,week,day', opPeriodType: 'day' },
  macdgcwhMedium: { strategy: 'macdgcwh', trendPeriodTypes: 'year,month,week', opPeriodType: 'week' },
  macdgcwhLong: { strategy: 'macdgcwh', trendPeriodTypes: 'year,month', opPeriodType: 'month' },
  macdgcwhrShort: { strategy: 'macdgcwhr', trendPeriodTypes: 'month,week,day', opPeriodType: 'day' },
  macdgcwhrMedium: { strategy: 'macdgcwhr', trendPeriodTypes: 'year,month,week', opPeriodType: 'week' },
  macdgcwhrLong: { strategy: 'macdgcwhr', trendPeriodTypes: 'year,month', opPeriodType: 'month' },
  macdgcwhu: { strategy: 'macdgcwhu', trendPeriodTypes: 'month,week,day', opPeriodType: 'day' },
  waveccbreakShort: { strategy: 'waveccbreak', trendPeriodTypes: 'month,week,day', opPeriodType: 'day' },
  waveccbreakMedium: { strategy: 'waveccbreak', trendPeriodTypes: 'year,month,week', opPeriodType: 'week' },
  waveccbreakLong: { strategy: 'waveccbreak', trendPeriodTypes: 'year,month', opPeriodType: 'month' },
  daymin60: { strategy: 'daymin60', trendPeriodTypes: 'week,day,min60', opPeriodType: 'min60' },
  weekmin60: { strategy: 'weekmin60', trendPeriodTypes: 'month,week,min60', opPeriodType: 'min60' },
  dayweek: { strategy: 'dayweek', trendPeriodTypes: 'month,week,day', opPeriodType: 'day' },
  daymonth: { strategy: 'daymonth', trendPeriodTypes: 'year,month,day', opPeriodType: 'month' }
};

function normalizeCode(code) {
  if (!code) return '';
  const c = String(code).trim().toLowerCase();
  if (c.startsWith('sh') || c.startsWith('sz')) return c;
  if (/^\d{6}$/.test(c)) return c.startsWith('6') ? `sh${c}` : `sz${c}`;
  return c;
}

function mapRecommendation(item, strategyId) {
  const code = normalizeCode(item.code);
  const changePct = item.changeRate != null
    ? +(item.changeRate * 100).toFixed(2)
    : 0;
  const tags = [];
  if (item.newFlag) tags.push('今日新推');
  return {
    id: `${strategyId}-cn-${code}`,
    strategy: strategyId,
    code,
    name: item.name,
    price: item.close,
    changePct,
    tags,
    summary: item.trendMessage || item.signalMessage || '',
    signalMessage: item.signalMessage || '',
    trendMessage: item.trendMessage || '',
    day: item.day || ''
  };
}

export function fetchRecommendations(strategyId, page = 1, size = 20) {
  const api = STRATEGY_API[strategyId] || STRATEGY_API.ultra;
  return client.get('/stock/findMy', {
    params: {
      ...api,
      all: 1,
      page,
      size
    },
    timeout: 120000
  }).then((data) => {
    const rawItems = data.items || data.list || [];
    const items = rawItems.map((item) => mapRecommendation(item, strategyId));
    return {
      items,
      page: data.currentPage || page,
      totalNum: data.totalNum != null ? data.totalNum : items.length,
      hasMore: data.isMore === 1 || data.isMore === true
        || (data.totalNum != null && page * size < data.totalNum)
    };
  });
}

export function fetchWatchlist() {
  return client.get('/watchlist');
}

export function fetchHistory(filter = 'all') {
  return client.get('/history', { params: { filter } });
}

export function fetchKlines(code, period = 'week', limit = 80) {
  return client.get(`/stock/${encodeURIComponent(code)}/klines`, {
    params: { period, limit }
  });
}

export function fetchStockDetail(code) {
  return client.get(`/stock/${encodeURIComponent(code)}/detail`);
}

export { STRATEGY_API };
