package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.util.CommandValidator;
import lombok.Data;

import java.util.List;

/**
 * Created by dlyxn on 2017/5/9.
 */
@Data
public class AdReleasePlanStagePojoExt {
    /**
     * 主键
     */
    private Long id;
    /**
     * 计划id
     */
    private Long planId;
    /**
     * 节点序号
     */
    private Integer sequence;
    /**
     * 应用id
     */
    private Long projectId;
    /**
     * 应用名称
     */
    private String projectName;
    /**
     * 环境 id_type
     */
    private String env;
    /**
     * 环境类型
     */
    private Integer envType;
    /**
     * 环境id
     */
    private Integer envId;
    /**
     * groupId
     */
    private Long groupId;
    /**
     * appId
     */
    private List<AdReleaseAppIdPojoExt> appId;
    /**
     * 包名称
     */
    private String packageName;
    /**
     * 操作类型，1-发布，2-重启
     */
    private Integer operType;
    /**
     * 发布类型   1-全量，3-bate发布，2灰度发布
     */
    private Integer type;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 审核人id
     */
    private Long auditor;
    /**
     * 审核时间
     */
    private String reviewTime;
    /**
     * 0-失败，1-成功,2-待执行，3-执行中，4-删除
     */
    private Integer result;
    /**
     * 发布日志
     */
    private String deployComment;
    /**
     * 发布备注
     */
    private String remarks;
    /**
     * 流水d
     */
    private Long branchId;
    /**
     *
     */
    private Long proTagId;
    /**
     * job token
     */
    private String jobToken;
    /**
     * 环境名称
     */
    private String envName;
    /**
     * 暂停
     */
    private Integer pause;
    /**
     * 发布时间
     */
    private String time;
    /**
     * 审核人名称
     */
    private String auditorName;
    /**
     * 节点开始图标
     */
    private boolean nodeStartIcon;
    /**
     * 节点暂停图标
     */
    private boolean nodePauseIcon;
    /**
     * 最新发布包
     */
    private String packageVersion;

    /**
     * 判空
     */
    public void formatCheck() {
        CommandValidator.assertObjectNotNull("groupId", groupId);
        CommandValidator.assertObjectNotNull("projectId", projectId);
        CommandValidator.assertObjectNotNull("operType", operType);
        CommandValidator.assertObjectNotNull("type", type);
        CommandValidator.assertObjectNotNull("env", env);
        CommandValidator.assertObjectNotNull("sequence", sequence);
    }
}
