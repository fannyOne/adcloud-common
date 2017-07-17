package com.asiainfo.comm.module.role.dao.impl;

import com.asiainfo.comm.module.models.AdRole;
import com.asiainfo.comm.module.models.query.QAdRole;
import com.avaje.ebean.PagedList;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/11 0011.
 */
@Component
public class AdRoleDAO {
    public List<AdRole> qryAll() {
        return new QAdRole().state.eq(1).orderBy("ROLE_NAME ASC").findList();
    }

    public PagedList<AdRole> qryPages(int page, int pageSize) {
        return new QAdRole().state.eq(1).findPagedList(page, pageSize);
    }

    public AdRole qryById(long roleId) {
        List<AdRole> roles = new QAdRole().roleId.eq(roleId).findList();
        if (roles != null && roles.size() > 0) {
            return roles.get(0);
        } else {
            return null;
        }
    }

    public int qryCountByName(String roleName) {
        return new QAdRole().roleName.eq(roleName).state.eq(1).findRowCount();
    }
}
