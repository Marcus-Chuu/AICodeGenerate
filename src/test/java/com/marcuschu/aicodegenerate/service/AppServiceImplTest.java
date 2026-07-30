package com.marcuschu.aicodegenerate.service;

import com.marcuschu.aicodegenerate.ai.model.core.AiCodeGeneratorFacade;
import com.marcuschu.aicodegenerate.ai.model.enums.CodeGenTypeEnum;
import com.marcuschu.aicodegenerate.exception.BusinessException;
import com.marcuschu.aicodegenerate.exception.ErrorCode;
import com.marcuschu.aicodegenerate.model.entity.App;
import com.marcuschu.aicodegenerate.model.entity.ChatHistory;
import com.marcuschu.aicodegenerate.model.entity.User;
import com.marcuschu.aicodegenerate.model.enums.ChatHistoryMessageTypeEnum;
import com.marcuschu.aicodegenerate.model.vo.AppVO;
import com.marcuschu.aicodegenerate.model.vo.UserVO;
import com.marcuschu.aicodegenerate.service.impl.AppServiceImpl;
import com.mybatisflex.core.paginate.Page;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppServiceImplTest {

    private final AppServiceImpl appService = new AppServiceImpl();

    private final UserService userService = mock(UserService.class);

    AppServiceImplTest() {
        ReflectionTestUtils.setField(appService, "userService", userService);
    }

    @Test
    void validAppShouldRequireInitPromptWhenAdding() {
        App app = new App();

        BusinessException exception = assertThrows(
                BusinessException.class, () -> appService.validApp(app, true)
        );

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void validAppShouldAcceptValidCreateData() {
        App app = App.builder()
                .appName("测试应用")
                .initPrompt("创建一个任务记录网站")
                .priority(0)
                .build();

        assertDoesNotThrow(() -> appService.validApp(app, true));
    }

    @Test
    void validAppShouldRejectNegativePriority() {
        App app = App.builder().priority(-1).build();

        BusinessException exception = assertThrows(
                BusinessException.class, () -> appService.validApp(app, false)
        );

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getAppVOPageShouldConvertRecordsAndHideLogicDeleteField() {
        App app = App.builder()
                .id(1L)
                .appName("测试应用")
                .userId(2L)
                .isDelete(0)
                .build();
        Page<App> appPage = new Page<>(1, 10, 1);
        appPage.setRecords(List.of(app));
        User creator = User.builder().id(2L).userName("创建者").build();
        UserVO creatorVO = new UserVO();
        creatorVO.setId(2L);
        creatorVO.setUserName("创建者");
        when(userService.listByIds(anyCollection())).thenReturn(List.of(creator));
        when(userService.getUserVO(creator)).thenReturn(creatorVO);

        Page<AppVO> result = appService.getAppVOPage(appPage);

        assertEquals(1, result.getRecords().size());
        assertEquals("测试应用", result.getRecords().getFirst().getAppName());
        assertEquals("创建者", result.getRecords().getFirst().getUser().getUserName());
        assertFalse(Arrays.stream(AppVO.class.getDeclaredFields())
                .anyMatch(field -> "isDelete".equals(field.getName())));
    }

    @Test
    void chatToGenCodeShouldPersistUserAndCompletedAiMessages() {
        AppServiceImpl service = spy(new AppServiceImpl());
        AiCodeGeneratorFacade facade = mock(AiCodeGeneratorFacade.class);
        ChatHistoryService chatHistoryService = mock(ChatHistoryService.class);
        ReflectionTestUtils.setField(service, "aiCodeGeneratorFacade", facade);
        ReflectionTestUtils.setField(service, "chatHistoryService", chatHistoryService);
        App app = App.builder()
                .id(1L)
                .userId(2L)
                .codeGenType(CodeGenTypeEnum.MULTI_FILE.getValue())
                .build();
        User loginUser = User.builder().id(2L).build();
        ChatHistory userHistory = ChatHistory.builder().id(100L).build();
        doReturn(app).when(service).getById(1L);
        when(chatHistoryService.saveMessage(
                1L, 2L, "创建博客", ChatHistoryMessageTypeEnum.USER, null
        )).thenReturn(userHistory);
        when(facade.generateAndSaveCodeStream(
                "创建博客", CodeGenTypeEnum.MULTI_FILE, 1L
        )).thenReturn(Flux.just("代码片段一", "代码片段二"));

        List<String> chunks = service.chatToGenCode(1L, "创建博客", loginUser)
                .collectList()
                .block();

        assertEquals(List.of("代码片段一", "代码片段二"), chunks);
        verify(chatHistoryService).saveMessage(
                1L,
                2L,
                "代码片段一代码片段二",
                ChatHistoryMessageTypeEnum.AI,
                100L
        );
    }

    @Test
    void chatToGenCodeShouldPersistFailureMessageWhenAiStreamFails() {
        AppServiceImpl service = spy(new AppServiceImpl());
        AiCodeGeneratorFacade facade = mock(AiCodeGeneratorFacade.class);
        ChatHistoryService chatHistoryService = mock(ChatHistoryService.class);
        ReflectionTestUtils.setField(service, "aiCodeGeneratorFacade", facade);
        ReflectionTestUtils.setField(service, "chatHistoryService", chatHistoryService);
        App app = App.builder()
                .id(1L)
                .userId(2L)
                .codeGenType(CodeGenTypeEnum.MULTI_FILE.getValue())
                .build();
        User loginUser = User.builder().id(2L).build();
        when(chatHistoryService.saveMessage(
                1L, 2L, "创建博客", ChatHistoryMessageTypeEnum.USER, null
        )).thenReturn(ChatHistory.builder().id(100L).build());
        doReturn(app).when(service).getById(1L);
        when(facade.generateAndSaveCodeStream(
                "创建博客", CodeGenTypeEnum.MULTI_FILE, 1L
        )).thenReturn(Flux.concat(
                Flux.just("部分回复"),
                Flux.error(new RuntimeException("模型服务异常"))
        ));

        assertThrows(RuntimeException.class, () -> service.chatToGenCode(
                1L, "创建博客", loginUser
        ).collectList().block());

        verify(chatHistoryService).saveMessage(
                eq(1L),
                eq(2L),
                argThat(message -> message.contains("部分回复") && message.contains("模型服务异常")),
                eq(ChatHistoryMessageTypeEnum.AI),
                eq(100L)
        );
    }

    @Test
    void chatToGenCodeShouldPersistFailureMessageWhenAiThrowsBeforeReturningStream() {
        AppServiceImpl service = spy(new AppServiceImpl());
        AiCodeGeneratorFacade facade = mock(AiCodeGeneratorFacade.class);
        ChatHistoryService chatHistoryService = mock(ChatHistoryService.class);
        ReflectionTestUtils.setField(service, "aiCodeGeneratorFacade", facade);
        ReflectionTestUtils.setField(service, "chatHistoryService", chatHistoryService);
        App app = App.builder()
                .id(1L)
                .userId(2L)
                .codeGenType(CodeGenTypeEnum.MULTI_FILE.getValue())
                .build();
        User loginUser = User.builder().id(2L).build();
        when(chatHistoryService.saveMessage(
                1L, 2L, "创建博客", ChatHistoryMessageTypeEnum.USER, null
        )).thenReturn(ChatHistory.builder().id(100L).build());
        doReturn(app).when(service).getById(1L);
        when(facade.generateAndSaveCodeStream(
                "创建博客", CodeGenTypeEnum.MULTI_FILE, 1L
        )).thenThrow(new RuntimeException("模型初始化失败"));

        assertThrows(RuntimeException.class, () -> service.chatToGenCode(
                1L, "创建博客", loginUser
        ).collectList().block());

        verify(chatHistoryService).saveMessage(
                eq(1L),
                eq(2L),
                argThat(message -> message.contains("模型初始化失败")),
                eq(ChatHistoryMessageTypeEnum.AI),
                eq(100L)
        );
    }

    @Test
    void removeAppByIdShouldAlsoRemoveChatHistory() {
        AppServiceImpl service = spy(new AppServiceImpl());
        ChatHistoryService chatHistoryService = mock(ChatHistoryService.class);
        ReflectionTestUtils.setField(service, "chatHistoryService", chatHistoryService);
        doReturn(true).when(service).removeById(1L);

        assertTrue(service.removeAppById(1L));

        verify(chatHistoryService).removeByAppId(1L);
    }
}
