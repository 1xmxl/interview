package com.interview.service;

import com.interview.domain.dto.SessionSubmit;
import com.interview.domain.po.InterviewSessions;
import com.interview.domain.vo.SessionHistoryVO;
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
public interface IInterviewSessionsService extends IService<InterviewSessions> {

    void createSession(SessionSubmit submit);

    InterviewSessions getSession(Long id);

    List<SessionHistoryVO> getHistory(Long id);
}
