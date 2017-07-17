package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.common.pojo.pojoExt.AdBranchCheckPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.vmEnvInfoPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.vmEvnPojoExt;
import com.asiainfo.comm.module.build.service.impl.AdBranchImpl;
import com.asiainfo.comm.module.build.service.impl.AdDcosDeployDtlImpl;
import com.asiainfo.comm.module.build.service.impl.AdDcosDeployInfoImpl;
import com.asiainfo.comm.module.build.service.impl.AdJenkinsInfoImpl;
import com.asiainfo.comm.module.deploy.dao.impl.AdVirtualBranchRelateDAO;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.models.query.QAdDcosBranchRelate;
import com.asiainfo.comm.module.models.query.QAdVirtualBranchRelate;
import com.asiainfo.comm.module.models.query.QAdVirtualEnvironment;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlRow;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhangpeng on 2016/10/18.
 */
@Component("AdVirtualEnvironmentDAO")
public class AdVirtualEnvironmentDAO {
    @Autowired
    AdProjectDAO adProjectDAO;
    @Autowired
    AdBranchImpl adBranchImpl;
    @Autowired
    AdDcosDeployInfoImpl adDcosDeployInfoImpl;
    @Autowired
    AdDcosDeployDtlImpl adDcosDeployDtlImpl;
    @Autowired
    AdJenkinsInfoImpl adJenkinsInfoImpl;
    @Autowired
    AdVirtualBranchRelateDAO adVirtualBranchRelateDAO;
    @Qualifier("adcloud")
    @Autowired
    EbeanServer ebeanServer;
    @Autowired
    private AdBranchDAO adBranchDAO;

    public void beginTraction() {
        ebeanServer.beginTransaction();
    }

    public void endTraction() {
        ebeanServer.endTransaction();
    }

    public void commitTraction() {
        ebeanServer.commitTransaction();
    }

    public List<AdVirtualEnvironment> getAllVmEnv(int pageNum, int pageSize, String[] projects) {
        QAdVirtualEnvironment qAdVirtualEnvironment = new QAdVirtualEnvironment().state.eq(1);
        for (String project : projects) {
            qAdVirtualEnvironment = qAdVirtualEnvironment.or().adProject.projectId.eq(Long.valueOf(project));
        }
        List<AdVirtualEnvironment> adVirtualEnvironmentList = qAdVirtualEnvironment.findPagedList(pageNum, pageSize)
            .getList();
        return adVirtualEnvironmentList;
    }

    public long getVmEnvCount(String[] projects) {
        QAdVirtualEnvironment qAdVirtualEnvironment = new QAdVirtualEnvironment().state.eq(1);
        for (String project : projects) {
            qAdVirtualEnvironment = qAdVirtualEnvironment.or().adProject.projectId.eq(Long.valueOf(project));
        }
        return qAdVirtualEnvironment.findRowCount();
    }

    public AdVirtualEnvironment getSingleVmEvn(long projectId, long virtualId) throws Exception {
        QAdVirtualEnvironment qAdVirtualEnvironment = new QAdVirtualEnvironment();
        qAdVirtualEnvironment.virtualId.eq(virtualId);
        qAdVirtualEnvironment.adProject.projectId.eq(projectId);
        AdVirtualEnvironment adVirtualEnvironment = qAdVirtualEnvironment.findUnique();
        return adVirtualEnvironment;

    }

    public AdVirtualEnvironment qryVirtureName(long virtualId, long projectId, String virtureName) {
        List<AdVirtualEnvironment> List = new QAdVirtualEnvironment().virtualName.eq(virtureName).adProject.projectId
            .eq(projectId).state.eq(1).findList();
        if (List != null && List.size() > 0 && List.get(0).getVirtualId() != virtualId) {
            return List.get(0);
        }
        return null;
    }

    public AdVirtualEnvironment qryIinfoByBranchID(long branchId) {
        List<AdVirtualEnvironment> List = new QAdVirtualEnvironment().state.eq(1).adBranch.branchId.eq(branchId)
            .findList();
        if (List != null && List.size() > 0) {
            return List.get(0);
        }
        return null;
    }

    public void updateVmEvnById(vmEvnPojoExt req) throws Exception {
        vmEnvInfoPojoExt[] envInfoPojoExts;
        AdVirtualEnvironment adVirtualEnvironment;
        AdVirtualEnvironment list;
        if (req != null && req.getObj() != null) {
            envInfoPojoExts = req.getObj();
            adVirtualEnvironment = qryVirtureName(req.getVirtualId(), req.getProjectId(), envInfoPojoExts[0]
                .getVirtualName());
            if (adVirtualEnvironment != null) {
                throw new Exception("该流水环境已存在");
            }
            list = getSingleVmEvn(req.getProjectId(), req.getVirtualId());
            if (list != null) {
                list.setFileName(envInfoPojoExts[0].getFileName());
                list.setFilePath(envInfoPojoExts[0].getFilePath());
                if (envInfoPojoExts[0].getServerPassword() != null && !StringUtils.isEmpty(envInfoPojoExts[0].getServerPassword())) {
                    list.setServerPassword(envInfoPojoExts[0].getServerPassword());
                }
                list.setServerUrl(envInfoPojoExts[0].getServerUrl());
                list.setVirtualName(envInfoPojoExts[0].getVirtualName());
                list.setServerUsername(envInfoPojoExts[0].getServerUsername());
                list.setDestinationAddress(envInfoPojoExts[0].getDestinationAddress());
                list.setPackageName(envInfoPojoExts[0].getPackageName());
                list.setSourceAddress(envInfoPojoExts[0].getSourceAddress());
                ebeanServer.save(list);
            }
        } else {
            throw new Exception("传入参数为空");
        }
    }

    public void updateVmEvnRelateById(vmEvnPojoExt req) throws Exception {
        String[] branchIdArray = null;
        List<AdVirBranchRelate> adVirBranchRelateList = new ArrayList<>();
        if (req != null && req.getObj() != null) {
            vmEnvInfoPojoExt[] envInfoPojoExts;
            envInfoPojoExts = req.getObj();
            if (!StringUtils.isEmpty(envInfoPojoExts[0].getBranchIds())) {
                branchIdArray = envInfoPojoExts[0].getBranchIds().split(",");
            }
            List<AdVirtualBranchRelate> adVirtualBranchRelates = new QAdVirtualBranchRelate().adVirtualEnvironment.virtualId.eq(req.getVirtualId()).findList();
            if (CollectionUtils.isNotEmpty(adVirtualBranchRelates)) {
                for (AdVirtualBranchRelate adVirtualBranchRelate : adVirtualBranchRelates) {
                    adVirtualBranchRelate.setState(0L);
                    adVirtualBranchRelate.save();
                }
                ebeanServer.saveAll(adVirtualBranchRelates);
            }
            if (ArrayUtils.isNotEmpty(branchIdArray)) {
                AdVirBranchRelate adVirtualBranchRelate = null;
                for (String branchId : branchIdArray) {
                    adVirtualBranchRelate = new AdVirBranchRelate();
                    adVirtualBranchRelate.setState(1l);
                    adVirtualBranchRelate.setBranchId(Long.valueOf(branchId));
                    adVirtualBranchRelate.setVmId(req.getVirtualId());
                    adVirtualBranchRelate.setCreateDate(new Date());
                    adVirBranchRelateList.add(adVirtualBranchRelate);
                }
                if (CollectionUtils.isNotEmpty(adVirBranchRelateList)) {
                    ebeanServer.saveAll(adVirBranchRelateList);
                }
            }
        }
    }

    public String getServerUrl(AdBranch adBranch) {//传adBranch对象来查找部署主机ip
        //获取envtype
        String envType = adBranch.getEnvType();
        if (adBranch.getEnvId() == null) {
            return "";
        }
        long envID = adBranch.getEnvId();
        String str = "";
        //根据EN_Type来判断是虚机环境还是dcos环境
        if ("vm".equals(envType)) {
            List<AdVirtualEnvironment> adVirtualEnvironmentList = new QAdVirtualEnvironment().virtualId.eq(envID).findList();
            if (CollectionUtils.isNotEmpty(adVirtualEnvironmentList)) {
                str = adVirtualEnvironmentList.get(0).getServerUrl();
            }
        } else if ("dcos".equals(envType)) {
            str = adDcosDeployDtlImpl.getDcosAppIdByDeployInfoId(envID);
        }
        return str;
    }

    /**
     * 根据环境id删除环境
     *
     * @param envId 要删除的环境ID，逻辑删除，设置0 失效
     * @return
     */
    public int deleteSingleInfo(long envId) {
        AdVirtualEnvironment adVirtualEnvironment = qryVmById(envId);//qryIinfoByBranchID(branchId);
        if (adVirtualEnvironment != null) {
            adVirtualEnvironment.setState(0L);
            adVirtualEnvironment.save();
            adVirtualBranchRelateDAO.deleteRelationByEnvId(envId);
            return 0;
        }
        return 1;
    }

    public AdVirtualEnvironment qryById(Long envId) {
        return new QAdVirtualEnvironment().virtualId.eq(envId).state.eq(1).adBranch.state.eq(1).findUnique();
    }

    public List<AdVirtualEnvironment> qryAllVmEnv() {
        return new QAdVirtualEnvironment().state.eq(1).findList();
    }

    /**
     * 将一个字符串转化为输入流
     */
    public InputStream getStringStream(String sInputString) {
        if (sInputString != null && !sInputString.trim().equals("")) {
            try {
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());
                return tInputStringStream;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /*****************************************************改造方法****************************************/
    /**
     * 存入环境信息，返回环境Id
     *
     * @param projectId 应用id
     * @param sName
     * @param envName   虚拟机名字
     * @param spwd
     * @param surl
     * @param fpath
     * @param fname
     * @param packname
     * @param sAdds
     * @param dAdds
     * @param region
     * @return 返回新建虚拟机Id
     * @throws Exception
     */
    public AdVirtualEnvironment addReformVmEnvSet(long projectId, String sName, String envName, String spwd, String
        surl, String fpath, String fname, String packname, String sAdds, String dAdds, Integer region) throws
        Exception {
        AdProject adProject;
        adProject = adProjectDAO.qryById(projectId);                                //根据id查询应用
        if (adProject == null) {
            throw new Exception("应用不存在");
        }
        AdVirtualEnvironment advm = new AdVirtualEnvironment();                     //存储环境信息，瞬时态
        advm.setAdProject(adProject);
        advm.setVirtualName(envName);
        advm.setServerUsername(sName);
        advm.setServerPassword(spwd);
        advm.setServerUrl(surl);
        advm.setFileName(fname);
        advm.setFilePath(fpath);
        advm.setSourceAddress(sAdds);
        advm.setPackageName(packname);
        advm.setDestinationAddress(dAdds);
        advm.setState(1L);
        advm.setRegion(region);
        advm.save();                                                            //瞬时态→持久态
        return advm;
    }

    /**
     * 根据环境id查询环境
     *
     * @param envId
     * @return
     */
    public AdVirtualEnvironment qryVmById(long envId) {
        AdVirtualEnvironment adVirtualEnvironment = new QAdVirtualEnvironment().virtualId.eq(envId).state.eq(1L)
            .findUnique();
        return adVirtualEnvironment;
    }


    public List<AdVirtualBranchRelate> getVMEnvByBranchId(long virId) {
        List<AdVirtualBranchRelate> adVirtualBranchRelateList = new QAdVirtualBranchRelate().state.eq(1).adVirtualEnvironment.virtualId.eq(virId).findList();
        return adVirtualBranchRelateList;
    }

    public List<AdDcosBranchRelate> getDcosEnvByBranchId(long delopyInfoId) {
        List<AdDcosBranchRelate> adDcosBranchRelateList = new QAdDcosBranchRelate().state.eq(1).adDcosDeployInfo.deployInfoId.eq(delopyInfoId).findList();
        return adDcosBranchRelateList;
    }

    /**
     * 根据region查找相关region的所有vm
     *
     * @param region
     * @return
     */
    public List<AdBranchCheckPojoExt> qryEnvRlateBranchByRegion(Long projectId, int region, String envType, long virId) {
        String branchType = "";
        if (region == 1) {
            branchType = "1,2,3,4,5";
        } else if (region == 2) {
            branchType = "1,2,3,4";
        }
        List<AdBranchCheckPojoExt> adBranchCheckPojoExtList = new ArrayList<AdBranchCheckPojoExt>();
        List<SqlRow> branchList = adBranchDAO.qryBranchByProjectAndBranchtype(projectId, branchType);
        if (CollectionUtils.isNotEmpty(branchList)) {
            List<AdVirtualBranchRelate> adVirtualEnvironmentList = new ArrayList<>();
            List<AdDcosBranchRelate> adDcosDeployInfoList = new ArrayList<>();
            if (("dcos").equals(envType)) {
                adDcosDeployInfoList = getDcosEnvByBranchId(virId);
            } else if (("vm").equals(envType)) {
                adVirtualEnvironmentList = getVMEnvByBranchId(virId);
            }
            AdBranchCheckPojoExt adBranchCheckPojoExt = null;
            for (SqlRow sqlRow : branchList) {
                adBranchCheckPojoExt = new AdBranchCheckPojoExt();
                adBranchCheckPojoExt.setBranchId(sqlRow.getString("branch_id"));
                adBranchCheckPojoExt.setBranchName(sqlRow.getString("branch_desc"));
                adBranchCheckPojoExt.setFlag(false);
                if (!adVirtualEnvironmentList.isEmpty()) {
                    for (AdVirtualBranchRelate adVirtualEnvironment : adVirtualEnvironmentList) {
                        if (adVirtualEnvironment.getAdBranch() != null && (adVirtualEnvironment.getAdBranch().getBranchId() + "").equals(sqlRow.getString("branch_id"))) {
                            adBranchCheckPojoExt.setFlag(true);
                            break;
                        }
                    }
                }
                if (!adDcosDeployInfoList.isEmpty()) {
                    for (AdDcosBranchRelate adDcosDeployInfo : adDcosDeployInfoList) {
                        if (adDcosDeployInfo.getAdBranch() != null && (adDcosDeployInfo.getAdBranch().getBranchId() + "").equals(sqlRow.getString("branch_id"))) {
                            adBranchCheckPojoExt.setFlag(true);
                            break;
                        }
                    }
                }
                adBranchCheckPojoExtList.add(adBranchCheckPojoExt);
            }
        }
        return adBranchCheckPojoExtList;
    }

}

