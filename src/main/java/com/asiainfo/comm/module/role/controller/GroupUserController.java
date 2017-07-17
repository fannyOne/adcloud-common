package com.asiainfo.comm.module.role.controller;

/**
 * Created by zhenghp on 2016/8/26.
 */

import com.asiainfo.comm.common.pojo.BooleanPojo;
import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.GroupUserDelExtPojo;
import com.asiainfo.comm.common.pojo.pojoExt.GroupUserExtPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.CommonPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.GroupUserPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.UserAuthorizationPojo;
import com.asiainfo.comm.module.build.service.impl.AdBranchImpl;
import com.asiainfo.comm.module.build.service.impl.AdGroupImpl;
import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.role.service.impl.AdGroupUserImpl;
import com.asiainfo.comm.module.role.service.impl.RightManagerImpl;
import com.asiainfo.comm.module.role.service.impl.VerifyRightImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@lombok.extern.slf4j.Slf4j
@RestController
@RequestMapping(value = "/groupUser")
public class GroupUserController extends BaseController {
    @Autowired
    AdGroupUserImpl impl;
    @Autowired
    GroupUserConverter converter;
    @Autowired
    VerifyRightImpl verifyRightImpl;
    @Autowired
    AdGroupImpl adGroupImpl;
    @Autowired
    RightConverter rightConverter;
    @Autowired
    RightManagerImpl rightManager;
    @Autowired
    AdBranchImpl adBranchImpl;


    @RequestMapping(value = "/qryUsers", produces = "application/json")
    public Pojo qryUserByGroup(Long groupId) throws Exception {
        if (null == groupId) {
            throw new Exception("项目编号不能为空");
        }
        if (null == adGroupImpl.qryById(groupId)) {
            log.error("groupid:" + groupId + "没有查到对象的group信息");
            throw new Exception("groupid:" + groupId + "没有查到对象的group信息");
        }
        GroupUserPojo pojo = new GroupUserPojo();
        pojo.setGroupId(groupId);
        pojo.setMembers(converter.converter(impl.qryByGroupId(groupId)));
        return pojo;
    }

    @ResponseBody
    @RequestMapping(value = "/updateUsers")
    public Pojo updateUsers(@RequestBody GroupUserExtPojo pojo, HttpServletRequest request) throws Exception {
        pojo.formatCheck();
        long groupId = pojo.getGroupId();
        if (null == adGroupImpl.qryById(groupId)) {
            log.error("groupid:" + groupId + "没有查到对应的group信息");
            throw new Exception("groupid:" + groupId + "没有查到对象的group信息");
        }
        if (!verifyRightImpl.verifyGroupMaster(groupId, getUserName(request))) {
            log.error("用户" + getUserName(request) + "没有操作该GROUP" + groupId + "的权限");
            throw new Exception("用户" + getUserName(request) + "没有操作该GROUP的权限");
        }
        impl.addOrUpdate(groupId, converter.converterToAdGroupUser(pojo));
        return new CommonPojo();
    }

    @ResponseBody
    @RequestMapping(value = "/delUser")
    public Pojo delUser(@RequestBody GroupUserDelExtPojo pojo, HttpServletRequest request) throws Exception {
        pojo.formatCheck();
        long groupId = pojo.getGroupId();
        if (null == adGroupImpl.qryById(groupId)) {
            log.error("groupid:" + groupId + "没有查到对象的group信息");
            throw new Exception("groupid:" + groupId + "没有查到对象的group信息");
        }
        if (!verifyRightImpl.verifyGroupMaster(groupId, getUserName(request))) {
            log.error("用户" + getUserName(request) + "没有操作该GROUP" + groupId + "的权限");
            throw new Exception("用户" + getUserName(request) + "没有操作该GROUP的权限");
        }
        BooleanPojo booleanPojo = new BooleanPojo();
        booleanPojo.setResult(impl.delete(groupId, pojo.getUserName()));
        return booleanPojo;
    }

    @RequestMapping(value = "/qryUserType")
    public Pojo qryUserType(Long groupId, String userName) {
        GroupUserPojo pojo = new GroupUserPojo();
        pojo.setGroupId(groupId);
        pojo.setMembers(converter.converter(impl.qryByGroupIdAndUserName(groupId, userName)));
        return pojo;
    }

    @RequestMapping(value = "/qryUserRight")
    public Pojo qryUserRight(Long groupId, String userName) {
        UserAuthorizationPojo pojo = new UserAuthorizationPojo();
        pojo.setUserName(userName);
        pojo.setGroupAuthorization(rightConverter.ConverterRight(rightManager.qryRight(userName, groupId), groupId));
        return pojo;
    }

    @RequestMapping(value = "/qryUserRightByBranchId")
    public Pojo qryUserRightByBranchId(Long branchId, String userName) throws Exception {
        UserAuthorizationPojo pojo = new UserAuthorizationPojo();
        pojo.setUserName(userName);
        //根据branchId获取group信息
        AdBranch adBranch = adBranchImpl.qryById(branchId);
        if (null == adBranch || null == adBranch.getAdProject() || null == adBranch.getAdProject().getAdGroup()) {
            throw new Exception("根据流水编号：" + branchId + "没有查到该流水对应的项目信息");
        }
        Long groupId = adBranch.getAdProject().getAdGroup().getGroupId();
        pojo.setGroupAuthorization(rightConverter.ConverterRight(rightManager.qryRight(userName, groupId), groupId));
        return pojo;
    }

    //根据用户名模糊查找项目下的成员
    @RequestMapping(value = "/qryByUserNameAndGroupId", produces = "application/json")
    public Pojo qryByUserNameAndGroupId(String userName, Long groupId) {
        return impl.qryByUserNameAndGroupId(userName, groupId);
    }
}
