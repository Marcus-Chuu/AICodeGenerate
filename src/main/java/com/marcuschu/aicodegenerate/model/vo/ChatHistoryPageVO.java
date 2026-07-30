package com.marcuschu.aicodegenerate.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 应用对话历史游标分页视图。
 */
@Data
public class ChatHistoryPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 按时间正序返回的消息列表，便于聊天窗口直接展示。
     */
    private List<ChatHistoryVO> records;

    /**
     * 加载更早消息时使用的游标，没有更多消息时为空。
     */
    private Long nextCursor;

    private boolean hasMore;
}
