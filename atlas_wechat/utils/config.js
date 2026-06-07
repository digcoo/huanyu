/**
 * 运行时配置（atlas_wechat）
 *
 * 环境说明：
 * - local  : 开发者工具 / 本机模拟器 → 127.0.0.1
 * - device : 真机预览 / 真机调试 → 电脑局域网 IP（与手机同一 WiFi）
 * - prod   : 正式版 / 体验版 → HTTPS 合法域名
 * - auto   : 自动识别（devtools=local，真机=device）
 *
 * 真机调试步骤：
 * 1. 复制 config.local.example.js → config.local.js，填写 DEVICE_HOST
 * 2. 手机与电脑连同一 WiFi，防火墙放行 9010
 * 3. 开发者工具勾选「不校验合法域名…」
 */
function loadLocalOverrides() {
  try {
    return require('./config.local.js');
  } catch (e) {
    return {};
  }
}

function detectRuntimeEnv() {
  try {
    if (typeof wx !== 'undefined' && wx.getSystemInfoSync) {
      var info = wx.getSystemInfoSync();
      if (info.platform === 'devtools') {
        return 'local';
      }
      return 'device';
    }
  } catch (err) {
    /* ignore */
  }
  return 'local';
}

function buildBaseUrl(env, local) {
  var host = local.DEVICE_HOST || '192.168.0.110';
  var port = local.DEVICE_PORT || 9010;
  var prodUrl = local.PROD_BASE_URL || 'https://your-domain.com/tts';

  if (env === 'prod') {
    return prodUrl.replace(/\/$/, '');
  }
  if (env === 'device') {
    return 'http://' + host + ':' + port + '/tts';
  }
  return 'http://127.0.0.1:' + port + '/tts';
}

var local = loadLocalOverrides();
var envSetting = local.ENV || 'auto';
var runtimeEnv = envSetting === 'auto' ? detectRuntimeEnv() : envSetting;
var baseUrl = buildBaseUrl(runtimeEnv, local);

module.exports = {
  /** 当前生效环境：local | device | prod */
  env: runtimeEnv,

  /** 原始配置项（auto/local/device/prod） */
  envSetting: envSetting,

  useMock: false,
  fallbackOnError: true,
  baseUrl: baseUrl,
  apiTimeout: 15000,

  enabledMarkets: ['cn'],
  marketComingSoonTip: '该市场即将开放，敬请期待',

  requireLoginForWatchlist: true,
  syncRemote: true,

  /** 供调试页展示 */
  getEnvLabel: function () {
    var map = {
      local: '本机模拟器',
      device: '真机局域网',
      prod: '正式环境'
    };
    return (map[runtimeEnv] || runtimeEnv) + ' · ' + baseUrl;
  }
};
