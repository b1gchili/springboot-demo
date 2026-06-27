package org.example.student.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 完成积分任务请求参数。
 */
@Data
public class CompleteTaskRequest {
    @NotBlank(message = "任务编码不能为空")
    private String taskCode;

    private String bizId;
}
