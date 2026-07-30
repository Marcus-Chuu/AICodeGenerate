package com.marcuschu.aicodegenerate.model.dto.chathistory;

import com.marcuschu.aicodegenerate.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 管理员对话历史查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatHistoryAdminQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long appId;

    private Long userId;

    /**
     * 消息类型：user / ai。
     */
    private String messageType;
}
