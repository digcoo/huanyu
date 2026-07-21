import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true }
    },
    {
      path: '/login/callback',
      name: 'login-callback',
      component: () => import('@/views/LoginCallbackView.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      meta: { public: true },
      children: [
        { path: '', name: 'home', component: () => import('@/views/HomeView.vue'), meta: { public: true } },
        { path: 'stock/:code', name: 'stock', component: () => import('@/views/StockDetailView.vue'), meta: { public: true } },
        { path: 'watchlist', name: 'watchlist', component: () => import('@/views/WatchlistView.vue') },
        { path: 'history', name: 'history', component: () => import('@/views/HistoryView.vue') }
      ]
    }
  ]
});

router.beforeEach((to) => {
  const auth = useAuthStore();
  auth.hydrate();
  if (to.meta.public) {
    return true;
  }
  const matched = to.matched.some((r) => r.meta && r.meta.public);
  if (matched) {
    return true;
  }
  if (!auth.loggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }
  return true;
});

export default router;
