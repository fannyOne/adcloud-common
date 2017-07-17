package com.asiainfo.comm.module.build.controller;

import com.asiainfo.comm.module.build.service.impl.AdJenkinsInfoImpl;
import com.asiainfo.comm.module.models.AdJenkinsInfo;
import com.asiainfo.util.SshUtil;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDAO;
import com.asiainfo.comm.module.models.AdProject;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by loujian on 16/8/1.
 */
@Controller
@RequestMapping("/thirdPart")
public class ThirdPartController {

    @Autowired
    AdProjectDAO adProjectDAO;
    @Value("${gitlab.server.url}")
    private String gitlabServerUrl;
    @Autowired
    AdJenkinsInfoImpl adJenkinsInfoImpl;
    //LFkTZyaBWgJziSf19wXc
    @Value("${gitlab.admin.token}")
    private String token;
    private OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

    @RequestMapping(value = "/getGitlibUrl", produces = "application/json")
    public
    @ResponseBody
    Map<String, String> getGitlibUrl(@RequestParam(value = "projectId") String projectId) {
        Map<String, String> result = new HashMap<String, String>();

        AdProject project = adProjectDAO.getSystemById(Long.parseLong(projectId));
        //http://20.26.25.47/api/v3/projects/51/repository/branches?private_token=LFkTZyaBWgJziSf19wXc   分支
        result.put("branches", gitlabServerUrl + "/api/v3/projects/" + project.getGitProjectid() + "/repository/branches?private_token=" + token);
        //http://20.26.25.47/api/v3/projects/51/repository/tags/?private_token=LFkTZyaBWgJziSf19wXc     tags
        result.put("tags", gitlabServerUrl + "/api/v3/projects/" + project.getGitProjectid() + "/repository/tags?private_token=" + token);
        // http://20.26.25.47/api/v3/projects/51/repository/commits/?private_token=LFkTZyaBWgJziSf19wXc   commits
        result.put("commits", gitlabServerUrl + "/api/v3/projects/" + project.getGitProjectid() + "/repository/commits?private_token=" + token);
        // http://20.26.25.47/api/v3/projects/51/repository/tree?ref_name=dev0804&private_token=LFkTZyaBWgJziSf19wXc
        result.put("files", gitlabServerUrl + "/api/v3/projects/" + project.getGitProjectid() + "/repository/tree?private_token=" + token);

        result.put("codeStore", project.getCodeStore());
        return result;

    }

    @RequestMapping(value = "/getGitlibUrlByGitId", produces = "application/json")
    public
    @ResponseBody
    Map<String, String> getGitlibUrlByGitId(@RequestParam(value = "projectId") String projectId) {
        Map<String, String> result = new HashMap<String, String>();
        //http://20.26.25.47/api/v3/projects/51/repository/branches?private_token=LFkTZyaBWgJziSf19wXc   分支
        result.put("branches", gitlabServerUrl + "/api/v3/projects/" + projectId + "/repository/branches?private_token=" + token);
        //http://20.26.25.47/api/v3/projects/51/repository/tags/?private_token=LFkTZyaBWgJziSf19wXc     tags
        result.put("tags", gitlabServerUrl + "/api/v3/projects/" + projectId + "/repository/tags?private_token=" + token);
        // http://20.26.25.47/api/v3/projects/51/repository/commits/?private_token=LFkTZyaBWgJziSf19wXc   commits
        result.put("commits", gitlabServerUrl + "/api/v3/projects/" + projectId + "/repository/commits?private_token=" + token);
        // http://20.26.25.47/api/v3/projects/51/repository/tree?ref_name=dev0804&private_token=LFkTZyaBWgJziSf19wXc
        result.put("files", gitlabServerUrl + "/api/v3/projects/" + projectId + "/repository/tree?private_token=" + token);
        return result;

    }
    //curl --request POST --header "PRIVATE-TOKEN: 9koXpg98eAheJpvBs5tK" "https://gitlab.example.com/api/v3/projects/5/repository/branches?branch_name=newbranch&ref=master"
    //http://aigitlab.com/devops/adcloud-platform.git

    @RequestMapping(value = "/createBranch", produces = "application/json")
    public
    @ResponseBody
    boolean createBranch(@RequestParam(value = "branchName") String branchName, @RequestParam(value = "fromBranchName") String fromBranchName, @RequestParam(value = "projectId") String projectId) {
        boolean result = false;
        AdProject project = adProjectDAO.getSystemById(Long.parseLong(projectId));
        AdJenkinsInfo adJenkinsInfo = adJenkinsInfoImpl.qryByJkId(1L);
        String codeStore = project.getCodeStore();
        String stores = codeStore.split("//")[1];//codeStore.substring(codeStore.indexOf("/"));
        stores = stores == null ? "devops/adcloud-common.git" : stores;
        SshUtil sshUtil = new SshUtil(adJenkinsInfo.getJenkinsUrl(), adJenkinsInfo.getServerUsername(), adJenkinsInfo.getServerPassword(), "utf-8");
        String cmd = "cd /app/aideploy/sbin;  sh  CreatBranch.sh '"
            + branchName + "' '" + stores + "' '" + fromBranchName + "'";

        try {
            sshUtil.exec(cmd);
            result = true;
        } catch (Exception e) {
        }
        return result;
    }

    @RequestMapping(value = "/createBranchByGitId", produces = "application/json")
    public
    @ResponseBody
    boolean createBranchByGitId(@RequestParam(value = "branchName") String branchName, @RequestParam(value = "fromBranchName") String fromBranchName, @RequestParam(value = "store") String store) {
        boolean result = false;
        //AdProject project = adProjectDAO.getSystemById(Long.parseLong(projectId));
        //String codeStore = project.getCodeStore();
        AdJenkinsInfo adJenkinsInfo = adJenkinsInfoImpl.qryByJkId(1L);
        String stores = store.split("//")[1];//codeStore.substring(codeStore.indexOf("/"));
        stores = stores == null ? "devops/adcloud-common.git" : stores;
        SshUtil sshUtil = new SshUtil(adJenkinsInfo.getJenkinsUrl(), adJenkinsInfo.getServerUsername(), adJenkinsInfo.getServerPassword(), "utf-8");
        String cmd = "cd /app/aideploy/sbin;  sh  CreatBranch.sh '"
            + branchName + "' '" + stores + "' '" + fromBranchName + "'";

        try {
            sshUtil.exec(cmd);
            result = true;
        } catch (Exception e) {
        }
        return result;
    }

    @RequestMapping(value = "/getStatus", produces = "application/json")
    public
    @ResponseBody
    Map<String, Boolean> getStatus(@RequestBody Map<String, String> params) {
        Map<String, Boolean> result = new HashMap<String, Boolean>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            Boolean canCon = canConnect(entry.getValue());
            result.put(entry.getKey(), canCon);
        }
        return result;
    }

    private Boolean canConnect(String url) {
        String urlNameString = url;
        URL realUrl = null;
        try {
            realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = null;
            connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            // 建立实际的连接
            connection.connect();
            Map<String, List<String>> map = connection.getHeaderFields();
            if (map.containsKey("Content-Type")) {
                return true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
