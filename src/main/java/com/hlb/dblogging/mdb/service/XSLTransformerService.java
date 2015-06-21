package com.hlb.dblogging.mdb.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.hlb.dblogging.app.context.ApplicationContextProvider;
import com.hlb.dblogging.jpa.model.AuditDetail;
import com.hlb.dblogging.jpa.model.AuditMaster;
import com.hlb.dblogging.jpa.service.AuditDetailService;
import com.hlb.dblogging.jpa.service.AuditMasterService;
import com.hlb.dblogging.jpa.service.QueryXML;
import com.hlb.dblogging.log.utility.ApplLogger;
import com.hlb.dblogging.log.utility.LogLevel;
import com.hlb.dblogging.xml.utility.XSLTransformer;

@Service
public class XSLTransformerService {
	
	

	public void getPath() throws IOException{
		InputStream in = getClass().getResourceAsStream("/sample.xml");
		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		System.out.println("Input : "+input.ready());
		System.out.println("Class Loader is : "+this.getClass().getClassLoader());
		System.out.println("Class Loader for XML with / is : "+this.getClass().getClassLoader().getResourceAsStream("/sample.xml"));
		System.out.println("Class Loader for XML without / is : "+this.getClass().getClassLoader().getResourceAsStream("sample.xml"));
		System.out.println("Class for XML with / is : "+this.getClass().getResourceAsStream("/sample.xml"));
		System.out.println("Class for XML without is : "+this.getClass().getResourceAsStream("sample.xml"));
		
	}
	
	public void processXMLMessage(Message message) throws ParseException{
		byte data[] = null;
		String inputStreamString = null;
		ByteArrayOutputStream outputXML = null;
		ByteArrayOutputStream saveOnErrorXMLMessage= new ByteArrayOutputStream();
		try {
			BytesMessage bm = (BytesMessage) message;
			data = new byte[(int) bm.getBodyLength()];
			bm.readBytes(data);
			//InputStream input = new ByteArrayInputStream(data);
			outputXML = new ByteArrayOutputStream();
			
			// Storing input stream in string for multiple times using the same stream until get stored in database
			inputStreamString = new String(data);
			QueryXML xmlQueryService =   new QueryXML();
			ApplLogger.getLogger().debug("XML Data from Queue Is : "+ inputStreamString);
			
			// check whether LogLevel with database value matches value from Message, for saving message to database.
			String logLevel = xmlQueryService.getLogLevelMessage(new ByteArrayInputStream(inputStreamString.getBytes()));
			
			if(saveMessageIfLogLevelMatches(logLevel)){
				ApplLogger.getLogger().info("Message is going to be processed as loglevel matches with the configured one.."+XSLTransformer.logLevel);
				// checking whether MessageFormat XML or not to process with XSLT
				if(xmlQueryService.checkWhetherContentIsXml(new ByteArrayInputStream(inputStreamString.getBytes())))
				{
					// Source xslt = new StreamSource(new File("sample-content.xsl"));
					TransformerFactory factory = TransformerFactory.newInstance();
					Source xmlInput = new StreamSource(new ByteArrayInputStream(inputStreamString.getBytes()));
					Source xslt = new StreamSource(new ByteArrayInputStream(XSLTransformer.xslTranformerStream.getBytes(StandardCharsets.UTF_8)));
					
					//Source xslt = new StreamSource(XSLTransformerService.class.getResourceAsStream("/xml-transformer.xsl"));
					Transformer transformer = factory.newTransformer(xslt);
					transformer.transform(xmlInput,	new StreamResult(outputXML));
				}else{
					ApplLogger.getLogger().info("MessageFormat is not XML.. So not transforming using XSLT...");
					IOUtils.copy(new ByteArrayInputStream(inputStreamString.getBytes()), outputXML);
				}
				ApplLogger.getLogger().info("Output XML Data is : " + outputXML);
				// Parse XML and Map to POJO and then save them to Database..
				processXmlAndSave(saveOnErrorXMLMessage, outputXML);
				}
			else{
				ApplLogger.getLogger().info("LogLevel of Message does not match with the configured LogLevel : "+XSLTransformer.logLevel);
				ApplLogger.getLogger().warn("Message is not going to get saved to db : "+inputStreamString);
			}
		} catch (Exception e) {
			ApplLogger.getLogger().warn("Message is not going to get saved to db : "+inputStreamString);
			ApplLogger.getLogger().error("Exception caught while parsing the xml from Queue and can't parse :",e);
		}
		finally{
			// Nullify all properties.
			data=null;
			inputStreamString=null;
		}
		
	}
	
	
	@SuppressWarnings("rawtypes")
	public void processXmlAndSave(ByteArrayOutputStream saveOnErrorXMLMessage, ByteArrayOutputStream outputXML ) throws IOException{
		saveOnErrorXMLMessage = outputXML;
		ApplLogger.getLogger().info("XML formatting and pre processing is done.. Starting parse xml and save..");
		List beanList = new QueryXML().mappingXMLToPojo(new ByteArrayInputStream(outputXML.toByteArray()));
		if(beanList!=null && beanList.size()==2){
			AuditMaster auditMasterBean = null;
			AuditDetail auditDetailBean = null;
			if(beanList.get(0) instanceof AuditMaster){
				auditMasterBean = (AuditMaster) beanList.get(0);
				auditDetailBean = (AuditDetail) beanList.get(1);
			}else{
				auditMasterBean = (AuditMaster) beanList.get(1);
				auditDetailBean = (AuditDetail) beanList.get(0);
			}
			ApplLogger.getLogger().info("AuditMaster Data is : "+auditMasterBean);
			ApplLogger.getLogger().info("AuditDetail Data is : "+auditDetailBean);
			long auditMasterSavedId ;
			try{
				ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
				AuditMasterService auditMasterService = (AuditMasterService) applicationContext.getBean("auditMasterService"); 
				if(auditMasterService!=null)
					auditMasterSavedId = auditMasterService.create(auditMasterBean);
				else{
					ApplLogger.getLogger().info("AuditMasterService bean is not created by spring container..");
					throw new RuntimeException("AuditMasterService bean is not created by spring container..");
				}

				AuditDetailService auditDetailService = (AuditDetailService) applicationContext.getBean("auditDetailService"); 
				if(auditDetailService!=null && auditMasterSavedId >0){
					auditDetailBean.setAuditMasterId(auditMasterSavedId);
					auditDetailService.create(auditDetailBean);
				}
				else{
					ApplLogger.getLogger().info("AuditDetailService bean is not created by spring container..");
					throw new RuntimeException("AuditDetailService bean is not created by spring container or AuditMaster data is not saved");
				}
				
				ApplLogger.getLogger().info("Queue message is processed successfully with the message unique id : "+auditMasterBean.getUniqueProcessID());
			}catch(Exception e){
				ApplLogger.getLogger().error("Exception caught while saving to DB and saving the message to FileSystem.. :",e);
				FileUtils.writeByteArrayToFile(new File(XSLTransformer.retryPath+"Message_"+getFormattedTimestamp()), saveOnErrorXMLMessage.toByteArray());
			}
			finally{
				saveOnErrorXMLMessage.close();
				outputXML.close();
			}
		}
	}
	
	
	
	
	
	
	
	private String getFormattedTimestamp(){
		// Create an instance of SimpleDateFormat used for formatting 
		// the string representation of date (month/day/year)
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_SSS");

		// Get the date today using Calendar object.
		Date today = Calendar.getInstance().getTime();        
		// Using DateFormat format method we can create a string 
		// representation of a date with the defined format.
		String reportDate = df.format(today);
		return reportDate;
	}
	
	private boolean saveMessageIfLogLevelMatches(String logLevel){
		if(logLevel==null || logLevel.isEmpty())
			return false;
		else{
			if(XSLTransformer.logLevel.equalsIgnoreCase(LogLevel.DEBUG.toString()) || XSLTransformer.logLevel.equalsIgnoreCase(LogLevel.ALL.toString()))
				return true;
			else if(XSLTransformer.logLevel.equalsIgnoreCase(LogLevel.INFO.toString()) && ( LogLevel.INFO.toString().equalsIgnoreCase(logLevel) || LogLevel.WARN.toString().equalsIgnoreCase(logLevel) || LogLevel.ERROR.toString().equalsIgnoreCase(logLevel) || LogLevel.FATAL.toString().equalsIgnoreCase(logLevel)))
				return true;
			else if(XSLTransformer.logLevel.equalsIgnoreCase(LogLevel.WARN.toString()) && (LogLevel.WARN.toString().equalsIgnoreCase(logLevel) || LogLevel.ERROR.toString().equalsIgnoreCase(logLevel) || LogLevel.FATAL.toString().equalsIgnoreCase(logLevel)))
				return true;
			else if(XSLTransformer.logLevel.equalsIgnoreCase(LogLevel.ERROR.toString()) && (LogLevel.ERROR.toString().equalsIgnoreCase(logLevel) || LogLevel.FATAL.toString().equalsIgnoreCase(logLevel)))
				return true;
			else if(XSLTransformer.logLevel.equalsIgnoreCase(LogLevel.FATAL.toString()) && LogLevel.FATAL.toString().equalsIgnoreCase(logLevel))
				return true;
		}
		return false;
	}
	
	
}
