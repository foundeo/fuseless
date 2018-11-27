package com.foundeo.fuseless;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;


import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.EventListener;
import java.util.Map;

import org.apache.log4j.Logger;

public class ServletContextWrapper implements ServletContext {

    private ServletContext servletContext;

    private static final Logger LOG = Logger.getLogger(ServletContextWrapper.class);
    
    public ServletContextWrapper(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public ServletContext getContext(String uripath) {
        return this.servletContext.getContext(uripath);
    }

    @Override
    public String getContextPath() {
        return this.servletContext.getContextPath();
    }


    @Override
    public int getMajorVersion() {
        return this.servletContext.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return this.servletContext.getMinorVersion();
    }

    @Override
    public int getEffectiveMinorVersion() {
        return this.servletContext.getEffectiveMinorVersion();
    }

    @Override
    public int getEffectiveMajorVersion() {
        return this.servletContext.getEffectiveMajorVersion();
    }



    @Override
    public String getMimeType(String file) {
        return this.servletContext.getMimeType(file);
    }

    @Override
    public Set getResourcePaths(String paths) {
        return this.servletContext.getResourcePaths(paths);
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        if (path!=null) {
            throw new MalformedURLException("getResource: " + path);
        } 
        return ServletContextWrapper.class.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return ServletContextWrapper.class.getResourceAsStream(path);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return this.servletContext.getRequestDispatcher(path);
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return this.servletContext.getNamedDispatcher(name);
    }

    @Override
    @Deprecated
    public Servlet getServlet(String name) throws ServletException {
        return this.servletContext.getServlet(name);
    }

    @Override
    @Deprecated
    public Enumeration getServlets() {
        return this.servletContext.getServlets();
    }

    @Override
    @Deprecated
    public Enumeration getServletNames() {
        return this.servletContext.getServletNames();
    }

    @Override
    public void log(String msg) {
        this.servletContext.log(msg);
    }

    @Override
    @Deprecated
    public void log(Exception exception, String msg) {
        this.servletContext.log(exception, msg);
    }

    @Override
    public void log(String msg, Throwable throwable) {
        this.servletContext.log(msg, throwable);
    }

    @Override
    public String getRealPath(String path) {
        if (path.equals("/")) {
            return "/var/task/app";
        } else if (path.equals("/WEB-INF")) {
            return "/tmp/lucee/web/WEB-INF";
        }
        return this.servletContext.getRealPath(path);
    }

    @Override
    public String getServerInfo() {
        return this.servletContext.getServerInfo() + "; Foundeo FuseLess v0.0.4";
    }

    @Override
    public String getInitParameter(String path) {
        return this.servletContext.getInitParameter(path);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return this.servletContext.getInitParameterNames();
    }

    @Override
    public Object getAttribute(String name) {
        return this.servletContext.getAttribute(name);
    }

    @Override
    public Enumeration getAttributeNames() {
        return this.servletContext.getAttributeNames();
    }

    @Override
    public void setAttribute(String name, Object value) {
        this.servletContext.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        this.servletContext.removeAttribute(name);
    }

    @Override
    public String getServletContextName() {
        return this.servletContext.getServletContextName();
    }

    @Override
    public boolean setInitParameter(String s, String s1) {
        return this.servletContext.setInitParameter(s, s1);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String s, String s1) {
        return this.servletContext.addServlet(s, s1);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String s, Servlet servlet) {
        return this.servletContext.addServlet(s, servlet); 
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> aClass) {
        return this.servletContext.addServlet(s, aClass);
    }


    @Override
    public <T extends Servlet> T createServlet(Class<T> aClass) throws ServletException {
        return this.servletContext.createServlet(aClass);
    }


    @Override
    public ServletRegistration getServletRegistration(String s) {
        return this.servletContext.getServletRegistration(s);
    }


    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return this.servletContext.getServletRegistrations();
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String name, String filterClass) {
        return this.servletContext.addFilter(name, filterClass);
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String name, Filter filter) {
        return this.servletContext.addFilter(name, filter);
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String name, Class<? extends Filter> filterClass) {
        return this.servletContext.addFilter(name, filterClass);
    }


    @Override
    public <T extends Filter> T createFilter(Class<T> aClass) throws ServletException {
        return this.servletContext.createFilter(aClass);
    }


    @Override
    public FilterRegistration getFilterRegistration(String s) {
        return this.servletContext.getFilterRegistration(s);
    }


    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return this.servletContext.getFilterRegistrations();
    }



    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return this.servletContext.getSessionCookieConfig();
    }


    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> set) {
        this.servletContext.setSessionTrackingModes(set);
    }


    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return this.servletContext.getDefaultSessionTrackingModes();
    }


    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return this.servletContext.getEffectiveSessionTrackingModes();
    }


    @Override
    public void addListener(String s) {
        this.servletContext.addListener(s);
    }


    @Override
    public <T extends EventListener> void addListener(T t) {
        //this.servletContext.addListener(t);
    }


    @Override
    public void addListener(Class<? extends EventListener> aClass) {
        //this.servletContext.addListener(aClass);
    }


    @Override
    public <T extends EventListener> T createListener(Class<T> aClass) throws ServletException {
        return this.servletContext.createListener(aClass);
    }


    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return this.servletContext.getJspConfigDescriptor();
    }


    @Override
    public ClassLoader getClassLoader() {
        return this.servletContext.getClassLoader();
    }


    @Override
    public void declareRoles(String... strings) {
        this.servletContext.declareRoles(strings);
    }


    @Override
    public String getVirtualServerName() {
        return this.servletContext.getVirtualServerName();
    }


}
