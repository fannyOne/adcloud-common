package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.enums.FileState;
import com.asiainfo.util.AnsibleCommandUtils;
import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.util.SshUtil;
import com.asiainfo.comm.module.build.dao.impl.AdJenkinsInfoDAO;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDeployPackageDAO;
import com.asiainfo.comm.module.build.dao.impl.AdStageDAO;
import com.asiainfo.comm.module.deploy.service.impl.VirtualDeployInfoImpl;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.models.ansibleCommand.FileCommand;
import com.asiainfo.comm.module.models.ansibleCommand.FtpPutCommand;
import com.asiainfo.comm.module.models.functionModels.GetUrlCommand;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by HK on 2016/8/8.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class AdProjectDeployPackageImpl {

    @Autowired
    AdProjectDeployPackageDAO projectDeployPackageDAO;
    @Autowired
    AdStageDAO stageDAO;
    @Autowired
    AdJenkinsInfoDAO jenkinsInfoDAO;
    @Autowired
    VirtualDeployInfoImpl virtualDeployInfoImpl;
    @Autowired
    AdStaticDataImpl bsStaticDataImpl;
    @Value("${artifactory.user.name}")
    private String artifactoryUser;
    @Value("${artifactory.user.password}")
    public String artifactorypassword;

    public List<AdProjectDeployPackage> qryByBranchId(Long branchId) {
        List<AdProjectDeployPackage> deployPackageList = null;
        AdStage stage = stageDAO.qryByBranchAndStageCode(branchId, 1);
        String commitId;
        if (stage != null) {
            commitId = stage.getPreCommitId();
            deployPackageList = projectDeployPackageDAO.qryByCommitId(commitId);
        }
        return deployPackageList;
    }

    /**
     * 根据下载路径下载包,上传到dcos的ftp ,并取得该包的MD5
     *
     * @param projectDeployPackage
     * @return
     */
    public String uploadToDocs(AdProjectDeployPackage projectDeployPackage, AdDcosDeployInfo adDcosDeployInfo) {
        AdBranch adBranch = projectDeployPackage.getAdBranch();
        AdJenkinsInfo jkInfo = jenkinsInfoDAO.qryByJkId(adBranch.getAdJenkinsInfo().getJenkinsId());
        String jenkinsUrl = jkInfo.getJenkinsUrl();
        String serverUsername = jkInfo.getServerUsername();
        String serverPassword = jkInfo.getServerPassword();
        //建立连接
        SshUtil sshUtil = new SshUtil(jenkinsUrl, serverUsername, serverPassword, "utf-8");
        boolean suc = false;
        //调用ftp上传
        String cmd = "cd /app/aideploy/sbin;  sh  Artifactory_download_ftp.sh '"
            + projectDeployPackage.getPackagePath() + "' "
            + " '" + adDcosDeployInfo.getDcosFtpUrl() + "'  '" + adDcosDeployInfo.getDcosFtpUsername() + "' '" + adDcosDeployInfo.getDcosFtpPassword() + "' '" + adDcosDeployInfo.getDcosFtpPath() + "'";
        log.error("md5input" + cmd);
        String md5 = sshUtil.exec(cmd);
        log.error("md5output" + md5);
        return md5.substring(md5.indexOf("md5=") + 4, md5.indexOf("=md5"));
    }

    public String uploadToDocsByAnsible(AdProjectDeployPackage deployPackage, AdDcosDeployInfo deployLog) {
        SshUtil sshUtil = virtualDeployInfoImpl.getVMDeploySSH();
        List<AdStaticData> adStaticData = bsStaticDataImpl.qryByCodeType("VM_FTP_DEPLOY");
        String packagePath = adStaticData.get(0).getExternCodeType() + deployPackage.getPackagePath().split("/")[7] + "_package/";
        //创建目录
        FileCommand fileCommand = new FileCommand();
        fileCommand.setHostIp(adStaticData.get(0).getCodeValue());
        fileCommand.setDest(packagePath);
        fileCommand.setState(FileState.directory.toString());
        String fileResutl = AnsibleCommandUtils.exec(sshUtil, AnsibleCommandUtils.fileCommand(fileCommand));
        log.error(fileResutl);


        //下载文件
        GetUrlCommand getUrlCommand = new GetUrlCommand();
        getUrlCommand.setHostIp(adStaticData.get(0).getCodeValue());
        getUrlCommand.setUrl(deployPackage.getPackagePath());
        getUrlCommand.setUrlUser(artifactoryUser);
        getUrlCommand.setUrlPassword(artifactorypassword);
        getUrlCommand.setDest(packagePath);
        String cmd = AnsibleCommandUtils.getUrlCommand(getUrlCommand);
        String md5 = AnsibleCommandUtils.exec(sshUtil, cmd);
        log.error(md5);
        //FTP
        FtpPutCommand command = new FtpPutCommand();
        command.setHostIp(adStaticData.get(0).getCodeValue());
        command.setFtpService(deployLog.getDcosFtpUrl());
        String packageName = deployPackage.getPackagePath().split("/")[deployPackage.getPackagePath().split("/").length - 1];
        command.setFileName(packagePath + packageName);
        command.setFtpUser(deployLog.getDcosFtpUsername());
        command.setFtpPassword(deployLog.getDcosFtpPassword());
        command.setFtpPath(deployLog.getDcosFtpPath());
        String cmd2 = AnsibleCommandUtils.ftpPutCommand(command);
        String ftpResult = AnsibleCommandUtils.exec(sshUtil, cmd2);
        log.error(ftpResult);
        JSONObject jsonObject = JSONObject.fromObject(md5.substring(md5.indexOf("{")));
        return String.valueOf(jsonObject.get("md5sum"));
    }


    //根据下载路径下载包
    public boolean downloadPackageToHost(AdProjectDeployPackage projectDeployPackage, SshUtil sshUtil) {
        AdBranch adBranch = projectDeployPackage.getAdBranch();
        long startTime = new Date().getTime();
        boolean suc = false;
        String cmd = "cd /app/aideploy/sbin;  sh Artifactory_download.sh '"
            + projectDeployPackage.getPackagePath() + "' " + "'" + adBranch.getOriginPath() + "'";
        while (!suc) {
            if (DateConvertUtils.getMinSpace(startTime,
                new Date().getTime()) > 20) {
                return false;
            }
            String retValue = sshUtil.exec(cmd);
            if (!retValue.trim().equals("")) {
                return true;
            }
        }
        return false;
    }

    public AdProjectDeployPackage qryById(Long packageId) {
        return projectDeployPackageDAO.qryById(packageId);
    }

    public List<AdProjectDeployPackage> qryPackageByBranchId(long branchId) {
        return projectDeployPackageDAO.qryByBranchId(branchId);
    }
}
