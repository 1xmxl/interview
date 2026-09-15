// api/resume.js
import request from './request.js';
import { mock, USE_MOCK } from '../mock/index.js';

export default {
  // resume-service 已实现 → 真实
  upload: (filePath, formData) => USE_MOCK.resumeUpload ? mock.uploadResume() : request.upload('/res/resumes/upload', filePath, formData),
  updateStatus: (resumeId, data) => USE_MOCK.resumeList ? Promise.resolve(true) : request.put(`/res/resumes/${resumeId}/updateStatus`, data),
  list: () => USE_MOCK.resumeList ? mock.listResumes() : request.get('/res/resumes'),
  detail: (id) => USE_MOCK.resumeList ? mock.resumeDetail(id) : request.get(`/res/resumes/${id}`),
  delete: (id) => USE_MOCK.resumeList ? mock.deleteResume() : request.delete(`/res/resumes/${id}`),
  getSkills: (id) => USE_MOCK.resumeList ? mock.resumeDetail(id).then(r => r.skills || []) : request.get(`/res/resumes/${id}/skills`),
  getExperiences: (id) => USE_MOCK.resumeList ? mock.resumeDetail(id).then(r => r.experiences || []) : request.get(`/res/resumes/${id}/experiences`),
  // 后端无 reparse 接口 → mock
  reparse: (id) => USE_MOCK.reparse ? mock.reparseResume() : request.post(`/res/resumes/${id}/reparse`),
};