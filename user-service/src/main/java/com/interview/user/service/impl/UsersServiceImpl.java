package com.interview.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.interview.DTO.LoginRequest;
import com.interview.DTO.LoginResponse;
import com.interview.DTO.UserDTO;
import com.interview.DTO.UsersDTO;

import com.interview.context.UserContext;
import com.interview.user.domain.po.Users;
import com.interview.user.mapper.UsersMapper;
import com.interview.user.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import com.interview.utils.JwtUtil;


import java.time.Duration;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2026-06-15
 */
@Service
@RequiredArgsConstructor
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtUtil jwtUtil;


    @Override
    public UsersDTO getUsers(String username, String password) {
        Users one = lambdaQuery().eq(Users::getUsername, username).one();
        if (one == null || !BCrypt.checkpw(password, one.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        UsersDTO usersDTO = BeanUtil.copyProperties(one, UsersDTO.class);
        return usersDTO;
    }

    @Override
    public void updateUsers(UserDTO usersDTO) {
        String userId = UserContext.getUserId();
        String username = usersDTO.getUsername();
        String email = usersDTO.getEmail();
        var updater = lambdaUpdate().eq(Users::getId, userId)
                .set(Users::getUsername, username)
                .set(Users::getEmail, email);
        // 只有传了新密码才重新哈希，前端改资料可以不传密码
        if (usersDTO.getPasswordHash() != null && !usersDTO.getPasswordHash().isBlank()) {
            String hashpw = BCrypt.hashpw(usersDTO.getPasswordHash(), BCrypt.gensalt());
            updater.set(Users::getPasswordHash, hashpw);
        }
        updater.update();
    }
}
