package com.marcuschu.aicodegenerate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.marcuschu.aicodegenerate.ai.model.core.AiCodeGeneratorFacade;
import com.marcuschu.aicodegenerate.ai.model.enums.CodeGenTypeEnum;
import com.marcuschu.aicodegenerate.constant.AppConstant;
import com.marcuschu.aicodegenerate.exception.BusinessException;
import com.marcuschu.aicodegenerate.exception.ErrorCode;
import com.marcuschu.aicodegenerate.exception.ThrowUtils;
import com.marcuschu.aicodegenerate.mapper.AppMapper;
import com.marcuschu.aicodegenerate.model.dto.app.AppQueryRequest;
import com.marcuschu.aicodegenerate.model.entity.App;
import com.marcuschu.aicodegenerate.model.entity.User;
import com.marcuschu.aicodegenerate.model.vo.AppVO;
import com.marcuschu.aicodegenerate.service.AppService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 应用 服务层实现。
 *
 * @author MarcusChu
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService {

    private static final int MAX_APP_NAME_LENGTH = 256;

    private static final int MAX_COVER_LENGTH = 512;

    private static final int MAX_INIT_PROMPT_LENGTH = 10000;


    /**
     * AI 生成门面
     */
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    private static final Set<String> SORT_FIELD_WHITELIST = Set.of(
            "id", "appName", "codeGenType", "deployKey", "priority", "userId",
            "deployedTime", "editTime", "createTime", "updateTime"
    );

    @Override
    public void validApp(App app, boolean add) {
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        String appName = app.getAppName();
        String cover = app.getCover();
        String initPrompt = app.getInitPrompt();
        Integer priority = app.getPriority();

        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化提示词不能为空");
        }
        if (appName != null) {
            ThrowUtils.throwIf(StrUtil.isBlank(appName), ErrorCode.PARAMS_ERROR, "应用名称不能为空");
            ThrowUtils.throwIf(appName.length() > MAX_APP_NAME_LENGTH,
                    ErrorCode.PARAMS_ERROR, "应用名称过长");
        }
        if (cover != null) {
            ThrowUtils.throwIf(cover.length() > MAX_COVER_LENGTH,
                    ErrorCode.PARAMS_ERROR, "应用封面地址过长");
        }
        if (initPrompt != null) {
            ThrowUtils.throwIf(initPrompt.length() > MAX_INIT_PROMPT_LENGTH,
                    ErrorCode.PARAMS_ERROR, "初始化提示词过长");
        }
        if (priority != null) {
            ThrowUtils.throwIf(priority < 0, ErrorCode.PARAMS_ERROR, "优先级不能小于 0");
        }
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        String safeSortField = SORT_FIELD_WHITELIST.contains(sortField) ? sortField : "createTime";

        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(safeSortField, "ascend".equals(sortOrder));
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        return appVO;
    }

    @Override
    public Page<AppVO> getAppVOPage(Page<App> appPage) {
        List<App> appList = appPage.getRecords();
        Page<AppVO> appVOPage = new Page<>(
                appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow()
        );
        if (CollUtil.isEmpty(appList)) {
            appVOPage.setRecords(new ArrayList<>());
            return appVOPage;
        }
        appVOPage.setRecords(appList.stream().map(this::getAppVO).toList());
        return appVOPage;
    }



    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 调用 AI 生成代码
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }


    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 8. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 9. 返回可访问的 URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }


}
