package com;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * Created by guojain on 8/8/16.
 */
public class SpringApplicationcontextUtil {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * 在spring context之外获得spring bean对象
     *
     * @param name
     * @return Object
     * @throws BeansException
     */
    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);
    }
}
