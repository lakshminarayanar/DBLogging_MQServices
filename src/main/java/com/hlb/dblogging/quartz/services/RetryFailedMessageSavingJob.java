package com.hlb.dblogging.quartz.services;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class RetryFailedMessageSavingJob extends QuartzJobBean{	

	SaveMessageServiceJob messageServiceJob = new SaveMessageServiceJob();
	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException{
			try {
				messageServiceJob.saveAllFailedMessagesOfToday();
			} catch (Exception e) {
				e.printStackTrace();
			}		
	}
	public SaveMessageServiceJob getMessageServiceJob() {
		return messageServiceJob;
	}
	public void setMessageServiceJob(SaveMessageServiceJob messageServiceJob) {
		this.messageServiceJob = messageServiceJob;
	}	
	
	
	
	
}
