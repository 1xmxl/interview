// 管理端接口：用户列表、角色授权（苍穹权限模型，仅院系管理员可用）
import { mock, USE_MOCK } from '../mock/index.js'
import request from './request.js'

export default {
  // 用户列表（支持 role、keyword 过滤）
  listUsers: (params) => USE_MOCK.admin
    ? mock.adminListUsers()
    : request.get('/us/users/admin/list', params),

  // 修改用户角色
  updateRole: (userId, roleCode) => USE_MOCK.admin
    ? mock.adminUpdateRole(userId, roleCode)
    : request.put('/us/users/admin/' + userId + '/role?roleCode=' + encodeURIComponent(roleCode)),

  // 角色列表
  roles: () => USE_MOCK.admin
    ? mock.adminRoles()
    : request.get('/us/users/admin/roles'),

  // 查看角色权限
  rolePermissions: (roleCode) => USE_MOCK.admin
    ? mock.adminRolePermissions(roleCode)
    : request.get('/us/users/admin/permissions', { roleCode }),

  // 角色授权（整体覆盖）
  grantRole: (roleCode, permissions) => USE_MOCK.admin
    ? Promise.resolve(true)
    : request.post('/us/users/admin/roles/' + roleCode + '/permissions', permissions),
}
