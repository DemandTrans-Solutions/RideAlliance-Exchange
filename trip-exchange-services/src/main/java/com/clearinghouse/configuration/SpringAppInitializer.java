/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.configuration;


import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 *
 * @author manisha
 */
public class SpringAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{SpringAppConfiguration.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    /*
    @Override
    protected Filter[] getServletFilters() {
        Filter[] singleton = {new OldCorsFilterDeprecated()};
        return singleton;
    }
*/
}
