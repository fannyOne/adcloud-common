package com.asiainfo.schedule;

import com.asiainfo.schedule.sync.SonarScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * Created by zhangpeng on 2016/7/5.
 */
@lombok.extern.slf4j.Slf4j
public class SonarScanSchedule {
    @Autowired
    private SonarScan sonarScan;

    @Scheduled(cron = "0 0 23 */1 * *")
    public void getSonarsCur() throws Exception { // TODO 获取最近一次的Sonar信息
        try {
            sonarScan.getSonarsCur();
        }catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

}


