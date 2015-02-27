package com.hlb.dblogging.quartz.services;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.hlb.dblogging.log.utility.ApplLogger;

public class RetryFailedMessageSavingJob extends QuartzJobBean{	

	SaveMessageService messageServiceJob = new SaveMessageService();
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException{
			try {
				ApplLogger.getLogger().info("Quartz schedular is starting again..."+new Date());
				messageServiceJob.saveAllFailedMessagesInDirectory();
			} catch (Exception e) {
				e.printStackTrace();
			}		
	}
	public SaveMessageService getMessageServiceJob() {
		return messageServiceJob;
	}
	public void setMessageServiceJob(SaveMessageService messageServiceJob) {
		this.messageServiceJob = messageServiceJob;
	}	
	
	
	
	
}
