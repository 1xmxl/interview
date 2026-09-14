package com.interview.controller;


import com.interview.domain.dto.ConfigSubbmit;
import com.interview.domain.po.InterviewConfigs;
import com.interview.service.IInterviewConfigsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2026-07-20
 */
@RestController
@RequestMapping("/interviews")
@Slf4j
@RequiredArgsConstructor
public class InterviewConfigsController {
    private final IInterviewConfigsService interviewConfigsService;

    @PostMapping("/configs")
    public void createConfigs(@RequestBody ConfigSubbmit configSubbmit) {
        log.info("开始创建新的面试配置");
        interviewConfigsService.createConfigs(configSubbmit);
    }

    @GetMapping("/configs")
    public List<InterviewConfigs> getConfigs() {
        log.info("获取用户面试配置列表");
        return interviewConfigsService.getConfigs();
    }

    @GetMapping("/configs/{id}")
    public InterviewConfigs getConfig(@PathVariable Long id) {
        log.info("获取面试配置详情, id={}", id);
        return interviewConfigsService.getConfig(id);
    }

    @PutMapping("/configs/{id}")
    public void updateConfig(@PathVariable Long id, @RequestBody ConfigSubbmit configSubbmit) {
        log.info("更新面试配置, id={}", id);
        interviewConfigsService.updateConfig(id, configSubbmit);
    }

    @DeleteMapping("/configs/{id}")
    public void deleteConfig(@PathVariable Long id) {
        log.info("删除面试配置, id={}", id);
        interviewConfigsService.deleteConfig(id);
    }
}
