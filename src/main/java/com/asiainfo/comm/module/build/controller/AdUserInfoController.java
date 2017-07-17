package com.asiainfo.comm.module.build.controller;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.UserNotificationPojo;
import com.asiainfo.comm.common.pojo.pojoExt.WorkingSpaceFirstPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.WorkingSpaceSecondPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.WorkingSpaceThirdPojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.*;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.comm.module.build.service.impl.*;
import com.asiainfo.comm.module.models.AdUser;
import com.asiainfo.comm.module.models.AdUserRoleRel;
import com.asiainfo.comm.module.models.functionModels.QLAdGroup;
import com.asiainfo.comm.module.models.functionModels.QLAdGroupUser;
import com.asiainfo.comm.module.models.functionModels.QLAdProject;
import com.asiainfo.comm.module.models.functionModels.SAdBranch;
import com.asiainfo.comm.module.role.controller.BaseController;
import com.asiainfo.comm.module.role.service.impl.AdGroupUserImpl;
import com.asiainfo.comm.module.role.service.impl.AdProjectImpl;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/26.
 */
@RequestMapping(value = "/user")
@RestController
public class AdUserInfoController extends BaseController {
    @Autowired
    AdProjectImpl projectImpl;
    @Autowired
    AdGroupImpl adGroupImpl;

    @Autowired
    AdUserRoleRelImpl userRoleRelImpl;
    @Autowired
    AdGroupUserImpl groupUserImpl;
    @Autowired
    AdUserBranchImpl userBranchImpl;
    @Autowired
    AdTreeDataImpl treeDataImpl;
    @Autowired
    AdUserImpl userImpl;
    @Autowired
    AdStaticDataImpl bsStaticDataImpl;

    //根据用户查询隔离的资源
    @RequestMapping(value = "/qryProjectOfUser", produces = "application/json")
    public String qryProjectOfUser(HttpServletRequest request) {
        String username = getUserName(request);
        return JsonpUtil.modelToJson(adGroupImpl.qryGroupOfUser(username));
    }

    // 查询用户角色
    @RequestMapping(value = "/qryUserByUsername", produces = "application/json")
    public String qryUserByUsername(@RequestParam Map map) {
        UserPojo user = new UserPojo();
        String userName = (String) map.get("username");
        if (userName == null) {
            userName = "";
        }
        if (StringUtils.isNotEmpty(userName)) {
            List<AdUserRoleRel> rels = userRoleRelImpl.qryByUser(userName);
            if (rels != null && rels.size() > 0) {
                AdUserRoleRel rel = rels.get(0);
                user.setRoleName(rel.getAdRole().getRoleName());
                user.setRoleId(rel.getAdRole().getRoleId());
                user.setRoleLevel(rel.getAdRole().getRoleLevel());
                user.setUsername(userName);
            } else {
                user.setRoleName("访客");
                user.setRoleId(-1);
                user.setRoleLevel(100);
            }
        }
        return JsonpUtil.modelToJson(user);
    }

    //个人工作台下拉选内容查询
    @RequestMapping(value = "/qryWorkingSpaceSelect")
    public String qryWorkingSpaceSelect(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        WorkingSpaceSelect poj = new WorkingSpaceSelect();
        List<WorkingSpaceFirstPojoExt> groupPojList = new ArrayList<>();

        List<AdUserRoleRel> userRoleRelList = userRoleRelImpl.qryByUser(username);
        boolean isAdmin = false;
        for (AdUserRoleRel rel : userRoleRelList) {
            if (rel.getAdRole().getRoleLevel() == 0) {
                isAdmin = true;
                break;
            }
        }
        // 若不是超级管理员，则根据权限查询
        if (!isAdmin) {
            List<QLAdGroupUser> groupUsers = groupUserImpl.qryLByUserName(username);
            for (QLAdGroupUser groupUser : groupUsers) {
                if (groupUser.getQlAdGroup() == null) {
                    continue;
                }
                WorkingSpaceFirstPojoExt groupPoj = new WorkingSpaceFirstPojoExt();
                QLAdGroup group = groupUser.getQlAdGroup();
                //非最后一级用-1
                groupPoj.setId(-1);
                groupPoj.setText(group.getGroupName());
                List<WorkingSpaceSecondPojoExt> children = new ArrayList<>();
                List<QLAdProject> projectList = group.getProjectList();
                if (projectList != null && projectList.size() > 0) {
                    for (QLAdProject project : projectList) {
                        WorkingSpaceSecondPojoExt secondChild = new WorkingSpaceSecondPojoExt();
                        //非最后一级用-1
                        secondChild.setId(-1);
                        secondChild.setText(project.getProjectName());
                        List<WorkingSpaceThirdPojoExt> thirdChildren = new ArrayList<>();
                        List<SAdBranch> branchList = project.getBranchList();
                        if (branchList != null && branchList.size() > 0) {
                            String branchDesc;
                            for (SAdBranch branch : branchList) {
                                branchDesc = branch.getBranchDesc();
                                WorkingSpaceThirdPojoExt thirdChild = new WorkingSpaceThirdPojoExt();
                                thirdChild.setId(branch.getBranchId());
                                thirdChild.setText(StringUtils.isNotEmpty(branchDesc) ? branchDesc : branch.getBranchName());
                                thirdChildren.add(thirdChild);
                            }
                        }
                        secondChild.setChildren(thirdChildren);
                        children.add(secondChild);
                    }
                }
                groupPoj.setChildren(children);
                groupPojList.add(groupPoj);
            }
        }
        // 是超级管理员，则全量查询
        else {
            List<QLAdGroup> groups = adGroupImpl.qryL();
            for (QLAdGroup group : groups) {
                WorkingSpaceFirstPojoExt groupPoj = new WorkingSpaceFirstPojoExt();
                //非最后一级用-1
                groupPoj.setId(-1);
                groupPoj.setText(group.getGroupName());
                List<WorkingSpaceSecondPojoExt> children = new ArrayList<>();
                List<QLAdProject> projectList = group.getProjectList();
                if (projectList != null && projectList.size() > 0) {
                    for (QLAdProject project : projectList) {
                        WorkingSpaceSecondPojoExt secondChild = new WorkingSpaceSecondPojoExt();
                        //非最后一级用-1
                        secondChild.setId(-1);
                        secondChild.setText(project.getProjectName());
                        List<WorkingSpaceThirdPojoExt> thirdChildren = new ArrayList<>();
                        List<SAdBranch> branchList = project.getBranchList();
                        if (branchList != null && branchList.size() > 0) {
                            String branchDesc;
                            for (SAdBranch branch : branchList) {
                                branchDesc = branch.getBranchDesc();
                                WorkingSpaceThirdPojoExt thirdChild = new WorkingSpaceThirdPojoExt();
                                thirdChild.setId(branch.getBranchId());
                                thirdChild.setText(StringUtils.isNotEmpty(branchDesc) ? branchDesc : branch.getBranchName());
                                thirdChildren.add(thirdChild);
                            }
                        }
                        secondChild.setChildren(thirdChildren);
                        children.add(secondChild);
                    }
                }
                groupPoj.setChildren(children);
                groupPojList.add(groupPoj);
            }
        }
        poj.setPrjBranchOfAuth(groupPojList);
        return JsonpUtil.modelToJson(poj);
    }

    //个人工作台标签页查询
    @RequestMapping(value = "/qryPersonalWorkingSpace", produces = "application/json")
    public String qryPersonalWorkingSpace(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");

        // 判断是否是超级管理员
        List<AdUserRoleRel> userRoleRelList = userRoleRelImpl.qryByUser(username);
        boolean isAdmin = false;
        for (AdUserRoleRel rel : userRoleRelList) {
            if (rel.getAdRole().getRoleLevel() == 0) {
                isAdmin = true;
                break;
            }
        }
        WorkingSpacePojo poj;
        if (isAdmin) {
            if (null != userId) {
                poj = userBranchImpl.qryWorkingSpaceByAdminUser(userId);
            } else {
                poj = userBranchImpl.qryWorkingSpaceByAdminUser(username);
            }
        } else {
            if (null != userId) {
                poj = userBranchImpl.qryWorkingSpaceByUser(userId);
            } else {
                poj = userBranchImpl.qryWorkingSpaceByUser(username);
            }
        }
        return JsonpUtil.modelToJson(poj);
    }

    @RequestMapping(value = "/qryTreeData", produces = "application/json")
    public String qryTreeData(HttpServletRequest request, @RequestParam Map<String, String> params) {
        Pojo poj;
        Long userId = getUserId(request);
        Integer dataType = Integer.parseInt(params.get("dataType"));
        Integer reportType = Integer.parseInt(params.get("reportType"));
        switch (dataType) {
            case 1://查詢AdTreeData中的值
                if (!params.containsKey("treeType")) {
                    poj = new AdTreeDataPojo();
                    poj.setRetCode("500");
                    poj.setRetMessage("缺少参数\"treeType\"");
                    break;
                }
                Integer rtreeType = Integer.parseInt(params.get("treeType"));
                poj = treeDataImpl.qryTreeData(rtreeType, userId, reportType);
                break;
            case 2://查詢Group中的关联值
                boolean isAdmin = true;
                poj = treeDataImpl.qryGroupData(userId, isAdmin, reportType);
                break;
            case 3://插叙Project中的关联值
                poj = treeDataImpl.qryProjectData();
                break;
            default://参数错误
                poj = new AdTreeDataPojo();
                poj.setRetCode("500");
                poj.setRetMessage("类型错误");
                break;
        }
        return JsonpUtil.modelToJson(poj);
    }

    @RequestMapping(value = "/qrySonarGroup", produces = "application/json")
    public String qrySonarGroup(HttpServletRequest request, @RequestParam Map<String, String> params) {
        Pojo poj;
        Long userId = getUserId(request);
        Integer reportType = Integer.parseInt(params.get("reportType"));
        boolean isAdmin = true;
        poj = treeDataImpl.qrySonarGroup(userId, isAdmin, reportType);
        return JsonpUtil.modelToJson(poj);
    }

    @RequestMapping(value = "/qryOnlineNumber", produces = "application/json")
    public String qryOnlineNumber(HttpServletRequest request) {
        HttpSession session = request.getSession();
        int invalidTime = session.getMaxInactiveInterval();
        IntegerPojo poj = userImpl.qryOnlineNumber(invalidTime);
        return JsonpUtil.modelToJson(poj);
    }

    @RequestMapping(value = "/getUserNotification", produces = "application/json")
    public String getNotificationInfo(HttpServletRequest req, @RequestParam Map map) throws IOException {
        UserNotificationPojo poj = new UserNotificationPojo();
        if (null != map.get("userName")) {
            String userName = (String) map.get("userName");
            AdUser adUser = userImpl.qryByName(userName);
            String notification = adUser.getNotification();
            poj.setSysNotify(true);
            poj.setEmailNotify(false);
            poj.setSmsNotify(false);

            if (!org.springframework.util.StringUtils.isEmpty(notification) && notification.length() > 2) {
                String sysNotify = notification.substring(0, 1);
                String emailNotify = notification.substring(1, 2);
                String smsNotify = notification.substring(2, 3);

                if (sysNotify != null && "1".equals(sysNotify)) {
                    poj.setSysNotify(true);
                }
                if (emailNotify != null && "1".equals(emailNotify)) {
                    poj.setEmailNotify(true);
                }
                if (smsNotify != null && "1".equals(smsNotify)) {
                    poj.setSmsNotify(true);
                }
            }
        }
        return JsonpUtil.modelToJson(poj);
    }
}
