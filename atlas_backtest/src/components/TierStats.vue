<script setup>
defineProps({
  tiers: { type: Array, default: () => [] }
});
</script>

<template>
  <div v-if="tiers && tiers.length" class="tier-card">
    <h3>档位统计</h3>
    <table>
      <thead>
        <tr>
          <th>档位</th>
          <th>信号数</th>
          <th>胜率</th>
          <th>均收益</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in tiers" :key="r.tier">
          <td>{{ r.tier }}</td>
          <td class="mono">{{ r.count }}</td>
          <td class="mono">{{ r.winRate }}%</td>
          <td class="mono" :class="r.avgPnl >= 0 ? 'text-green' : 'text-red'">
            {{ r.avgPnl > 0 ? '+' : '' }}{{ r.avgPnl }}%
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.tier-card {
  background: var(--bg-panel);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 16px;
}
.tier-card h3 {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 12px;
}
table { width: 100%; border-collapse: collapse; font-size: 13px; }
th, td { padding: 8px 12px; text-align: left; border-bottom: 1px solid var(--border); }
th { color: var(--text-dim); font-weight: 500; }
</style>
