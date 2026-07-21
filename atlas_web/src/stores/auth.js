import { defineStore } from 'pinia';
import { getToken, getUser, saveSession, clearSession, isLoggedIn } from '@/api/session';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    user: getUser()
  }),
  getters: {
    loggedIn: (state) => !!state.token,
    nickname: (state) => (state.user && state.user.nickname) || 'Atlas 用户'
  },
  actions: {
    setSession(token, user) {
      this.token = token;
      this.user = user || null;
      saveSession(token, user);
    },
    logout() {
      this.token = '';
      this.user = null;
      clearSession();
    },
    hydrate() {
      this.token = getToken();
      this.user = getUser();
      return isLoggedIn();
    }
  }
});
