package com.marcuschu.aicodegenerate.service;

import com.marcuschu.aicodegenerate.model.entity.ChatHistory;
import com.marcuschu.aicodegenerate.model.enums.ChatHistoryMessageTypeEnum;
import com.marcuschu.aicodegenerate.model.vo.ChatHistoryPageVO;
import com.marcuschu.aicodegenerate.service.impl.ChatHistoryServiceImpl;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class ChatHistoryServiceImplTest {

    @Test
    void saveMessageShouldPersistMessageTypeAndParentId() {
        ChatHistoryServiceImpl service = spy(new ChatHistoryServiceImpl());
        doAnswer(invocation -> {
            ChatHistory history = invocation.getArgument(0);
            history.setId(100L);
            return true;
        }).when(service).save(any(ChatHistory.class));

        ChatHistory result = service.saveMessage(
                1L, 2L, "AI 回复", ChatHistoryMessageTypeEnum.AI, 99L
        );

        assertEquals(100L, result.getId());
        assertEquals("ai", result.getMessageType());
        assertEquals(99L, result.getParentId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listAppChatHistoryShouldReturnTenMessagesInAscendingOrder() {
        ChatHistoryServiceImpl service = spy(new ChatHistoryServiceImpl());
        List<ChatHistory> records = LongStream.iterate(20L, id -> id - 1)
                .limit(11)
                .mapToObj(id -> ChatHistory.builder()
                        .id(id)
                        .appId(1L)
                        .userId(2L)
                        .message("消息 " + id)
                        .messageType(ChatHistoryMessageTypeEnum.USER.getValue())
                        .build())
                .toList();
        Page<ChatHistory> page = new Page<>(1, 11, 11);
        page.setRecords(records);
        doReturn(page).when(service).page(any(Page.class), any(QueryWrapper.class));

        ChatHistoryPageVO result = service.listAppChatHistory(1L, null);

        assertEquals(10, result.getRecords().size());
        assertEquals(11L, result.getRecords().getFirst().getId());
        assertEquals(20L, result.getRecords().getLast().getId());
        assertEquals(11L, result.getNextCursor());
        assertTrue(result.isHasMore());
    }

    @Test
    void removeByAppIdShouldDeleteByApplicationCondition() {
        ChatHistoryServiceImpl service = spy(new ChatHistoryServiceImpl());
        doReturn(true).when(service).remove(any(QueryWrapper.class));

        assertTrue(service.removeByAppId(1L));

        verify(service).remove(any(QueryWrapper.class));
    }
}
