package com.interview.user.service.impl;

import com.interview.user.domain.po.UserProfiles;
import com.interview.user.mapper.UserProfilesMapper;
import com.interview.user.service.IUserProfilesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2026-06-15
 */
@Service
public class UserProfilesServiceImpl extends ServiceImpl<UserProfilesMapper, UserProfiles> implements IUserProfilesService {

}
