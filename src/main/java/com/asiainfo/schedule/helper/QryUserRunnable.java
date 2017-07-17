package com.asiainfo.schedule.helper;

import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.module.build.service.impl.GitlibUserService;
import com.asiainfo.comm.module.models.AdUserRoleRel;
import com.asiainfo.comm.module.models.functionModels.GitLabUser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YangRY
 * 2016/7/13 0013.
 */
public class QryUserRunnable implements Runnable {
    int runType;
    Log log = LogFactory.getLog(QryUserRunnable.class);
    GitlibUserService gitlabUserService;
    Map<String, String> params;

    public QryUserRunnable(GitlibUserService gitlabUserService, Map<String, String> params, int runType) {
        this.gitlabUserService = gitlabUserService;
        this.params = params;
        this.runType = runType;
    }

    public QryUserRunnable(GitlibUserService gitlabUserService, int runType) {
        this.gitlabUserService = gitlabUserService;
        this.runType = runType;
    }

    @Override
    public void run() {
        switch (runType) {
            case 1:
                if (params == null) {
                    params = new HashMap<>();
                }
                params.put("per_page", "100");
                if (params.containsKey("search") && params.get("search") != null && StringUtils.isNotEmpty(params.get("search"))) {
                    long size = gitlabUserService.getAllUsersNumber(params);
                    CommConstants.Role.USER_SEARCH_TOTAL.put(params.get("search"), size);
                    log.error("更新条件查询总条数：" + size + "\n搜索条件：" + params.get("search"));
                } else {
                    Map<String, GitLabUser> userMap = gitlabUserService.getAllUsersMap(params);
                    CommConstants.Role.SET_GIT_USERS(userMap);
//                    CommConstants.Role.GIT_USERS = userMap;
                    log.error("更新非条件查询用户信息");
                }
                break;
            case 2:
                List<AdUserRoleRel> relList = gitlabUserService.qryAllRoleRel();
                if (relList != null && relList.size() > 0) {
                    for (AdUserRoleRel roleRel : relList) {
                        CommConstants.Role.USER_ROLE.put(roleRel.getUserName()
                            , roleRel.getAdRole());
                    }
                    log.error("保存角色信息到本地，总条数：" + relList.size());
                }
                break;
        }
        if (CommConstants.Role.THREAD_POOL > 0) {
            CommConstants.Role.CHANGE_THREAD_POOL(-1);
//            CommConstants.Role.THREAD_POOL--;
        }
    }
}
