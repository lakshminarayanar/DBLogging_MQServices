package com.hlb.dblogging.mdb.listener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import com.hlb.dblogging.log.utility.ApplLogger;
import com.hlb.dblogging.mdb.service.XSLTransformerService;
   
  /**  
  * Class handles incoming messages  
  *  
  * @see PointOfIssueMessageEvent  
  */  
  public class MessageListenerBean implements MessageListener  
  {  
 
      private static final Logger logger_c = Logger.getLogger(MessageListenerBean.class);  
  
      /**  
      * Method implements JMS onMessage and acts as the entry  
       * point for messages consumed by Springs DefaultMessageListenerContainer.  
       * When DefaultMessageListenerContainer picks a message from the queue it  
      * invokes this method with the message payload.  
      */  
      public void onMessage(Message message)  
      {  
           logger_c.debug("Received message from queue [" + message +"]");  
   
           /* The message must be of type TextMessage */  
           if (message instanceof TextMessage)  
          {  
                try  
                {  
                     String msgText = ((TextMessage) message).getText();  
                    //logger_c.debug("About to process message: " + msgText);  
   
                    logger_c.debug("Data is received : "+msgText);
 
              }  
               catch (JMSException jmsEx_p)  
                {  
                    String errMsg = "An error occurred extracting message";  
                   logger_c.error(errMsg, jmsEx_p);  
               }  
         }  
         else if(message instanceof BytesMessage)
           {  
        	 try {
				new XSLTransformerService().processXMLMessage(message);
			} catch (ParseException e) {
				e.printStackTrace();
			};
           }
       }
      
  } 