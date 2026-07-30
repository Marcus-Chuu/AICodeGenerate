package com.marcuschu.aicodegenerate.service;

import com.marcuschu.aicodegenerate.model.dto.chathistory.ChatHistoryAdminQueryRequest;
import com.marcuschu.aicodegenerate.model.entity.ChatHistory;
import com.marcuschu.aicodegenerate.model.enums.ChatHistoryMessageTypeEnum;
import com.marcuschu.aicodegenerate.model.vo.ChatHistoryPageVO;
import com.marcuschu.aicodegenerate.model.vo.ChatHistoryVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * 对话历史 服务层。
 *
 * @author MarcusChu
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存一条对话消息。
     */
    ChatHistory saveMessage(Long appId, Long userId, String message,
                            ChatHistoryMessageTypeEnum messageType, Long parentId);

    /**
     * 按游标查询应用对话历史，每次返回最新的 10 条消息。
     */
    ChatHistoryPageVO listAppChatHistory(Long appId, Long lastId);

    /**
     * 获取管理员分页查询条件，始终按照创建时间降序排序。
     */
    QueryWrapper getQueryWrapper(ChatHistoryAdminQueryRequest queryRequest);

    /**
     * 转换分页视图。
     */
    Page<ChatHistoryVO> getChatHistoryVOPage(Page<ChatHistory> chatHistoryPage);

    /**
     * 删除某个应用的全部对话历史。
     */
    boolean removeByAppId(Long appId);


    /**
     * 对话记忆初始化时, 从数据库加载对话历史到记忆中
     * @param appId 应用 ID
     * @param chatMemory 对话历史
     * @param maxCount 最大加载数
     * @return int
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

}
