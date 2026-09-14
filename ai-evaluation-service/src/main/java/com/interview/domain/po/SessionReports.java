package com.interview.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 面试完整报告
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("session_reports")
@ApiModel(value = "SessionReports对象", description = "面试完整报告")
public class SessionReports implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 一个会话一份报告（唯一） */
    @TableField("session_id")
    private Long sessionId;

    /** Markdown/HTML 格式完整报告 */
    @TableField("report_content")
    private String reportContent;

    /** 维度分数 JSON，如 {"technical":85,"communication":72,...} */
    private String metrics;

    @TableField("generated_at")
    private LocalDateTime generatedAt;
}
