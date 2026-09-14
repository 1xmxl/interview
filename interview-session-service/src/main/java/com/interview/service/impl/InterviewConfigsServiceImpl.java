package com.interview.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interview.context.UserContext;
import com.interview.domain.dto.ConfigSubbmit;
import com.interview.domain.po.InterviewConfigs;
import com.interview.mapper.InterviewConfigsMapper;
import com.interview.service.IInterviewConfigsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2026-07-20
 */
@Service
public class InterviewConfigsServiceImpl extends ServiceImpl<InterviewConfigsMapper, InterviewConfigs> implements IInterviewConfigsService {

    @Override
    public void createConfigs(ConfigSubbmit configSubbmit) {
        InterviewConfigs interviewConfigs = BeanUtil.copyProperties(configSubbmit, InterviewConfigs.class);
        String userId = UserContext.getUserId();
        LocalDateTime now = LocalDateTime.now();
        interviewConfigs.setCreatedAt(now);
        interviewConfigs.setUserId(Long.parseLong(userId));
        this.save(interviewConfigs);
    }

    @Override
    public List<InterviewConfigs> getConfigs() {
        Long userId = Long.parseLong(UserContext.getUserId());
        LambdaQueryWrapper<InterviewConfigs> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewConfigs::getUserId, userId)
                .orderByDesc(InterviewConfigs::getCreatedAt);
        return this.list(wrapper);
    }

    @Override
    public InterviewConfigs getConfig(Long id) {
        Long userId = Long.parseLong(UserContext.getUserId());
        LambdaQueryWrapper<InterviewConfigs> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewConfigs::getId, id)
                .eq(InterviewConfigs::getUserId, userId);
        return this.getOne(wrapper);
    }

    @Override
    public void updateConfig(Long id, ConfigSubbmit configSubbmit) {
        InterviewConfigs existing = getOwnConfig(id);
        BeanUtil.copyProperties(configSubbmit, existing);
        this.updateById(existing);
    }

    @Override
    public void deleteConfig(Long id) {
        InterviewConfigs existing = getOwnConfig(id);
        this.removeById(existing.getId());
    }

    private InterviewConfigs getOwnConfig(Long id) {
        Long userId = Long.parseLong(UserContext.getUserId());
        LambdaQueryWrapper<InterviewConfigs> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewConfigs::getId, id)
                .eq(InterviewConfigs::getUserId, userId);
        InterviewConfigs existing = this.getOne(wrapper);
        if (existing == null) {
            throw new RuntimeException("配置不存在或无权限操作");
        }
        return existing;
    }
}
