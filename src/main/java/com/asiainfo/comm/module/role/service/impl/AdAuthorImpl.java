package com.asiainfo.comm.module.role.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.AuthorPojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.CommonPojo;
import com.asiainfo.comm.module.role.dao.impl.AdAuthorDAO;
import com.asiainfo.comm.module.role.dao.impl.AdRoleDAO;
import com.avaje.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YangRY
 * 2016/7/11 0011.
 */
@Component
public class AdAuthorImpl {
    @Autowired
    AdAuthorDAO adAuthorDAO;
    @Autowired
    AdRoleDAO roleDAO;

    //获取工程名称和勾选状态
    public List<AuthorPojoExt> qryAuthorAndSignCheck(long roleId) {
        List<SqlRow> rows = adAuthorDAO.qryAuthorAndSignCheck(roleId);
        ArrayList<AuthorPojoExt> authors = new ArrayList<>();
        if (rows != null && rows.size() > 0) {
            for (SqlRow row : rows) {
                AuthorPojoExt poj = new AuthorPojoExt();
                poj.setProjectName(row.getString("PROJECT_NAME"));
                poj.setProjectId(row.getLong("PROJECT_ID"));
                poj.setIsChecked(row.getInteger("IS_CHECKED"));
                authors.add(poj);
            }
        }
        return authors;
    }

    public CommonPojo changeRoleAuthor(long roleId, String projectIds) {
        return adAuthorDAO.changeRoleAuthor(roleId, projectIds);
    }
}
