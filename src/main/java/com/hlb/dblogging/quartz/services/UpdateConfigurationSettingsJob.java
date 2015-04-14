package com.hlb.dblogging.quartz.services;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.hlb.dblogging.log.utility.ApplLogger;
import com.hlb.dblogging.xslt.utility.SystemConfigPropertiesService;

public class UpdateConfigurationSettingsJob extends QuartzJobBean{

	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException{
			try {
				ApplLogger.getLogger().info("Quartz schedular is starting to update Configuration..."+new Date());
				new SystemConfigPropertiesService().updateConfigurationWithDatabaseValues();
			} catch (Exception e) {
				e.printStackTrace();
			}		
	}
}
