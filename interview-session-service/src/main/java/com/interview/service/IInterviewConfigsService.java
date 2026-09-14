package com.interview.service;

import com.interview.domain.dto.ConfigSubbmit;
import com.interview.domain.po.InterviewConfigs;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2026-07-20
 */
public interface IInterviewConfigsService extends IService<InterviewConfigs> {

    void createConfigs(ConfigSubbmit configSubbmit);

    List<InterviewConfigs> getConfigs();

    InterviewConfigs getConfig(Long id);

    void updateConfig(Long id, ConfigSubbmit configSubbmit);

    void deleteConfig(Long id);
}
