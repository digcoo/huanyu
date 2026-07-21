<template>
  <div class="login-page">
    <div class="card login-card">
      <h1>微信扫码登录</h1>
      <p class="muted">使用微信扫描下方二维码，确认后在浏览器中自动登录</p>

      <div class="qr-wrap">
        <img v-if="qrDataUrl" :src="qrDataUrl" alt="登录二维码" class="qr-img" />
        <div v-else class="qr-placeholder">加载二维码…</div>
      </div>

      <p class="status">{{ statusText }}</p>

      <button v-if="devMode" class="dev-btn" :disabled="loading" @click="simulateScan">
        开发环境：模拟扫码成功
      </button>

      <p v-if="devMode" class="hint muted">
        未配置微信开放平台 AppId 时走开发模式；生产环境请在 backend 配置
        <code>atlas.auth.wx.app-id</code> 与 redirect-uri
      </p>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import QRCode from 'qrcode';
import { createWxQrSession, devConfirmWxQr, pollWxQrSession } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const state = ref('');
const qrDataUrl = ref('');
const devMode = ref(false);
const loading = ref(false);
const statusText = ref('等待扫码…');
let pollTimer = null;

async function startSession() {
  loading.value = true;
  try {
    const data = await createWxQrSession();
    state.value = data.state;
    devMode.value = !!data.devMode;
    const content = data.qrUrl || data.qrContent || data.state;
    qrDataUrl.value = await QRCode.toDataURL(content, { width: 220, margin: 1 });
    statusText.value = devMode.value ? '开发模式：可模拟扫码或用手机扫二维码' : '请使用微信扫描二维码';
    startPoll();
  } catch (e) {
    statusText.value = '创建登录会话失败：' + (e.message || e);
  } finally {
    loading.value = false;
  }
}

function startPoll() {
  stopPoll();
  pollTimer = setInterval(async () => {
    if (!state.value) return;
    try {
      const res = await pollWxQrSession(state.value);
      if (res.status === 'ok' && res.token) {
        auth.setSession(res.token, res.user);
        statusText.value = '登录成功，正在跳转…';
        stopPoll();
        const redirect = route.query.redirect || '/';
        router.replace(typeof redirect === 'string' ? redirect : '/');
      } else if (res.status === 'expired') {
        statusText.value = '二维码已过期，正在刷新…';
        stopPoll();
        startSession();
      }
    } catch {
      /* ignore transient poll errors */
    }
  }, 2000);
}

function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
}

async function simulateScan() {
  if (!state.value) return;
  loading.value = true;
  try {
    const res = await devConfirmWxQr(state.value);
    if (res.status === 'ok' && res.token) {
      auth.setSession(res.token, res.user);
      router.replace((route.query.redirect) || '/');
    }
  } catch (e) {
    statusText.value = '模拟登录失败：' + (e.message || e);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  if (auth.loggedIn) {
    router.replace('/');
    return;
  }
  startSession();
});

onBeforeUnmount(stopPoll);
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.login-card {
  width: min(420px, 100%);
  text-align: center;
}
.qr-wrap {
  margin: 24px auto;
  width: 220px;
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
}
.qr-img {
  width: 200px;
  height: 200px;
}
.qr-placeholder {
  color: #64748b;
}
.status {
  min-height: 24px;
}
.dev-btn {
  margin-top: 12px;
  padding: 10px 16px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--bg);
  color: var(--accent);
}
.hint {
  font-size: 12px;
  margin-top: 16px;
  line-height: 1.5;
}
code {
  color: var(--accent);
}
</style>
