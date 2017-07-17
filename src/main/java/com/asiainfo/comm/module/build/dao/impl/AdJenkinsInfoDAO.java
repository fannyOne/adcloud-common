package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.externalservice.jenkins.client.JenkinsClient;
import com.asiainfo.comm.externalservice.jenkins.guice.JenkinsWsClientGuiceModule;
import com.asiainfo.comm.externalservice.jenkins.jobconfig.entity.JobConfig;
import com.asiainfo.comm.externalservice.jenkins.jobs.Job;
import com.asiainfo.comm.module.models.AdJenkinsInfo;
import com.asiainfo.comm.module.models.query.QAdJenkinsInfo;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;

/**
 * Created by YangRY
 * on 2016/6/20 0020.
 */
@Component
public class AdJenkinsInfoDAO {

    public AdJenkinsInfo qryByJkId(long jkId) {
        List<AdJenkinsInfo> lists = new QAdJenkinsInfo().state.eq(1).jenkinsId.eq(jkId).findList();
        if (lists != null && lists.size() > 0) {
            return lists.get(0);
        }
        return null;
    }

    public List<AdJenkinsInfo> qryAllJenkins() {
        List<AdJenkinsInfo> lists = new QAdJenkinsInfo().state.eq(1).findList();
        return lists;
    }

    /**
     * 根据jenkins 配置和job名称获取脚本内容
     *
     * @param jenkinsId jenkins配置ID
     * @param jobName   job名字
     * @return 脚本内容存在返回脚本内容，否则返回 error
     */
    public String getJobContent(final long jenkinsId, final String jobName) throws Exception {
        //获得Jenkins信息
        AdJenkinsInfo jenkinsInfo = this.qryByJkId(jenkinsId);
        if (null == jenkinsInfo) {
            throw new Exception("没有Jenkins信息！");
        }
        String url = "http://" + jenkinsInfo.getJenkinsUrl() + ":" + jenkinsInfo.getServerPort();
        URL gUrl = new URL(url);
        Injector injector = Guice.createInjector(new JenkinsWsClientGuiceModule(gUrl, jenkinsInfo.getJenkinsUsername(), jenkinsInfo.getJenkinsPassword()));
        if (null == injector) {
            throw new Exception("没有获取注入信息");
        }
        JenkinsClient client = injector.getInstance(JenkinsClient.class);
        if (null == client) {
            throw new Exception("获取Jenkins客户端失败");
        }
        Job job = client.retrieveJob(jobName);
        if (null == job) {
            throw new Exception("获取Job失败");
        }
        JobConfig jobInfo = job.getJobinfo();
        if (null == jobInfo) {
            throw new Exception("获取job信息失败");
        }
        client.close();
        if (null == jobInfo.getBuilders() || jobInfo.getBuilders().isEmpty()) {
            throw new Exception("脚本内容不存在");
        }
        return jobInfo.getBuilders().get(0).getCommand();
    }

}
