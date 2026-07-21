<template>
  <div class="layout">
    <header class="header">
      <div class="brand">Atlas · 寰宇投研</div>
      <nav class="nav">
        <router-link to="/">策略</router-link>
        <router-link to="/watchlist">自选</router-link>
        <router-link to="/history">历史</router-link>
      </nav>
      <div class="user">
        <template v-if="auth.loggedIn">
          <span class="muted">{{ auth.nickname }}</span>
          <button class="link-btn" @click="logout">退出</button>
        </template>
        <router-link v-else to="/login" class="login-link">微信登录</router-link>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const router = useRouter();

function logout() {
  auth.logout();
  router.push({ name: 'login' });
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}
.header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border);
  background: rgba(10, 14, 23, 0.95);
  position: sticky;
  top: 0;
  z-index: 10;
}
.brand {
  font-weight: 700;
  letter-spacing: 0.02em;
}
.nav {
  display: flex;
  gap: 16px;
  flex: 1;
}
.nav a.router-link-active {
  color: var(--accent);
}
.user {
  display: flex;
  align-items: center;
  gap: 12px;
}
.link-btn {
  background: none;
  border: none;
  color: var(--text-muted);
}
.login-link {
  color: var(--accent);
}
.main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}
</style>
