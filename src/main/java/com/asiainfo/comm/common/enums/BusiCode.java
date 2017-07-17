package com.asiainfo.comm.common.enums;


public enum BusiCode {
    // 模块操作码的范围 10 0001 ~ 19 9999

    // build 11 0001 ~ 11 9999
    BUILD_START(110001L, "构建"),
    ROLL_BACK(110002L, "回滚"),
    RESTART(110003L, "重启"),
    BUILD_STOP(110004L, "停止构建"),
    // deploy 12 0001 ~ 12 9999
    DEPLOY_START(120001L, "开始部署"),//未使用
    RE_DEPLOY(120002L, "部署"),
    // other 13 0001 ~ 13 9999
    AUTO_TEST_START(130001L, "自动化测试"),//未使用
    // user 140001~149999
    SIGN_IN(140001L, "登录"),
    SIGN_OUT(140002L, "注销"),
    FASTEN_SIGN_IN(140003L, "订阅"),
    FASTEN_SIGN_PULL(140004L, "取消订阅"),
    CODE_QRY(140005L, "查看代码"),
    // project 15 0001 ~ 15 9999
    ADD_GROUP(150001L, "添加项目"),
    DEL_GROUP(150002L, "删除项目"),
    ADD_PROJECT(150003L, "添加应用"),
    DEL_PROJECT(150004L, "删除应用"),
    ADD_BRANCH(150005L, "添加流水"),
    DEL_BRANCH(150006L, "删除流水"),
    UPDATE_BRANCH_JK(150007L, "修改节点配置"),
    UPDATE_BRANCH(150008L, "修改流水"),
    COPY_BRANCH(150009L, "复制流水"),
    // 系统操作码的范围 20 0001 ~ 29 9999
    // role 21 0001 ~ 29 9999
    USER_ROLE_ADD(210001L, "添加用户角色"),
    CHANGE_ROLE_AUTHOR(210002L, "修改角色权限"),
    UPDATE_GROUP_MEMBER(210003L, "修改项目角色"),
    DEL_GROUP_MEMBER(210004L, "删除项目权限"),
    // 环境相关操作 30 0001 ~ 39 9999
    // env 31 0001 ~ 31 9999
    ENV_DCOS_ADD(310001L, "添加DCOS环境"),
    ENV_DCOS_MODIFY(310002L, "修改DCOS环境"),
    ENV_DCOS_DEL(310003L, "删除DCOS环境"),
    ENV_VM_ADD(310004L, "添加虚拟机环境"),
    ENV_VM_MODIFY(310005L, "修改虚拟机环境"),
    ENV_VM_DEL(310006L, "删除虚拟机环境"),
    // release 32 0001 ~ 32 9999
    RELEASE_ADD(320001L, "新建发布计划"),//未使用
    RELEASE_MODIFY(320002L, "修改发布计划"),//未使用
    RELEASE_CLOSE(320003L, "删除发布计划"),//未使用
    RELEASE_QRY(320004L, "查看发布记录"),//未使用
    // 统计类操作 40 0001 ~ 49 9999
    // report 41 0001 ~ 41 9999
    REPORT_BUILD(410001L, "查看构建报表"),//未使用
    REPORT_BUILD_DEPLOY(410002L, "查看构建部署报表"),//未使用
    REPORT_SONAR(410003L, "查看代码质量报表"),//未使用
    REPORT_ACCESS(410003L, "查看平台接入报表"),//未使用
    // export 41 0001 ~ 41 9999
    EXPORT_BUILD_DEPLOY(420001L, "导出构建部署报表"),//未使用
    EXPORT_SONAR(420002L, "导出代码质量报表"),//未使用
    EXPORT_ACCESS(420003L, "导出平台接入报表");//未使用


    /**
     * 枚举编号
     */
    private Long code;

    /**
     * 枚举详情
     */
    private String description;

    /**
     * 构造方法
     *
     * @param code        枚举编号
     * @param description 枚举详情
     */
    private BusiCode(Long code, String description) {
        this.code = code;
        this.description = description;
    }

    public static BusiCode getByCode(Long code) {
        for (BusiCode param : values()) {
            if (param.getCode().equals(code)) {
                return param;
            }
        }
        return null;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        String s = getClass().getName();
        String message = "code:" + this.code + ",description:" + this.description;
        return s + message;
    }

}
