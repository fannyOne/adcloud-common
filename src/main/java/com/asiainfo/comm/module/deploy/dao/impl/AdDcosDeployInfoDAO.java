package com.asiainfo.comm.module.deploy.dao.impl;

import com.asiainfo.comm.common.pojo.pojoExt.AppIdinfoPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.dcosEnvInfoPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.dcosEnvPojoExt;
import com.asiainfo.comm.module.build.dao.impl.AdBranchDAO;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDAO;
import com.asiainfo.comm.module.build.dao.impl.AdVirtualEnvironmentDAO;
import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.asiainfo.comm.module.build.service.impl.AdBranchImpl;
import com.asiainfo.comm.module.build.service.impl.AdDcosDeployDtlImpl;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.models.query.QAdDcosDeployDtl;
import com.asiainfo.comm.module.models.query.QAdDcosDeployInfo;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


@Component("AdDcosDeployInfoDAO")
public class AdDcosDeployInfoDAO {
    @Autowired
    AdVirtualEnvironmentDAO adVirtualEnvironmentDAO;
    @Autowired
    AdProjectDAO adProjectDAO;
    @Autowired
    AdBranchImpl adBranchImpl;
    @Autowired
    AdDcosDeployDtlImpl adDcosDeployDtlImpl;
    @Autowired
    SystemDeployDAO systemDeployDAO;
    @Autowired
    AdParaDetailDAO bsParaDetailDAO;
    @Autowired
    private AdBranchDAO adBranchDAO;
    @Autowired
    private AdDcosBranchRelateDAO adDcosBranchRelateDAO;

    public AdDcosDeployInfo qryDcosDeployInfoById(long deployInfoId) {
        AdDcosDeployInfo adDcosDeployInfo = new QAdDcosDeployInfo().deployInfoId.eq(deployInfoId)
            .findUnique();
        return adDcosDeployInfo;
    }

    public List<SqlRow> qryEnvInfo(Long projectId, String branchType) {
        String[] branchTypes = StringUtils.split(branchType, ",");
        List<Integer> types = new ArrayList<>();
        for (String type : branchTypes) {
            types.add(Integer.parseInt(type));
        }
        String sql = "SELECT CONCAT(a.deploy_info_id,'_dcos') AS ENV_ID,a.env_name ENV_NAME " +
            "FROM ad_dcos_deploy_info a where a.state=1 and  a.project_id=:project_id and a" +
            ".region IN(:branchType) union all SELECT CONCAT(b.virtual_id,'_vm') " +
            "ENV_ID,b.virtual_name ENV_NAME FROM ad_virtual_environment b WHERE b.state=1 and b" +
            ".project_id=:project_id and b.region IN(:branchType)";
        List<SqlRow> sqlRow = Ebean.createSqlQuery(sql).setParameter("branchType", types).setParameter("project_id", projectId)
            .findList();
        return sqlRow;
    }


    public List<AdDcosDeployInfo> qryDcosDeployInfoByProjectId(long projectId) {
        List<AdDcosDeployInfo> adDcosDeployInfoList = new QAdDcosDeployInfo().adProject.projectId
            .eq(projectId).state.eq(1).findList();
        return adDcosDeployInfoList;
    }


    public AdDcosDeployInfo getSingleDcosEvn(long projectId, long id) {
        AdDcosDeployInfo adDcosDeployInfo = null;
        List<AdDcosDeployInfo> adDcosDeployInfoList = new QAdDcosDeployInfo().adProject.projectId
            .eq(projectId).deployInfoId.eq(id).findList();
        if (adDcosDeployInfoList != null && adDcosDeployInfoList.size() > 0) {
            adDcosDeployInfo = adDcosDeployInfoList.get(0);
        }
        return adDcosDeployInfo;
    }

    public List<AdDcosDeployInfo> getAllDcosInfo(int pageNum, int pageSize, String[] projects) {
        QAdDcosDeployInfo qAdDcosDeployInfo = new QAdDcosDeployInfo().state.eq(1L);
        for (String project : projects) {
            qAdDcosDeployInfo = qAdDcosDeployInfo.or().adProject.projectId.eq(Long.valueOf
                (project));
        }
        List<AdDcosDeployInfo> adDcosDeployInfoList = qAdDcosDeployInfo.findPagedList(pageNum,
            pageSize).getList();
        return adDcosDeployInfoList;
    }

    public long getDcosConut(String[] projects) {
        QAdDcosDeployInfo qAdDcosDeployInfo = new QAdDcosDeployInfo().state.eq(1);
        for (String project : projects) {
            qAdDcosDeployInfo = qAdDcosDeployInfo.or().adProject.projectId.eq(Long.valueOf
                (project));
        }
        return qAdDcosDeployInfo.findRowCount();
    }

    public AdDcosDeployInfo qryById(Long envId) {
        return new QAdDcosDeployInfo().state.eq(1).deployInfoId.eq(envId).findUnique();
    }

    public AdDcosDeployInfo qryInfoByPkId(long id) {
        AdDcosDeployInfo adDcosDeployInfo = new QAdDcosDeployInfo().deployInfoId.eq(id)
            .findUnique();
        return adDcosDeployInfo;
    }

    public void updateDcosEvnById(dcosEnvPojoExt req) throws Exception {
        Ebean.execute(() -> {
            AdDcosDeployInfo adDcosDeployInfo;                                      //DCOS环境信息
            if (req != null && req.getObj() != null) {                              //要修改的参数存在
                dcosEnvInfoPojoExt envInfoPojoExt = req.getObj()[0];                //获得要修改的dcosEnvInfoPojoExt
                adDcosDeployInfo = qryInfoByPkId(req.getInfoId());                  //根据环境id获得环境信息
                if (adDcosDeployInfo != null) {                                     //DCOS环境存在
                    adDcosDeployInfo.setEnvName(envInfoPojoExt.getEnvName());
                    adDcosDeployInfo.save();                                            //更新信息

                    adDcosDeployDtlImpl.deleteStateByDeployInfoId(adDcosDeployInfo.getDeployInfoId(), 1); //把先前存的appid删除

                    for (int i = 0; i < envInfoPojoExt.getAppids().size(); i++) { //appid存在，设置dtl信息
                        AdDcosDeployDtl adDcosDeployDtl = new AdDcosDeployDtl();
                        adDcosDeployDtl.setDeployInfoId(adDcosDeployInfo.getDeployInfoId());
                        adDcosDeployDtl.setPackageName(envInfoPojoExt.getAppids().get(i).getPackageName());
                        adDcosDeployDtl.setAppid(envInfoPojoExt.getAppids().get(i).getAppid());
                        adDcosDeployDtl.setPriorityNum(Integer.valueOf(envInfoPojoExt.getAppids().get(i)
                            .getPriorityNum()));
                        adDcosDeployDtl.setState(1);
                        adDcosDeployDtl.save();             //存储
                    }
                    adDcosBranchRelateDAO.deleteRelationByEnvId(adDcosDeployInfo.getDeployInfoId());
                    String[] branchIdArray = null;
                    if (StringUtils.isNotEmpty(envInfoPojoExt.getBranchIds())) {
                        branchIdArray = envInfoPojoExt.getBranchIds().split(",");
                    }
                    if (branchIdArray != null && branchIdArray.length > 0) { //数组不为空，遍历所有分支id
                        for (String branchId : branchIdArray) {
                            AdBranch adBranch = adBranchDAO.qryById(Long.parseLong(branchId)); //根据id查询获得分支信息，存入虚机流水对应表中
                            adDcosBranchRelateDAO.addDcosBranchRelate(adBranch, adDcosDeployInfo, 1L, new Date());
                            //新建对应关系
                        }
                    }
                }
                //TODO
            }
        });
    }


    public List<AdDcosDeployInfo> qryAllDcos() {
        return new QAdDcosDeployInfo().state.eq(1).findList();
    }

    public List<AdDcosDeployDtl> qryDcosDeployDtlByBranchId(long branchId) {
        List<AdDcosDeployDtl> adDcosDeployDtlList = new QAdDcosDeployDtl().state.eq(1).branchId
            .eq(branchId).orderBy(" priorityNum ASC").findList();
        return adDcosDeployDtlList;
    }


    /**
     * @param dcosDeployId
     * @return
     */
    public List<AdDcosDeployDtl> qryDcosDeployDtlByDcosDeployId(long dcosDeployId) {
        List<AdDcosDeployDtl> adDcosDeployDtlList = new QAdDcosDeployDtl().state.eq(1)
            .deployInfoId.eq(dcosDeployId).orderBy(" priorityNum ASC").findList();
        return adDcosDeployDtlList;
    }

    /**
     * @param reqs
     * @throws Exception
     */
    public AdDcosDeployInfo addReformDcosInfo(dcosEnvInfoPojoExt reqs) throws Exception {
        AdDcosDeployInfo adDcosDeployInfo = new AdDcosDeployInfo();
        List<AdParaDetail> adParaDetails;
        if (null == reqs) {
            throw new Exception("信息有误");
        }
        if (reqs.getVisitSource().equals("adcloud")) {
            if (reqs.getRegion() == 1) {
                adParaDetails = bsParaDetailDAO.qryByDetails("X", "DCOS_RELEASE", "DCOS_RELEASE_PRO");
            } else if (reqs.getRegion() == 2) {
                adParaDetails = bsParaDetailDAO.qryByDetails("X", "DCOS_RELEASE", "DCOS_RELEASE_REQ");
            } else {
                throw new Exception("环境归属域有误");
            }
        } else {
            if (reqs.getRegion() == 1) {
                adParaDetails = bsParaDetailDAO.qryByDetails("X", "DCOS_RELEASE", "DCOS_RELEASE_CMP_PRO");
            } else if (reqs.getRegion() == 2) {
                adParaDetails = bsParaDetailDAO.qryByDetails("X", "DCOS_RELEASE", "DCOS_RELEASE_CMP_REQ");
            } else {
                throw new Exception("环境归属域有误");
            }
        }
        if (adParaDetails != null) {
            for (AdParaDetail bpd : adParaDetails) {
                reqs.setDcosFtpUrl(bpd.getPara5());
                reqs.setDcosFtpUsername(bpd.getParaDesc());
                reqs.setDcosFtpPassword(bpd.getRemarks());
                reqs.setDcosFtpPort("21");
                reqs.setDocsServerUrl(bpd.getPara3());
                reqs.setDocsUserName(bpd.getPara1());
                reqs.setDocsUserPassword(bpd.getPara2());
                reqs.setDcosFtpPath(bpd.getPara4());
            }
        }
        AdProject adProject;
        adProject = adProjectDAO.qryById(reqs.getProjectId());
        if (adProject == null) {
            throw new Exception("应用不存在");
        }
        adDcosDeployInfo.setAdProject(adProject);
        //******开始设置环境信息
        adDcosDeployInfo.setDocsUserName(reqs.getDocsUserName());
        adDcosDeployInfo.setDocsUserPassword(reqs.getDocsUserPassword());
        adDcosDeployInfo.setDocsServerUrl(reqs.getDocsServerUrl());
        adDcosDeployInfo.setDcosFtpPath(reqs.getDcosFtpPath());
        adDcosDeployInfo.setDcosFtpUrl(reqs.getDcosFtpUrl());
        adDcosDeployInfo.setDcosFtpUsername(reqs.getDcosFtpUsername());
        adDcosDeployInfo.setDcosFtpPassword(reqs.getDcosFtpPassword());
        if (StringUtils.isEmpty(reqs.getDcosFtpPort())) {
            adDcosDeployInfo.setDcosFtpPort("21");
        } else {
            adDcosDeployInfo.setDcosFtpPort(reqs.getDcosFtpPort());
        }
        adDcosDeployInfo.setState(1L);
        adDcosDeployInfo.setRegion(reqs.getRegion());
        adDcosDeployInfo.setEnvName(reqs.getEnvName());
        adDcosDeployInfo.save();
        //设置环境信息结束，存储信息

        if (!reqs.getAppids().isEmpty()) {
            //AppIds存在
            for (AppIdinfoPojoExt appIdinfoPojoExt : reqs.getAppids()) {
                //遍历存入DTL
                AdDcosDeployDtl adDcosDeployDtl = new AdDcosDeployDtl();
                adDcosDeployDtl.setAppid(appIdinfoPojoExt.getAppid());
                adDcosDeployDtl.setPackageName(appIdinfoPojoExt.getPackageName());
                //adDcosDeployDtl.setBranchId(adBranch.getBranchId());
                adDcosDeployDtl.setDeployInfoId(adDcosDeployInfo.getDeployInfoId());
                adDcosDeployDtl.setPriorityNum(Integer.valueOf(appIdinfoPojoExt
                    .getPriorityNum()));
                adDcosDeployDtl.setState(1);
                adDcosDeployDtl.save();
            }
        }
        return adDcosDeployInfo;
    }


    /**
     * @param deployInfoId 要删除的dcosid
     * @return
     */
    public int deleteReformDcosSigleInfo(long deployInfoId) {
        AdDcosDeployInfo adDcosDeployInfo = qryInfoByDeployInfoId(deployInfoId);    //根据id获取Dcos
        if (adDcosDeployInfo != null) {
            adDcosDeployInfo.setState(0L);                                          //设置状态失效
            adDcosDeployInfo.save();
            adDcosDeployDtlImpl.updateStateByDeployInfoId(deployInfoId, 0); //把以前的数据state置为0
            adDcosBranchRelateDAO.deleteRelationByEnvId(deployInfoId);
            return 0;
        }
        return 1;
    }

    /**
     * @param deployInfoId 要查找的id
     * @return
     */
    public AdDcosDeployInfo qryInfoByDeployInfoId(long deployInfoId) {
        AdDcosDeployInfo adDcosDeployInfo = new QAdDcosDeployInfo().deployInfoId.eq(deployInfoId)
            .state.eq(1).findUnique();
        return adDcosDeployInfo;
    }

    /**
     * 根据域和应用id查找环境
     *
     * @param region    要查询的域
     * @param projectId //要查询的应用id
     * @return
     */
    public List<AdDcosDeployInfo> qryDcosByRegion(Integer region, long projectId) {
        List<AdDcosDeployInfo> adDcosDeployInfos = new QAdDcosDeployInfo().region.eq(region).adProject.projectId.eq(projectId).findList();
        return adDcosDeployInfos;
    }

    /**
     * @param envInfoPojoExt 要查询的环境信息
     * @return
     */
    public Map<String, Object> qryDcosInfoByRegion(dcosEnvInfoPojoExt envInfoPojoExt) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        List<AdBranch> trueList = new ArrayList<AdBranch>();    //关联流水
        List<AdBranch> falseList = new ArrayList<AdBranch>();   //未关联流水
        if (envInfoPojoExt != null) {
            if (StringUtils.isNotEmpty(envInfoPojoExt.getRegion().toString())) {
                List<AdDcosDeployInfo> adDcosDeployInfos = qryDcosByRegion(envInfoPojoExt.getRegion(), envInfoPojoExt.getProjectId()); //查找应用下对应域的所有环境
                if (!adDcosDeployInfos.isEmpty()) {
                    for (AdDcosDeployInfo adDcosDeployInfo : adDcosDeployInfos) { //获得所有流水
                        List<AdDcosBranchRelate> adDcosBranchRelates = adDcosBranchRelateDAO.qryBranchsByEnvId
                            (adDcosDeployInfo.getDeployInfoId());
                        if (!envInfoPojoExt.getDeployInfoId().equals(adDcosDeployInfo.getDeployInfoId())) {   //未关联流水
                            for (AdDcosBranchRelate adDcosBranchRelate : adDcosBranchRelates) {
                                AdBranch adBranch = adBranchDAO.qryById(adDcosBranchRelate.getAdBranch().getBranchId());
                                falseList.add(adBranch);
                            }
                        } else {    //已关联流水
                            for (AdDcosBranchRelate adDcosBranchRelate : adDcosBranchRelates) {
                                AdBranch adBranch = adBranchDAO.qryById(adDcosBranchRelate.getAdBranch().getBranchId());
                                trueList.add(adBranch);
                            }
                        }
                    }
                }

            }
        }
        retMap.put("false", falseList);
        retMap.put("true", trueList);
        return retMap;
    }
}
