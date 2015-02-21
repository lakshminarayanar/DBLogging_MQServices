package com.hlb.dblogging.app.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    public void setApplicationContext(ApplicationContext arg0)
            throws BeansException {
        applicationContext = arg0;

    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
