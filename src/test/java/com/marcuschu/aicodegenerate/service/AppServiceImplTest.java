package com.marcuschu.aicodegenerate.service;

import com.marcuschu.aicodegenerate.exception.BusinessException;
import com.marcuschu.aicodegenerate.exception.ErrorCode;
import com.marcuschu.aicodegenerate.model.entity.App;
import com.marcuschu.aicodegenerate.model.vo.AppVO;
import com.marcuschu.aicodegenerate.service.impl.AppServiceImpl;
import com.mybatisflex.core.paginate.Page;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppServiceImplTest {

    private final AppServiceImpl appService = new AppServiceImpl();

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

        Page<AppVO> result = appService.getAppVOPage(appPage);

        assertEquals(1, result.getRecords().size());
        assertEquals("测试应用", result.getRecords().getFirst().getAppName());
        assertFalse(Arrays.stream(AppVO.class.getDeclaredFields())
                .anyMatch(field -> "isDelete".equals(field.getName())));
    }
}
