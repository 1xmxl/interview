// api/dashboard.js —— 首页数据概览（演示用聚合接口，后端无需实现）
import request from './request.js';
import { mock, USE_MOCK } from '../mock/index.js';

export default {
  getStats: () => USE_MOCK.dashboard ? mock.getDashboard() : request.get('/us/users/me/stats'),
};