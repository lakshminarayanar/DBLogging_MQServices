package com.hlb.dblogging.quartz.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;

import com.hlb.dblogging.app.context.ApplicationContextProvider;
import com.hlb.dblogging.log.utility.ApplLogger;
import com.hlb.dblogging.mdb.service.XSLTransformerService;
import com.hlb.dblogging.xml.utility.XSLTransformer;

public class SaveMessageService  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5603005414859339769L;
	ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
	// EmployeeService employeeService = (EmployeeService) applicationContext.getBean("employeeService");
	
	public void saveAllFailedMessagesInDirectory(){
		XSLTransformerService xmlService = new XSLTransformerService();
		ByteArrayOutputStream saveOnErrorXMLMessage = null;
		ByteArrayOutputStream outputXML = null;
		try{
		File folder = new File(XSLTransformer.retryPath);
		 for (final File fileEntry : folder.listFiles()) {
		        if (fileEntry.isFile()) {
		        	try{
		            ApplLogger.getLogger().info("Absolute path of currently processing file : "+fileEntry.getAbsolutePath());
		            File xmlFille = new File(fileEntry.getAbsolutePath());
		            saveOnErrorXMLMessage = new ByteArrayOutputStream();
		            outputXML = new ByteArrayOutputStream();
		            IOUtils.copy(new ByteArrayInputStream(FileUtils.readFileToByteArray(xmlFille)), outputXML);
		            // processing and retrying to save xml to database
		            xmlService.processXmlAndSave(saveOnErrorXMLMessage, outputXML);
		            // Remove the file 
		            fileEntry.delete();
		        	}catch(Exception e){
		        		
		        	}
		        	finally{
		        		saveOnErrorXMLMessage.close();
		        		outputXML.close();
		        	}
		        }
		    }
		 
		}catch(Exception e){
			ApplLogger.getLogger().error("Error caught while saving the file info again...!!!",e);
		}
	}
	
	public static void main(String[] args) {
		new SaveMessageService().saveAllFailedMessagesInDirectory();
	}
	
}
