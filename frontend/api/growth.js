// api/growth.js
import request from './request.js';
import { mock, USE_MOCK } from '../mock/index.js';

export default {
  // user-growth-service 空壳 → mock
  getSkills: () => USE_MOCK.growth ? mock.getSkills() : request.get('/ug/growth/skills'),
  getSkillHistory: (skill, period) => USE_MOCK.growth ? mock.getSkillHistory(skill, period) : request.get('/ug/growth/skills/history', { skill, period }),
  listReports: () => USE_MOCK.growth ? mock.listReports() : request.get('/ug/growth/reports'),
  reportDetail: (id) => USE_MOCK.growth ? mock.reportDetail(id) : request.get(`/ug/growth/reports/${id}`),
  getWeaknesses: () => USE_MOCK.growth ? mock.getWeaknesses() : request.get('/ug/growth/weaknesses'),
};