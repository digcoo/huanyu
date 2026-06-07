/**
 * 本地覆盖配置（可选）
 * 复制 config.local.example.js 为 config.local.js 并修改 DEVICE_HOST
 */
module.exports = {
  /** 强制环境：'auto' | 'local' | 'device' | 'prod'，默认 auto */
  ENV: 'auto',

  /** 真机调试：填开发电脑局域网 IP（ipconfig / ifconfig 查看） */
  DEVICE_HOST: '192.168.0.110',

  DEVICE_PORT: 9010,

  /** 正式环境 HTTPS 域名（上线前配置） */
  PROD_BASE_URL: 'https://your-domain.com/tts'
};
