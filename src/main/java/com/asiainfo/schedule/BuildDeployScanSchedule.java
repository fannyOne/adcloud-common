package com.asiainfo.schedule;

import com.asiainfo.schedule.sync.BuildDeployScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.ParseException;

/**
 * Created by zhangpeng on 2016/7/12.
 */
@lombok.extern.slf4j.Slf4j
public class BuildDeployScanSchedule {
    @Autowired
    BuildDeployScan buildDeployScan;

    @Scheduled(cron = "0 0 23 */1 * *")
    public void getBuildDeploydata() throws ParseException {
        try {
            buildDeployScan.getBuildDeploydata();
        }catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
