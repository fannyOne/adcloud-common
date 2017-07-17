package com.asiainfo.auth;

import com.asiainfo.auth.sso.gitlib.api.GitlabAPI;
import com.asiainfo.auth.sso.gitlib.api.models.GitlabSession;
import com.asiainfo.auth.sso.gitlib.api.models.GitlabUser;
import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoMaster.CommonPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.UserPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.UserPwdPojo;
import com.asiainfo.comm.module.build.service.impl.AdUserImpl;
import com.asiainfo.comm.module.models.AdUser;
import com.asiainfo.comm.module.models.AdUserRoleRel;
import com.asiainfo.comm.module.role.controller.BaseController;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.util.MailUtil;
import com.asiainfo.util.StringUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by guojian on 2016-07-06.
 */
@lombok.extern.slf4j.Slf4j
@Controller
@RequestMapping("/auth")
public class OAuth2Controller extends BaseController {
    OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
    @Value("${gitlab.server.url}")
    private String gitlabServerUrl;
    @Value("/oauth/authorize")
    private String authorizePath;
    @Value("/oauth/token")
    private String tokenPath;

    @Value("${gitlab.admin.token}")
    private String token;

    @Autowired
    AdUserRoleRelImpl userRoleRelImpl;

    @Autowired
    AdUserImpl adUserImpl;

    private String currentUser;

    @PreDestroy
    public void cleanUp() {
        oAuthClient.shutdown();
    }


    private String cookies = "";

    @RequestMapping("/getCookies")
    public
    @ResponseBody
    String getCookies(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        UserPojo user = new UserPojo();
        String name;
        String userName;
        String email;
        Integer id;
        String userDetail = (String) request.getSession().getAttribute("userDetail");
        if (org.apache.commons.lang.StringUtils.isNotEmpty(userDetail)) {
            JSONObject jsonObject = JSONObject.fromObject(userDetail);
            userName = jsonObject.getString("username");
            name = jsonObject.getString("name");
            email = jsonObject.getString("email");
            id = jsonObject.getInt("id");
            // 获取权限信息
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(userName)) {
                List<AdUserRoleRel> rels = userRoleRelImpl.qryByUser(userName);
                if (rels != null && rels.size() > 0) {
                    AdUserRoleRel rel = rels.get(0);
                    user.setRoleName(rel.getAdRole().getRoleName());
                    user.setRoleId(rel.getAdRole().getRoleId());
                    user.setRoleLevel(rel.getAdRole().getRoleLevel());
                    user.setUsername(userName);
                    user.setDisplayName(name);
                    user.setRetMessage((String) request.getSession().getAttribute("nameHash"));
                } else {
                    user.setUsername(userName);
                    user.setDisplayName(name);
                    user.setRoleName("访客");
                    user.setRoleId(-1);
                    user.setRoleLevel(100);
                    user.setRetMessage((String) request.getSession().getAttribute("nameHash"));
                }
                user.setEmail(email);
                user.setId(id);
                return JsonpUtil.modelToJson(user);
            }
            /**
             * add Cookie
             * 备注：加入cookie前台无法取用，暂时不用
             * */
            /*Cookie nameCookie = new Cookie("name2", URLEncoder.encode(name, "UTF-8"));
            nameCookie.setPath(request.getContextPath() + "/");
            nameCookie.setMaxAge((int) (DateConvertUtils.ONE_DAY_MILLIS / 1000));
            response.addCookie(nameCookie);
            Cookie userNameCookie = new Cookie("username2", URLEncoder.encode(userName, "UTF-8"));
            userNameCookie.setPath(request.getContextPath());
            userNameCookie.setMaxAge((int) (DateConvertUtils.ONE_DAY_MILLIS / 1000));
            response.addCookie(userNameCookie);
            Cookie nameHashCookie = new Cookie("nameHash2", URLEncoder.encode(
                (String) request.getSession().getAttribute("nameHash"), "UTF-8"));
            nameHashCookie.setPath(request.getContextPath());
            nameHashCookie.setMaxAge((int) (DateConvertUtils.ONE_DAY_MILLIS / 1000));
            response.addCookie(nameHashCookie);*/
            /**
             * add Cookie end
             * */
        }
        return null;
    }

    @RequestMapping(value = "/signIn")
    @ResponseBody
    public Map<String, String> login(HttpServletRequest req, @RequestParam(value = "username") String username, @RequestParam(value = "password") String password) throws IOException, OAuthSystemException, OAuthProblemException {
        Map<String, String> result = new HashMap<String, String>();
        result.put("flag", "true");

        GitlabSession gitlabSession = GitlabAPI.connect(gitlabServerUrl, username, password);
        if (null == gitlabSession || gitlabSession.getPrivateToken().length() <= 0) {
            result.put("flag", "false");
            return result;
        }
        String privateToken = gitlabSession.getPrivateToken();
        //返回个人token用户前端查询个人关联的项目
        result.put("projects", gitlabServerUrl + "/api/v3/projects?private_token=" + privateToken);
        //用系统管理员查询，获取个人用户的额外信息

        StringBuffer stringBuffer = new StringBuffer(gitlabServerUrl + "/api/v3/users?private_token=");
        stringBuffer.append(token).append("&username=").append(username);
        OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(stringBuffer.toString()).buildQueryMessage();
        OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
        String userDetail = resourceResponse.getBody();
        String userName = null;
        String name = null;
        if (org.apache.commons.lang.StringUtils.isNotEmpty(userDetail)) {
            JSONArray jsonArray = JSONArray.fromObject(userDetail);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            userName = jsonObject.getString("username");
            name = jsonObject.getString("name");
            if (name.startsWith("\\")) {
                name = java.net.URLDecoder.decode(name, "utf-8");
                StringBuilder sb = new StringBuilder();
                int n = name.length() / 6;
                for (int i = 0, j = 2; i < n; i++, j += 6) {
                    String code = name.substring(j, j + 4);
                    char ch = (char) Integer.parseInt(code, 16);
                    sb.append(ch);
                }
                name = sb.toString();
                jsonObject.put("name", name);
            }
            userDetail = jsonObject.toString();
        }
        //将登录名存储成哈希值键值对（暂时保留，作为一个唯一的key）
        String nameHash = null;
        if (userName != null) {
            nameHash = userName.hashCode() + new StringUtil().getRandomStr(10);
        }
        req.getSession().setAttribute("userDetail", userDetail);
        req.getSession().setAttribute("nameHash", nameHash);
        req.getSession().setAttribute("username", userName);
        req.getSession().setAttribute("privateToken", privateToken);
        AdUser aduser = adUserImpl.qryOrCreateUser(userName, name);
        if (null != aduser) {
            req.getSession().setAttribute("userId", aduser.getUserId());
            req.getSession().setAttribute("displayName", aduser.getDisplayName());
            //公告使用
            result.put("firstLogin", "" + aduser.getFirstLogin());
            aduser.setSessionId(req.getSession().getId());
            aduser.setLastLoginDate(new Date());
            aduser.setActiveDate(new Date());
            aduser.setPassword(password);
            aduser.setVerificationCode(null);
            aduser.setVerificationTime(null);
            adUserImpl.updateUser(aduser);
        }
        return result;
    }


    //    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/logout")
    public void logout(HttpServletRequest request) throws IOException {
        AdUser user = getAdUserByUserId(request);
        user.setSessionId(null);
        adUserImpl.updateUser(user);
        request.getSession().invalidate();
    }

    /**
     * 把中文转成Unicode码
     *
     * @param str
     * @return
     */
    private String chinaToUnicode(String str) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            int chr1 = (char) str.charAt(i);
            if (chr1 >= 19968 && chr1 <= 171941) {//汉字范围 \u4e00-\u9fa5 (中文)
                sb.append("\\u" + Integer.toHexString(chr1));
            } else {
                sb.append("\\u" + str.charAt(i));
            }
        }
        return sb.toString();
    }

    /**
     * 判断是否为中文字符
     *
     * @param c
     * @return
     */
    private boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    @RequestMapping(value = "/signUp")
    public
    @ResponseBody
    Map<String, String> signUp(@RequestParam(value = "email") String email, @RequestParam(value = "name") String name, @RequestParam(value = "username") String username, @RequestParam(value = "password") String password) throws UnsupportedEncodingException {
        Map<String, String> result = new HashMap<String, String>();
        StringBuffer stringBuffer = new StringBuffer(gitlabServerUrl + "/api/v3/users?private_token=");
        stringBuffer.append(token).append("&email=");
        stringBuffer.append(email).append("&password=");
        stringBuffer.append(password).append("&username=");
        stringBuffer.append(username).append("&name=").append(URLEncoder.encode(name, "UTF-8"));
        String ls_message = "";
        try {
            GitlabAPI gitlabAPI = new GitlabAPI(gitlabServerUrl, token);
            List<GitlabUser> gitlabUsers = gitlabAPI.findUsers(email);
            if (gitlabUsers == null || gitlabUsers.isEmpty()) {
                gitlabUsers = gitlabAPI.findUsers(username);
            } else {
                ls_message = "邮箱信息重复请重新填写";
            }
            if (gitlabUsers == null || gitlabUsers.isEmpty()) {
                OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(stringBuffer.toString()).buildQueryMessage();
                OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);
                String userDetail = resourceResponse.getBody();
                String userName = null;
                if (org.apache.commons.lang.StringUtils.isNotEmpty(userDetail)) {
                    JSONObject jsonObject = JSONObject.fromObject(userDetail);
                    if (jsonObject.containsKey("message")) {
                        result.put("flag", "false");
                        result.put("returnMsg", jsonObject.getString("message"));
                        return result;
                    }
                    userName = jsonObject.getString("username");
                }
                if (userName != null) {
                    result.put("flag", "true");
                }
            } else {
                if (StringUtils.isEmpty(ls_message)) {
                    ls_message = "登录名重复请重新填写";
                }
                result.put("returnMsg", ls_message);
                result.put("flag", "false");
            }


        } catch (Exception e) {
            result.put("flag", "false");
            result.put("returnMsg", e.getCause().getMessage());

        }
        return result;
    }

    @RequestMapping(value = "/modification")
    public
    @ResponseBody
    Map<String, String> modificationPassword(HttpServletRequest req, @RequestParam Map map) {
        Map<String, String> result = new HashMap<String, String>();
        String getMessage;
        try {
            String newpassword = (String) map.get("newpassword");
            if (newpassword != null && !"".equals(newpassword)) {
                int id = Integer.valueOf((String) map.get("id"));
                GitlabAPI gitlabAPI = GitlabAPI.connect(gitlabServerUrl, token);
                gitlabAPI.updateUser(id, newpassword);
                log.error("userId===" + id + "newpassword====" + newpassword);
            }
            String email = (String) map.get("email");
            String userName = (String) map.get("userName");
            AdUser adUser = adUserImpl.qryByName(userName);
            if (adUser != null) {
                String notifyStr = "";
                boolean sysNotify = true;
                boolean emailNotify = true;
                boolean smsNotify = true;
                if (map.get("notification[sysNotify]") != null) {
                    sysNotify = Boolean.parseBoolean((String) map.get("notification[sysNotify]"));
                }
                if (map.get("notification[emailNotify]") != null) {
                    emailNotify = Boolean.parseBoolean((String) map.get("notification[emailNotify]"));
                }
                if (map.get("notification[smsNotify]") != null) {
                    smsNotify = Boolean.parseBoolean((String) map.get("notification[smsNotify]"));
                }

                if (sysNotify) {
                    notifyStr = notifyStr + "1"; //系统提示
                } else {
                    notifyStr = notifyStr + "0"; //系统提示
                }

                if (emailNotify) {
                    notifyStr = notifyStr + "1"; //邮件提示
                } else {
                    notifyStr = notifyStr + "0"; //邮件提示
                }

                if (smsNotify) {
                    notifyStr = notifyStr + "1"; //短信提示
                } else {
                    notifyStr = notifyStr + "0"; //短信提示
                }

                adUser.setNotification(notifyStr);

                if (StringUtils.isEmpty(adUser.getEmail())) {
                    adUser.setEmail(email);
                }
                adUser.update();
            }

            result.put("state", "true");
            result.put("returnMsg", "");
        } catch (IOException e) {
            JSONArray jsonArray = JSONArray.fromObject("[" + e.getMessage() + "]");
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            getMessage = jsonObject.getString("message");
            log.error("getMessage===" + getMessage);
            result.put("state", "false");
            result.put("returnMsg", getMessage);
            log.error(e.getMessage(), e);
        }
        return result;

    }


    //密码重置发送验证码
    @RequestMapping(value = "/sendVerificationCode", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public UserPwdPojo sendVerificationCode(@RequestBody UserPwdPojo userPojo) throws Exception{
        AdUser adUser = adUserImpl.qryByName(userPojo.getUsername());
        if (null == adUser){
            throw new Exception("该用户不存在！");
        }

        String email = adUser.getEmail();

        if (StringUtils.isEmpty(email)){
            GitlabAPI gitlabAPI = GitlabAPI.connect(gitlabServerUrl, token);
            List<GitlabUser> gitUsers = gitlabAPI.findUsers(adUser.getLoginName());
            if (CollectionUtils.isNotEmpty(gitUsers)) {
                email = gitUsers.get(0).getEmail();
                if (StringUtils.isEmpty(email)){
                    throw new Exception("该用户没有设置邮箱，请联系管理员重置密码！");
                }
            }
        }

        String verficationCode = new StringUtil().getRandomStr(4);

        StringBuilder mailTitle = new StringBuilder("").append("AdCloud系统密码重置");
        StringBuilder mailContent = new StringBuilder("");
        mailContent.append("您的验证码是：").append(verficationCode)
            .append("，请于15分钟内登陆AdCloud系统完成新密码设置。");
        MailUtil.simpleMailSend(email, null, mailContent.toString(), mailTitle.toString());

        adUser.setVerificationCode(verficationCode);
        adUser.setVerificationTime(new Date());
        adUserImpl.updateUser(adUser);

        userPojo.setEmail(email);
        return userPojo;
    }

    //密码重置修改密码
    @RequestMapping(value = "/updatePwd", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Pojo updatePwd(@RequestBody UserPwdPojo userPojo) throws Exception{
        Pojo retpojo = new CommonPojo();
        AdUser adUser = adUserImpl.qryByName(userPojo.getUsername());
        if (adUser == null){
            throw new Exception("该用户不存在！");
        }

        if (StringUtils.isEmpty(userPojo.getVerificationCode())){
            throw new Exception("请输入验证码！");
        }

        if (StringUtils.isEmpty(adUser.getVerificationCode())){
            throw new Exception("请发送验证码到邮箱验证！");
        }

        long seconds = (new Date().getTime() - adUser.getVerificationTime().getTime())/1000;
        //超时时间15分钟
        if (seconds > 60*15){
            throw new Exception("验证码已超时，请重新发送！");
        }

        if (!userPojo.getVerificationCode().equalsIgnoreCase(adUser.getVerificationCode())){
            throw new Exception("验证码错误！");
        }

        if (org.apache.commons.lang.StringUtils.isNotEmpty(userPojo.getPassword())) {
            String newpassword = userPojo.getPassword();
            String email = adUser.getEmail();

            GitlabAPI gitlabAPI = GitlabAPI.connect(gitlabServerUrl, token);
            List<GitlabUser> gitUsers = gitlabAPI.findUsers(email);
            if (CollectionUtils.isNotEmpty(gitUsers)) {
                int id = gitUsers.get(0).getId();
                gitlabAPI.updateUser(id, newpassword);
                log.error("userId===" + id + "newpassword====" + newpassword);
            }
        }

        return retpojo;
    }
}
