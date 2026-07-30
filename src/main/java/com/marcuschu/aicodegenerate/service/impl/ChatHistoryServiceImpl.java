package com.marcuschu.aicodegenerate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.marcuschu.aicodegenerate.exception.ErrorCode;
import com.marcuschu.aicodegenerate.exception.ThrowUtils;
import com.marcuschu.aicodegenerate.mapper.ChatHistoryMapper;
import com.marcuschu.aicodegenerate.model.dto.chathistory.ChatHistoryAdminQueryRequest;
import com.marcuschu.aicodegenerate.model.entity.ChatHistory;
import com.marcuschu.aicodegenerate.model.enums.ChatHistoryMessageTypeEnum;
import com.marcuschu.aicodegenerate.model.vo.ChatHistoryPageVO;
import com.marcuschu.aicodegenerate.model.vo.ChatHistoryVO;
import com.marcuschu.aicodegenerate.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author MarcusChu
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
        implements ChatHistoryService {

    private static final int APP_HISTORY_PAGE_SIZE = 10;

    @Override
    public ChatHistory saveMessage(Long appId, Long userId, String message,
                                   ChatHistoryMessageTypeEnum messageType, Long parentId) {
        ThrowUtils.throwIf(appId == null || appId <= 0,
                ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0,
                ErrorCode.PARAMS_ERROR, "用户 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message),
                ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(messageType == null,
                ErrorCode.PARAMS_ERROR, "消息类型不能为空");

        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageType.getValue())
                .parentId(parentId)
                .build();
        boolean result = this.save(chatHistory);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存对话历史失败");
        return chatHistory;
    }

    @Override
    public ChatHistoryPageVO listAppChatHistory(Long appId, Long lastId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(lastId != null && lastId <= 0, ErrorCode.PARAMS_ERROR, "历史消息游标无效");

        if (lastId != null) {
            ChatHistory cursor = this.getById(lastId);
            ThrowUtils.throwIf(cursor == null || !appId.equals(cursor.getAppId()),
                    ErrorCode.PARAMS_ERROR, "历史消息游标无效");
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId)
                .lt("id", lastId)
                .orderBy("createTime", false)
                .orderBy("id", false);
        Page<ChatHistory> page = this.page(Page.of(1, APP_HISTORY_PAGE_SIZE + 1), queryWrapper);
        List<ChatHistory> records = new ArrayList<>(page.getRecords());
        boolean hasMore = records.size() > APP_HISTORY_PAGE_SIZE;
        if (hasMore) {
            records.remove(records.size() - 1);
        }
        Collections.reverse(records);

        List<ChatHistoryVO> historyVOList = records.stream()
                .map(this::getChatHistoryVO)
                .toList();
        ChatHistoryPageVO result = new ChatHistoryPageVO();
        result.setRecords(historyVOList);
        result.setHasMore(hasMore);
        result.setNextCursor(hasMore && !records.isEmpty() ? records.getFirst().getId() : null);
        return result;
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryAdminQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        String messageType = queryRequest.getMessageType();
        ThrowUtils.throwIf(StrUtil.isNotBlank(messageType)
                        && ChatHistoryMessageTypeEnum.getEnumByValue(messageType) == null,
                ErrorCode.PARAMS_ERROR, "消息类型无效");
        return QueryWrapper.create()
                .eq("appId", queryRequest.getAppId())
                .eq("userId", queryRequest.getUserId())
                .eq("messageType", messageType)
                .orderBy("createTime", false)
                .orderBy("id", false);
    }

    @Override
    public Page<ChatHistoryVO> getChatHistoryVOPage(Page<ChatHistory> chatHistoryPage) {
        Page<ChatHistoryVO> chatHistoryVOPage = new Page<>(
                chatHistoryPage.getPageNumber(),
                chatHistoryPage.getPageSize(),
                chatHistoryPage.getTotalRow()
        );
        List<ChatHistory> records = chatHistoryPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            chatHistoryVOPage.setRecords(new ArrayList<>());
            return chatHistoryVOPage;
        }
        chatHistoryVOPage.setRecords(records.stream().map(this::getChatHistoryVO).toList());
        return chatHistoryVOPage;
    }

    @Override
    public boolean removeByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0,
                ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        return this.remove(QueryWrapper.create().eq("appId", appId));
    }

    private ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory) {
        if (chatHistory == null) {
            return null;
        }
        ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
        BeanUtil.copyProperties(chatHistory, chatHistoryVO);
        return chatHistoryVO;
    }




    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }







}
