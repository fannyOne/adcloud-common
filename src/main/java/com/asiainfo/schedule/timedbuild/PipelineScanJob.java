package com.asiainfo.schedule.timedbuild;

import com.asiainfo.comm.common.pojo.pojoMaster.ManualHandPojo;
import com.asiainfo.comm.module.build.service.impl.AdBranchImpl;
import com.asiainfo.comm.module.build.service.impl.JenkinsImpl;
import com.asiainfo.comm.module.deploy.dao.impl.AdTimedBuildTaskLogDAO;
import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.schedule.PipelineScanSchedule;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.CronUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by liuzx on 2017/5/23.
 * <p>
 * 扫描流水线根据,根据流水线的cron表达式定时构建
 */
@lombok.extern.slf4j.Slf4j
@Component
public class PipelineScanJob {

    private Date now = new Date();

    @Autowired
    JenkinsImpl jenkinsImpl;

    @Autowired
    AdBranchImpl adBranchImpl;

    @Autowired
    AdTimedBuildTaskLogDAO adTimedBuildTaskLogDAO;

    @Async
    public void scan() {
        try{
            Date scanNext = CronUtil.getNextDate(PipelineScanSchedule.cron, new Date());
            log.error("PipelineScanJob is running at " + now + " - " + scanNext);
            //开始扫描
            List<AdBranch> list = adBranchImpl.qryBranchCronExsit();
            int num = 0;
            if (CollectionUtils.isNotEmpty(list)) {
                for (AdBranch adBranch : list) {
                    String buildCron = adBranch.getBuildCron();
                    try {
                        if (StringUtils.isNotEmpty(PipelineScanSchedule.cron) && StringUtils.isNotEmpty(buildCron) && CronUtil.checkCron(buildCron)) {
                            Date buildNext = CronUtil.getNextDate(buildCron, now);
                            if (buildNext.getTime() <= scanNext.getTime()) {
                                //构建该流水
                                num++;
                                execute(adBranch);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.toString(),e);
                    }
                }
            }
            log.error("PipelineScanJob end, " + num + " branch build");
            now = scanNext;
        }catch (Exception e) {
            log.error(e.toString(),e);
        }
    }

    public void execute(AdBranch adBranch) throws Exception{
        ManualHandPojo manualHandPojo = jenkinsImpl.triggerJenkins(adBranch.getBranchId(), adBranch.getBranchType(), CommConstants.SYSTEM_BUILD_OP_ID+"", false, "");
        if ("200".equals(manualHandPojo.getRetCode())) {
            adTimedBuildTaskLogDAO.save(adBranch.getBranchId(), "success", manualHandPojo.getRetMessage());
        } else {
            adTimedBuildTaskLogDAO.save(adBranch.getBranchId(), "fail", manualHandPojo.getRetMessage());
        }
    }
}
