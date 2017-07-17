package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.util.CommandValidator;
import lombok.Data;

import java.util.List;

/**
 * 发布计划
 */
@Data
public class AdReleasePlanPojoExt {
    /**
     * 主键
     */
    private Long id;
    /**
     * groupId
     */
    private Long groupId;
    /**
     * 发布时间
     */
    private String releaseTime;
    /**
     * 节点个数
     */
    private Integer stageNum;
    /**
     * 计划状态，0-失败，1-成功，2-待执行, 3-执行中，4-删除
     */
    private Integer planState;
    /**
     * 计划开始时间
     */
    private String startTime;
    /**
     * 计划结束时间
     */
    private String endTime;
    /**
     * 失败节点位置
     */
    private Integer failStage;
    /**
     * 操作员名称
     */
    private String operator;
    /**
     * 暂停，0-正常，1-暂停
     */
    private Integer pause;
    /**
     *
     */
    private Integer pauseStage;
    /**
     * 节点信息
     */
    private List<AdReleasePlanStagePojoExt> releaseNode;
    /**
     * 计划标头开始图标
     */
    private boolean planStarIcon;
    /**
     * 暂停图标
     */
    private boolean planPauseIcon;

    /**
     * 判空
     */
    public void formatCheck() {
        CommandValidator.assertObjectNotNull("groupId", groupId);
        CommandValidator.assertObjectNotNull("releaseNode", releaseNode);
    }
}
