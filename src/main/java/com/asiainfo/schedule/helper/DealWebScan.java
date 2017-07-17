package com.asiainfo.schedule.helper;

import com.asiainfo.comm.module.build.service.impl.AdOperationImpl;
import com.asiainfo.comm.module.models.AdStage;
import com.asiainfo.comm.module.models.AdStageLogDtl;
import com.asiainfo.util.JerseyClient;

import java.text.SimpleDateFormat;

/**
 * Created by YangRY
 * 2016/10/19 0019.
 */
public class DealWebScan implements Runnable {
    private AdStage stage;
    private AdStageLogDtl stageLogDtl;
    private AdOperationImpl operationImpl;


    public DealWebScan(AdStage stage, AdStageLogDtl stageLogDtl, AdOperationImpl operationImpl) {
        this.stage = stage;
        this.stageLogDtl = stageLogDtl;
        this.operationImpl = operationImpl;
    }

    @Override
    public void run() {
        String sessionId = JerseyClient.getSessionID();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
        boolean notDone = true;
        while (notDone) {
            try {
                notDone = operationImpl.dealWebScan(stage, stageLogDtl, sessionId, sdf);
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
