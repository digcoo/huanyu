<template>
  <div>
    <StrategyNav
      :family="family"
      :tier="tier"
      :show-tier-row="true"
      :families="STRATEGY_FAMILIES"
      :tier-tabs="tierTabs"
      @family-change="onFamilyChange"
      @tier-change="onTierChange"
    />

    <div class="toolbar">
      <label>
        周期
        <select v-model="period" @change="reload">
          <option value="min30">30分</option>
          <option value="min60">60分</option>
          <option value="day">日</option>
          <option value="week">周</option>
          <option value="month">月</option>
        </select>
      </label>
      <span class="muted">共 {{ total }} 条</span>
      <button :disabled="loading" @click="reload">刷新</button>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="loading && !items.length" class="muted">加载中…</p>

    <div class="list">
      <article v-for="item in items" :key="item.id" class="card item" @click="goDetail(item)">
        <div class="row">
          <strong>{{ item.name }}</strong>
          <span class="code muted">{{ item.code }}</span>
          <span :class="item.changePct >= 0 ? 'up' : 'down'">
            {{ item.changePct >= 0 ? '+' : '' }}{{ item.changePct }}%
          </span>
        </div>
        <div class="tags">
          <span v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</span>
        </div>
        <p class="summary muted">{{ item.summary }}</p>
      </article>
    </div>

    <button v-if="hasMore" class="load-more" :disabled="loadingMore" @click="loadMore">
      {{ loadingMore ? '加载中…' : '加载更多' }}
    </button>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import StrategyNav from '@/components/StrategyNav.vue';
import { fetchRecommendations } from '@/api/stock';
import { useAppStore } from '@/stores/app';
import {
  STRATEGY_FAMILIES,
  strategyIdFor,
  tierTabsFor
} from '@/domain/strategy-nav';

const app = useAppStore();
const router = useRouter();

const family = ref('ultra');
const tier = ref('bucket');
const period = ref(app.activePeriod);
const items = ref([]);
const page = ref(1);
const total = ref(0);
const hasMore = ref(false);
const loading = ref(false);
const loadingMore = ref(false);
const error = ref('');

const strategyId = computed(() => strategyIdFor(family.value, tier.value));
const tierTabs = computed(() => tierTabsFor(family.value));

function onFamilyChange(id) {
  family.value = id;
  tier.value = id === 'ultra' ? 'bucket' : 'short';
  reload();
}

function onTierChange(id) {
  tier.value = id;
  reload();
}

async function loadPage(nextPage, append) {
  if (append) loadingMore.value = true;
  else loading.value = true;
  error.value = '';
  try {
    const res = await fetchRecommendations(strategyId.value, nextPage, 20);
    if (append) {
      items.value = items.value.concat(res.items);
    } else {
      items.value = res.items;
    }
    page.value = res.page || nextPage;
    total.value = res.totalNum;
    hasMore.value = res.hasMore;
    app.setStrategy(strategyId.value);
    app.setPeriod(period.value);
  } catch (e) {
    error.value = e.message || '加载失败';
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
}

function reload() {
  page.value = 1;
  loadPage(1, false);
}

function loadMore() {
  loadPage(page.value + 1, true);
}

function goDetail(item) {
  router.push({ name: 'stock', params: { code: item.code }, query: { strategy: item.strategy } });
}

watch(strategyId, reload, { immediate: true });
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
select {
  margin-left: 8px;
  background: var(--bg-elevated);
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 4px 8px;
}
.list {
  display: grid;
  gap: 12px;
}
.item {
  cursor: pointer;
  transition: border-color 0.15s;
}
.item:hover {
  border-color: var(--accent);
}
.row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.code {
  font-size: 13px;
}
.tags {
  display: flex;
  gap: 6px;
  margin: 8px 0;
}
.tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(56, 189, 248, 0.12);
  color: var(--accent);
}
.summary {
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.load-more {
  display: block;
  width: 100%;
  margin-top: 16px;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--bg-elevated);
  color: var(--text);
}
.error {
  color: #f87171;
}
</style>
