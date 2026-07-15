<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  trades: { type: Array, default: () => [] }
});

const filter = ref('');
const sortKey = ref('signalDay');
const sortAsc = ref(false);

const filtered = computed(() => {
  let list = [...props.trades];
  const q = filter.value.trim().toLowerCase();
  if (q) {
    list = list.filter((t) =>
      (t.code || '').toLowerCase().includes(q)
      || (t.name || '').toLowerCase().includes(q)
    );
  }
  list.sort((a, b) => {
    const av = a[sortKey.value];
    const bv = b[sortKey.value];
    if (typeof av === 'number' && typeof bv === 'number') {
      return sortAsc.value ? av - bv : bv - av;
    }
    return sortAsc.value
      ? String(av).localeCompare(String(bv))
      : String(bv).localeCompare(String(av));
  });
  return list;
});

function toggleSort(key) {
  if (sortKey.value === key) sortAsc.value = !sortAsc.value;
  else { sortKey.value = key; sortAsc.value = false; }
}

function sortIcon(key) {
  if (sortKey.value !== key) return '';
  return sortAsc.value ? ' ↑' : ' ↓';
}
</script>

<template>
  <div class="table-card">
    <div class="table-toolbar">
      <input v-model="filter" class="search" placeholder="搜索代码 / 名称…" />
      <span class="count">共 {{ filtered.length }} 笔</span>
    </div>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th @click="toggleSort('code')">代码{{ sortIcon('code') }}</th>
            <th @click="toggleSort('name')">名称{{ sortIcon('name') }}</th>
            <th @click="toggleSort('signalDay')">信号日{{ sortIcon('signalDay') }}</th>
            <th @click="toggleSort('exitDay')">平仓日{{ sortIcon('exitDay') }}</th>
            <th>买入</th>
            <th>卖出</th>
            <th @click="toggleSort('tier')">档位{{ sortIcon('tier') }}</th>
            <th @click="toggleSort('pnlPct')">收益率{{ sortIcon('pnlPct') }}</th>
            <th>结果</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in filtered" :key="t.code + t.signalDay">
            <td class="mono">{{ t.code }}</td>
            <td>{{ t.name || '—' }}</td>
            <td class="mono">{{ t.signalDay }}</td>
            <td class="mono">{{ t.exitDay }}</td>
            <td class="mono">{{ t.entryPrice }}</td>
            <td class="mono">{{ t.exitPrice }}</td>
            <td>{{ t.tier || '—' }}</td>
            <td class="mono" :class="t.win ? 'text-green' : 'text-red'">
              {{ t.pnlPct > 0 ? '+' : '' }}{{ t.pnlPct }}%
            </td>
            <td>
              <span :class="['badge', t.win ? 'badge-win' : 'badge-loss']">
                {{ t.win ? '盈' : '亏' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!filtered.length" class="empty">无匹配交易</p>
    </div>
  </div>
</template>

<style scoped>
.table-card {
  background: var(--bg-panel);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}
.table-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
}
.search {
  flex: 1;
  max-width: 280px;
  padding: 8px 12px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text);
}
.count { font-size: 12px; color: var(--text-muted); margin-left: auto; }
.table-wrap { overflow-x: auto; max-height: 520px; overflow-y: auto; }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
th {
  position: sticky;
  top: 0;
  background: var(--bg-elevated);
  padding: 10px 12px;
  text-align: left;
  font-weight: 500;
  color: var(--text-muted);
  cursor: pointer;
  white-space: nowrap;
  border-bottom: 1px solid var(--border);
}
th:hover { color: var(--text); }
td { padding: 9px 12px; border-bottom: 1px solid var(--border); }
tr:hover td { background: var(--bg-hover); }
.empty { padding: 40px; text-align: center; color: var(--text-dim); }
</style>
