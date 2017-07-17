package com.asiainfo.schedule.helper;

import com.asiainfo.comm.module.models.AdStaticData;
import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.util.SshUtil;
import com.asiainfo.comm.module.build.dao.impl.AdBranchDAO;
import com.asiainfo.comm.module.build.dao.impl.AdJenkinsInfoDAO;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDeployPackageDAO;
import com.asiainfo.comm.module.build.dao.impl.AdStageDAO;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdJenkinsInfo;
import com.asiainfo.comm.module.models.AdProjectDeployPackage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/8/4.
 */
@lombok.extern.slf4j.Slf4j
public class UploadArtifactoryRunnable implements Runnable {
    String commitId;
    AdBranchDAO branchDAO;
    AdJenkinsInfoDAO jenkinsInfoDAO;
    AdProjectDeployPackageDAO packageDAO;
    AdStageDAO adStageDAO;
    AdStaticDataImpl bsStaticImpl;
    String originRote;
    String targetRote;
    String fileType;
    long buildSeqId;
    //    AdProjectDAO adProjectDAO;
    List<AdProjectDeployPackage> packageList = new ArrayList<>();
    private String artifactoryUrl;
    private String artifactoryName;
    private String artifactoryPassword;
    private long branchId;

    public UploadArtifactoryRunnable(long buildSeqId, long branchId, String commitId,
                                     AdBranchDAO branchDAO, AdJenkinsInfoDAO jenkinsInfoDAO,
                                     AdProjectDeployPackageDAO packageDAO, AdStageDAO adStageDAO, String artifactoryUrl,
                                     String artifactoryName, String artifactoryPassword, AdStaticDataImpl bsStaticImpl) {
        this.branchId = branchId;
        this.commitId = commitId;
        this.branchDAO = branchDAO;
        this.jenkinsInfoDAO = jenkinsInfoDAO;
        this.packageDAO = packageDAO;
        this.buildSeqId = buildSeqId;
        this.adStageDAO = adStageDAO;
//        this.adProjectDAO=adProjectDAO;
        this.artifactoryUrl = artifactoryUrl;
        this.artifactoryName = artifactoryName;
        this.artifactoryPassword = artifactoryPassword;
        this.bsStaticImpl = bsStaticImpl;
    }

    @Override
    public void run() {
        //调用脚本
        String repositoryName = "";
//        String repositoryName = branchDAO.qryById(branchId).getAdProject().getDeployRepository();
        List<AdStaticData> adStaticDatas = bsStaticImpl.qryByCodeType("ARTIFACTORY");
        if (adStaticDatas != null && adStaticDatas.size() > 0)
            repositoryName = adStaticDatas.get(0).getCodeValue();
        delCommintId(repositoryName);//两次的commitId若一样，怎先删除前一个commitId的包，在上传后一个commitId。
        AdBranch adBranch = branchDAO.qryById(branchId);
        targetRote = branchDAO.qryBuildFilePath(branchId, commitId);
        fileType = adBranch.getBuildFileType();
        originRote = adBranch.getOriginPath();
        AdJenkinsInfo jkInfo = jenkinsInfoDAO.qryByJkId(adBranch.getAdJenkinsInfo().getJenkinsId());
        String jenkinsUrl = jkInfo.getJenkinsUrl();
        String serverUsername = jkInfo.getServerUsername();
        String serverPassword = jkInfo.getServerPassword();
        SshUtil sshUtil = new SshUtil(jenkinsUrl, serverUsername, serverPassword, "utf-8");
        boolean suc;
        /* 为了防止回调失败，可上传包的状态无更新，旧包一直上传-测*/
//        AdStage adStage1 = adStageDAO.qryStageByStep(branchId, 1);
//        List<AdProjectDeployPackage> adProjectDeployPackage = packageDAO.qryByCommitAndBranch(commitId, branchId);
//        if (adProjectDeployPackage != null && adProjectDeployPackage.size() > 0) {
//            adStageDAO.updateStageIsupdate(adStage1.getStageId(), CommConstants.BuildConstants.STAGE.IS_UPDATE.UPDATED);
//            suc = true;
//        } else {
//            adStageDAO.updateStageIsupdate(adStage1.getStageId(), CommConstants.BuildConstants.STAGE.IS_UPDATE.NOT_UPDATE);
        suc = false;
//        }
         /*结束*/
        if (StringUtils.isEmpty(originRote) || StringUtils.isEmpty(targetRote) || StringUtils.isEmpty(repositoryName)) {
            log.error("部署包的目录地址或者仓库名为空！！！请检查repositoryName==" + repositoryName);
            suc = true;
        }
        if (null == commitId || commitId.length() <= 0) { //commmitId为空，则不进行上传包
            log.error("commitid 为空,不进行上传包，请检查!");
            suc = true;
        }

        long startTime = new Date().getTime();
        int upCount = 0;
        while (!suc) {
            upCount++;
            if (DateConvertUtils.getMinSpace(startTime,
                new Date().getTime()) > 20 || upCount > 3) {
                AdProjectDeployPackage projectDeployPackage = new AdProjectDeployPackage();
                projectDeployPackage.setAdBranch(adBranch);
                projectDeployPackage.setCommitId(commitId);
                projectDeployPackage.setCreateDate(new Date());
                projectDeployPackage.setExt1("failed");
                packageDAO.save(projectDeployPackage);
                break;
            }
            String retValue = sshUtil.exec("cd /app/aideploy/sbin;  sh Artifactory_upload.sh '"
                + originRote + "' '" + targetRote + "' '" + fileType + "' '" + repositoryName + "'");
            if (!retValue.trim().equals("")) {
                log.error("部署包上传成功！！！");
                suc = true;
            }
            retValue = "[" + retValue.trim().replace("\u0000", "").replaceAll("[\\n\\t\\r]", "").replaceAll("\\}\\{", "},{");
            String realValue = retValue + "]";
            JSONArray json = JSONArray.fromObject(realValue);
            JSONObject obj;
            int i = 0;
            for (; i < json.size(); i++) {
                obj = json.getJSONObject(i);
                String downloadUri = null;
                AdProjectDeployPackage projectDeployPackage = new AdProjectDeployPackage();
                projectDeployPackage.setAdBranch(adBranch);
                projectDeployPackage.setCommitId(commitId);
                projectDeployPackage.setCreateDate(new Date());
                if (obj.get("downloadUri") != null) {
                    downloadUri = (String) obj.get("downloadUri");
                    projectDeployPackage.setPackagePath(downloadUri);
                    projectDeployPackage.setExt1("success");
                } else {
                    projectDeployPackage.setPackagePath(retValue.trim());
                    projectDeployPackage.setExt1("failed");
                }
                projectDeployPackage.setBuildSeqId(buildSeqId);
                packageList.add(projectDeployPackage);
            }
            if (packageList.size() > 0) {
                packageDAO.save(packageList);
            }
        }
        //判断流水包的个数，只保留最近的5条记录
        System.out.println("判断流水包的个数，只保留最近的5条记录==" + branchId);
        AdBranch adBranch1 = branchDAO.qryBranchByid(branchId);
        if (adBranch1 != null) {
//            AdProject adProject = adProjectDAO.qryById(adBranch1.getAdProject().getProjectId());
//            if (adProject != null) {
            List<AdProjectDeployPackage> adList = packageDAO.qryByBranchId(branchId);
            if (CollectionUtils.isNotEmpty(adList)) {
                int pCounts = 1;
                if (StringUtils.isNotEmpty(repositoryName)) {
                    Artifactory artifactory = ArtifactoryClient.create(artifactoryUrl, artifactoryName, artifactoryPassword);
                    for (int i = 1; i < adList.size(); i++) {
                        if (!(adList.get(i).getCommitId().equals(adList.get(i - 1).getCommitId()))) {
                            pCounts++;
                        }
                        if (pCounts > 5) {
                            if (StringUtils.isNotEmpty(adList.get(i).getPackagePath()) && adList.get(i).getPackagePath().contains("http")) {
                                String[] packPath = adList.get(i).getPackagePath().split("/");
                                String path = packPath[5] + "/" + packPath[6] + "/" + packPath[7] + "/" + packPath[8];
//                                System.out.println("path==" + path);
                                try {
                                    artifactory.repository(repositoryName).delete(path);
                                } catch (Exception e) {

                                }
                                packageDAO.deleteBycommitAndbranchId(adList.get(i).getAdBranch().getBranchId(), adList.get(i).getCommitId());
                            }
                        }
                    }
                } else {
                    log.error("部署包仓库名为空！！！请检查repositoryName==" + repositoryName);
                }
            }
//            }
        }
        String atr = "curl -u " + artifactoryName + ":" + artifactoryPassword + " -X POST " + artifactoryUrl + "api/trash/empty";
        String trash = sshUtil.exec(atr);
        log.error("清空垃圾箱trash=" + trash);
    }

    //如存在commitId为XXX的部署包，先删除commitIdXXX的部署包(针对修了配置文件，而没修改代码，但需要上传包的情况)
    public void delCommintId(String repositoryName) {
        if (StringUtils.isNotEmpty(repositoryName)) {
            List<AdProjectDeployPackage> adProjectDeployPackageList = packageDAO.qryByCommitAndBranch(commitId, branchId);
            Artifactory artifactory = ArtifactoryClient.create(artifactoryUrl, artifactoryName, artifactoryPassword);
            log.error("adProjectDeployPackageList.size==" + adProjectDeployPackageList.size());
            for (AdProjectDeployPackage adProjectDeployPackage1 : adProjectDeployPackageList) {
                if (adProjectDeployPackage1.getPackagePath().contains("http")) {
                    String[] packPath = adProjectDeployPackage1.getPackagePath().split("/");
                    String path = packPath[5] + "/" + packPath[6] + "/" + packPath[7] + "/" + packPath[8];
                    try {
                        artifactory.repository(repositoryName).delete(path);
                    } catch (Exception e) {

                    }
                    packageDAO.deleteBycommitAndbranchId(adProjectDeployPackage1.getAdBranch().getBranchId(), adProjectDeployPackage1.getCommitId());
                }
            }
        } else {
            log.error("部署仓库为空！！！！");
        }
    }
}
