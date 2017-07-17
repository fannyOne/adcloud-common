package com.asiainfo.comm.module.build.controller;

import com.asiainfo.comm.common.pojo.pojoExt.AdBuildScriptPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.ConstructionDurationExtPojo;
import com.asiainfo.comm.common.pojo.pojoExt.ConstructionDurationValueExtPojo;
import com.asiainfo.comm.module.build.dao.impl.AdBranchDAO;
import com.asiainfo.comm.module.build.dao.impl.AdBuildDeployDataDAO;
import com.asiainfo.comm.module.build.dao.impl.AdJenkinsInfoDAO;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDAO;
import com.asiainfo.comm.module.build.service.impl.AdBranchImpl;
import com.asiainfo.comm.module.build.service.impl.AdBuildLogImpl;
import com.asiainfo.comm.module.build.service.impl.AdGroupImpl;
import com.asiainfo.comm.module.build.service.impl.JenkinsImpl;
import com.asiainfo.comm.module.common.AdStaticDataDAO;
import com.asiainfo.comm.module.deploy.service.impl.SystemDeployImpl;
import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdJenkinsInfo;
import com.asiainfo.comm.module.models.AdProject;
import com.asiainfo.comm.module.models.AdStaticData;
import com.asiainfo.util.JsonpUtil;
import com.avaje.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangpeng on 2016/8/24.
 */
@lombok.extern.slf4j.Slf4j
@RestController
@RequestMapping(value = "/reports")
public class AdReportController {
    @Autowired
    AdBuildDeployDataDAO adBuildDeployDataDAO;
    @Autowired
    AdBranchImpl adBranchImpl;
    @Autowired
    AdBuildLogImpl adBuildLogImpl;
    @Autowired
    AdGroupImpl adGroupImpl;
    @Autowired
    SystemDeployImpl systemDeploy;
    @Autowired
    AdStaticDataDAO bsStaticDataDAO;
    @Autowired
    AdJenkinsInfoDAO adJenkinsInfoDAO;
    @Autowired
    AdProjectDAO adProjectDAO;
    @Autowired
    AdBranchDAO adBranchDAO;
    @Autowired
    JenkinsImpl jenkinsImpl;

    @RequestMapping(value = "/qryBuildScript", produces = "application/json")
    public AdBuildScriptPojoExt qryBuildScript(@RequestParam Map<String, String> param) {
        AdBuildScriptPojoExt adBuildScriptPojoExt = new AdBuildScriptPojoExt();
        String buildShell = "";
        AdJenkinsInfo jenkinsInfo;
        AdStaticData adStaticData = bsStaticDataDAO.qryStaticDataByCodeValue("JENKINS_SET", "USERJK");
        jenkinsInfo = adJenkinsInfoDAO.qryByJkId(Long.valueOf(adStaticData.getCodeName()));
        if ("mesos".equals(jenkinsInfo.getJenkinsMode())) {
            AdBranch adBranch = adBranchDAO.qryAdBranchByname(param.get("branchName"), "", 0);//根据条件查询分支             //项目详细信息
            AdProject adProject = adProjectDAO.getSystemById(Long.valueOf(adBranch.getAdProject().getProjectId()));
            if (bsStaticDataDAO.qryStaticDataByCodeValue("OLD_GROUP", String.valueOf(adProject.getAdGroup().getGroupId())) == null) {
                buildShell = jenkinsInfo.getPathShell() + param.get("branchName") + "-down/\n";
            }

        }
        buildShell = buildShell + jenkinsImpl.qryCompileScript(param.get("compileTool"), param.get("compileVersion"), param.get("buildTool"));
        buildShell = buildShell + systemDeploy.qryBuildScriptByCode(param.get("buildTool").split(" ")[0], param.get("compileTool")) + "\r\n";
        adBuildScriptPojoExt.setBuildShell(buildShell);
        return adBuildScriptPojoExt;
    }

    @RequestMapping(value = "/qryDeployPath", produces = "application/json")
    public AdBuildScriptPojoExt qryDeployPath(@RequestParam Map<String, String> param) {
        AdBuildScriptPojoExt adBuildScriptPojoExt = new AdBuildScriptPojoExt();
        AdJenkinsInfo jenkinsInfo = null;
        AdStaticData adStaticData = bsStaticDataDAO.qryStaticDataByCodeValue("JENKINS_SET", "USERJK");
        jenkinsInfo = adJenkinsInfoDAO.qryByJkId(Long.valueOf(adStaticData.getCodeName()));
        if ("mesos".equals(jenkinsInfo.getJenkinsMode())) {
            AdBranch adBranch = adBranchDAO.qryAdBranchByname(param.get("branchName"), "", 0);//根据条件查询分支             //项目详细信息
            AdProject adProject = adProjectDAO.getSystemById(Long.valueOf(adBranch.getAdProject().getProjectId()));
            if (bsStaticDataDAO.qryStaticDataByCodeValue("OLD_GROUP", String.valueOf(adProject.getAdGroup().getGroupId())) == null) {
                String buildShell = jenkinsInfo.getPathShell() + param.get("branchName") + "-down/";
                buildShell = buildShell.substring(3);
                adBuildScriptPojoExt.setBuildShell(buildShell);
                return adBuildScriptPojoExt;

            }


        }
        adBuildScriptPojoExt.setBuildShell("/app/aideploy/jenkins/jobs/" + param.get("branchName") + "-down/workspace/");
        return adBuildScriptPojoExt;
    }


    @RequestMapping(value = "/construnction")
    public String getDayBuildReport(long projectId, long branchId) {
        ConstructionDurationExtPojo constructionDurationExtPojo = new ConstructionDurationExtPojo();
        List<ConstructionDurationValueExtPojo> consPojos = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SqlRow> list = adBuildDeployDataDAO.getNearlyTenBuildLog(projectId, branchId);
        if (list.size() != 0) {
            for (SqlRow sqlRow : list) {
                ConstructionDurationValueExtPojo consValueExtPojo = new ConstructionDurationValueExtPojo();
                consValueExtPojo.setStartDate(format.format(sqlRow.getDate("start_date")));
                if (sqlRow.getInteger("results") == 4) {
                    consValueExtPojo.setFlag(1);
                } else {
                    consValueExtPojo.setFlag(2);
                }
                if (sqlRow.getDouble("times") <= 0) {
                    consValueExtPojo.setTime(0);
                } else {
                    consValueExtPojo.setTime(sqlRow.getDouble("times"));
                }
                consPojos.add(consValueExtPojo);
            }
            constructionDurationExtPojo.setBuildInfo(consPojos);
            return JsonpUtil.modelToJson(constructionDurationExtPojo);
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/download")
    public void downUrlTxt(HttpServletResponse response, HttpServletRequest request, String fileUrl) throws Exception {
        int fileUrlLength = fileUrl.split("/").length;
        String fileName = fileUrl.split("/")[fileUrlLength - 1];
        System.out.println("fileName=================================" + fileName);
        //告诉浏览器使用什么格式编译文件
        response.setContentType("application/octet-stream;charset=UTF-8");
        //在浏览器中弹出窗口,给文件名编码,防止中文乱码,区分火狐浏览器和非火狐浏览器
        if (request.getHeader("USER-AGENT").toLowerCase().indexOf("firefox") != -1) {
            //如果是火狐浏览器,则使用下面的方式为excel文件编码
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("GB2312"), "ISO-8859-1"));
        } else {
            response.setHeader("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
        }
        //设置客户端不缓存
        //addHeader增加头文件里没有的属性
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        fileUrl = "http://" + fileUrl;
        URL url = new URL(fileUrl);
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        uc.setConnectTimeout(3 * 1000);
        String username = "admin";
        String password = "password";
        String input = username + ":" + password;
        String encoding = new sun.misc.BASE64Encoder().encode(input.getBytes());
        uc.setRequestProperty("Authorization", "Basic " + encoding);
        uc.setDoInput(true);//设置是否要从 URL 连接读取数据,默认为true
        uc.connect();
        InputStream iputstream = uc.getInputStream();
        System.out.println("file size is:" + uc.getContentLength());//打印文件长度
        byte[] buffer = new byte[4 * 1024];
        int byteRead = -1;
        OutputStream out = response.getOutputStream();
        while ((byteRead = (iputstream.read(buffer))) != -1) {
            out.write(buffer, 0, byteRead);
        }
        out.close();
        iputstream.close();
        uc.disconnect();
    }

}
