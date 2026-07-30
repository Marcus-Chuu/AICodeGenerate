package com.marcuschu.aicodegenerate.controller;

import com.marcuschu.aicodegenerate.annotation.AuthCheck;
import com.marcuschu.aicodegenerate.common.BaseResponse;
import com.marcuschu.aicodegenerate.common.ResultUtils;
import com.marcuschu.aicodegenerate.constant.UserConstant;
import com.marcuschu.aicodegenerate.exception.ErrorCode;
import com.marcuschu.aicodegenerate.exception.ThrowUtils;
import com.marcuschu.aicodegenerate.model.dto.chathistory.ChatHistoryAdminQueryRequest;
import com.marcuschu.aicodegenerate.model.dto.chathistory.ChatHistoryQueryRequest;
import com.marcuschu.aicodegenerate.model.entity.App;
import com.marcuschu.aicodegenerate.model.entity.ChatHistory;
import com.marcuschu.aicodegenerate.model.entity.User;
import com.marcuschu.aicodegenerate.model.vo.ChatHistoryPageVO;
import com.marcuschu.aicodegenerate.model.vo.ChatHistoryVO;
import com.marcuschu.aicodegenerate.service.AppService;
import com.marcuschu.aicodegenerate.service.ChatHistoryService;
import com.marcuschu.aicodegenerate.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 对话历史控制层。
 *
 * @author MarcusChu
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private AppService appService;

    @Autowired
    private UserService userService;

    /**
     * 游标分页查询某个应用的对话历史。
     * 首次不传 lastId，之后使用返回的 nextCursor 向前加载。
     * 仅应用创建者和管理员可以查看。
     */
    @PostMapping("/app/list/page/vo")
    @AuthCheck
    public BaseResponse<ChatHistoryPageVO> listAppChatHistory(
            @RequestBody ChatHistoryQueryRequest queryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(queryRequest == null || queryRequest.getAppId() == null
                        || queryRequest.getAppId() <= 0,
                ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(queryRequest.getLastId() != null && queryRequest.getLastId() <= 0,
                ErrorCode.PARAMS_ERROR, "历史消息游标无效");

        User loginUser = userService.getLoginUser(request);
        App app = appService.getById(queryRequest.getAppId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!isAdmin && !Objects.equals(app.getUserId(), loginUser.getId()),
                ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");

        return ResultUtils.success(chatHistoryService.listAppChatHistory(
                queryRequest.getAppId(), queryRequest.getLastId()
        ));
    }

    /**
     * 管理员分页查询所有应用的对话历史，按照创建时间降序排列。
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistoryVO>> listChatHistoryByPageByAdmin(
            @RequestBody ChatHistoryAdminQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(queryRequest.getPageNum() <= 0 || queryRequest.getPageSize() <= 0,
                ErrorCode.PARAMS_ERROR);

        Page<ChatHistory> chatHistoryPage = chatHistoryService.page(
                Page.of(queryRequest.getPageNum(), queryRequest.getPageSize()),
                chatHistoryService.getQueryWrapper(queryRequest)
        );
        return ResultUtils.success(chatHistoryService.getChatHistoryVOPage(chatHistoryPage));
    }
}
