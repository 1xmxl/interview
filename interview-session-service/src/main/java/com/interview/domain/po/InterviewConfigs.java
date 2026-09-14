package com.interview.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author author
 * @since 2026-07-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("interview_configs")
@ApiModel(value="InterviewConfigs对象", description="")
public class InterviewConfigs implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("name")
    private String name;

    @TableField("interviewer_style")
    private String interviewerStyle;

    @TableField("difficulty")
    private String difficulty;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> targetSkills;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> questionCategories;

    @TableField("language")
    private String language;

    @TableField("created_at")
    private LocalDateTime createdAt;


}
