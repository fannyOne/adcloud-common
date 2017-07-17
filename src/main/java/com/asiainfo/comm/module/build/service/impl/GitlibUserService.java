package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.GitUserPojoExt;
import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.module.build.dao.impl.AdBranchDAO;
import com.asiainfo.comm.module.build.dao.impl.AdDockImagesDAO;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDAO;
import com.asiainfo.comm.module.models.AdRole;
import com.asiainfo.comm.module.models.AdUserRoleRel;
import com.asiainfo.comm.module.models.functionModels.GitLabUser;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Administrator on 2016-07-11.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class GitlibUserService {
    @Autowired
    AdProjectDAO adProjectDAO;
    @Autowired
    AdUserRoleRelImpl adUserRoleRelImpl;
    @Autowired
    AdDockImagesDAO adDockImagesDAO;
    @Autowired
    AdBranchDAO adBranchDAO;
    @Value("${gitlab.server.url}")
    private String gitlabServerUrl;
    @Value("${gitlab.admin.token}")
    private String token;

    public List<GitUserPojoExt> qryUsers(Map<String, String> map, int roleType) {
        long time = new Date().getTime();
        long time2 = 0;
        List<GitUserPojoExt> lists = new ArrayList<>();
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        StringBuffer qryCond = new StringBuffer(gitlabServerUrl + "/api/v3/users?utf8=%E2%9C%93" + "&private_token=" + token);
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                qryCond.append("&" + entry.getKey() + "=" + entry.getValue());
            }
        }
        try {
            OAuthClientRequest allUserRequest = new OAuthBearerClientRequest(qryCond.toString()).buildQueryMessage();
            OAuthResourceResponse allUserResponse = oAuthClient.resource(allUserRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
            String usersString = allUserResponse.getBody();
            time2 = new Date().getTime();
            log.error("动态查询时间:" + (time2 - time) + "毫秒");
            JSONArray jsonArray = JSONArray.fromObject(usersString);
            int i = 0;
            for (; i < jsonArray.size(); i++) {
                GitUserPojoExt user = new GitUserPojoExt();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                user.setUsername(jsonObject.getString("username"));
                user.setDisplayName(jsonObject.getString("name"));
                user.setEmail(jsonObject.getString("email"));
                user.setId(jsonObject.getLong("id"));
                //查询角色
                if (roleType == 1) {
                    user = qryUserRole(jsonObject.getString("username"), user);
                }
                lists.add(user);
            }

        } catch (Exception e) {
        }
        log.error("动态组装时间:" + (new Date().getTime() - time2) + "毫秒");
        return lists;
    }

    public GitUserPojoExt qryUserRole(String username, GitUserPojoExt user) {
        //存在于内存
        if (CommConstants.Role.USER_ROLE.containsKey(username)) {
            user.setRoleId(CommConstants.Role.USER_ROLE.get(username)
                .getRoleId());
            user.setRoleLevel(CommConstants.Role.USER_ROLE.get(username)
                .getRoleLevel());
            user.setRoleName(CommConstants.Role.USER_ROLE.get(username)
                .getRoleName());
        }
        //不存在于内存
        else {
            List<AdUserRoleRel> adUserRoleRels = adUserRoleRelImpl
                .qryByUser(username);
            if (adUserRoleRels != null && adUserRoleRels.size() > 0) {
                user.setRoleId(adUserRoleRels.get(0).getAdRole().getRoleId());
                user.setRoleName(adUserRoleRels.get(0).getAdRole().getRoleName());
                user.setRoleLevel(adUserRoleRels.get(0).getAdRole()
                    .getRoleLevel());
                CommConstants.Role.USER_ROLE.put(username
                    , adUserRoleRels.get(0).getAdRole());
            }
            //无权限
            else {
                user.setRoleName("访客");
                user.setRoleId(-1);
                AdRole userRole = new AdRole();
                userRole.setRoleName("访客");
                userRole.setRoleId(-1L);
            }
        }
        return user;
    }

    public Map<String, GitLabUser> getAllUsersMap(Map<String, String> map) {
        long time = new Date().getTime();
        Map<String, GitLabUser> maps = new HashMap<>();
        boolean down = false;
        int page = 1;
        int size = 0;
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        StringBuffer qryCond = new StringBuffer(gitlabServerUrl + "/api/v3/users?utf8=%E2%9C%93" + "&private_token=" + token);
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                qryCond.append("&" + entry.getKey() + "=" + entry.getValue());
            }
        }
        String qryCondStr = qryCond.toString() + "&page=";
        try {
            while (!down) {
                OAuthClientRequest allUserRequest = new OAuthBearerClientRequest(qryCondStr + page).buildQueryMessage();
                OAuthResourceResponse allUserResponse = oAuthClient.resource(allUserRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
                String usersString = allUserResponse.getBody();
                JSONArray jsonArray = JSONArray.fromObject(usersString);
                int i = 0;
                for (; i < jsonArray.size(); i++) {
                    GitLabUser user = new GitLabUser();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    user.setUsername(jsonObject.getString("username"));
                    user.setDisplayName(jsonObject.getString("name"));
                    user.setEmail(jsonObject.getString("email"));
                    user.setId(jsonObject.getLong("id"));
                    user.setBio(jsonObject.getString("bio"));
                    user.setSkype(jsonObject.getString("skype"));
                    user.setLinkedin(jsonObject.getString("linkedin"));
                    user.setTwitter(jsonObject.getString("twitter"));
                    user.setWebsiteUrl(jsonObject.getString("website_url"));
                    user.setThemeId(jsonObject.getLong("theme_id"));
                    user.setColorSchemeId(jsonObject.getLong("color_scheme_id"));
                    user.setIsAdmin(jsonObject.getBoolean("is_admin"));
                    user.setAvatarUrl(jsonObject.getString("avatar_url"));
                    user.setCanCreateGroup(jsonObject.getBoolean("can_create_group"));
                    user.setCurrentSignInAt(jsonObject.getString("current_sign_in_at"));
                    user.setTwoFactorEnabled(jsonObject.getBoolean("two_factor_enabled"));
                    maps.put(user.getUsername(), user);
                }
                if (i != 0) {
                    size += i;
                    page++;
                } else {
                    down = true;
                }
            }
            CommConstants.Role.SET_USER_NUMBER(size);
            log.error("用户总数：" + size);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.error("（填装用户）全量组装时间：" + (new Date().getTime() - time) + "毫秒");
        return maps;
    }

    public long getAllUsersNumber(Map<String, String> map) {
        long size = 0;
        long time = new Date().getTime();
        long time2;
        boolean down = false;
        int page = 1;
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        StringBuffer qryCond = new StringBuffer(gitlabServerUrl + "/api/v3/users?utf8=%E2%9C%93" + "&private_token=" + token + "&per_page=100");
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                qryCond.append("&" + entry.getKey() + "=" + entry.getValue());
            }
        }
        String qryCondStr = qryCond.toString() + "&page=";
        try {
            while (!down) {
                OAuthClientRequest allUserRequest = new OAuthBearerClientRequest(qryCondStr + page).buildQueryMessage();
                OAuthResourceResponse allUserResponse = oAuthClient.resource(allUserRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
                String usersString = allUserResponse.getBody();
                JSONArray jsonArray = JSONArray.fromObject(usersString);
                if (jsonArray.size() != 0) {
                    size += jsonArray.size();
                    page++;
                } else {
                    down = true;
                }
            }
            log.error("用户总数：" + size);
            time2 = new Date().getTime();
            log.error("（总数查询）全量查询时间：" + (time2 - time) + "毫秒");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return size;
    }

    public List<AdUserRoleRel> qryAllRoleRel() {
        return adUserRoleRelImpl.qryAll();
    }

}
