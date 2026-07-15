/**
 * 列表页视图上下文：策略 + 周期 + 市场的单一真相源。
 * 所有加载/切周期/切策略必须先 commit，异步回调用 generation + context 校验，禁止依赖 setData 后的 this.data。
 */

var chartPeriods = require('./chart-periods');
var strategyParams = require('./strategy-params');
var strategyNav = require('./strategy-nav');

function allowedPeriods() {
  return chartPeriods.ALL_CHART_PERIODS;
}

function normalizePeriod(strategyId, period) {
  return chartPeriods.normalizeChartPeriod(
    period,
    strategyParams.defaultChartPeriod(strategyId)
  );
}

/** 切换策略/档位：使用该策略的默认图表周期 */
function periodForStrategySwitch(strategyId) {
  return normalizePeriod(strategyId, strategyParams.defaultChartPeriod(strategyId));
}

function buildNavState(strategyId) {
  var parsed = strategyNav.parseStrategyId(strategyId);
  return {
    activeStrategy: parsed.strategyId,
    activeStrategyFamily: parsed.family,
    activeCascadeTier: parsed.tier,
    showCascadeTierRow: strategyNav.showTierRow(parsed.family),
    strategyTitle: strategyNav.strategyTitleFor(parsed.strategyId),
    allowedPeriods: allowedPeriods()
  };
}

/**
 * @typedef {{ strategyId: string, period: string, marketId: string }} ViewContext
 */

function createContext(strategyId, period, marketId) {
  var sid = strategyId || 'ultra';
  return {
    strategyId: sid,
    period: normalizePeriod(sid, period),
    marketId: marketId || 'cn'
  };
}

function readContext(page, overrides) {
  overrides = overrides || {};
  if (page._viewCtx && !overrides.strategyId && overrides.period == null && !overrides.marketId) {
    return page._viewCtx;
  }
  var base = page._viewCtx || createContext(
    page.data.activeStrategy,
    page.data.activePeriod,
    page.data.activeMarket
  );
  return createContext(
    overrides.strategyId != null ? overrides.strategyId : base.strategyId,
    overrides.period != null ? overrides.period : base.period,
    overrides.marketId != null ? overrides.marketId : base.marketId
  );
}

/** 同步写入 _viewCtx、storage，并 setData（周期/市场/导航） */
function commitContext(page, ctx, extraSetData) {
  page._viewCtx = ctx;
  wx.setStorageSync('activeStrategy', ctx.strategyId);
  wx.setStorageSync('activePeriod', ctx.period);
  var patch = Object.assign({
    activePeriod: ctx.period,
    activeMarket: ctx.marketId
  }, buildNavState(ctx.strategyId), extraSetData || {});
  page.setData(patch);
  return ctx;
}

function initSession(page) {
  page._viewCtx = null;
  page._loadGen = 0;
  page._klineGen = 0;
}

function bumpLoadGen(page) {
  page._loadGen = (page._loadGen || 0) + 1;
  return page._loadGen;
}

function bumpKlineGen(page) {
  page._klineGen = (page._klineGen || 0) + 1;
  return page._klineGen;
}

function isLoadStale(page, gen) {
  return gen !== page._loadGen;
}

function isKlineStale(page, gen, ctx) {
  if (gen !== page._klineGen) return true;
  var cur = page._viewCtx;
  if (!cur || !ctx) return false;
  return cur.strategyId !== ctx.strategyId || cur.period !== ctx.period;
}

function contextLabel(ctx) {
  return ctx.strategyId + '@' + ctx.period;
}

module.exports = {
  allowedPeriods: allowedPeriods,
  normalizePeriod: normalizePeriod,
  periodForStrategySwitch: periodForStrategySwitch,
  buildNavState: buildNavState,
  createContext: createContext,
  readContext: readContext,
  commitContext: commitContext,
  initSession: initSession,
  bumpLoadGen: bumpLoadGen,
  bumpKlineGen: bumpKlineGen,
  isLoadStale: isLoadStale,
  isKlineStale: isKlineStale,
  contextLabel: contextLabel
};
