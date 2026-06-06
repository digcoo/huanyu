const { MARKETS } = require('./mock');
const config = require('./config');

function isMarketEnabled(marketId) {
  return config.enabledMarkets.indexOf(marketId) >= 0;
}

function buildMarketsForUI() {
  return MARKETS.map(function (m) {
    return Object.assign({}, m, {
      enabled: isMarketEnabled(m.id)
    });
  });
}

module.exports = {
  isMarketEnabled: isMarketEnabled,
  buildMarketsForUI: buildMarketsForUI
};
