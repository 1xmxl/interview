// api/interview.js
import request from './request.js';
import { mock, USE_MOCK } from '../mock/index.js';

export default {
  // 配置 CRUD：interview-session 已实现（⚠️ 服务当前编译不过则不可用）→ 真实
  createConfig: (data) => USE_MOCK.configs ? mock.saveConfig(data) : request.post('/is/interviews/configs', data),
  listConfigs: () => USE_MOCK.configs ? mock.listConfigs() : request.get('/is/interviews/configs'),
  getConfig: (id) => USE_MOCK.configs ? mock.configDetail(id) : request.get(`/is/interviews/configs/${id}`),
  updateConfig: (id, data) => USE_MOCK.configs ? mock.saveConfig(data, id) : request.put(`/is/interviews/configs/${id}`, data),
  deleteConfig: (id) => USE_MOCK.configs ? mock.deleteConfig(id) : request.delete(`/is/interviews/configs/${id}`),

  // 会话：创建/查询已实现 → 真实；start/answer/end 未实现 → mock
  createSession: (data) => USE_MOCK.createSession ? mock.createSession(data) : request.post('/is/interviews/sessions', data),
  getSession: (id) => USE_MOCK.createSession ? mock.getSession(id) : request.get(`/is/interviews/sessions/${id}`),
  startSession: (id) => USE_MOCK.sessionFlow ? mock.startSession(id) : request.post(`/is/interviews/sessions/${id}/start`),
  submitAnswer: (id, data) => USE_MOCK.sessionFlow ? mock.submitAnswer(id, data) : request.post(`/is/interviews/sessions/${id}/answer`, data),
  endSession: (id) => USE_MOCK.sessionFlow ? mock.endSession(id) : request.post(`/is/interviews/sessions/${id}/end`),
  // getHistory 后端空壳 → mock
  getHistory: (id) => USE_MOCK.history ? mock.getHistory(id) : request.get(`/is/interviews/sessions/${id}/history`),
};