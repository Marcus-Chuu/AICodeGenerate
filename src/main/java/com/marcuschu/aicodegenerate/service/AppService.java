package com.marcuschu.aicodegenerate.service;

import com.marcuschu.aicodegenerate.model.dto.app.AppQueryRequest;
import com.marcuschu.aicodegenerate.model.entity.App;
import com.marcuschu.aicodegenerate.model.entity.User;
import com.marcuschu.aicodegenerate.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

/**
 * 应用 服务层。
 *
 * @author MarcusChu
 */
public interface AppService extends IService<App> {

    /**
     * 校验应用数据
     *
     * @param app 应用
     * @param add 是否为创建校验
     */
    void validApp(App app, boolean add);

    /**
     * 获取应用查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用视图
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用分页视图
     */
    Page<AppVO> getAppVOPage(Page<App> appPage);

    /**
     * 删除应用并清理该应用的全部对话历史。
     */
    boolean removeAppById(Long appId);


    /**
     * 对话生成应用
     * @param appId 应用 ID
     * @param message
     * @param loginUser
     * @return Flux<String>
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);


    /**
     * 部署服务
     * @param appId 应用 ID
     * @param loginUser 登录用户
     * @return String
     */
    String deployApp(Long appId, User loginUser);
}
