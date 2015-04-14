package com.hlb.dblogging.xslt.utility;

import org.springframework.context.ApplicationContext;

import com.hlb.dblogging.app.context.ApplicationContextProvider;
import com.hlb.dblogging.jpa.model.ConfigurationProperties;
import com.hlb.dblogging.jpa.service.ConfigurationPropertiesService;
import com.hlb.dblogging.log.utility.ApplLogger;
import com.hlb.dblogging.xml.utility.XSLTransformer;

public class SystemConfigPropertiesService {
	public void updateConfigurationWithDatabaseValues() {

		try{
			ApplLogger.getLogger().info("Started initializing the configuration properties from Database");
			new XSLTransformer();

			ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
			ConfigurationPropertiesService configurationService = (ConfigurationPropertiesService) applicationContext.getBean("configurationPropertiesService"); 
			ConfigurationProperties configuration =	configurationService.getApplicationConfiguration();
			XSLTransformer.logLevel = configuration.getLogLevel(); 
			XSLTransformer.retryPath = configuration.getRetryPath();
			if(XSLTransformer.xslTranformerStream==null || configuration.getXslTransformer()!=null)
				XSLTransformer.xslTranformerStream = configuration.getXslTransformer();
			ApplLogger.getLogger().info("LogLevel is configured to save messages : "+XSLTransformer.logLevel);
			ApplLogger.getLogger().info("Retry path is configured to save messages : "+XSLTransformer.retryPath);
			ApplLogger.getLogger().info("Completed successfully initializing the configuration properties from Database");
		}catch(Exception e){
			ApplLogger.getLogger().error("Error while initializing the configuration values..",e);
		}
	}

}