import axios from 'axios';
import { getToken, clearSession } from './session';

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/tts',
  timeout: 60000
});

client.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (res) => {
    const body = res.data;
    if (body && typeof body.code === 'number') {
      if (body.code === 0) {
        return body.data;
      }
      return Promise.reject(new Error(body.message || '请求失败'));
    }
    return body;
  },
  (err) => {
    if (err.response && err.response.status === 401) {
      clearSession();
    }
    return Promise.reject(err);
  }
);

export default client;
