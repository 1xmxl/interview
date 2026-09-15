// api/feedback.js
import request from './request.js';
import { mock, USE_MOCK } from '../mock/index.js';

export default {
  // feedback-analysis-service 空壳 → mock
  getSpeechAnalysis: (sessionId) => USE_MOCK.feedback ? mock.getSpeechAnalysis() : request.get(`/fa/feedback/${sessionId}/speech`),
  getAnalytics: (params) => USE_MOCK.feedback ? mock.getAnalytics() : request.get('/fa/feedback/analytics', params),
};