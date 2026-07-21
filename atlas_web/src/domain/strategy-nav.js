export const STRATEGY_FAMILIES = [
  { id: 'ultra', name: '超短线', icon: '⚡' },
  { id: 'macdgc', name: 'MACD金叉', icon: '✦' },
  { id: 'macdgcwh', name: 'MACD金叉波段', icon: '⬆' },
  { id: 'macdgcwhr', name: 'MACD金叉回踩', icon: '↘' },
  { id: 'macdgcwhu', name: 'MACD金叉上移', icon: '⇧' },
  { id: 'waveccbreak', name: '凹凸突破', icon: '⬡' }
];

export const WAVE_TIER_TABS = [
  { id: 'short', name: '短线' },
  { id: 'medium', name: '中线' },
  { id: 'long', name: '长线' }
];

export const ULTRA_SUB_TABS = [
  { id: 'bucket', name: '跨日桶' },
  { id: 'daymin60', name: '日小时' },
  { id: 'weekmin60', name: '周小时' },
  { id: 'dayweek', name: '日周' },
  { id: 'daymonth', name: '日月' }
];

export function strategyIdFor(family, tier) {
  if (family === 'ultra') {
    if (tier === 'daymin60') return 'daymin60';
    if (tier === 'weekmin60') return 'weekmin60';
    if (tier === 'dayweek') return 'dayweek';
    if (tier === 'daymonth') return 'daymonth';
    return 'ultra';
  }
  if (family === 'macdgc') {
    if (tier === 'medium') return 'macdgcMedium';
    if (tier === 'long') return 'macdgcLong';
    return 'macdgcShort';
  }
  if (family === 'macdgcwh') {
    if (tier === 'medium') return 'macdgcwhMedium';
    if (tier === 'long') return 'macdgcwhLong';
    return 'macdgcwhShort';
  }
  if (family === 'macdgcwhr') {
    if (tier === 'medium') return 'macdgcwhrMedium';
    if (tier === 'long') return 'macdgcwhrLong';
    return 'macdgcwhrShort';
  }
  if (family === 'macdgcwhu') {
    return 'macdgcwhu';
  }
  if (family === 'waveccbreak') {
    if (tier === 'medium') return 'waveccbreakMedium';
    if (tier === 'long') return 'waveccbreakLong';
    return 'waveccbreakShort';
  }
  return 'ultra';
}

export function showTierRow(family) {
  return family !== 'ultra' && family !== 'macdgcwhu';
}

export function tierTabsFor(family) {
  return family === 'ultra' ? ULTRA_SUB_TABS : WAVE_TIER_TABS;
}
