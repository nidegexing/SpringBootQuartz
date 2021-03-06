package cn.bill.quartz.configuration;

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import cn.bill.quartz.entity.CronEntity;
/**
 * 
 * Description: Quartz定时器管理类<br/>
 * Date:2018年9月12日 下午1:21:20 <br/>
 *
 * @author fengminbiao@126.com
 * @version
 */
@Configuration
public class QuartzManager
{
	@Autowired
	private Scheduler scheduler;
	/**
	 * 启动定时器
	 * @param cronEntity 表达式管理器
	 * @throws SchedulerException
	 */
	public void start(CronEntity cronEntity) throws SchedulerException
	{
		if (cronEntity != null)
		{
			startJob(scheduler, cronEntity.getQuarzName(), cronEntity.getCron(), cronEntity.getSchedulerClass());
		}
	}

	/**
	 * 启动任务
	 * @param scheduler
	 * @param name
	 * @param cron
	 * @param className
	 * @throws SchedulerException
	 */
	@SuppressWarnings("unchecked")
	private void startJob(Scheduler scheduler, String name, String cron, String className) throws SchedulerException
	{
		// 通过JobBuilder构建JobDetail实例，JobDetail规定只能是实现Job接口的实例
		// JobDetail 是具体Job实例
		Class<Job> jobClass = null;
		try
		{
			// 实例化具体的Job任务
			jobClass = (Class<Job>) Class.forName(className);
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(name, "group1").build();
		// 基于表达式构建触发器
		CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
		// CronTrigger表达式触发器 继承于Trigger
		// TriggerBuilder 用于构建触发器实例
		CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(name, "group1")
				.withSchedule(cronScheduleBuilder).build();
		scheduler.scheduleJob(jobDetail, cronTrigger);
	}

	/**
	 * 获取Job信息
	 *
	 * @param name
	 * @param group
	 * @return
	 * @throws SchedulerException
	 */
	public String getJobInfo(String name, String group) throws SchedulerException
	{
		TriggerKey triggerKey = new TriggerKey(name, group);
		CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
		return String.format("time:%s,state:%s", cronTrigger.getCronExpression(),
				scheduler.getTriggerState(triggerKey).name());
	}

	/**
	 * 修改某个任务的执行时间
	 *
	 * @param name
	 * @param group
	 * @param time
	 * @return
	 * @throws SchedulerException
	 */
	public boolean modifyJob(String name, String group, String time) throws SchedulerException
	{
		Date date = null;
		TriggerKey triggerKey = new TriggerKey(name, group);
		CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
		String oldTime = cronTrigger.getCronExpression();
		if (!oldTime.equalsIgnoreCase(time))
		{
			CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(time);
			CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group)
					.withSchedule(cronScheduleBuilder).build();
			date = scheduler.rescheduleJob(triggerKey, trigger);
		}
		return date != null;
	}

	/**
	 * 暂停所有任务
	 *
	 * @throws SchedulerException
	 */
	public void pauseAllJob() throws SchedulerException
	{
		scheduler.pauseAll();
	}

	/**
	 * 暂停某个任务
	 *
	 * @param name
	 * @param group
	 * @throws SchedulerException
	 */
	public void pauseJob(String name, String group) throws SchedulerException
	{
		JobKey jobKey = new JobKey(name, group);
		JobDetail jobDetail = scheduler.getJobDetail(jobKey);
		if (jobDetail == null)
			return;
		scheduler.pauseJob(jobKey);
	}

	/**
	 * 恢复所有任务
	 *
	 * @throws SchedulerException
	 */
	public void resumeAllJob() throws SchedulerException
	{
		scheduler.resumeAll();
	}

	/**
	 * 恢复某个任务
	 *
	 * @param name
	 * @param group
	 * @throws SchedulerException
	 */
	public void resumeJob(String name, String group) throws SchedulerException
	{
		JobKey jobKey = new JobKey(name, group);
		JobDetail jobDetail = scheduler.getJobDetail(jobKey);
		if (jobDetail == null)
			return;
		scheduler.resumeJob(jobKey);
	}

	/**
	 * 删除某个任务
	 *
	 * @param name
	 * @param group
	 * @throws SchedulerException
	 */
	public void deleteJob(String name, String group) throws SchedulerException
	{
		JobKey jobKey = new JobKey(name, group);
		JobDetail jobDetail = scheduler.getJobDetail(jobKey);
		if (jobDetail == null)
			return;
		scheduler.deleteJob(jobKey);
	}
}