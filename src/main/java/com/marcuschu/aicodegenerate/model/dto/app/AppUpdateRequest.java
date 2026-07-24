package com.marcuschu.aicodegenerate.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新应用请求
 */
@Data
public class AppUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;


    /**
     * 优先级
     */
    private Integer priority;
}



