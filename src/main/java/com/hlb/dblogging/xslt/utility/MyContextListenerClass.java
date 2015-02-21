package com.hlb.dblogging.xslt.utility;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

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
		ApplLogger.getLogger().info("contextInitialized started... @@@ ");
		new XSLTransformer();
		ApplLogger.getLogger().info("contextInitialized completed... @@@ ");
		

	}

	public void contextDestroyed(ServletContextEvent event) {

		/*
		 * This method is invoked when the Servlet Context (the Web application)
		 * is undeployed or WebLogic Server shuts down.
		 */
		ApplLogger.getLogger().info("contextDestroyed completed... @@@ ");

	}
}