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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 单题评估记录
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("answer_evaluations")
@ApiModel(value = "AnswerEvaluations对象", description = "单题评估记录")
public class AnswerEvaluations implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    @TableField("question_id")
    private Long questionId;

    /** 逻辑关联 session_db 的答案 id */
    @TableField("answer_id")
    private Long answerId;

    @TableField("technical_score")
    private BigDecimal technicalScore;

    @TableField("clarity_score")
    private BigDecimal clarityScore;

    @TableField("depth_score")
    private BigDecimal depthScore;

    @TableField("feedback_text")
    private String feedbackText;

    @TableField("improvement_suggestions")
    private String improvementSuggestions;

    @TableField("evaluated_at")
    private LocalDateTime evaluatedAt;
}
