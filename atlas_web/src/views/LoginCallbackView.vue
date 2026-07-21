<template>
  <div class="callback-page">
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { pollWxQrSession } from '@/api/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const message = ref('正在完成登录…');

onMounted(async () => {
  const token = route.query.token;
  const state = route.query.state;
  if (token) {
    auth.setSession(String(token), { nickname: '微信用户' });
    message.value = '登录成功';
    router.replace('/');
    return;
  }
  if (state) {
    try {
      const res = await pollWxQrSession(String(state));
      if (res.status === 'ok' && res.token) {
        auth.setSession(res.token, res.user);
        router.replace('/');
        return;
      }
    } catch (e) {
      message.value = '登录失败：' + (e.message || e);
      return;
    }
  }
  message.value = '登录参数无效';
  setTimeout(() => router.replace('/login'), 1500);
});
</script>

<style scoped>
.callback-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
