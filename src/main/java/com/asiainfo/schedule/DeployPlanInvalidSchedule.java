package com.asiainfo.schedule;

import com.asiainfo.schedule.key.DeployPlanInvalidJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * Created by YangRY
 * 2016/10/21 0021.
 * 处理失效的发布计划
 */
@lombok.extern.slf4j.Slf4j
public class DeployPlanInvalidSchedule {
    @Autowired
    DeployPlanInvalidJob deployPlanInvalidJob;

    @Scheduled(cron = "0 */5 * * * *")//每分钟执行一次
    public void dealPlan() {
        try {
            deployPlanInvalidJob.dealPlan();
        }catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
