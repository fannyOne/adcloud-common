package com.asiainfo.schedule.key;

import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.comm.module.autoTest.service.impl.AdAutoTestLogImpl;
import com.asiainfo.comm.module.autoTest.service.impl.AdSeqTestRelateImpl;
import com.asiainfo.comm.module.build.service.impl.*;
import com.asiainfo.comm.module.models.AdPipeLineState;
import com.asiainfo.comm.module.models.AdRmpBranchRelate;
import com.asiainfo.comm.module.models.AdStage;
import com.asiainfo.schedule.helper.RunAutoTest;
import com.asiainfo.comm.module.common.AdParaDetailImpl;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import org.apache.commons.lang.StringUtils;
import org.quartz.TriggerUtils;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by YangRY
 * 2016/7/5 0005.
 */
@lombok.extern.slf4j.Slf4j
@Component
public class AutoTestPlatform {
    //    private Executor pool = Executors.newFixedThreadPool(100);
    @Autowired
    AdPipeLineStateImpl pipeLineStateImpl;
    @Autowired
    AdRmpBranchRelateImpl rmpBranchRelateImpl;
    @Autowired
    AdStaticDataImpl staticDataImpl;
    @Autowired
    AdAutoTestLogImpl autoTestLogImpl;
    @Autowired
    AdStageImpl adStageImpl;
    @Autowired
    AdParaDetailImpl bsParaDetailImpl;
    @Autowired
    AdSeqTestRelateImpl seqTestRelateImpl;
    @Autowired
    AdStageLogDtlImpl adStageLogDtlImpl;

    @Async
    public void autoTest() throws SQLException {
        // 定时同步数据
        /*
         * 进程唯一性处理
         */
        /*List<AdStaticData> data = staticDataImpl.qryByCodeType("SYNC_RMP");
        if (data == null || data.size() <= 0 || data.get(0).getCodeValue() == null || !data.get(0).getCodeValue().equals(AD_CLOUD_IP)) {
            return;
        }*/
        /*
         * 进程唯一性处理结束
         */


        log.error("AutoTestPlatform is running");
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date();
        String jobSchedule;
        long dateCha;
        List<AdStage> adStageList = adStageImpl.qryAutoTestStages(8);
        if (!org.springframework.util.CollectionUtils.isEmpty(adStageList)) {
            for (AdStage adStage : adStageList) {
                if (StringUtils.isNotEmpty(adStage.getJobSchedule())) {
                    if (!"00000".equals(adStage.getJobSchedule())) {
                        jobSchedule = adStage.getJobSchedule();
                        String crontabExpress = "0 " + jobSchedule;
                        if (crontabExpress.charAt(crontabExpress.length() - 1) == '*') {
                            crontabExpress = crontabExpress.substring(0, crontabExpress.length() - 1) + "?";
                        }
                        try {
                            CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
                            cronTriggerImpl.setCronExpression(crontabExpress);
                            Calendar calendar = Calendar.getInstance();
                            Date now = calendar.getTime();
                            calendar.add(Calendar.MONTH, 1);//时间长度一个月
                            List<Date> dates = TriggerUtils.computeFireTimesBetween(cronTriggerImpl, null, now, calendar.getTime());
                            dateCha = DateConvertUtils.getSpace(date.getTime(), dates.get(0).getTime());
                            log.error("时间列表读取完成");
                            if (dateCha >= 0 && dateCha < 60000) {
                                log.error("定时器满足要求");
                                List<AdPipeLineState> adPipeLineStateList = pipeLineStateImpl.qryByLastBuildResult(adStage.getAdBranch().getBranchId(), 2);
                                if (!org.springframework.util.CollectionUtils.isEmpty(adPipeLineStateList)) {
                                    AdRmpBranchRelate rmpBranchRelate = rmpBranchRelateImpl.qryByBranchId(adStage.getAdBranch().getBranchId());
                                    if (rmpBranchRelate != null) {
                                        if (adStageImpl.updateState(adStage, 1)) {
                                            log.error("INBRANCH__ID“" + adStage.getAdBranch().getBranchId() + "_" + adStage.getAdBranch().getBranchName() + "”BEGINAUTOTEST");
                                            Thread thread = new Thread(new RunAutoTest(adStage.getAdBranch(), adStage,
                                                rmpBranchRelate, staticDataImpl, autoTestLogImpl,
                                                adStageImpl, bsParaDetailImpl,
                                                seqTestRelateImpl, adStageLogDtlImpl));
                                            thread.start();
                                        } else {
                                            log.error("ERRORBRANCH1“" + adStage.getAdBranch().getBranchId() + "_" + adStage.getAdBranch().getBranchName() + "AUTOTESTING");
                                        }
                                    } else {
                                        log.error("ERRORBRANCH2“" + adStage.getAdBranch().getBranchId() + "_" + adStage.getAdBranch().getBranchName() + "没有配置正确的系统大类！");
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }

    }
}
