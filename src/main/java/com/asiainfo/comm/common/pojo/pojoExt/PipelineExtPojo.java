package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by yangry on 2016/6/15 0015.
 */
@Data
public class PipelineExtPojo implements Comparable<PipelineExtPojo> {
    private String name;//环境名称
    private String date;
    private long branchId;
    private String branchDesc;
    private int buildType;
    private List<JobExtPojo> job;
    private StageinfoExtPojo stageInfo;
    private boolean canRollBack;//是否能回滚
    private boolean canOperation;//是否有权限操作
    private long seqId;//构建流水的编号
    private Date startTime;
    private Long averageTime;
    private Long process;
    private Long stage;
    private String state;
    //流水添加时间
    private Date addTime;
    //流水类型
    private Integer branchType;

    public PipelineExtPojo() {
        this.canRollBack = true;
        this.canOperation = true;
    }

    /**
     * 添加排序规则,先根据类型,再根据添加时间先后顺序
     * 其他的排序要求可继续扩展
     *
     * @param pipel
     * @return
     */
    @Override
    public int compareTo(PipelineExtPojo pipel) {

        int result = pipel.branchType - this.branchType;
        switch (result) {
            case 0:
                //再根据添加时间
                if (pipel.branchId > this.branchId) {
                    return -1;
                } else {
                    return 1;
                }
        }
        return result > 0 ? 1 : -1;

    }


}
