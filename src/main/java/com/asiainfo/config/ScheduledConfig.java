package com.asiainfo.config;

import com.asiainfo.schedule.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Created by Administrator on 2016/8/4.
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ScheduledConfig {

    @Value("${schedul.deploy:none}")
    private String schedulModel;


    @Bean
    public AutoTestPlatformSchedule getAutoTestPlatform() {
        if (!schedulModel.contains("schedulModel")) {
            return null;
        }
        return new AutoTestPlatformSchedule();
    }

    @Bean
    public SonarScanSchedule getSonarScan() {
        if (!schedulModel.contains("sonarScan")) {
            return null;
        }
        return new SonarScanSchedule();
    }

    @Bean
    public BuildDeployScanSchedule getBuildDeployScan() {
        if (!schedulModel.contains("buildDeployScan")) {
            return null;
        }
        return new BuildDeployScanSchedule();
    }

    @Bean
    public DeployPlanInvalidSchedule getDeployInvalidPlan() {
        if (!schedulModel.contains("schedulModel")) {
            return null;
        }
        return new DeployPlanInvalidSchedule();
    }

    @Bean
    public DeployPlanSchedule getDeployPlan() {
        if (!schedulModel.contains("schedulModel")) {
            return null;
        }
        return new DeployPlanSchedule();
    }

    @Bean
    public PipelineScanSchedule getPipelineScanSchedule() {
        if (!schedulModel.contains("schedulModel")) {
            return null;
        }
        return new PipelineScanSchedule();
    }

    /**
     * 线程池
     */
    @Bean
    public AsyncTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("ADCloudExecutor-");
        executor.initialize();
        return executor;
    }

}
