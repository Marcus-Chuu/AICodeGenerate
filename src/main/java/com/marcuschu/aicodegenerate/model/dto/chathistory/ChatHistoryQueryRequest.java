package com.marcuschu.aicodegenerate.model.dto.chathistory;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用对话历史游标查询请求。
 */
@Data
public class ChatHistoryQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用 id。
     */
    private Long appId;

    /**
     * 上一页最早一条消息的 id，首次查询不传。
     */
    private Long lastId;
}
