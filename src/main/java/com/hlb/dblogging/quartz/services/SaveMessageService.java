package com.hlb.dblogging.quartz.services;

import java.io.Serializable;

import org.springframework.context.ApplicationContext;

import com.hlb.dblogging.app.context.ApplicationContextProvider;

public class SaveMessageService  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5603005414859339769L;
	ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
	// EmployeeService employeeService = (EmployeeService) applicationContext.getBean("employeeService");
	
	public void saveAllFailedMessagesOfToday(){
		
	}

}
