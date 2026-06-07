const config = require('./config');

function normalizeResponse(body) {
  if (!body || typeof body !== 'object') {
    return { ok: false, code: -1, message: '响应格式错误', data: null };
  }
  if (typeof body.code === 'number') {
    return { ok: body.code === 0, code: body.code, message: body.message || '', data: body.data };
  }
  if (body.statusCode !== undefined) {
    var ok = String(body.statusCode) === '200';
    return {
      ok: ok,
      code: ok ? 0 : Number(body.statusCode) || -1,
      message: body.errorMessage || '',
      data: body.data
    };
  }
  return { ok: true, code: 0, message: 'ok', data: body };
}

function request(options, authRetry) {
  authRetry = authRetry || 0;
  return new Promise(function (resolve, reject) {
    wx.request({
      url: config.baseUrl + options.path,
      method: options.method || 'GET',
      data: options.data || {},
      header: Object.assign({
        'Content-Type': 'application/json'
      }, options.header || {}),
      timeout: config.apiTimeout,
      success: function (res) {
        if (res.statusCode === 401 && authRetry < 1 && options.skipAuthRetry !== true) {
          var auth = require('./auth');
          auth.clearSession();
          auth.login().then(function () {
            request(options, authRetry + 1).then(resolve).catch(reject);
          }).catch(reject);
          return;
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(normalizeResponse(res.data));
        } else {
          reject(new Error('HTTP ' + res.statusCode));
        }
      },
      fail: function (err) {
        reject(err);
      }
    });
  });
}

function get(path, data, header, extraOptions) {
  return request(Object.assign({ path: path, method: 'GET', data: data, header: withAuth(header) }, extraOptions || {}));
}

function post(path, data, header, extraOptions) {
  return request(Object.assign({ path: path, method: 'POST', data: data, header: withAuth(header) }, extraOptions || {}));
}

function del(path, data, header, extraOptions) {
  return request(Object.assign({ path: path, method: 'DELETE', data: data, header: withAuth(header) }, extraOptions || {}));
}

function withAuth(header) {
  const auth = require('./auth');
  const token = auth.getToken();
  if (!token) return header || {};
  return Object.assign({}, header, { Authorization: 'Bearer ' + token });
}

module.exports = {
  get: get,
  post: post,
  del: del,
  request: request,
  normalizeResponse: normalizeResponse
};
