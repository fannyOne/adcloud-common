package com.asiainfo.schedule.key;

import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.module.build.service.impl.GitlibUserService;
import com.asiainfo.comm.module.models.AdUserRoleRel;
import com.asiainfo.comm.module.models.functionModels.GitLabUser;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YangRY
 * 2016/7/13 0013.
 */
@lombok.extern.slf4j.Slf4j
@Component
public class UserRoleMaps {
    @Autowired
    AdUserRoleRelImpl adUserRoleRelImpl;

    @Autowired
    GitlibUserService gitlabUserService;

    @Scheduled(fixedDelay = 3600000)//每小时更新一次用户内容
    public void setUserRoleMap() {
        List<AdUserRoleRel> relList = adUserRoleRelImpl.qryAll();
        for (AdUserRoleRel userRoleRel : relList) {
            CommConstants.Role.USER_ROLE.put(userRoleRel.getUserName(), userRoleRel.getAdRole());
        }
        Map<String, String> params = new HashMap<>();
        params.put("per_page", "100");
        Map<String, GitLabUser> userMap = gitlabUserService.getAllUsersMap(params);
        CommConstants.Role.SET_GIT_USERS(userMap);
        log.error("更新非条件查询用户信息");
    }

}
