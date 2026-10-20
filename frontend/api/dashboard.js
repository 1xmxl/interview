// api/dashboard.js —— 首页数据概览（聚合 user-service /users/me/stats）
import request from './request.js';
import { mock, USE_MOCK } from '../mock/index.js';

export default {
  getStats: () => USE_MOCK.dashboard ? mock.getDashboard() : request.get('/us/users/me/stats'),
};