package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.pojoMaster.AdUserPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.IntegerPojo;
import com.asiainfo.comm.module.build.dao.impl.AdUserDAO;
import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.asiainfo.comm.module.models.AdUser;
import com.asiainfo.comm.module.role.dao.impl.AdUserRoleRelDAO;
import com.asiainfo.util.DateConvertUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zhenghp on 2016/8/9.
 */
@Component
public class AdUserImpl {
    @Autowired
    AdUserDAO userDAO;
    @Autowired
    AdParaDetailDAO bsParaDetailDAO;
    @Autowired
    AdUserRoleRelDAO adUserRoleRelDAO;


    public AdUser qryById(Long userId) {
        return userDAO.getUserById(userId);
    }

    /**
     * 查询或创建用户信息
     *
     * @param LoginName
     * @param name
     * @return
     */
    public AdUser qryOrCreateUser(String LoginName, String name) {
        List<AdUser> adUsers = userDAO.qryUserByLoginName(LoginName);
        AdUser adUser;
        if (CollectionUtils.isEmpty(adUsers)) {
            adUser = userDAO.create(LoginName, name);
            loginSwitch(LoginName);
            return adUser;
        }
        changeDisplayName(adUsers.get(0), name);
        if (adUsers.get(0).getFirstLogin() != null && 1 == adUsers.get(0).getFirstLogin()) {
            userDAO.updateUserFirst(0, adUsers.get(0).getUserId());
        }
        return adUsers.get(0);
    }

    public void loginSwitch(String loginName) {
        String regionId = "X";
        String paraType = "USER_ROLE";
        String paraCode = "用户角色";
        String para1 = bsParaDetailDAO.qryByDetails(regionId, paraType, paraCode).get(0).getPara1();
        if ("1".equals(para1)) {
            String roleId = "12";
            adUserRoleRelDAO.saveUserRoleRel(roleId, loginName);
        }
    }

    /**
     * 修改用户的展示名
     *
     * @param adUser
     * @param name
     */
    private void changeDisplayName(AdUser adUser, String name) {
        if (StringUtils.isEmpty(adUser.getDisplayName()) || !adUser.getDisplayName().equals(name)) {
            adUser.setDisplayName(name);
            userDAO.update(adUser);
        }
    }

    public long countUsersCount() {
        return userDAO.countUsers();
    }

    public long countLastMonth() {
        return userDAO.countUserCreateDate(DateConvertUtils.getStartTimeInMonth());
    }

    public AdUserPojo qryStageLogUser(String projectId) {
        AdUserPojo pojo = new AdUserPojo();
        pojo.setAdUsers(userDAO.qryUserSqlRowByProject(projectId));
        return pojo;
    }

    public AdUser qryByName(String userName) {
        List<AdUser> userList = userDAO.qryUserByLoginName(userName);
        if (userList != null && userList.size() > 0) {
            return userList.get(0);
        } else {
            return null;
        }
    }

    public void updateUser(AdUser user) {
        userDAO.update(user);
    }

    public IntegerPojo qryOnlineNumber(int invalidTime) {
        IntegerPojo poj = new IntegerPojo();
        poj.setNumber(userDAO.qryOnlineNumber(invalidTime));
        return poj;
    }

}
