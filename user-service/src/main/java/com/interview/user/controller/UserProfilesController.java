package com.interview.user.controller;


import com.interview.context.UserContext;
import com.interview.user.domain.po.UserProfiles;
import com.interview.user.service.IUserProfilesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  用户扩展资料控制器
 * </p>
 *
 * @author author
 * @since 2026-06-15
 */
@RestController
@RequestMapping("/user-profiles")
@RequiredArgsConstructor
@Slf4j
public class UserProfilesController {

    private final IUserProfilesService userProfilesService;

    /** 获取当前登录用户的扩展资料 */
    @GetMapping("/me")
    public UserProfiles getMyProfile() {
        Long userId = Long.parseLong(UserContext.getUserId());
        log.info("获取当前用户扩展资料, userId={}", userId);
        return userProfilesService.getById(userId);
    }

    /** 按用户 ID 查询扩展资料 */
    @GetMapping("/list")
    public UserProfiles getByUserId(@RequestParam Long userId) {
        log.info("查询用户扩展资料, userId={}", userId);
        return userProfilesService.getById(userId);
    }

    @GetMapping("/{userId}")
    public UserProfiles getById(@PathVariable Long userId) {
        log.info("查询用户扩展资料详情, userId={}", userId);
        return userProfilesService.getById(userId);
    }

    @PostMapping
    public void save(@RequestBody UserProfiles userProfiles) {
        log.info("新增用户扩展资料, userId={}", userProfiles.getUserId());
        userProfilesService.save(userProfiles);
    }

    @PutMapping("/{userId}")
    public void update(@PathVariable Long userId, @RequestBody UserProfiles userProfiles) {
        log.info("更新用户扩展资料, userId={}", userId);
        userProfiles.setUserId(userId);
        userProfilesService.updateById(userProfiles);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("删除用户扩展资料, userId={}", userId);
        userProfilesService.removeById(userId);
    }
}
