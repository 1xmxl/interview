package com.interview.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户技能成长记录
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_skill_progress")
@ApiModel(value = "UserSkillProgress对象", description = "用户技能成长记录")
public class UserSkillProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("skill_name")
    private String skillName;

    /** 当次评估得分 */
    private BigDecimal score;

    /** TECHNICAL / COMMUNICATION / PROBLEM_SOLVING 等维度 */
    private String dimension;

    @TableField("recorded_at")
    private LocalDateTime recordedAt;
}
