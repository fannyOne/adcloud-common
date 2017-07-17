package com.asiainfo.comm.module.role.controller;

import com.asiainfo.comm.common.pojo.BooleanPojo;
import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.GroupAdminPojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.AdGroupAdminUserPojo;
import com.asiainfo.comm.module.build.service.impl.AdGroupImpl;
import com.asiainfo.comm.module.models.AdGroup;
import com.asiainfo.comm.module.models.AdGroupAdminUser;
import com.asiainfo.comm.module.role.service.impl.AdGroupAdminUserImpl;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by zhenghp on 2016/12/20.
 */
@lombok.extern.slf4j.Slf4j
@RestController
@RequestMapping(value = "/groupAdminUser")
public class GroupAdminUserController extends BaseController {
    @Autowired
    AdGroupImpl adGroupImpl;
    @Autowired
    AdGroupAdminUserImpl adGroupAdminUserImpl;

    @RequestMapping(value = "/qryGroupAdminRelatGroup", produces = "application/json")
    public Pojo qryGroupAdminRelatGroup(String userName) {
        AdGroupAdminUserPojo pojo = new AdGroupAdminUserPojo();
        List<GroupAdminPojoExt> pojos = Lists.newArrayList();
        List<AdGroupAdminUser> adminUsers = adGroupAdminUserImpl.qryByUsername(userName);
        List<AdGroup> adGroups = adGroupImpl.qryAllGroup();
        if (CollectionUtils.isNotEmpty(adGroups)) {
            for (AdGroup group : adGroups) {
                GroupAdminPojoExt pojoExt = new GroupAdminPojoExt();
                pojoExt.setGroupId(group.getGroupId());
                pojoExt.setGroupName(group.getGroupName());
                if (CollectionUtils.isNotEmpty(adminUsers)) {
                    for (AdGroupAdminUser adminUser : adminUsers) {
                        if (pojoExt.getGroupId().equals(adminUser.getAdGroup().getGroupId())) {
                            pojoExt.setIsChecked(1);
                        }
                    }
                }
                pojos.add(pojoExt);
            }
        }
        pojo.setGroupAdminPojoExt(pojos);
        return pojo;
    }

    @RequestMapping(value = "/saveGroupAdminRelatGroup", produces = "application/json")
    public Pojo saveGroupAdminRelatGroup(String userName, String groupIds, HttpServletRequest request) {
        BooleanPojo pojo = new BooleanPojo();
        adGroupAdminUserImpl.update(userName, groupIds, getUserId(request));
        return pojo;
    }
}
