package com.hlb.dblogging.xslt.utility;

import java.util.HashMap;
import java.util.List;

import org.springframework.context.ApplicationContext;

import com.hlb.dblogging.app.context.ApplicationContextProvider;
import com.hlb.dblogging.jpa.model.ConfigurationProperties;
import com.hlb.dblogging.jpa.model.XSLT;
import com.hlb.dblogging.jpa.service.ConfigurationPropertiesService;
import com.hlb.dblogging.jpa.service.XSLTService;
import com.hlb.dblogging.log.utility.ApplLogger;
import com.hlb.dblogging.xml.utility.XSLTransformer;

public class SystemConfigPropertiesService {
	public void updateConfigurationWithDatabaseValues() {

		try{
			ApplLogger.getLogger().info("Started initializing the configuration properties from Database");
			ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
			ConfigurationPropertiesService configurationService = (ConfigurationPropertiesService) applicationContext.getBean("configurationPropertiesService"); 
			ConfigurationProperties configuration =	configurationService.getApplicationConfiguration();
			XSLTransformer.logLevel = configuration.getLogLevel(); 
			XSLTransformer.retryPath = configuration.getRetryPath();
			XSLTransformer.isSaveEnabled = configuration.isSaveEnabled();
			XSLTransformer.isViewEnabled = configuration.isViewEnabled();
			XSLTService xsltService = (XSLTService) applicationContext.getBean("xsltService");
			List<XSLT>	dbListOfXSLT =	xsltService.getAllActiveAndNotDeleted("SAVE");
			if(dbListOfXSLT!=null && !dbListOfXSLT.isEmpty()){
				XSLTransformer.xsltMap = new HashMap<String, String>();
				for (XSLT xslt : dbListOfXSLT) {
					if(XSLTransformer.xsltMap!=null){
						XSLTransformer.xsltMap.put(xslt.getTransType(), xslt.getXsltFile());
						ApplLogger.getLogger().info("Adding to the Application Cache Trans Type : "+xslt.getTransType()+" Mode :"+xslt.getViewOrSave());
					}else
						ApplLogger.getLogger().info("Map is returning null object, just check it before adding objects to it."+XSLTransformer.xsltMap);
				}
			}
			
			ApplLogger.getLogger().info("LogLevel is configured to save messages : "+XSLTransformer.logLevel);
			ApplLogger.getLogger().info("Retry path is configured to save messages : "+XSLTransformer.retryPath);
			ApplLogger.getLogger().info("Completed successfully initializing the configuration properties from Database");
		}catch(Exception e){
			ApplLogger.getLogger().error("Error while initializing the configuration values..",e);
			throw new RuntimeException("Error while initializing the configuration values from the Database, Application may not work properly.. ");
		}
	}

}