package com.asiainfo.schedule;

import com.asiainfo.schedule.timedbuild.PipelineScanJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * Created by liuzx on 2017/5/23.
 * <p>
 * 扫描流水线根据,根据流水线的cron表达式定时构建
 */
@lombok.extern.slf4j.Slf4j
public class PipelineScanSchedule {

    public final static String cron = "0/30 * * * * ?";

    @Autowired
    PipelineScanJob pipelineScanJob;

    @Scheduled(cron = cron)
    public void scan() {
        try{
            pipelineScanJob.scan();
        }catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
