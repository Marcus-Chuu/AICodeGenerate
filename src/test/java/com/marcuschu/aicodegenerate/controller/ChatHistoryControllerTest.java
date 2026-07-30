package com.marcuschu.aicodegenerate.controller;

import com.marcuschu.aicodegenerate.common.BaseResponse;
import com.marcuschu.aicodegenerate.exception.BusinessException;
import com.marcuschu.aicodegenerate.exception.ErrorCode;
import com.marcuschu.aicodegenerate.model.dto.chathistory.ChatHistoryQueryRequest;
import com.marcuschu.aicodegenerate.model.entity.App;
import com.marcuschu.aicodegenerate.model.entity.User;
import com.marcuschu.aicodegenerate.model.vo.ChatHistoryPageVO;
import com.marcuschu.aicodegenerate.service.AppService;
import com.marcuschu.aicodegenerate.service.ChatHistoryService;
import com.marcuschu.aicodegenerate.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatHistoryControllerTest {

    @Mock
    private ChatHistoryService chatHistoryService;

    @Mock
    private AppService appService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ChatHistoryController controller;

    @Test
    void ownerShouldBeAbleToReadApplicationHistory() {
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(1L);
        User loginUser = User.builder().id(2L).userRole("user").build();
        ChatHistoryPageVO pageVO = new ChatHistoryPageVO();
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(appService.getById(1L)).thenReturn(App.builder().id(1L).userId(2L).build());
        when(chatHistoryService.listAppChatHistory(1L, null)).thenReturn(pageVO);

        BaseResponse<ChatHistoryPageVO> response =
                controller.listAppChatHistory(queryRequest, request);

        assertSame(pageVO, response.getData());
    }

    @Test
    void nonOwnerShouldNotBeAbleToReadApplicationHistory() {
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(1L);
        User loginUser = User.builder().id(3L).userRole("user").build();
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(appService.getById(1L)).thenReturn(App.builder().id(1L).userId(2L).build());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> controller.listAppChatHistory(queryRequest, request)
        );

        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
        verify(chatHistoryService, never()).listAppChatHistory(1L, null);
    }

    @Test
    void adminShouldBeAbleToReadAnyApplicationHistory() {
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(1L);
        User loginUser = User.builder().id(3L).userRole("admin").build();
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(appService.getById(1L)).thenReturn(App.builder().id(1L).userId(2L).build());
        when(chatHistoryService.listAppChatHistory(1L, null))
                .thenReturn(new ChatHistoryPageVO());

        controller.listAppChatHistory(queryRequest, request);

        verify(chatHistoryService).listAppChatHistory(1L, null);
    }
}
