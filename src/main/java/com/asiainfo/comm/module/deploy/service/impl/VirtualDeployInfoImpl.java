package com.asiainfo.comm.module.deploy.service.impl;

import com.asiainfo.comm.common.enums.FileState;
import com.asiainfo.comm.module.models.AdStaticData;
import com.asiainfo.util.AnsibleCommandUtils;
import com.asiainfo.util.JSchUtil;
import com.asiainfo.util.SshUtil;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDeployPackageDAO;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.asiainfo.comm.module.deploy.dao.impl.VirtualDeployInfoDAO;
import com.asiainfo.comm.module.models.AdProjectDeployPackage;
import com.asiainfo.comm.module.models.AdSystemDeployLog;
import com.asiainfo.comm.module.models.AdVirtualEnvironment;
import com.asiainfo.comm.module.models.ansibleCommand.DeployCommand;
import com.asiainfo.comm.module.models.ansibleCommand.FileCommand;
import com.asiainfo.comm.module.models.ansibleCommand.RestartCommand;
import com.asiainfo.comm.module.models.functionModels.GetUrlCommand;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by guojian on 9/28/16.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class VirtualDeployInfoImpl {

    @Value("${artifactory.user.name}")
    private String artifactoryUser;
    @Value("${artifactory.user.password}")
    private String artifactorypassword;
    @Autowired
    private VirtualDeployInfoDAO virtualDeployInfoDAO;
    @Autowired
    SystemDeployLogImpl systemDeployLogImpl;
    @Autowired
    AdStaticDataImpl bsStaticDataImpl;
    @Autowired
    AdProjectDeployPackageDAO adProjectDeployPackageDAO;

    private SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public List<AdVirtualEnvironment> findByProjectId(Long projectId) {
        return virtualDeployInfoDAO.findByProjectId(projectId);
    }

    public String deployVirturl(String commitId, Long virtualId) {
        String flag = "success";
        try {

            List<AdProjectDeployPackage> deployPackages = adProjectDeployPackageDAO.qryByCommitId(commitId);
            if (deployPackages.size() < 1) {
                return "no Repository for this commit id!";
            }
            AdVirtualEnvironment ave = virtualDeployInfoDAO.findById(virtualId);
            SshUtil sshUtil = new SshUtil(ave.getServerUrl(), ave.getServerUsername(), ave.getServerPassword(), "utf-8");
            for (AdProjectDeployPackage deployPackage : deployPackages) {
                String cmd = "cd " + ave.getFilePath() + "; sh " + ave.getFileName() + " " + deployPackage.getPackagePath();
                String st = sshUtil.exec(cmd);
                System.out.print(st);
            }
        } catch (Exception e) {
            flag = e.getLocalizedMessage();
        }
        return flag;
    }

    public String deployVirturl(String commitId, Long envId, AdSystemDeployLog deployLog, Long branchId) {
        AdStaticData AdStaticData = bsStaticDataImpl.qryStaticDataByCodeValue("VM_ANSIBLE", String.valueOf(envId));
        if (null != AdStaticData) return deployVirturlAnsible(commitId, envId, deployLog, branchId);
        return deployVirturlShell(commitId, envId, deployLog, branchId);

    }

    public String deployVirturlShell(String commitId, Long envId, AdSystemDeployLog deployLog, Long branchId) {
        String flag = "success";
        deployLog.setStartTime(new Date());
        deployLog.setDeployComment("");
        String packageName = "";
        String logs = "";
        try {
            AdVirtualEnvironment ave = virtualDeployInfoDAO.findById(envId);
            List<AdProjectDeployPackage> deployPackages = adProjectDeployPackageDAO.qryByCommitAndBranch(commitId, branchId);
            if (deployPackages.size() < 1) {
                deployLog.setDeployResult(2);
                deployLog.setEndTime(new Date());
                flag = "no Repository for this commit id!";
                deployLog.setDeployComment(flag);
                systemDeployLogImpl.addLogsBySystemId(deployLog);
                return flag;
            }
            log.error("重启主机1=ave.getServerUrl()" + ave.getServerUrl() + "==" + ave.getServerUsername() + "==" + ave.getServerPassword());
            for (AdProjectDeployPackage deployPackage : deployPackages) {
                if (deployPackage.getPackagePath().split("/").length > 0) {
                    packageName = deployPackage.getPackagePath().split("/")[deployPackage.getPackagePath().split("/").length - 1];
                    if (!ave.getPackageName().equals(packageName)) {
                        continue;
                    }
                    String md5 = vmUploadToDocs(deployPackage, ave); //选择包ftp到对应的虚机
                    logs = "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h>";
                    deployLog.setDeployComment(deployLog.getDeployComment() + logs + ": upload packages: " + md5 + "\t\n");
                    if (md5.equals("failed")) {
                        deployLog.setDeployResult(2);
                        deployLog.setEndTime(new Date());
                        deployLog.setDeployComment(deployLog.getDeployComment() + md5);
                        break;
                    }
                    //执行发布重启
                    for (String ip : ave.getServerUrl().split(",")) {
                        String st = JSchUtil.sshShell(ip, ave.getServerUsername(), ave.getServerPassword(), -1, "", "", ave.getFilePath(), ave.getFileName());
                        deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> " + "restart ip: " + ip + " shell: " + ave.getFileName() + "\t\n");
                        deployLog.setDeployComment(deployLog.getDeployComment() + st + "\t\n");
                        deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> " + "重启 " + ip + " : " + ave.getFileName() + "完成====================================" + "\t\n");
                        systemDeployLogImpl.addLogsBySystemId(deployLog);
                    }
                }
            }
            deployLog.setDeployResult(1);
            deployLog.setEndTime(new Date());
            deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> " + flag);
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        } catch (Exception e) {
            deployLog.setDeployResult(2);
            deployLog.setEndTime(new Date());
            flag = e.getLocalizedMessage();
            deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> " + flag);
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        }
        return flag;
    }

    public String restartVirturl(Long envId, AdSystemDeployLog deployLog) {
        AdStaticData AdStaticData = bsStaticDataImpl.qryStaticDataByCodeValue("VM_ANSIBLE", String.valueOf(envId));
        if (null != AdStaticData) return restartVirturlByAnsible(envId, deployLog);
        return restartVirturlbyShell(envId, deployLog);
    }

    public String restartVirturlbyShell(Long envId, AdSystemDeployLog deployLog) {
        String flag = "success";
        deployLog.setStartTime(new Date());
        String logs = "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> begin restart: ";
        deployLog.setDeployComment(logs + "\t\n");
        try {

            AdVirtualEnvironment ave = virtualDeployInfoDAO.findById(envId);
            for (String ip : ave.getServerUrl().split(",")) {
                log.error("重启主机2ip=" + ip + "==" + ave.getServerUsername() + "==" + ave.getServerPassword());
                String st = JSchUtil.sshShell(ip, ave.getServerUsername(), ave.getServerPassword(), -1, "", "", ave.getFilePath(), ave.getFileName());
                logs = "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> restart ip: " + ip + " shell: " + ave.getFileName();
                deployLog.setDeployComment(deployLog.getDeployComment() + logs);
                deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> " + st + "\t\n");
                deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> " + "重启" + ip + " : " + ave.getFileName() + "完成====================================" + "\t\n");
                systemDeployLogImpl.addLogsBySystemId(deployLog);
            }
            deployLog.setDeployResult(1);
            deployLog.setEndTime(new Date());
            deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> " + flag);
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        } catch (Exception e) {
            deployLog.setDeployResult(2);
            deployLog.setEndTime(new Date());
            flag = e.getMessage();
            log.error(e.getMessage(), e);
            deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> " + flag);
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        }
        return flag;
    }

    public AdVirtualEnvironment qryById(long envId) {
        return virtualDeployInfoDAO.findById(envId);
    }

    //根据下载路径下载包,上传到dcos的ftp ,并取得该包的MD5
    public String vmUploadToDocs(AdProjectDeployPackage projectDeployPackage, AdVirtualEnvironment adVirtualEnvironment) throws Exception {
        try {
            List<AdStaticData> AdStaticData = bsStaticDataImpl.qryByCodeType("VM_DEPLOY_IP");
            String[] linuxIp = AdStaticData.get(0).getCodeValue().split(":");
            String jenkinsUrl = linuxIp[0];
            String serverUsername = linuxIp[1];
            String serverPassword = linuxIp[2];
            log.error("upload jenkinsUrl=" + jenkinsUrl + "==" + serverUsername + "==" + serverPassword);
            //建立连接
            SshUtil sshUtil = new SshUtil(jenkinsUrl, serverUsername, serverPassword, "utf-8");

            //调用ftp上传
            String cmd = "cd " + AdStaticData.get(0).getExternCodeType() + "; sh  Artifactory_download_ftp_vm.sh '"
                + projectDeployPackage.getPackagePath() + "' "
                + " '" + adVirtualEnvironment.getServerUrl() + "'  '" + adVirtualEnvironment.getServerUsername() + "' '" + adVirtualEnvironment.getServerPassword() + "' '" + adVirtualEnvironment.getDestinationAddress() + "'";
            log.error("resultInput" + cmd);
            String md5 = sshUtil.exec_noResult(cmd);
            log.error("resultOutput" + md5);
            return md5;
        } catch (Exception e) {
            throw e;
        }
    }

    public String restartVirturlByAnsible(Long envId, AdSystemDeployLog deployLog) {
        String flag = "success";
        deployLog.setStartTime(new Date());
        String logs = "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> begin restart: ";
        deployLog.setDeployComment(logs + "\t\n");
        try {
            AdVirtualEnvironment adVirtualEnvironment = virtualDeployInfoDAO.findById(envId);
            SshUtil sshUtil = getVMDeploySSH();
            RestartCommand command = new RestartCommand();
            command.setHostIp(adVirtualEnvironment.getServerUrl());
            command.setPath(adVirtualEnvironment.getFilePath());
            command.setShell(adVirtualEnvironment.getFileName());
            String cmd = AnsibleCommandUtils.restartCommand(command);
            String st = AnsibleCommandUtils.exec(sshUtil, cmd);
            deployLog.setDeployResult(1);
            deployLog.setEndTime(new Date());
            deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> " + st + "\t\n");
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            deployLog.setDeployResult(2);
            deployLog.setEndTime(new Date());
            flag = e.getMessage();
            deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> " + flag);
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        }
        return flag;
    }

    public String deployVirturlAnsible(String commitId, Long envId, AdSystemDeployLog deployLog, Long branchId) {
        String flag = "success";
        deployLog.setStartTime(new Date());
        try {
            AdVirtualEnvironment ve = virtualDeployInfoDAO.findById(envId);
            if (null == ve) {
                flag = "根据环境编号:" + envId + "未能找到对应的环境信息";
                throw new Exception(flag);
            }
            List<AdProjectDeployPackage> deployPackages = adProjectDeployPackageDAO.qryByCommitAndBranch(commitId, branchId);
            if (CollectionUtils.isEmpty(deployPackages)) {
                flag = "根据commitId:" + commitId + "未能获取对应的部署包信息";
                throw new Exception(flag);
            }
            for (AdProjectDeployPackage deployPackage : deployPackages) {
                if (StringUtils.isEmpty(deployPackage.getPackagePath()) || deployPackage.getPackagePath().split("/").length < 8) {
                    flag = "部署包下载路径错误";
                    throw new Exception(flag);
                }
                String packageName = deployPackage.getPackagePath().split("/")[deployPackage.getPackagePath().split("/").length - 1];
                if (!ve.getPackageName().equals(packageName)) {
                    continue;
                }
                //从仓库中下载文件到跳板机
                //根据流水类型判断是生产还是测试环境跳板机
                List<AdStaticData> adStaticData = bsStaticDataImpl.qryByCodeType("VM_FTP_DEPLOY");
                String packagePath = adStaticData.get(0).getExternCodeType() + deployPackage.getPackagePath().split("/")[7] + "_package/";
                SshUtil sshUtil = getVMDeploySSH();
                //创建目录
                FileCommand fileCommand = new FileCommand();
                fileCommand.setHostIp(adStaticData.get(0).getCodeValue());
                fileCommand.setDest(packagePath);
                fileCommand.setState(FileState.directory.toString());
                AnsibleCommandUtils.exec(sshUtil, AnsibleCommandUtils.fileCommand(fileCommand));

                GetUrlCommand getUrlCommand = new GetUrlCommand();
                getUrlCommand.setHostIp(adStaticData.get(0).getCodeValue());
                getUrlCommand.setUrl(deployPackage.getPackagePath());
                getUrlCommand.setUrlUser(artifactoryUser);
                getUrlCommand.setUrlPassword(artifactorypassword);
                getUrlCommand.setDest(packagePath);
                String cmd = AnsibleCommandUtils.getUrlCommand(getUrlCommand);
                String st = AnsibleCommandUtils.exec(sshUtil, cmd);
                String logs = "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h>";
                deployLog.setDeployComment(logs + ": download packages: " + st + "\t\n");
                log.error(deployLog.getDeployComment());
                //从跳板机中上传到对应虚机
                DeployCommand command = new DeployCommand();
                command.setHostIp(ve.getServerUrl());
                command.setSPath(packagePath);
                command.setDPath(ve.getDestinationAddress());
                command.setFileName(packageName);
                String cmd1 = AnsibleCommandUtils.deployCommand(command);
                String st1 = AnsibleCommandUtils.exec(sshUtil, cmd1);
                String logs1 = "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h>";
                // deployLog.setDeployComment(deployLog.getDeployComment() +logs1 + ": scp packages command: " + cmd1 + "\t\n");
                deployLog.setDeployComment(deployLog.getDeployComment() + logs1 + ": scp packages: " + st1 + "\t\n");
                //执行发布重启
                RestartCommand restartCommand = new RestartCommand();
                restartCommand.setHostIp(ve.getServerUrl());
                restartCommand.setPath(ve.getFilePath());
                restartCommand.setShell(ve.getFileName());
                String cmd2 = AnsibleCommandUtils.restartCommand(restartCommand);
                String st2 = AnsibleCommandUtils.exec(sshUtil, cmd2);
                String logs2 = "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h>";
                //deployLog.setDeployComment(deployLog.getDeployComment() +logs2 + ": restart command: " + cmd2 + "\t\n");
                deployLog.setDeployComment(deployLog.getDeployComment() + logs2 + ": restart : " + st2 + "\t\n");
            }
            deployLog.setDeployResult(1);
            deployLog.setEndTime(new Date());
            deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> " + flag);
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            deployLog.setDeployResult(2);
            deployLog.setEndTime(new Date());
            flag = e.getLocalizedMessage();
            deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> " + flag);
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        }
        return flag;
    }

    public SshUtil getVMDeploySSH() {
        List<AdStaticData> AdStaticData = bsStaticDataImpl.qryByCodeType("VM_DEPLOY_IP");
        String[] linuxIp = AdStaticData.get(0).getCodeValue().split(":");
        String jenkinsUrl = linuxIp[0];
        String serverUsername = linuxIp[1];
        String serverPassword = linuxIp[2];
        log.error("upload jenkinsUrl=" + jenkinsUrl + "==" + serverUsername + "==" + serverPassword);
        return new SshUtil(jenkinsUrl, serverUsername, serverPassword, "utf-8");
    }

}
