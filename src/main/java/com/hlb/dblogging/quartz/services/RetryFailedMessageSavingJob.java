package com.hlb.dblogging.quartz.services;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class RetryFailedMessageSavingJob extends QuartzJobBean{	

	SaveMessageService messageServiceJob = new SaveMessageService();
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException{
			try {
				messageServiceJob.saveAllFailedMessagesOfToday();
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
