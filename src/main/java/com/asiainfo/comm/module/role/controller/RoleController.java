package com.asiainfo.comm.module.role.controller;

import com.asiainfo.comm.common.pojo.pojoExt.AuthorPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.GitUserPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.RolePojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.AuthorPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.CommonPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.UserRolesPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.UsersPojo;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.comm.module.build.service.impl.GitlibUserService;
import com.asiainfo.comm.module.models.AdRole;
import com.asiainfo.comm.module.models.AdUserRoleRel;
import com.asiainfo.comm.module.role.service.impl.AdAuthorImpl;
import com.asiainfo.comm.module.role.service.impl.AdRoleImpl;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import com.asiainfo.schedule.helper.QryUserRunnable;
import com.avaje.ebean.PagedList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by YangRY
 * 2016/7/11 0011.
 */
@RestController
@RequestMapping(value = "/role")
public class RoleController {
    @Autowired
    AdRoleImpl roleImpl;
    @Autowired
    AdAuthorImpl authorImpl;
    @Autowired
    AdUserRoleRelImpl userRoleRelImpl;
    @Autowired
    GitlibUserService gitlabUserService;
    private Executor pool = Executors.newFixedThreadPool(100);//线程池

    //根据用户查询角色
    @RequestMapping(value = "/yryQryAdcloudIp", produces = "application/json")
    public String yryQryAdcloudIp(@RequestParam Map params) {
        return CommConstants.BuildConstants.AD_CLOUD_IP;
    }

    //根据用户查询角色
    @RequestMapping(value = "/userRoles", produces = "application/json")
    public String qryRolesByUser(@RequestParam Map params) {
        String userName = (String) params.get("userName");
        List<AdUserRoleRel> roles = userRoleRelImpl.qryByUser(userName);
        UserRolesPojo poj = new UserRolesPojo();
        List<RolePojoExt> roleExts = new ArrayList<>();
        try {
            if (roles != null && roles.size() > 0) {
                poj.setTotal(roles.size());
                for (AdUserRoleRel role : roles) {
                    RolePojoExt ext = new RolePojoExt();
                    ext.setRoleName(role.getAdRole().getRoleName());
                    ext.setRoleId(role.getAdRole().getRoleId());
                    roleExts.add(ext);
                }
                poj.setRoles(roleExts);

            }
        } catch (Exception e) {
            poj = new UserRolesPojo();
            poj.setRetCode("500");
            poj.setRetMessage(e.getMessage());
        } finally {
            return JsonpUtil.modelToJson(poj);
        }
    }

    //查询所有角色
    @RequestMapping(value = "/qryAllRoles", produces = "application/json")
    public String qryAllRoles(@RequestParam Map params) {
        UserRolesPojo poj = new UserRolesPojo();
        List<AdRole> roles = roleImpl.qryAll();
        List<RolePojoExt> roleExts = new ArrayList<>();
        try {
            if (roles != null && roles.size() > 0) {
                poj.setTotal(roles.size());
                for (AdRole role : roles) {
                    RolePojoExt ext = new RolePojoExt();
                    ext.setRoleName(role.getRoleName());
                    ext.setRoleId(role.getRoleId());
                    roleExts.add(ext);
                }
                poj.setRoles(roleExts);
            }
        } catch (Exception e) {
            poj = new UserRolesPojo();
            poj.setRetCode("500");
            poj.setRetMessage(e.getMessage());
        } finally {
            return JsonpUtil.modelToJson(poj);
        }
    }

    //分页查询角色
    @RequestMapping(value = "/qryRoles", produces = "application/json")
    public String qryRolePages(@RequestParam Map params) {
        UserRolesPojo poj = new UserRolesPojo();
        PagedList<AdRole> roles = roleImpl.qryPages(params);
        List<RolePojoExt> roleExts = new ArrayList<>();
        try {
            if (roles != null) {
                poj.setTotal(roles.getTotalRowCount());
                if (roles.getList() != null && roles.getList().size() > 0) {
                    for (AdRole role : roles.getList()) {
                        RolePojoExt ext = new RolePojoExt();
                        ext.setRoleName(role.getRoleName());
                        ext.setRoleId(role.getRoleId());
                        roleExts.add(ext);
                    }
                    poj.setRoles(roleExts);
                }
            }
        } catch (Exception e) {
            poj = new UserRolesPojo();
            poj.setRetCode("500");
            poj.setRetMessage(e.getMessage());
        } finally {
            return JsonpUtil.modelToJson(poj);
        }
    }

    //根据角色查询权限
    @RequestMapping(value = "/qryAuthor", produces = "application/json")
    public String qryAuthorByRole(@RequestParam Map params) {
        String roleIdStr = (String) params.get("roleId");
        long roleId = 0;
        if (roleIdStr != null && StringUtils.isNotEmpty(roleIdStr)) {
            roleId = Long.parseLong(roleIdStr);
        }
        AuthorPojo poj = new AuthorPojo();
        try {
            poj.setRoleId(roleId);
            List<AuthorPojoExt> pojExts = authorImpl.qryAuthorAndSignCheck(roleId);
            poj.setAuthors(pojExts);
        } catch (Exception e) {
            poj = new AuthorPojo();
            poj.setRetCode("500");
            poj.setRetMessage(e.getMessage());
        } finally {
            return JsonpUtil.modelToJson(poj);
        }
    }

    //编辑权限
    @RequestMapping(value = "/editAuthor", produces = "application/json")
    public String changeRoleAuthor(@RequestParam Map map) {
        CommonPojo poj = new CommonPojo();
        try {
            String roleId = (String) map.get("roleId");
            String projectIds = (String) map.get("authors");
            if (StringUtils.isNotEmpty(roleId) && projectIds != null) {
                poj = authorImpl.changeRoleAuthor(Long.parseLong(roleId), projectIds);
            }
            return JsonpUtil.modelToJson(poj);
        } catch (Exception e) {
            poj.setRetCode("500");
            poj.setRetMessage(e.getMessage());
            return JsonpUtil.modelToJson(poj);
        }
    }

    //查询重复权限名
    @RequestMapping(value = "/qryRepeatName", produces = "application/json")
    public String qryRoleByName(String roleName) {
        CommonPojo poj = new CommonPojo();
        int count = roleImpl.qryCountByName(roleName);
        poj.setRetMessage(count + "");
        return JsonpUtil.modelToJson(poj);
    }

    //条件查询用户（实时）
    @RequestMapping(value = "/qryUsers", produces = "application/json")
    public String qryUsers(@RequestParam Map map) {
        UsersPojo users = new UsersPojo();
        String pageStr = (String) map.get("page");
        String userName = (String) map.get("username");
        String roleIdStr = (String) map.get("roleId");
        String per_page = (String) map.get("per_page");
        if (pageStr == null || !StringUtils.isNotEmpty(pageStr)) {
            pageStr = "1";
        }
        if (userName == null) {
            userName = "";
        }
        if (roleIdStr == null) {
            roleIdStr = "";
        }
        if (per_page == null) {
            per_page = "10";
        }
        List<GitUserPojoExt> list;
        Map<String, String> params = new HashMap<>();
        params.put("search", userName);
        long size;
        //99为 系统管理时查询全部人员
        if (StringUtils.isNotEmpty(roleIdStr) && 99 != Long.parseLong(roleIdStr)) {
            users = userRoleRelImpl.qryUserByRole(Long.parseLong(roleIdStr), userName
                , pageStr, users, Integer.parseInt(per_page));
            if (CommConstants.Role.THREAD_POOL < CommConstants.Role.THREAD_NUMBER) {
                CommConstants.Role.CHANGE_THREAD_POOL(1);
                pool.execute(new QryUserRunnable(gitlabUserService, 1));
            }
        } else {
            //条件查询
            if (StringUtils.isNotEmpty(userName)) {
                //已经存在于内存
                if (CommConstants.Role.USER_SEARCH_TOTAL.containsKey(userName)) {
                    size = CommConstants.Role.USER_SEARCH_TOTAL.get(userName);
                    users.setTotal(size);
                }
                //未存在于内存
                else {
                    size = gitlabUserService.getAllUsersNumber(params);
                    users.setTotal(size);
                }
                //更新内存
                if (CommConstants.Role.THREAD_POOL < CommConstants.Role.THREAD_NUMBER) {
                    CommConstants.Role.CHANGE_THREAD_POOL(1);
                    pool.execute(new QryUserRunnable(gitlabUserService, params, 1));
                }
            }
            //非条件查询
            //未存于内存
            else if (CommConstants.Role.USER_NUMBER == 0) {
                size = gitlabUserService.getAllUsersNumber(params);
                users.setTotal(size);
                CommConstants.Role.SET_USER_NUMBER(size);
                if (CommConstants.Role.THREAD_POOL < CommConstants.Role.THREAD_NUMBER) {
                    CommConstants.Role.CHANGE_THREAD_POOL(1);
                    pool.execute(new QryUserRunnable(gitlabUserService, 1));
                }
            }
            //已经存于内存
            else {
                users.setTotal(CommConstants.Role.USER_NUMBER);
                if (CommConstants.Role.THREAD_POOL < CommConstants.Role.THREAD_NUMBER) {
                    CommConstants.Role.CHANGE_THREAD_POOL(1);
                    pool.execute(new QryUserRunnable(gitlabUserService, 1));
                }
            }
            params.put("page", pageStr);
            params.put("per_page", per_page);
            int roleType = 0;
            if (StringUtils.isNotEmpty(roleIdStr) && 99 == Long.parseLong(roleIdStr)) {
                roleType = 1;
            }
            list = gitlabUserService.qryUsers(params, roleType);
            users.setUsers(list);
        }
        if (CommConstants.Role.THREAD_POOL < CommConstants.Role.THREAD_NUMBER) {
            CommConstants.Role.CHANGE_THREAD_POOL(1);
            pool.execute(new QryUserRunnable(gitlabUserService, 2));
        }
        return JsonpUtil.modelToJson(users);
    }

    @RequestMapping(value = "/editUserRole", produces = "application/json")
    public String editUserRole(@RequestParam Map<String, String> map) {
        CommonPojo poj = new CommonPojo();
        try {
            poj = userRoleRelImpl.editUserRole(map);
            if (CommConstants.Role.THREAD_POOL < CommConstants.Role.THREAD_NUMBER) {
                CommConstants.Role.CHANGE_THREAD_POOL(1);
                pool.execute(new QryUserRunnable(gitlabUserService, 2));
            }
        } catch (Exception e) {
            poj.setRetCode("500");
            poj.setRetMessage(e.getMessage());
        }
        return JsonpUtil.modelToJson(poj);
    }
}
