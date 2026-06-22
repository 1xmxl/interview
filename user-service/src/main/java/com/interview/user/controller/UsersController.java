package com.interview.user.controller;



import cn.hutool.core.bean.BeanUtil;
import com.interview.DTO.LoginRequest;
import com.interview.DTO.LoginResponse;
import com.interview.DTO.UserDTO;
import com.interview.DTO.UsersDTO;
import com.interview.context.UserContext;
import com.interview.user.domain.po.Users;
import com.interview.user.service.IUsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2026-06-15
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UsersController {
    private final IUsersService usersService;

    @GetMapping("/me")
    public UsersDTO getUsers(@RequestParam("username") String username,@RequestParam("password") String password) {

        log.info("start to getUser username={},password={}", username, password);
        return usersService.getUsers(username, password);
    }
    @PostMapping("/save")
    public void saveNewUsers(@RequestBody UserDTO usersDTO) {
        Users users = BeanUtil.copyProperties(usersDTO, Users.class);
        users.setCreatedAt(LocalDateTime.now());
        users.setUpdatedAt(LocalDateTime.now());
        usersService.save(users);
    }
    @GetMapping("/me")
    public Users getUsers() {
        String userId = UserContext.getUserId();
        return usersService.getById(userId);
    }
    @PutMapping("/me")
    public void updateUsers(@RequestBody UserDTO usersDTO) {
        usersService.updateUsers(usersDTO);
    }
}
