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
import org.springframework.context.ApplicationContext;

import com.hlb.dblogging.app.context.ApplicationContextProvider;
import com.hlb.dblogging.jpa.model.AuditDetail;
import com.hlb.dblogging.jpa.model.AuditMaster;
import com.hlb.dblogging.jpa.service.AuditDetailService;
import com.hlb.dblogging.jpa.service.AuditMasterService;
import com.hlb.dblogging.jpa.service.QueryXML;
import com.hlb.dblogging.log.utility.ApplLogger;
import com.hlb.dblogging.xml.utility.XSLTransformer;

public class XSLTransformerService {

/*	static {
		try{
			System.out.println("Started static block... @@@@@@@@@@@@@@@@@@@@@@@@@");
			TransformerFactory factory = TransformerFactory.newInstance();
			//Source xslt = new StreamSource(new File("sample-content.xsl"));
			InputStream input = XSLTransformerService.class.getResourceAsStream("/xml-transformer.xsl");
			Source xslt = new StreamSource(input);
			Transformer transformer = factory.newTransformer(xslt);
			
			InputStream input2 = XSLTransformerService.class.getResourceAsStream("/sample.xml");
			Source text = new StreamSource(input2);
			//Source text = new StreamSource(new File("sample.xml"));
			transformer.transform(text, new StreamResult(new File("xslt-output.xml")));
			System.out.println("Input and Input2 as follows : "+input+input2);
			System.out.println("Mission Done");
			
			new XSLTransformerService().getPath();
			//String test= IOUtils.toString(new ClassPathResource("sample.xml").getInputStream());
			//System.out.println("File path : "+test);
		}catch(Exception e){
			System.out.println("************************************ Exception stack trace here................");
			e.printStackTrace();
		}
	}
*/
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
	
	@SuppressWarnings("rawtypes")
	public void processXMLMessage(Message message) throws ParseException{
		byte data[] = null;
		ByteArrayOutputStream saveOnErrorXMLMessage= new ByteArrayOutputStream();
		try {
			BytesMessage bm = (BytesMessage) message;
			data = new byte[(int) bm.getBodyLength()];
			bm.readBytes(data);
			InputStream input = new ByteArrayInputStream(data);
			ByteArrayOutputStream outputXML = new ByteArrayOutputStream();
			TransformerFactory factory = TransformerFactory.newInstance();
			// Source xslt = new StreamSource(new File("sample-content.xsl"));
			ApplLogger.getLogger().debug("XML Data from Queue Is : "+ new String(data));
			Source xmlInput = new StreamSource(input);
			Source xslt = new StreamSource(new ByteArrayInputStream(XSLTransformer.xslTranformerStream.getBytes(StandardCharsets.UTF_8)));
			//Source xslt = new StreamSource(XSLTransformerService.class.getResourceAsStream("/xml-transformer.xsl"));
			Transformer transformer = factory.newTransformer(xslt);

			transformer.transform(xmlInput,	new StreamResult(outputXML));
			ApplLogger.getLogger().info("Output XML Data is : " + outputXML);
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

			try{
				ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
				AuditMasterService auditMasterService = (AuditMasterService) applicationContext.getBean("auditMasterService"); 
				if(auditMasterService!=null)
					auditMasterService.create(auditMasterBean);
				else
					ApplLogger.getLogger().info("AuditMasterService bean is not created by spring container..");

				AuditDetailService auditDetailService = (AuditDetailService) applicationContext.getBean("auditDetailService"); 
				if(auditDetailService!=null)
					auditDetailService.create(auditDetailBean);
				else
					ApplLogger.getLogger().info("AuditDetailService bean is not created by spring container..");
			}catch(Exception e){
				ApplLogger.getLogger().error("Exception caught while saving to DB and saving the message to FileSystem.. :",e);
				FileUtils.writeByteArrayToFile(new File("C:\\Recover\\Save\\Message_"+getFormattedTimestamp()), saveOnErrorXMLMessage.toByteArray());
			}
            
			}
		} catch (Exception e) {
			ApplLogger.getLogger().error("Exception caught while parsing the xml from Queue and can't parse :",e);
			try {
				FileUtils.writeByteArrayToFile(new File("C:\\Recover\\ProcessAndSave\\Message_"+getFormattedTimestamp()), data);
			} catch (IOException e1) {
				ApplLogger.getLogger().error("Exception caught while saving xml message to FileSystem :",e1);
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
	
	
}
