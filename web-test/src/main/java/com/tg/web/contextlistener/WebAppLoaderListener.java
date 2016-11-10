package com.tg.web.contextlistener;

import com.tg.tiny4j.core.ioc.beans.BeanDefinition;
import com.tg.tiny4j.core.ioc.context.ApplicationContext;
import com.tg.tiny4j.core.web.integration.AbstractWebContextListener;
import com.tg.tiny4j.core.web.integration.HandleAnnotation;
import com.tg.tiny4j.core.web.integration.HandleRegistry;
import com.tg.tiny4j.core.web.integration.WebApplicationContext;
import com.tg.tiny4j.web.integration.WebAppControllerReader;
import com.tg.tiny4j.web.metadata.BaseInfo;
import com.tg.tiny4j.web.metadata.ControllerInfo;
import com.tg.tiny4j.web.metadata.InterceptorInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;

/**
 * Created by twogoods on 16/11/2.
 */
public class WebAppLoaderListener extends AbstractWebContextListener {
    private static Logger log = LogManager.getLogger(WebAppLoaderListener.class);

    final WebAppControllerReader webAppControllerReader = new WebAppControllerReader();

    @Override
    public void registerHandle(HandleRegistry registry) {
        HandleAnnotation handle = new HandleAnnotation() {
            @Override
            public BeanDefinition handle(Class clazz) throws Exception {
                BaseInfo baseInfo = webAppControllerReader.read(clazz);
                return new BeanDefinition(baseInfo.getName(), baseInfo.getClassName());
            }
        };
        registry.addHandle(handle);
    }

    @Override
    public void requestMapInitialized(ServletContextEvent servletContextEvent, ApplicationContext applicationContext) throws Exception {
        webAppControllerReader.initRequestMap();
        Map<String, ControllerInfo> controllerInfoMap = webAppControllerReader.getRequestMapper().getApis();
        for (String key : controllerInfoMap.keySet()) {
            ControllerInfo controller = controllerInfoMap.get(key);
            controller.setObject(applicationContext.getBean(controller.getName()));
        }
        for (InterceptorInfo interceptor : webAppControllerReader.getRequestMapper().getInterceptorList()) {
            interceptor.setObj(applicationContext.getBean(interceptor.getName()));
        }
        servletContextEvent.getServletContext().setAttribute("webrequestmapper", webAppControllerReader.getRequestMapper());
    }
}