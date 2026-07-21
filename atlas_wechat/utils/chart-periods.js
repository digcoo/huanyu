var ALL_CHART_PERIODS = ['min60', 'min30', 'day', 'week', 'month', 'year'];

function isChartPeriod(period) {
  return ALL_CHART_PERIODS.indexOf(period) >= 0;
}

function normalizeChartPeriod(period, fallback) {
  return isChartPeriod(period) ? period : (fallback || 'week');
}

module.exports = {
  ALL_CHART_PERIODS: ALL_CHART_PERIODS,
  isChartPeriod: isChartPeriod,
  normalizeChartPeriod: normalizeChartPeriod
};
