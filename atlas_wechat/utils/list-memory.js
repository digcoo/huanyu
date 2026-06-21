/** 列表页内存控制：K 线条数、持有上限 */

var MAX_HELD_RECOMMENDATIONS = 120;
var MAX_SEEN_RECOMMENDATION_IDS = 2000;
var MAX_IGNORED_IDS = 500;

var LIST_KLINE_LIMIT = {
  min30: 32,
  day: 24,
  week: 24,
  month: 24,
  year: 24
};

function klineLimitForList(period) {
  return LIST_KLINE_LIMIT[period] || 24;
}

function cardMaxBars(period) {
  return klineLimitForList(period);
}

/** 只保留当前周期 K 线，避免切换周期后多周期堆积 */
function slimItemKlines(item, period) {
  if (!item) return item;
  var bars = item.klines && item.klines[period] ? item.klines[period] : (item.chartKlines || []);
  var klines = {};
  if (bars && bars.length) {
    klines[period] = bars;
  }
  return Object.assign({}, item, {
    klines: klines,
    chartKlines: bars
  });
}

function slimListKlines(list, period) {
  return (list || []).map(function (item) {
    return slimItemKlines(item, period);
  });
}

/** 列表过长时保留最新一段（加载更多在尾部追加） */
function trimHeldList(list, max) {
  max = max != null ? max : MAX_HELD_RECOMMENDATIONS;
  if (!list || list.length <= max) return list || [];
  return list.slice(list.length - max);
}

function capIgnoredIds(ids, max) {
  max = max != null ? max : MAX_IGNORED_IDS;
  var list = ids || [];
  if (list.length <= max) return list;
  return list.slice(list.length - max);
}

/** 分页去重：记录已加载过的推荐 id（轻量，可大于列表持有上限） */
function rememberSeenIds(seen, items) {
  var map = seen || {};
  (items || []).forEach(function (item) {
    if (item && item.id) map[item.id] = true;
  });
  var keys = Object.keys(map);
  if (keys.length <= MAX_SEEN_RECOMMENDATION_IDS) {
    return map;
  }
  var trimmed = {};
  keys.slice(keys.length - MAX_SEEN_RECOMMENDATION_IDS).forEach(function (id) {
    trimmed[id] = true;
  });
  return trimmed;
}

function initSeenIds(items) {
  return rememberSeenIds({}, items);
}

module.exports = {
  MAX_HELD_RECOMMENDATIONS: MAX_HELD_RECOMMENDATIONS,
  MAX_SEEN_RECOMMENDATION_IDS: MAX_SEEN_RECOMMENDATION_IDS,
  MAX_IGNORED_IDS: MAX_IGNORED_IDS,
  klineLimitForList: klineLimitForList,
  cardMaxBars: cardMaxBars,
  slimItemKlines: slimItemKlines,
  slimListKlines: slimListKlines,
  trimHeldList: trimHeldList,
  capIgnoredIds: capIgnoredIds,
  rememberSeenIds: rememberSeenIds,
  initSeenIds: initSeenIds
};
