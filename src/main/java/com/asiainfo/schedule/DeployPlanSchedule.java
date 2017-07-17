package com.asiainfo.schedule;

import com.asiainfo.schedule.key.DeployPlanJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * Created by YangRY
 * 2016/10/21 0021.
 * 抢占发布计划
 */
@lombok.extern.slf4j.Slf4j
public class DeployPlanSchedule {
    @Autowired
    DeployPlanJob deployPlanJob;

    @Scheduled(cron = "*/30 * * * * *")//每30秒执行一次
    public void dealPlan() {
        try {
            deployPlanJob.dealPlan();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
