package com.asiainfo.schedule.quartz.manager;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/** */

/**
 * Quartz管理类
 *
 * @author zz 2008-10-8 14:19:01
 * @version 1.00.000
 */
public class QuartzManager {
    private static SchedulerFactory sf = new StdSchedulerFactory();
    private static String JOB_GROUP_NAME = "group1";
    private static String TRIGGER_GROUP_NAME = "trigger1";

    /** */
    /**
     * 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
     *
     * @param jobName
     *            任务名
     * @param job
     *            任务
     * @param time
     *            时间设置，参考quartz说明文档
     * @throws SchedulerException
     * @throws ParseException
     */
//	public static void addJob(String jobName, Job job, String time,
//			Map<String, Object> params) throws SchedulerException,
//			ParseException {
//		Scheduler sched = sf.getScheduler();
//		JobDetail jobDetail = new JobDetail(jobName, JOB_GROUP_NAME,
//				job.getClass());// 任务名，任务组，任务执行类
//		// 参数
//		if (params != null) {
//			for (Map.Entry<String, Object> entry : params.entrySet()) {
//				jobDetail.getJobDataMap().put(entry.getKey(), entry.getValue());
//			}
//		}
//		// 触发器
//		CronTrigger trigger = new CronTrigger(jobName, TRIGGER_GROUP_NAME);// 触发器名,触发器组
//		trigger.setCronExpression(time);// 触发器时间设定
//		sched.scheduleJob(jobDetail, trigger);
//		// 启动
//		if (!sched.isShutdown())
//			sched.start();
//	}

    /** */
//	/**
//	 * 添加一个定时任务
//	 *
//	 * @param jobName
//	 *            任务名
//	 * @param jobGroupName
//	 *            任务组名
//	 * @param triggerName
//	 *            触发器名
//	 * @param triggerGroupName
//	 *            触发器组名
//	 * @param job
//	 *            任务
//	 * @param time
//	 *            时间设置，参考quartz说明文档
//	 * @throws SchedulerException
//	 * @throws ParseException
//	 */
//	public static void addJob(String jobName, String jobGroupName,
//			String triggerName, String triggerGroupName, Job job, String time,
//			Map<String, Object> params) throws SchedulerException,
//			ParseException {
//		Scheduler sched = sf.getScheduler();
//		JobDetail jobDetail = new JobDetail(jobName, jobGroupName,
//				job.getClass());// 任务名，任务组，任务执行类
//		// 参数
//		if (params != null) {
//			for (Map.Entry<String, Object> entry : params.entrySet()) {
//				jobDetail.getJobDataMap().put(entry.getKey(), entry.getValue());
//			}
//		}
//		// 触发器
//		CronTrigger trigger = new CronTrigger(triggerName, triggerGroupName);// 触发器名,触发器组
//		trigger.setCronExpression(time);// 触发器时间设定
//		sched.scheduleJob(jobDetail, trigger);
//		if (!sched.isShutdown())
//			sched.start();
//	}
    public static void addOneTimeJob(String jobName, Job job, String time,
                                     Map<String, Object> params) throws SchedulerException,
        ParseException {
        Scheduler sched = sf.getScheduler();
        JobDetail jobDetail = newJob(job.getClass()).withIdentity(jobName, JOB_GROUP_NAME).build();
//        JobDetail jobDetail = new JobDetail(jobName, JOB_GROUP_NAME,
//            job.getClass());// 任务名，任务组，任务执行类
        // 参数
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                jobDetail.getJobDataMap().put(entry.getKey(), entry.getValue());
            }
        }
//        Date date = new Date(time);
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date date = fmt.parse(time);
        // 触发器
        Trigger trigger = (SimpleTrigger) newTrigger().withIdentity(jobName, TRIGGER_GROUP_NAME).startAt(date).build();
//        Trigger trigger = new SimpleTrigger(jobName, TRIGGER_GROUP_NAME);// 触发器名,触发器组
        //trigger.setCronExpression(time);// 触发器时间设定
//        trigger.setStartTime(date);
//        trigger.set
        sched.scheduleJob(jobDetail, trigger);
        // 启动
        if (!sched.isShutdown())
            sched.start();
    }
    /** */
    /**
     * 修改一个任务的触发时间(使用默认的任务组名，触发器名，触发器组名)
     *
     * @param jobName
     * @param time
     * @throws SchedulerException
     * @throws ParseException
     */
//	public static void modifyJobTime(String jobName, String time)
//			throws SchedulerException, ParseException {
//		Scheduler sched = sf.getScheduler();
//		Trigger trigger = sched.getTrigger(jobName, TRIGGER_GROUP_NAME);
//		if (trigger != null) {
//			CronTrigger ct = (CronTrigger) trigger;
//			ct.setCronExpression(time);
//			sched.resumeTrigger(jobName, TRIGGER_GROUP_NAME);
//		}
//	}

    /** */
    /**
     * 修改一个任务的触发时间
     *
     * @param triggerName
     * @param triggerGroupName
     * @param time
     * @throws SchedulerException
     * @throws ParseException
     */
//	public static void modifyJobTime(String triggerName,
//			String triggerGroupName, String time) throws SchedulerException,
//			ParseException {
//		Scheduler sched = sf.getScheduler();
//		Trigger trigger = sched.getTrigger(triggerName, triggerGroupName);
//		if (trigger != null) {
//			CronTrigger ct = (CronTrigger) trigger;
//			// 修改时间
//			ct.setCronExpression(time);
//			// 重启触发器
//			sched.resumeTrigger(triggerName, triggerGroupName);
//		}
//	}

//	/** */
//	/**
//	 * 移除一个任务(使用默认的任务组名，触发器名，触发器组名)
//	 *
//	 * @param jobName
//	 * @throws SchedulerException
//	 */
//	public static void removeJob(String jobName) throws SchedulerException {
//		Scheduler sched = sf.getScheduler();
//		sched.pauseTrigger(jobName, TRIGGER_GROUP_NAME);// 停止触发器
//		sched.unscheduleJob(jobName, TRIGGER_GROUP_NAME);// 移除触发器
//		sched.deleteJob(jobName, JOB_GROUP_NAME);// 删除任务
//	}

    /** */
    /**
     * 移除一个任务
     *
     * @param jobName
     * @param jobGroupName
     * @param triggerName
     * @param triggerGroupName
     * @throws SchedulerException
     */
//	public static boolean removeJob(String jobName, String jobGroupName,
//			String triggerName, String triggerGroupName)
//			throws SchedulerException {
//		boolean removeSign = true;
//		Scheduler sched = sf.getScheduler();
//		sched.pauseTrigger(triggerName, triggerGroupName);// 停止触发器
//		removeSign = sched.unscheduleJob(triggerName, triggerGroupName) & // 移除触发器
//				sched.deleteJob(jobName, jobGroupName);// 删除任务
//		return removeSign;
//	}

//	public static String getTrigger(String triggerName, String triggerGroup)
//			throws SchedulerException {
//		Scheduler sched = sf.getScheduler();
//		Trigger trigger = sched.getTrigger(triggerName, triggerGroup);
//		return trigger.getCalendarName();
//	}
}
