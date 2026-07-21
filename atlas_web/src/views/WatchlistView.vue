<template>
  <div>
    <h2>自选池</h2>
    <p v-if="loading" class="muted">加载中…</p>
    <p v-else-if="!items.length" class="muted">暂无自选</p>
    <div class="list">
      <article v-for="item in items" :key="item.id" class="card item">
        <strong>{{ item.name }}</strong>
        <span class="muted">{{ item.code }}</span>
      </article>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { fetchWatchlist } from '@/api/stock';

const items = ref([]);
const loading = ref(true);

onMounted(async () => {
  try {
    items.value = await fetchWatchlist() || [];
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
}
</style>
