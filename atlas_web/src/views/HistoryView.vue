<template>
  <div>
    <h2>历史复盘</h2>
    <p v-if="loading" class="muted">加载中…</p>
    <p v-else-if="!items.length" class="muted">暂无历史记录</p>
    <div class="list">
      <article v-for="item in items" :key="item.recordId || item.id" class="card item">
        <div>
          <strong>{{ item.name }}</strong>
          <span class="muted"> {{ item.code }}</span>
        </div>
        <span :class="item.pnlPct >= 0 ? 'up' : 'down'">
          {{ item.pnlPct >= 0 ? '+' : '' }}{{ item.pnlPct }}%
        </span>
      </article>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { fetchHistory } from '@/api/stock';

const items = ref([]);
const loading = ref(true);

onMounted(async () => {
  try {
    const res = await fetchHistory('all');
    items.value = (res && res.items) || res || [];
  } catch (e) {
    console.warn(e);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}
.item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
