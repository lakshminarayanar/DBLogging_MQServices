package com.hlb.dblogging.xslt.utility;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.springframework.context.ApplicationContext;

import com.hlb.dblogging.app.context.ApplicationContextProvider;
import com.hlb.dblogging.jpa.model.ConfigurationProperties;
import com.hlb.dblogging.jpa.service.ConfigurationPropertiesService;
import com.hlb.dblogging.log.utility.ApplLogger;
import com.hlb.dblogging.xml.utility.XSLTransformer;

@WebListener
public final class MyContextListenerClass implements ServletContextListener {
	public void contextInitialized(ServletContextEvent event) {

		/*
		 * This method is called prior to the servlet context being initialized
		 * (when the Web application is deployed). You can initialize servlet
		 * context related data here.
		 */
		try{
		ApplLogger.getLogger().info("contextInitialized started... @@@ ");
		new XSLTransformer();
		ApplLogger.getLogger().info("contextInitialized completed... @@@ ");
		
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		ConfigurationPropertiesService configurationService = (ConfigurationPropertiesService) applicationContext.getBean("configurationPropertiesService"); 
		ConfigurationProperties configuration =	configurationService.getApplicationConfiguration();
		XSLTransformer.logLevel = configuration.getLogLevel(); 
		XSLTransformer.retryPath = configuration.getRetryPath();
		ApplLogger.getLogger().info("LogLevel is configured to save messages : "+XSLTransformer.logLevel);
		ApplLogger.getLogger().info("Retry path is configured to save messages : "+XSLTransformer.retryPath);
		}catch(Exception e){
			ApplLogger.getLogger().error("Error while initializing the values..",e);
		}
	}

	public void contextDestroyed(ServletContextEvent event) {

		/*
		 * This method is invoked when the Servlet Context (the Web application)
		 * is undeployed or WebLogic Server shuts down.
		 */
		ApplLogger.getLogger().info("contextDestroyed completed... @@@ ");

	}
}