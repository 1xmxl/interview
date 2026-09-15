// api/evaluation.js
import request from './request.js';
import { mock, USE_MOCK } from '../mock/index.js';

export default {
  // ai-evaluation-service 无 controller → mock
  getEval: (sessionId) => USE_MOCK.evaluation ? mock.getEval(sessionId) : request.get(`/ae/evaluations/${sessionId}`),
  getAnswerDetail: (sessionId, answerId) => USE_MOCK.evaluation ? mock.getAnswerDetail(sessionId, answerId) : request.get(`/ae/evaluations/${sessionId}/answers/${answerId}`),
  regenerateReport: (sessionId) => USE_MOCK.evaluation ? mock.regenerateReport() : request.post(`/ae/evaluations/${sessionId}/report/regenerate`),
};