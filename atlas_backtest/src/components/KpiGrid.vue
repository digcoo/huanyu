<script setup>
import { computed } from 'vue';

const props = defineProps({
  summary: { type: Object, default: null },
  analytics: { type: Object, default: null }
});

const pfText = computed(() => {
  const pf = props.analytics?.profitFactor;
  if (pf === Infinity) return '∞';
  return pf != null ? String(pf) : '—';
});

const winClass = computed(() => {
  const r = props.summary?.winRate || 0;
  return r >= 0.5 ? 'positive' : 'negative';
});
</script>

<template>
  <div v-if="summary" class="kpi-grid">
    <div class="kpi" :class="winClass">
      <span class="kpi-label">胜率</span>
      <span class="kpi-value">{{ summary.winRateText || '0%' }}</span>
      <span class="kpi-sub">{{ summary.winCount }} 胜 / {{ summary.lossCount }} 负</span>
    </div>
    <div class="kpi">
      <span class="kpi-label">信号数</span>
      <span class="kpi-value mono">{{ summary.signalCount }}</span>
      <span class="kpi-sub">扫描 {{ summary.stockCount }} 只股票</span>
    </div>
    <div class="kpi">
      <span class="kpi-label">平均收益</span>
      <span class="kpi-value mono" :class="(summary.avgPnlPct || 0) >= 0 ? 'text-green' : 'text-red'">
        {{ summary.avgPnlText || '0%' }}
      </span>
      <span class="kpi-sub">持有 {{ summary.holdDays }} 日</span>
    </div>
    <div class="kpi">
      <span class="kpi-label">期望收益</span>
      <span class="kpi-value mono">{{ analytics?.expectancy ?? '—' }}%</span>
      <span class="kpi-sub">每笔信号均值</span>
    </div>
    <div class="kpi">
      <span class="kpi-label">盈亏比</span>
      <span class="kpi-value mono">{{ pfText }}</span>
      <span class="kpi-sub">Profit Factor</span>
    </div>
    <div class="kpi">
      <span class="kpi-label">最大回撤</span>
      <span class="kpi-value mono text-red">{{ analytics?.maxDrawdown ?? '—' }}%</span>
      <span class="kpi-sub">累计收益曲线</span>
    </div>
    <div class="kpi">
      <span class="kpi-label">最大盈利</span>
      <span class="kpi-value mono text-green">+{{ summary.maxWinPct }}%</span>
      <span class="kpi-sub">单笔极值</span>
    </div>
    <div class="kpi">
      <span class="kpi-label">最大亏损</span>
      <span class="kpi-value mono text-red">{{ summary.maxLossPct }}%</span>
      <span class="kpi-sub">{{ ((summary.elapsedMs || 0) / 1000).toFixed(1) }}s 耗时</span>
    </div>
  </div>
</template>

<style scoped>
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
@media (max-width: 1200px) { .kpi-grid { grid-template-columns: repeat(2, 1fr); } }

.kpi {
  background: var(--bg-panel);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 16px;
}
.kpi.positive .kpi-value { color: var(--green); }
.kpi.negative .kpi-value { color: var(--red); }
.kpi-label {
  display: block;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-dim);
  margin-bottom: 6px;
}
.kpi-value { display: block; font-size: 22px; font-weight: 600; }
.kpi-sub { display: block; font-size: 12px; color: var(--text-muted); margin-top: 4px; }
</style>
