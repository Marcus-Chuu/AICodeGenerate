package com.marcuschu.aicodegenerate.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.marcuschu.aicodegenerate.ai.model.enums.CodeGenTypeEnum;
import com.marcuschu.aicodegenerate.annotation.AuthCheck;
import com.marcuschu.aicodegenerate.common.BaseResponse;
import com.marcuschu.aicodegenerate.common.DeleteRequest;
import com.marcuschu.aicodegenerate.common.ResultUtils;
import com.marcuschu.aicodegenerate.constant.AppConstant;
import com.marcuschu.aicodegenerate.constant.UserConstant;
import com.marcuschu.aicodegenerate.exception.ErrorCode;
import com.marcuschu.aicodegenerate.exception.ThrowUtils;
import com.marcuschu.aicodegenerate.model.dto.app.*;
import com.marcuschu.aicodegenerate.model.entity.App;
import com.marcuschu.aicodegenerate.model.entity.User;
import com.marcuschu.aicodegenerate.model.vo.AppVO;
import com.marcuschu.aicodegenerate.service.AppService;
import com.marcuschu.aicodegenerate.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 应用控制层
 *
 * @author MarcusChu
 */
@RestController
@RequestMapping("/app")
public class AppController {

    private static final int MAX_USER_PAGE_SIZE = 20;

    private static final String DEFAULT_APP_NAME = "未命名应用";

    @Autowired
    private AppService appService;

    @Autowired
    private UserService userService;

    /**
     * 用户创建应用
     */
    @PostMapping("/add")
    @AuthCheck
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest,
                                     HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setAppName(DEFAULT_APP_NAME);
        app.setPriority(0);
        app.setUserId(loginUser.getId());
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        appService.validApp(app, true);
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }

    /**
     * 用户修改自己的应用，目前仅支持修改应用名称
     */
    @PostMapping("/update")
    @AuthCheck
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest,
                                           HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null
                || appUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(appUpdateRequest.getAppName()),
                ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        User loginUser = userService.getLoginUser(request);
        App oldApp = getExistingApp(appUpdateRequest.getId());
        checkOwner(oldApp, loginUser);

        App app = new App();
        BeanUtil.copyProperties(appUpdateRequest, app);
        app.setEditTime(LocalDateTime.now());
        appService.validApp(app, false);
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用户删除自己的应用
     */
    @PostMapping("/delete")
    @AuthCheck
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest,
                                           HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null
                || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App oldApp = getExistingApp(deleteRequest.getId());
        checkOwner(oldApp, loginUser);
        boolean result = appService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用户根据 id 查看应用详情
     */
    @GetMapping("/get/vo")
    @AuthCheck
    public BaseResponse<AppVO> getAppVOById(@RequestParam("id") long id,
                                            HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        userService.getLoginUser(request);
        App app = getExistingApp(id);
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 用户分页查询自己的应用
     */
    @PostMapping("/my/list/page/vo")
    @AuthCheck
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(
            @RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        checkUserPageRequest(appQueryRequest);
        User loginUser = userService.getLoginUser(request);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", loginUser.getId())
                .like("appName", appQueryRequest.getAppName())
                .orderBy("createTime", false);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(appService.getAppVOPage(appPage));
    }

    /**
     * 用户分页查询精选应用，priority 大于 0 的应用为精选应用
     */
    @PostMapping("/good/list/page/vo")
    @AuthCheck
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(
            @RequestBody AppQueryRequest appQueryRequest) {
        checkUserPageRequest(appQueryRequest);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .gt("priority", AppConstant.GOOD_APP_PRIORITY)
                .like("appName", appQueryRequest.getAppName())
                .orderBy("priority", false)
                .orderBy("createTime", false);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(appService.getAppVOPage(appPage));
    }

    /**
     * 管理员删除任意应用
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null
                || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        getExistingApp(deleteRequest.getId());
        boolean result = appService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员更新任意应用
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(
            @RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null
                || appAdminUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        getExistingApp(appAdminUpdateRequest.getId());
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        app.setEditTime(LocalDateTime.now());
        appService.validApp(app, false);
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页查询应用列表，每页数量不限
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(
            @RequestBody AppQueryRequest appQueryRequest) {
        checkAdminPageRequest(appQueryRequest);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        Page<App> appPage = appService.page(
                Page.of(pageNum, pageSize), appService.getQueryWrapper(appQueryRequest)
        );
        return ResultUtils.success(appService.getAppVOPage(appPage));
    }

    /**
     * 管理员根据 id 查看任意应用详情
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getAppVOByIdByAdmin(@RequestParam("id") long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(appService.getAppVO(getExistingApp(id)));
    }

    private App getExistingApp(long id) {
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return app;
    }

    private void checkOwner(App app, User loginUser) {
        ThrowUtils.throwIf(!Objects.equals(app.getUserId(), loginUser.getId()),
                ErrorCode.NO_AUTH_ERROR);
    }

    private void checkUserPageRequest(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageNum = appQueryRequest.getPageNum();
        int pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0 || pageSize > MAX_USER_PAGE_SIZE,
                ErrorCode.PARAMS_ERROR, "每页最多查询 20 条应用");
    }

    private void checkAdminPageRequest(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(appQueryRequest.getPageNum() <= 0 || appQueryRequest.getPageSize() <= 0,
                ErrorCode.PARAMS_ERROR);
    }



    /**
     * 应用聊天生成代码（流式 SSE）
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param request 请求对象
     * @return 生成结果流
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务生成代码（流式）
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        // 转换为 ServerSentEvent 格式
        return contentFlux
                .map(chunk -> {
                    // 将内容包装成JSON对象
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(
                        // 发送结束事件
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }



    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }



}
