// api/user.js
import request from './request.js';
import { mock, USE_MOCK } from '../mock/index.js';

export default {
  // auth-service 已实现 → 真实
  register: (data) => USE_MOCK.auth ? mock.register(data) : request.post('/as/auth/register', data),
  login: (data) => USE_MOCK.auth ? mock.login(data) : request.post('/as/auth/login', data),
  refreshToken: (data) => USE_MOCK.auth ? mock.login(data) : request.post('/as/auth/refresh', data),
  logout: () => USE_MOCK.auth ? mock.logout() : request.post('/as/auth/logout'),
  // user-service 已新增"当前用户资料"接口 GET /users/users/me/profile → 真实
  getProfile: () => USE_MOCK.getProfile ? mock.getProfile() : request.get('/us/users/users/me/profile'),
  // user-service 已注册 UserInfoInterceptor，PUT /users/me 可用 → 真实
  updateProfile: (data) => USE_MOCK.updateProfile ? mock.updateProfile(data) : request.put('/us/users/me', data),
};