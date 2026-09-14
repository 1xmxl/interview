package com.interview.service.impl;

import com.interview.DTO.StructuredResume;
import com.interview.domain.po.ResumeProjects;
import com.interview.mapper.ResumeProjectsMapper;
import com.interview.service.IResumeProjectsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2026-06-22
 */
@Service
public class ResumeProjectsServiceImpl extends ServiceImpl<ResumeProjectsMapper, ResumeProjects> implements IResumeProjectsService {

    @Override
    public void saveResumeProjects(List<StructuredResume.ProjectEntry> resumeProjects, Long resumeId) {
        resumeProjects.forEach(resumeProject -> {
            // 安全解析日期（处理 null、空字符串、非法格式）
            LocalDate startDate = parseDateSafely(resumeProject.getStartDate());
            LocalDate endDate = parseDateSafely(resumeProject.getEndDate());

            ResumeProjects build = ResumeProjects.builder()
                    .resumeId(resumeId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .description(resumeProject.getDescription())
                    .technologies(resumeProject.getTechnologies())
                    .projectName(resumeProject.getProjectName())
                    .role(resumeProject.getRole())
                    .build();
            this.save(build);
        });
    }

    // 新增工具方法：安全解析日期
    private LocalDate parseDateSafely(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDate.now(); // 或返回 null（需确保数据库允许）
        }
        try {
            return LocalDate.parse(dateStr.trim()); // 自动处理前后空格
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format:, using current date as fallback");
            return LocalDate.now(); // 降级策略：用当前日期
        }
    }

    @Override
    public List<ResumeProjects> getListById(Long resumeId) {
        List<ResumeProjects> list = lambdaQuery().eq(ResumeProjects::getResumeId, resumeId).list();
        return list;
    }

    @Override
    public void deleteAllById(Long resumeId) {
        List<Long> collect = lambdaQuery().eq(ResumeProjects::getResumeId, resumeId).list().stream().map(
                a -> {
                    return a.getId();
                }
        ).collect(Collectors.toList());
        removeByIds(collect);
    }
}
