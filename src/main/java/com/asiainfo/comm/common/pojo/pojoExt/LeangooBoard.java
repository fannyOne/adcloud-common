package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by YangRY
 * 2016/10/10 0010.
 */
@Data
public class LeangooBoard {
    private long boardId;//看板ID
    private String boardName;//看板名称
    private int permission;//未知
    private String isStarBoard;//是否是星标看板
    private String position;//未知
    private boolean hasChart;//是否可以查看图标
    private int yaxisMaxValue;//y轴最大值
    private int finishedEffort;//已完成工作量
    private int remainEffort;//剩余
    private String statisticPattern;//维度：工作量/卡片
    private int xaxisMaxValue;//x轴最大值
    private List<LeangooChartPoint> points;//x-y键值对
    private String message;//没有数据的信息
}
