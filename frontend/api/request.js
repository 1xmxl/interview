// api/request.js
import config from './config.js';

function getToken() {
  return uni.getStorageSync('token') || '';
}

function request(options) {
  return new Promise((resolve, reject) => {
const token = getToken();
const headers = {
  'Content-Type': options.contentType || 'application/json',
  'Authorization': 'Bearer ' + token,   // 即使 token 为空，也发送 "Bearer "
  ...options.header,
};
    // 只在有 token 时才加 Authorization，避免发送 "Bearer "
    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }

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
  post: (url, data) => request({ url, method: 'POST', data }),
  put: (url, data) => request({ url, method: 'PUT', data }),
  delete: (url) => request({ url, method: 'DELETE' }),

  upload: (url, filePath, formData) => {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: config.baseURL + url,
        filePath,
        name: 'file',
        formData,
        header: {
          'Authorization': 'Bearer ' + getToken(),
        },
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