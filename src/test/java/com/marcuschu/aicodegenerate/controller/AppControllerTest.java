package com.marcuschu.aicodegenerate.controller;

import com.marcuschu.aicodegenerate.common.BaseResponse;
import com.marcuschu.aicodegenerate.exception.BusinessException;
import com.marcuschu.aicodegenerate.exception.ErrorCode;
import com.marcuschu.aicodegenerate.model.dto.app.AppAddRequest;
import com.marcuschu.aicodegenerate.model.dto.app.AppQueryRequest;
import com.marcuschu.aicodegenerate.model.dto.app.AppUpdateRequest;
import com.marcuschu.aicodegenerate.model.entity.App;
import com.marcuschu.aicodegenerate.model.entity.User;
import com.marcuschu.aicodegenerate.service.AppService;
import com.marcuschu.aicodegenerate.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppControllerTest {

    @Mock
    private AppService appService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AppController appController;

    @Test
    void addAppShouldBindCurrentUserAndInitDefaults() {
        User loginUser = User.builder().id(100L).build();
        AppAddRequest addRequest = new AppAddRequest();
        addRequest.setInitPrompt("创建一个任务记录网站");
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(appService.save(any(App.class))).thenAnswer(invocation -> {
            App app = invocation.getArgument(0);
            app.setId(200L);
            return true;
        });

        BaseResponse<Long> response = appController.addApp(addRequest, request);

        ArgumentCaptor<App> appCaptor = ArgumentCaptor.forClass(App.class);
        verify(appService).save(appCaptor.capture());
        App savedApp = appCaptor.getValue();
        assertEquals(200L, response.getData());
        assertEquals(100L, savedApp.getUserId());
        assertEquals("未命名应用", savedApp.getAppName());
        assertEquals(0, savedApp.getPriority());
    }

    @Test
    void updateAppShouldRejectNonOwner() {
        User loginUser = User.builder().id(100L).build();
        App oldApp = App.builder().id(1L).userId(999L).build();
        AppUpdateRequest updateRequest = new AppUpdateRequest();
        updateRequest.setId(1L);
        updateRequest.setAppName("新名称");
        when(userService.getLoginUser(request)).thenReturn(loginUser);
        when(appService.getById(1L)).thenReturn(oldApp);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> appController.updateApp(updateRequest, request)
        );

        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
        verify(appService, never()).updateById(any(App.class));
    }

    @Test
    void listMyAppsShouldRejectMoreThanTwentyItemsPerPage() {
        AppQueryRequest queryRequest = new AppQueryRequest();
        queryRequest.setPageNum(1);
        queryRequest.setPageSize(21);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> appController.listMyAppVOByPage(queryRequest, request)
        );

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        verify(userService, never()).getLoginUser(request);
    }
}
