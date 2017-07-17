package com.asiainfo.util;


import com.asiainfo.comm.module.build.dao.impl.AdDcosDeployDtlDAO;
import com.asiainfo.comm.module.build.dao.impl.AdVirtualEnvironmentDAO;
import com.asiainfo.comm.module.build.service.impl.AdGroupImpl;
import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.asiainfo.comm.module.deploy.dao.impl.AdDcosDeployInfoDAO;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.models.functionModels.GitLabUser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class CommConstants {

    /**
     * 构建常量类
     *
     * @author [xianan]
     */
    public static class BuildConstants {

        public static String AD_CLOUD_IP;//本机Ip
        public static String AD_CLOUD_PORT = "8090";//本机端口
        public static final String FORMAT_STATE_DATE = "yyyy-MM-dd HH:mm:ss";
        public static final String FORMAT_COMMIT_DATE = "yyyy-MM-dd HH:mm:ss";
        public static final String FORMAT_EXPIRE_DATE = "yyyy-MM-dd HH:mm:ss";
        public static final String FORMAT_DONE_DATE = "yyyy-MM-dd HH:mm:ss";
        public static final String FORMAT_CREATE_DATE = "yyyy-MM-dd HH:mm:ss";
        public static final String FORMAT_LAST_LOGIN_DATE = "yyyy-MM-dd HH:mm:ss";
        public static final String DATA_STATE_INUSE = "U";
        public static final String DATA_STATE_EXPIRED = "E";

        public static void UPDATE_IP(String ip) {
            AD_CLOUD_IP = ip;
        }

        public static class STATIC_DATE {
            public static final String CODE_TYPE_IP = "SYNC_RMP";
        }

        public static class SYSTEM {
            /**
             * 项目类型
             */
            public static class TYPE {
                public static final int TRIGGER_TYPE_PROJECT = 1;
                public static final int TRIGGER_TYPE_NEW_BUSI = 2;
            }
        }

        public static class ENV {
            /**
             * 构建触发方式
             */
            public static class ENV_TRIGGER_TYPE {
                public static final int TRIGGER_TYPE_QUARTZ = 1;
                public static final int TRIGGER_TYPE_REQUEST = 2;
            }

            /**
             * 状态
             */
            public static class ENV_STATE {
                public static final int CLOSED = 0;//关闭
                public static final int WAITTING = 1;//禁止
                public static final int BUILDING = 2;//构建
                public static final int DEPLOYING = 3;//发布
                public static final int ROLLBACKING = 4;//回退
                public static final int BUILDINGFAILURE = 5;//构建失败

            }
        }

        public static class STAGE {

            public static class IS_UPDATE {
                public static final int UPDATED = 1;        //已上传
                public static final int NOT_UPDATE = 2;  //未上传
            }
        }

        public static class BUILDlOG {
            public static class BUILD_TYPE {
                public static final int BUILD = 1;        //构建
                public static final int ROLLBACK = 2;  //回滚
                public static final int RESTART = 3;  //重启
                public static final int REDEPLOY = 4;  //重启
            }
        }

        public static class TASK {
            public static class TASK_RUN {
                public static final int RUN_SERVER_NUM = 9999;
            }
        }

        public static class SUBSYSTEM {
            /**
             * 构建类型
             */
            public static class SUBSYSTEM_CONSTRUCT_TYPE {
                public static final int GRADLE = 1;
                public static final int MAVEN = 2;
            }

            /**
             * 需求管理平台类型
             */
            public static class REQ_SYS_TYPE {

            }

            /**
             * 需求管理平台子类型
             */
            public static class REQ_SUB_SYS_TYPE {

            }
        }

        public static class OPERATION {
            /**
             * 操作类型
             */
            public static class TYPE {
                public static final int SHELL_OPT = 1;
                public static final int SYS_OPT = 2;
            }
        }

        public static class OPERATION_ORDER {
            /**
             * 操作结果
             */
            public static class RESULT {
                public static final String INIT = "0";
                public static final String EXECUTING = "1";
                public static final String SUCCESS = "2";
                public static final String FAILED = "3";
                public static final String CANCELED = "4";
                public static final String RELAT_FAILED = "5";
            }

            /**
             * 操作类型
             */
            public static class OPERATION_TYPE {

                public static final int JENKINS_OPT = 1;
                public static final int SHELL_OPT = 2;
                public static final int OTHER_OPT = 3;
                public static final int DOWNLOAD_OPT = 6;//需求管理平台同步数据下载代码

            }
        }

        public static class CODE_STORE {
            /**
             * 操作类型
             */
            public static class TYPE {
                public static final int SVN = 1;
                public static final int GIT = 2;
            }
        }


    }

    /**
     * 权限信息类
     */
    public static class Role {
        public static final int THREAD_NUMBER = 10;// 查询用户所允许的最多的线程数量
        public static Set<String> UN_CHECK_METHOD = new HashSet<>();

        static {
            UN_CHECK_METHOD.add("/project/qryIndex");
            UN_CHECK_METHOD.add("/");
            UN_CHECK_METHOD.add("/hi");
            UN_CHECK_METHOD.add("/hello");
            UN_CHECK_METHOD.add("/autoTest/changeStepByAutoTest");
            UN_CHECK_METHOD.add("/jobNotify");
            UN_CHECK_METHOD.add("/jobnotification");
            UN_CHECK_METHOD.add("/addSystemDeploy");
            UN_CHECK_METHOD.add("/updateSystemDeploy");
            UN_CHECK_METHOD.add("/error");
            UN_CHECK_METHOD.add("/thirdPart/getStatus");
            UN_CHECK_METHOD.add("/thirdPart/getGitlibUrlByGitId");
            UN_CHECK_METHOD.add("/thirdPart/getGitlibUrl");
            UN_CHECK_METHOD.add("/thirdPart/createBranchByGitId");
            UN_CHECK_METHOD.add("/thirdPart/createBranch");
            UN_CHECK_METHOD.add("/deployManager/saveDeployPlan");
            UN_CHECK_METHOD.add("/deployManager/deploy");
            UN_CHECK_METHOD.add("/deployManager/stageDeploy");
            UN_CHECK_METHOD.add("/deployManager/findByProjectId");
            UN_CHECK_METHOD.add("/deployManager/deployVirturl");
            UN_CHECK_METHOD.add("/deployManager/deployVirturlPlan");
            UN_CHECK_METHOD.add("/role/qryUsers");
            UN_CHECK_METHOD.add("/leangoo/runChartSpider");
            UN_CHECK_METHOD.add("/leangoo/getLeangooWebInfo");
            UN_CHECK_METHOD.add("/evn/updateVmEnv");
            UN_CHECK_METHOD.add("/evn/addVmEnv");
            UN_CHECK_METHOD.add("/envMonitor");
            UN_CHECK_METHOD.add("/newenvMonitor");
            UN_CHECK_METHOD.add("/evn/updateDcosEnv");
            UN_CHECK_METHOD.add("/webscan/doScan");
            UN_CHECK_METHOD.add("/user/qryOnlineNumber");
            UN_CHECK_METHOD.add("/project/qryReqNum");
            UN_CHECK_METHOD.add("/qryTaskBrCountRound");
            UN_CHECK_METHOD.add("/qryNewBusinessScrumData");
            UN_CHECK_METHOD.add("/api/v1/user/login");
            UN_CHECK_METHOD.add("/addReformSystemDeploy");
            UN_CHECK_METHOD.add("/evn/updateReformDcosEnv");
            UN_CHECK_METHOD.add("/evn/updateReformVmEnv");
            UN_CHECK_METHOD.add("/evn/addReformDcosEnvInfo");
            UN_CHECK_METHOD.add("/netAccept/addAcceptPlan");
            UN_CHECK_METHOD.add("/netAccept/updateAcceptPlan");
            UN_CHECK_METHOD.add("/netAccept/updateAcceptResult");
            UN_CHECK_METHOD.add("/netAccept/qryAcceptPlan");
            UN_CHECK_METHOD.add("/Monitor/updateEnvNodeDetail");
            UN_CHECK_METHOD.add("/Monitor/queryEnvInfo");
            UN_CHECK_METHOD.add("/copySystemDeploy");
            UN_CHECK_METHOD.add("/ReleasePlanTemplate/modTemplateName");
            UN_CHECK_METHOD.add("/ReleasePlanTemplate/delTemplate");
            UN_CHECK_METHOD.add("/ReleasePlanTemplate/modTemplateStages");

            UN_CHECK_METHOD.add("/adcloud/project/qryIndex");
            UN_CHECK_METHOD.add("/adcloud/");
            UN_CHECK_METHOD.add("/adcloud/hi");
            UN_CHECK_METHOD.add("/adcloud/hello");
            UN_CHECK_METHOD.add("/adcloud/autoTest/changeStepByAutoTest");
            UN_CHECK_METHOD.add("/adcloud/jobnotification");
            UN_CHECK_METHOD.add("/adcloud/envMonitor");
            UN_CHECK_METHOD.add("/adcloud/newenvMonitor");
            UN_CHECK_METHOD.add("/adcloud/addSystemDeploy");
            UN_CHECK_METHOD.add("/adcloud/updateSystemDeploy");
            UN_CHECK_METHOD.add("/adcloud/error");
            UN_CHECK_METHOD.add("/adcloud/thirdPart/getStatus");
            UN_CHECK_METHOD.add("/adcloud/thirdPart/createBranchByGitId");
            UN_CHECK_METHOD.add("/adcloud/thirdPart/createBranch");
            UN_CHECK_METHOD.add("/adcloud/thirdPart/getGitlibUrlByGitId");
            UN_CHECK_METHOD.add("/adcloud/thirdPart/getGitlibUrl");
            UN_CHECK_METHOD.add("/adcloud/deployManager/saveDeployPlan");
            UN_CHECK_METHOD.add("/adcloud/deployManager/deploy");
            UN_CHECK_METHOD.add("/adcloud/deployManager/stageDeploy");
            UN_CHECK_METHOD.add("/adcloud/role/qryUsers");
            UN_CHECK_METHOD.add("/adcloud/deployManager/findByProjectId");
            UN_CHECK_METHOD.add("/adcloud/deployManager/deployVirturl");
            UN_CHECK_METHOD.add("/adcloud/deployManager/deployVirturlPlan");
            UN_CHECK_METHOD.add("/adcloud/leangoo/runChartSpider");
            UN_CHECK_METHOD.add("/adcloud/leangoo/getLeangooWebInfo");
            UN_CHECK_METHOD.add("/adcloud/evn/updateVmEnv");
            UN_CHECK_METHOD.add("/adcloud/evn/updateDcosEnv");
            UN_CHECK_METHOD.add("/adcloud/evn/addVmEnv");
            UN_CHECK_METHOD.add("/adcloud/webscan/doScan");
            UN_CHECK_METHOD.add("/adcloud/jobNotify");
            UN_CHECK_METHOD.add("/adcloud/user/qryOnlineNumber");
            UN_CHECK_METHOD.add("/adcloud/project/qryReqNum");
            UN_CHECK_METHOD.add("/adcloud/qryTaskBrCountRound");
            UN_CHECK_METHOD.add("/adcloud/qryNewBusinessScrumData");
            UN_CHECK_METHOD.add("/adcloud/api/v1/user/login");
            UN_CHECK_METHOD.add("/adcloud/addReformSystemDeploy");
            UN_CHECK_METHOD.add("/adcloud/evn/updateReformDcosEnv");
            UN_CHECK_METHOD.add("/adcloud/evn/updateReformVmEnv");
            UN_CHECK_METHOD.add("/adcloud/evn/addReformDcosEnvInfo");
            UN_CHECK_METHOD.add("/adcloud/netAccept/addAcceptPlan");
            UN_CHECK_METHOD.add("/adcloud/netAccept/updateAcceptPlan");
            UN_CHECK_METHOD.add("/adcloud/netAccept/updateAcceptResult");
            UN_CHECK_METHOD.add("/adcloud/netAccept/qryAcceptPlan");
            UN_CHECK_METHOD.add("/adcloud/Monitor/updateEnvNodeDetail");
            UN_CHECK_METHOD.add("/adcloud/Monitor/queryEnvInfo");
            UN_CHECK_METHOD.add("/adcloud/qryNotice"); //系统帮助
            UN_CHECK_METHOD.add("/adcloud/copySystemDeploy");
            UN_CHECK_METHOD.add("/adcloud/ReleasePlanTemplate/modTemplateName");
            UN_CHECK_METHOD.add("/adcloud/ReleasePlanTemplate/delTemplate");
            UN_CHECK_METHOD.add("/adcloud/ReleasePlanTemplate/modTemplateStages");
        }

        public static Map<String, String> USER_DETAILS = new HashMap<>();
        public static int THREAD_POOL = 0;
        public static Map<String, GitLabUser> GIT_USERS = new HashMap<>();//用户信息
        public static long USER_NUMBER;//用户数量
        public static Map<String, Long> USER_SEARCH_TOTAL = new HashMap<>();
        public static Map<String, AdRole> USER_ROLE = new HashMap<>();//用户角色信息
        public static int ERR_OUT_OF_ROLE = 401;
        public static int ERR_NOT_LOGIN = 402;
        public static int ERR_INNER = 500;

        public static void SET_USER_NUMBER
            (long userNumber) {
            USER_NUMBER = userNumber;
        }

        public static void CHANGE_THREAD_POOL(int changeNumber) {
            THREAD_POOL += changeNumber;
        }

        public static void SET_GIT_USERS(Map users) {
            GIT_USERS = users;
        }
    }

    /**
     * WEB常量
     *
     * @author [mayc]
     * @version 1.0
     */
    public static class WebStatusCode {
        public static final String SUCCESS_CODE_VALUE = "200"; // 成功标示，前台交互的标示
        public static final String SYSTEM_ERROR_CODE_VALUE = "-9999"; // 系统错误标示
        public static final String SYSTEM_ERROR_SHOW_NAME = "-";
        public static final String SYSTEM_ERROR_MSG = "系统有点小问题，正在努力修复中";
        public static final String SECRET_CODE = "***********************************";
        public static final int SECRET_CODE_LENGTH = 8;

        private WebStatusCode() {

        }
    }

    /**
     * 网络爬虫所需常量
     */
    public static class SpiderConstant {
        public static SpiderUtil.HeaderModel HEADER_MODEL;

        public static void SET_HEADER_MODEL(SpiderUtil.HeaderModel model) {
            HEADER_MODEL = model;
        }
    }

    /**
     * ESB常量
     *
     * @author [mayc]
     * @version 1.0
     */
    public static class EsbConstant {

        public static final String BUSI_INFO = "BUSI_INFO";
        public static final String PUB_INFO = "PUB_INFO";
        public static final String RESULT_CODE = "RETURN_RESULT"; // 报文ESB_KEY_CODE
        // result
        // code
        public static final String RESULT_MSG = "RETURN_DESC"; // 报文ESB_KEY_CODE
        // result msg
        public static final String ERRORINFO = "ERRORINFO";
        public static final String RETINFO = "RetInfo";
        public static final String ESB_SUCCESS_CODE = "0";
        public static final String ESB_WARING_CODE = "11280001";
        public static final String ORG_ID_ESB_DEFAULT = "0";
        public static final int ESB_MAX_PERPAGE_TOTAL = 3000;
        public static final int ESB_MAX_TOTAL = 15000; // 大概要用掉 88m
        public static final String ESB_SYS_OP_ID = "20049127"; // ESB sysOPId
        // --10010852
        // SCRM生产工号20057971
        // 准发布可用工号20037686,手机营业厅系统--20049127
        public static final String ESB_SYS_PASSWORD = "8323f15343239abb72885940220a4f3e";

        /**
         * 调用ESB接口结果封装map成功代码（"S",map的key取RET_CODE）
         */
        public static final String RESULT_SUCCESS_CODE = "S";
        /**
         * 调用ESB接口结果封装map失败代码（"F",map的key取RET_CODE）
         */
        public static final String RESULT_FAIL_CODE = "F";

    }

    /**
     * WEB常量
     *
     * @author [mayc]
     * @version 1.0
     */
    public static class REQ {
        public static class TASK_STATE {
            public static final String CANCELD = "开发任务_已取消";
            public static final String RETURNED = "开发任务_已退回";
            public static final String CLOSED = "开发任务_已关闭";
            public static final String DEVELOGPING = "开发任务_开发中";
            public static final String WAITCOMPILE = "开发任务_待编译";
            public static final String TO_DISTRIBUTE = "开发任务_待安排开发工作";

        }

        public static class SUB_TASK_STATE {
            public static final String CANCELD = "开发子任务_已取消";
            public static final String RETURNED = "开发子任务_已退回";
            public static final String CLOSED = "开发子任务_已关闭";
            public static final String DEVELOGPING = "开发子任务_待开发";
            public static final String TO_DEPLOY = "开发子任务_待编译部署";
        }

        public static class BUGTASK_STATE {
            public static final String TO_FIX = "缺陷子任务_待修复";
            public static final String TO_DEPLOY = "缺陷子任务_待编译部署";
            public static final String TO_CONFIRM = "缺陷子任务_修复待确认";
            public static final String VERIFIED = "缺陷子任务_已验证";
            public static final String CLOSED = "缺陷子任务_已关闭";
            public static final String RETURNED = "缺陷子任务_已退回";

        }

        public static class TASK_TYPE {
            public static final int DEV_TASK = 1;//开发需求单
            public static final int BUG_TASK = 2;//缺陷需求单
        }

        public static final String PLAN_EXE_STATE_ONLINE = "1";
        public static final String PLAN_EXE_STATE_NOT_ONLINE = "0";

    }

    public static enum SYSNAME {
        ALL("全部系统", "-1"), CRM("CRM系统", "1"), BOSS("BOSS系统", "2"),
        BOMC("BOMC系统", "3"), SECURITY_SYSTEM("安全系统", "4"),
        BUSINESS_ANALYSIS("经分系统", "5"), PROJECT("项目", "6"),
        PHONE_MANAGEMENT("客服外呼和电话经理", "7"), INFO_MANAGEMENT("管理信息系统", "8"),
        E_CHANNEL("电子渠道系统", "9"), CHANNEL_SYNERGY("渠道协同", "10"), CUSTOMER_SERVICE_INTERVENTION("客服接入", "11"),
        CUSTOMER_SERVICE_BUSINESS("客服业务", "12"), BIG_DATA("大数据", "13"), PORTAL("门户网站", "14");

        public String value;
        public String key;

        SYSNAME(String value, String key) {
            this.value = value;
            this.key = key;
        }

        public static String getValueByKey(String key) {
            for (SYSNAME sysname : SYSNAME.values()) {
                if (sysname.key.equals(key)) {
                    return sysname.value;
                }
            }
            return null;
        }

        public static String getKeyByValue(String value) {
            for (SYSNAME sysname : SYSNAME.values()) {
                if (sysname.value.equals(value)) {
                    return sysname.key;
                }
            }
            return null;
        }
    }

    public static enum ISONlINE {
        ALL("全部", "-1"), ONLINE("已上线", "1"), STAY_ONLINE("待上线", "0");

        public String value;
        public String key;

        ISONlINE(String value, String key) {
            this.value = value;
            this.key = key;
        }

        public static String getValueByKey(String key) {
            for (ISONlINE isOnline : ISONlINE.values()) {
                if (isOnline.key.equals(key)) {
                    return isOnline.value;
                }
            }
            return null;
        }
    }

    public static enum DEVTASKSTATUS {
        ALL("全部", "-1"), CLOSED("已关闭", "1"), DEVELOPMENT("开发中", "2"),
        CANCLE("已取消", "3"), BACK("已退回", "4"), SECURITY_ROOM_BACK("安全室退回", "5"),
        BE_ARRANGED("待安排开发工作", "6"), TO_COMPILE("待编译", "7");

        public String value;
        public String key;

        DEVTASKSTATUS(String value, String key) {
            this.value = value;
            this.key = key;
        }

        public static String getValueByKey(String key) {
            for (DEVTASKSTATUS devTaskStatus : DEVTASKSTATUS.values()) {
                if (devTaskStatus.key.equals(key)) {
                    return devTaskStatus.value;
                }
            }
            return null;
        }
    }

    public static enum DEPLOYENV {
        DCOS("dcos"), VM("vm");
        public String value;

        DEPLOYENV(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class DEAL_RESULT {
        public static final int SUCCESS = 2;
        public static final int FAIL = 3;
    }

    public static class GIT_CHECK {
        public static final int CODESTORE_EXIST = 1;
        public static final int CODESTORE_NO = 2;
        public static final int BRANCH_EXIST = 1;
        public static final int BRANCH_NO = 2;
    }

    public static String qryBuildType() {
        return "DEFAULT_STAGE";
    }

    public static String qryNameEnd(String type) {
        String para_type;
        if (("1").equals(type)) {//准发布
            para_type = "DEV";
        } else if (("2").equals(type)) {//测试环境
            para_type = "QA";
        } else if (("5").equals(type)) {//生产环境
            para_type = "PROD";
        } else {              //发布和准发布环境
            para_type = "FB";
        }
        return para_type;
    }

    public static class USER_LEVEL {
        public static final int USER_LEVEL_ADMIN = 0;//系统管理员
        public static final int USER_PROJECT_ADMIN = 1;//项目管理员

    }

    public static class STAGE_EALRESULT {
        public static Map<String, String> dealMap = new HashMap<>();

        static {
            dealMap.put("0", "wait");
            dealMap.put("1", "run");
            dealMap.put("2", "success");
            dealMap.put("3", "fail");
        }
    }

    public static class BUILD_LOG {
        public static final int BUILD_BEGINBUILD =0;
        public static final int BUILD_BUILDING =1;
        public static final int BUILD_SUCCESS =2;
        public static final int BUILD_FAIL =3;
    }

    public static class ENV_DETAIL {
        public static String SUCCESS_CODE = "200";
    }

    public static class PIP_DEALRESULT {
        public static Map<String, String> dealMap = new HashMap<>();

        static {
            dealMap.put("0", "run");
            dealMap.put("2", "success");
            dealMap.put("3", "fail");
        }
    }

    public static class STAGE_CODE {
        public static final int downLoad = 1;
        public static final int building = 2;
        public static final int unitTest = 3;
        public static final int builded = 5;
        public static final int deploy = 6;
        public static final int restart = 7;
        public static final int autoTest = 8;
        public static final int webScan = 16;
        public static final int wvs = 15;
        public static final int proScan = 4;
        public static final int sql = 17;
    }

    public static class USER_DATA_RELATE {
        public static final int AD_TREE_DATA = 1;
        public static final int AD_GROUP = 2;
        public static final int AD_PROJECT = 3;
    }

    public static class TREE_DATA {
        public static final int OPERATION_REPORT = 3;
    }

    public static class USER_4A_ACCT_STATUS {
        public static final int LOCKED = 0;
        public static final int NORMAL = 1;
        public static final int FORBIDDEN = 2;
        public static final int INVALID = 3;
    }

    public static class STAGE_INFO {
        public static Map<String, String> stageInfo = new HashMap<>();

        static {
            stageInfo.put("1", "更新文件数");
            stageInfo.put("2", "编译文件数");
            stageInfo.put("3", "测试用例数");
            stageInfo.put("5", "编译文件数");
            stageInfo.put("6", "部署IP");
            stageInfo.put("8", "自动化测试");
            stageInfo.put("16", "扫描结果");
            stageInfo.put("15", "漏洞总数");
        }
    }

    public static class BUILD_LOG_EXPORT_COLUMN {
        public static Map<String, String> EXPORT_COLUMN = new HashMap<>();

        static {
            EXPORT_COLUMN.put("link", "环节");
            EXPORT_COLUMN.put("productName", "项目名称");
            EXPORT_COLUMN.put("success", "成功");
            EXPORT_COLUMN.put("false", "失败");
            EXPORT_COLUMN.put("allCount", "总次数");
            EXPORT_COLUMN.put("proSuccess", "成功率");
            EXPORT_COLUMN.put("duration", "时长");
        }
    }

    public static String getDownJobName(String shellpath, String jobName) {
        if (StringUtils.isNotEmpty(shellpath)) {
            shellpath = shellpath.replaceAll("jobName", jobName) + "\n";
        }
        return shellpath;
    }

    public static String mergeDeployScript(AdParaDetailDAO bsParaDetailDAO, String scriptType, String envInfo, AdVirtualEnvironmentDAO virDao, AdDcosDeployDtlDAO adDcosDeployDtlDAO, String originPath, AdVirtualEnvironment adVirtualEnvironment, List<AdDcosDeployDtl> adDcosDeployDtlList, AdGroupImpl adGroupImpl, AdDcosDeployInfoDAO dcosDeployInfoDAO, AdProject adProject) {
        long envId = 0;
        String envType = null;
        if (StringUtils.isNotEmpty(envInfo) && envInfo.indexOf("_") > 0) {
            String[] envArray = envInfo.split("_");
            envId = Long.parseLong(envArray[0]);
            envType = envArray[1];
        }
        String deployShell = "";
        if (("vm").equals(envType) || ("dcos").equals(envType)) {
            List<AdParaDetail> adParaDetailList = bsParaDetailDAO.qryByDetails("X", scriptType, envType);
            if (CollectionUtils.isNotEmpty(adParaDetailList)) {
                deployShell = adParaDetailList.get(0).getPara1();
            }
            if (("vm").equals(envType)) {
                if (adVirtualEnvironment == null) {
                    adVirtualEnvironment = virDao.qryVmById(envId);
                }
                if ("DEPLOY_SCRIPT".equals(scriptType)) {
                    deployShell = deployShell + " " + originPath + " " + adVirtualEnvironment.getPackageName() + " " + adVirtualEnvironment.getServerUrl() + " " + adVirtualEnvironment.getServerUsername() + " " + adVirtualEnvironment.getServerPassword() + " " + adVirtualEnvironment.getDestinationAddress();
                } else if ("RESTART_SCRIPT".equals(scriptType)) {
                    deployShell = deployShell + " " + adVirtualEnvironment.getServerUsername() + " " + adVirtualEnvironment.getServerPassword() + " " + adVirtualEnvironment.getServerUrl() + " " + adVirtualEnvironment.getFilePath() + " " + adVirtualEnvironment.getFileName();
                }
            } else if (("dcos").equals(envType)) {
                if (CollectionUtils.isEmpty(adDcosDeployDtlList)) {
                    adDcosDeployDtlList = adDcosDeployDtlDAO.qryDcosDeployDtlByDcosInfoId(envId);
                }
                AdDcosDeployInfo adDcosDeployInfo = dcosDeployInfoDAO.qryDcosDeployInfoById(envId);
                Boolean isCmpGroup = adGroupImpl.qryIsCmpGroup(adProject.getProjectId()); // 查询 是否 云管入口进来的项目
                if ("DEPLOY_SCRIPT".equals(scriptType)) {
                    StringBuffer sbApp = new StringBuffer();
                    StringBuffer sbPack = new StringBuffer();
                    for (AdDcosDeployDtl adDcosDeployDtl : adDcosDeployDtlList) {
                        sbApp.append(adDcosDeployDtl.getAppid() + ",");
                        sbPack.append(adDcosDeployDtl.getPackageName() + ",");
                    }
                    if (sbApp.length() > 0 && sbPack.length() > 0 && adDcosDeployInfo != null) {
                        if (null != isCmpGroup && isCmpGroup) { // 判断 是否 云管入口进来的项目
                            adParaDetailList = bsParaDetailDAO.qryByDetails("X", "DEPLOY_SCRIPT_CMP", envType);
                            deployShell = (null != adParaDetailList) ? adParaDetailList.get(0).getPara1() : "";
                            deployShell = deployShell + " " + sbApp.substring(0, sbApp.length() - 1) + " " + originPath + " " + adDcosDeployInfo.getDocsServerUrl() + " " + adDcosDeployInfo.getDocsUserName() + " " + adDcosDeployInfo.getDocsUserPassword();
                        } else {   // ftp 信息是从 adDcosDeployInfo 表里读的
                            deployShell = deployShell + " " + sbApp.substring(0, sbApp.length() - 1) + " " + originPath + " " + sbPack.substring(0, sbPack.length() - 1) + " " + adDcosDeployInfo.getDcosFtpPath() + " " + adDcosDeployInfo.getDcosFtpUrl() + " " + adDcosDeployInfo.getDcosFtpUsername() + " " + adDcosDeployInfo.getDcosFtpPassword() + " " + adDcosDeployInfo.getDocsServerUrl() + " " + adDcosDeployInfo.getDocsUserName() + " " + adDcosDeployInfo.getDocsUserPassword();
                        }
                    }
                } else if ("RESTART_SCRIPT".equals(scriptType)) {
                    StringBuffer sb = new StringBuffer();
                    for (AdDcosDeployDtl adDcosDeployDtl : adDcosDeployDtlList) {
                        sb.append(adDcosDeployDtl.getAppid() + ",");
                    }
                    if (sb.length() > 0) {
                        if (isCmpGroup != null && isCmpGroup) { // 判断 是否 云管入口进来的项目
                            adParaDetailList = bsParaDetailDAO.qryByDetails("X", "RESTART_SCRIPT_CMP", envType);
                            deployShell = (adParaDetailList != null) ? adParaDetailList.get(0).getPara1() : "";
                        }
                        deployShell = deployShell + " " + sb.substring(0, sb.length() - 1) + " " + adDcosDeployInfo.getDocsServerUrl() + " " + adDcosDeployInfo.getDocsUserName() + " " + adDcosDeployInfo.getDocsUserPassword();
                    }
                }
            }
        }
        return deployShell;
    }


    public static String changeSchedulFormat(String srcSchedule) {
        if (StringUtils.isEmpty(srcSchedule)) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        String[] specs = srcSchedule.split(":");
        for (int i = 0; i < specs.length; i++) {
            if (i < specs.length - 1) {
                specs[i] = " " + specs[i];
            }
            sb.insert(0, specs[i]);
        }
        for (int i = 0; i < 5 - specs.length; i++) {
            sb.append(" ");
            sb.append("*");
        }
        return sb.toString();
    }

    public static String retSchedulFormat(String srcSchedule) {
        String[] specs = srcSchedule.split(" ");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < specs.length; i++) {
            if (!("*").equals(specs[i]) && StringUtils.isNotEmpty(specs[i])) {
                specs[i] = ":" + specs[i];
                sb.insert(0, specs[i]);
            }
        }
        srcSchedule = sb.toString();
        if (StringUtils.isNotEmpty(srcSchedule)) {
            srcSchedule = srcSchedule.substring(1);
        }

        return srcSchedule;
    }

    public static final long SYSTEM_BUILD_OP_ID = 1;

    public static class filterUncheckUrl {
        public static Set<String> UN_CHECK_URL = new HashSet<>();

        static {
            UN_CHECK_URL.add("UpdateFouraIntfAppAcctServices");
        }
    }
}
