// api/request.js
import config from './config.js';

function getToken() {
  return uni.getStorageSync('token') || '';
}

function request(options) {
  return new Promise((resolve, reject) => {
    const token = getToken();
    // 只在有 token 时才带 Authorization，避免发出空的 "Bearer "
    // 写在 options.header 之前：调用方可以覆盖它（如刷新 token 时改用 refreshToken）
    const headers = {
      'Content-Type': options.contentType || 'application/json',
      ...(token ? { 'Authorization': 'Bearer ' + token } : {}),
      ...options.header,
    };

    uni.request({
      url: config.baseURL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: headers,
      success: (res) => {
        const { statusCode, data } = res;
        if (statusCode === 200) {
          // 后端无统一 code 字段，直接返回 data
          resolve(data);
        } else if (statusCode === 401) {
          uni.removeStorageSync('token');
          uni.reLaunch({ url: '/pages/login/login' });
          reject({ code: 401, message: '未登录或 token 过期' });
        } else {
          uni.showToast({ title: data.message || '请求失败', icon: 'none' });
          reject(data);
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误', icon: 'none' });
        reject(err);
      },
    });
  });
}

export default {
  get: (url, data) => request({ url, method: 'GET', data }),
  // 第三个参数是可选的额外请求头，用来覆盖默认的 Authorization 等
  post: (url, data, header) => request({ url, method: 'POST', data, header }),
  put: (url, data, header) => request({ url, method: 'PUT', data, header }),
  delete: (url, data, header) => request({ url, method: 'DELETE', data, header }),

  upload: (url, filePath, formData) => {
    return new Promise((resolve, reject) => {
      const token = getToken();
      uni.uploadFile({
        url: config.baseURL + url,
        filePath,
        name: 'file',
        formData,
        header: token ? { 'Authorization': 'Bearer ' + token } : {},
        success: (res) => {
          // 兼容后端返回空或非 JSON 的情况
          if (res.statusCode === 200) {
            try {
              const data = JSON.parse(res.data);
              // 如果有 code 字段且为 200 则取 data，否则直接返回解析结果
              if (data && data.code === 200) {
                resolve(data.data);
              } else {
                resolve(data);
              }
            } catch (e) {
              // 返回的不是 JSON，视为成功
              resolve(true);
            }
          } else {
            // 非 200 状态码尝试解析错误信息
            try {
              const data = JSON.parse(res.data);
              uni.showToast({ title: data.message || '上传失败', icon: 'none' });
              reject(data);
            } catch (e) {
              uni.showToast({ title: '上传失败', icon: 'none' });
              reject(res);
            }
          }
        },
        fail: (err) => {
          uni.showToast({ title: '网络错误', icon: 'none' });
          reject(err);
        },
      });
    });
  },
};
