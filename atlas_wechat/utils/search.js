const { buildStrategyRecommendations, MARKETS } = require('./mock');
const config = require('./config');
const stockApi = require('./stock-api');
const adapter = require('./adapter');

function findMarketLabel(marketId) {
  const meta = MARKETS.find(function (m) { return m.id === marketId; });
  return meta ? meta.icon + ' ' + meta.name : marketId;
}

function buildCatalog() {
  if (buildCatalog._cache) return buildCatalog._cache;

  const all = buildStrategyRecommendations();
  const seen = {};
  const list = [];

  Object.keys(all).forEach(function (strategyId) {
    const byMarket = all[strategyId];
    Object.keys(byMarket).forEach(function (market) {
      byMarket[market].forEach(function (item) {
        const key = item.market + '-' + item.code;
        if (seen[key]) return;
        seen[key] = true;
        list.push({
          id: item.id,
          code: item.code,
          name: item.name,
          market: item.market,
          marketLabel: findMarketLabel(item.market),
          price: item.price,
          changePct: item.changePct
        });
      });
    });
  });

  buildCatalog._cache = list;
  return list;
}

function findStockById(id) {
  if (!id) return null;
  const all = buildStrategyRecommendations();
  for (const strategyId in all) {
    const byMarket = all[strategyId];
    for (const market in byMarket) {
      const found = byMarket[market].find(function (item) { return item.id === id; });
      if (found) return found;
    }
  }
  return null;
}

function searchStocks(keyword, limit) {
  const q = (keyword || '').trim().toLowerCase();
  if (!q) return [];

  const codeQ = q.replace(/\s/g, '');
  const catalog = buildCatalog();

  return catalog.filter(function (item) {
    const code = String(item.code).toLowerCase();
    const name = String(item.name).toLowerCase();
    return code.indexOf(codeQ) >= 0 || name.indexOf(q) >= 0;
  }).slice(0, limit || 20);
}

function searchStocksAsync(keyword, limit) {
  if (config.useMock) {
    return Promise.resolve(searchStocks(keyword, limit));
  }
  return stockApi.search(keyword, limit).catch(function () {
    if (config.fallbackOnError) return searchStocks(keyword, limit);
    return [];
  });
}

function findStockByIdAsync(id) {
  if (!id) return Promise.resolve(null);
  if (config.useMock) {
    return Promise.resolve(findStockById(id));
  }

  var code = adapter.extractCode(id);
  var strategy = adapter.extractStrategy(id);
  return stockApi.fetchSummary(code).then(function (item) {
    if (!item) {
      return config.fallbackOnError ? findStockById(id) : null;
    }
    return Object.assign({}, item, { id: id, strategy: strategy });
  }).catch(function () {
    return config.fallbackOnError ? findStockById(id) : null;
  });
}

module.exports = {
  buildCatalog: buildCatalog,
  findStockById: findStockById,
  findStockByIdAsync: findStockByIdAsync,
  searchStocks: searchStocks,
  searchStocksAsync: searchStocksAsync
};
