/**
 * 正式版运行时配置（atlas_wechat）
 * useMock=false 联调 backend；fallbackOnError=true 时 API 失败回退本地 Mock
 */
module.exports = {
  useMock: false,
  fallbackOnError: true,
  baseUrl: 'http://127.0.0.1:9010/tts',
  apiTimeout: 15000,

  /** Phase 1 仅 A 股可用，其余 Tab 展示但置灰 */
  enabledMarkets: ['cn'],
  marketComingSoonTip: '该市场即将开放，敬请期待',

  /** 自选/历史需登录 */
  requireLoginForWatchlist: true
};
