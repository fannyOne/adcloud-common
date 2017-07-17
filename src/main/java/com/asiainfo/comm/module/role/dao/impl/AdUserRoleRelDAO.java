package com.asiainfo.comm.module.role.dao.impl;

import com.asiainfo.comm.common.pojo.pojoMaster.CommonPojo;
import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.module.models.AdRole;
import com.asiainfo.comm.module.models.AdUserRoleRel;
import com.asiainfo.comm.module.models.query.QAdUserRoleRel;
import com.asiainfo.comm.module.role.service.impl.AdRoleImpl;
import com.avaje.ebean.PagedList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by YangRY
 * 2016/7/12 0012.
 */
@Component
public class AdUserRoleRelDAO {
    @Autowired
    AdRoleImpl roleImpl;

    @Autowired
    AdGroupAdminUserDAO adGroupAdminUserDAO;

    public List<AdUserRoleRel> qryByUser(String opName) {
        return new QAdUserRoleRel().userName.eq(opName).state.eq(1).findList();
    }

    public List<AdUserRoleRel> qryAll() {
        return new QAdUserRoleRel().state.eq(1).findList();
    }

    public CommonPojo qryByUsers(String roleId, String usersName) {
        CommonPojo poj = new CommonPojo();
        if (roleId != null && StringUtils.isNotEmpty(roleId)) {
            AdRole role = roleImpl.qryById(Long.parseLong(roleId));
            QAdUserRoleRel relQ = new QAdUserRoleRel().state.eq(1);
            HashSet<String> usersNameSet = new HashSet<>();
            if (usersName != null && StringUtils.isNotEmpty(usersName)) {
                relQ = relQ.or();
                String[] usersNameList = usersName.split(",");
                for (String userName : usersNameList) {
                    usersNameSet.add(userName);
                    relQ = relQ.userName.eq(userName);
                }
                relQ = relQ.endOr();
            }
            List<AdUserRoleRel> relList = relQ.findList();
            /**
             * 对于已经存在的数据进行更新
             */
            relList.stream().filter(adUserRoleRel ->
                usersNameSet.contains(adUserRoleRel.getUserName()))
                .forEach(adUserRoleRel -> {
                    if (adUserRoleRel.getAdRole().getRoleLevel() == 1 && role.getRoleLevel() != 1) {
                        adGroupAdminUserDAO.del(adUserRoleRel.getUserName());
                    }
                    adUserRoleRel.setAdRole(role);
                    adUserRoleRel.save();
                    CommConstants.Role.USER_ROLE.put(adUserRoleRel.getUserName(), role);
                    usersNameSet.remove(adUserRoleRel.getUserName());
                });
            for (String userName : usersNameSet) {
                AdUserRoleRel rel = new AdUserRoleRel();
                rel.setAdRole(role);
                rel.setCreateDate(new Date());
                rel.setState(1);
                rel.setUserName(userName);
                rel.save();

                CommConstants.Role.USER_ROLE.put(userName, role);
            }
        } else {
            poj.setRetMessage("找不到该角色，角色Id：" + roleId);
        }
        return poj;
    }

    public void saveUserRoleRel(String roleId, String userName) {
        AdUserRoleRel userRoleRel = new AdUserRoleRel();
        AdRole role = roleImpl.qryById(Long.parseLong(roleId));
        userRoleRel.setAdRole(role);
        userRoleRel.setUserName(userName);
        userRoleRel.setState(1);
        userRoleRel.setCreateDate(new Date());
        userRoleRel.save();
        CommConstants.Role.USER_ROLE.put(userName, role);
    }

    public PagedList<AdUserRoleRel> qryRelByCond(long roleId, String userName, int page, int per_page) {
        QAdUserRoleRel qRel = new QAdUserRoleRel().adRole.roleId.eq(roleId)
            .state.eq(1);
        if (StringUtils.isNotEmpty(userName)) {
            qRel = qRel.userName.like("%" + userName + "%");
        }
        return qRel.orderBy(" USER_NAME ASC").findPagedList(page, per_page);
    }
}
