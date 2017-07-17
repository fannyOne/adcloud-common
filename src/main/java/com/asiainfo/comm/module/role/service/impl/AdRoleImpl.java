package com.asiainfo.comm.module.role.service.impl;

import com.asiainfo.comm.module.models.AdRole;
import com.asiainfo.comm.module.models.query.QAdRole;
import com.asiainfo.comm.module.role.dao.impl.AdRoleDAO;
import com.avaje.ebean.PagedList;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by YangRY
 * 2016/7/11 0011.
 */
@Component
public class AdRoleImpl {
    @Autowired
    AdRoleDAO roleDAO;

    public List<AdRole> qryAll() {
        return roleDAO.qryAll();
    }

    public PagedList<AdRole> qryPages(Map params) {
        int pageSize = 10;
        String page = (String) params.get("page");
        String pageSizeStr = (String) params.get("pageSize");
        if (StringUtils.isNotEmpty(pageSizeStr)) {
            pageSize = Integer.parseInt(pageSizeStr);
        }
        if (StringUtils.isNotEmpty(page)) {
            return roleDAO.qryPages(Integer.parseInt(page) - 1, pageSize);
        } else {
            return null;
        }
    }

    public int qryCountByName(String roleName) {
        return roleDAO.qryCountByName(roleName);
    }

    public AdRole qryById(long roleId) {
        List<AdRole> roles = new QAdRole().roleId.eq(roleId).state.eq(1).findList();
        if (roles != null && roles.size() > 0) {
            return roles.get(0);
        } else {
            return null;
        }
    }

}
