// api/user.js
import request from './request.js';
import { mock, USE_MOCK } from '../mock/index.js';

export default {
  // auth-service 已实现 → 真实
  register: (data) => USE_MOCK.auth ? mock.register(data) : request.post('/as/auth/register', data),
  login: (data) => USE_MOCK.auth ? mock.login(data) : request.post('/as/auth/login', data),
  // 后端：POST /auth/refresh 从 Authorization header 取 refresh token（值必须是 "Bearer <refreshToken>"），
  // 不需要请求体；返回 {"access_token": "...", "refresh_token": "..."}（下划线命名）
  refreshToken: () => USE_MOCK.auth
    ? mock.login({})
    : request.post('/as/auth/refresh', {}, {
        'Authorization': 'Bearer ' + (uni.getStorageSync('refreshToken') || ''),
      }),
  // 后端：POST /auth/logout 需要两个 header —— Authorization（自动带上）和 Refresh-Token，
  // 两者都要 "Bearer " 前缀，否则 refresh token 不会被吊销
  logout: () => USE_MOCK.auth
    ? mock.logout()
    : request.post('/as/auth/logout', {}, {
        'Refresh-Token': 'Bearer ' + (uni.getStorageSync('refreshToken') || ''),
      }),
  // user-service 已新增"当前用户资料"接口 GET /users/users/me/profile → 真实
  getProfile: () => USE_MOCK.getProfile ? mock.getProfile() : request.get('/us/users/users/me/profile'),
  // user-service 已注册 UserInfoInterceptor，PUT /users/me 可用 → 真实
  updateProfile: (data) => USE_MOCK.updateProfile ? mock.updateProfile(data) : request.put('/us/users/me', data),
};
