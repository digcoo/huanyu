const config = require('./config');
const detailMock = require('./detail-mock');
const stockApi = require('./stock-api');
const adapter = require('./adapter');

function loadDetail(id, period) {
  period = period || 'week';

  if (config.useMock) {
    return Promise.resolve(detailMock.getDetailById(id));
  }

  var code = adapter.extractCode(id);
  var strategy = adapter.extractStrategy(id);

  return Promise.all([
    stockApi.fetchDetail(code),
    stockApi.fetchKlines(code, period, 50),
    stockApi.fetchCompass(code)
  ]).then(function (results) {
    var detailRes = results[0];
    var bars = results[1];
    var compassRes = results[2];

    if (!detailRes.ok || !detailRes.data) {
      if (config.fallbackOnError) return detailMock.getDetailById(id);
      return null;
    }

    var d = detailRes.data;
    var klines = adapter.barsToKlines(bars);
    var profile = adapter.normalizeProfile(d.profile);
    var base = {
      id: id || adapter.makeStockId(strategy, 'cn', d.code),
      code: adapter.normalizeCode(d.code),
      name: d.name,
      market: d.market || 'cn',
      price: d.price,
      changePct: d.changePct,
      strategy: strategy,
      summary: (profile && profile.businessOneLiner) || d.businessBrief || d.mainBusiness || '',
      tags: [],
      showStrategy: false,
      resonance: null,
      klines: {}
    };
    base.klines[period] = klines;

    var compass = compassRes.ok && compassRes.data
      ? adapter.mapCompass(compassRes.data)
      : null;

    return detailMock.buildDetailFromStock(base, {
      industry: d.industry,
      profile: profile,
      compass: compass,
      keyMetrics: d.keyMetrics,
      stage: d.stage,
      stageHint: d.stageHint,
      healthScore: d.healthScore,
      healthRank: d.healthRank,
      healthBreakdown: d.healthBreakdown,
      radar: adapter.normalizeRadar(d.radar),
      portraitDimensions: d.portraitDimensions,
      competitors: d.competitors,
      industryChain: d.industryChain || { upstream: [], downstream: [], segments: [] }
    });
  }).catch(function () {
    if (config.fallbackOnError) return detailMock.getDetailById(id);
    return null;
  });
}

function loadKlinesForPeriod(id, period) {
  if (config.useMock) {
    var detail = detailMock.getDetailById(id);
    if (!detail || !detail.klines) return Promise.resolve([]);
    return Promise.resolve(detail.klines[period] ? detail.klines[period].slice() : []);
  }
  var code = adapter.extractCode(id);
  return stockApi.fetchKlines(code, period, 50).then(adapter.barsToKlines);
}

module.exports = {
  loadDetail: loadDetail,
  loadKlinesForPeriod: loadKlinesForPeriod
};
