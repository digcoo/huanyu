const config = require('./config');
const api = require('./api');

const TOKEN_KEY = 'authToken';
const USER_KEY = 'authUser';
const OPENID_KEY = 'authOpenid';

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || '';
}

function getOpenid() {
  return wx.getStorageSync(OPENID_KEY) || '';
}

function getUser() {
  return wx.getStorageSync(USER_KEY) || null;
}

function isLoggedIn() {
  return !!getToken();
}

function saveSession(token, user, openid) {
  wx.setStorageSync(TOKEN_KEY, token);
  if (user) wx.setStorageSync(USER_KEY, user);
  if (openid) wx.setStorageSync(OPENID_KEY, openid);
}

function clearSession() {
  wx.removeStorageSync(TOKEN_KEY);
  wx.removeStorageSync(USER_KEY);
  wx.removeStorageSync(OPENID_KEY);
}

function login() {
  return new Promise(function (resolve, reject) {
    wx.login({
      success: function (res) {
        if (!res.code) {
          reject(new Error('wx.login 未返回 code'));
          return;
        }
        if (config.useMock) {
          saveSession('mock-' + res.code.slice(0, 8), { nickname: 'Atlas 用户' }, 'mock-openid');
          resolve(getToken());
          return;
        }
        api.post('/auth/wx/login', { code: res.code }).then(function (result) {
          if (!result.ok || !result.data || !result.data.token) {
            reject(new Error(result.message || '登录失败'));
            return;
          }
          saveSession(result.data.token, result.data.user || null, result.data.openid || null);
          resolve(result.data.token);
        }).catch(reject);
      },
      fail: reject
    });
  });
}

function ensureLogin() {
  if (isLoggedIn()) return Promise.resolve(getToken());
  return login();
}

function promptLogin(title) {
  return new Promise(function (resolve, reject) {
    wx.showModal({
      title: title || '需要登录',
      content: '加入自选、查看自选池需先登录微信账号',
      confirmText: '去登录',
      cancelText: '取消',
      success: function (res) {
        if (!res.confirm) {
          reject(new Error('cancelled'));
          return;
        }
        ensureLogin().then(resolve).catch(reject);
      }
    });
  });
}

module.exports = {
  getToken: getToken,
  getOpenid: getOpenid,
  getUser: getUser,
  isLoggedIn: isLoggedIn,
  login: login,
  ensureLogin: ensureLogin,
  promptLogin: promptLogin,
  clearSession: clearSession,
  saveSession: saveSession
};
