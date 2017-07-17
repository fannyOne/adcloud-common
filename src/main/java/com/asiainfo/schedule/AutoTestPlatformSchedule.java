package com.asiainfo.schedule;

import com.asiainfo.schedule.key.AutoTestPlatform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.SQLException;

/**
 * Created by Steve.L
 */
@lombok.extern.slf4j.Slf4j
public class AutoTestPlatformSchedule {

    @Autowired
    AutoTestPlatform autoTestPlatform;

    @Scheduled(cron = "0 */1 * * * *")//每分钟执行一次
    public void autoTest() throws SQLException {
        try {
            autoTestPlatform.autoTest();
        }catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

}
