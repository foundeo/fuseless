package com.foundeo.fuseless;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.testutils.Timer;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;


import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.EnumSet;

import org.apache.log4j.Logger;

import java.io.File;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngine;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;


import lucee.loader.servlet.CFMLServlet;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static final Logger LOG = Logger.getLogger(StreamLambdaHandler.class);

    private static CFMLLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    private static HttpServlet cfmlServlet = null;

    static {
        try {

            LOG.info("StreamLambdaHandler initializing");
            handler = CFMLLambdaContainerHandler.getAwsProxyHandler();
            
            // we use the onStartup method of the handler to register our custom filter
            handler.onStartup(servletContext -> {
                long startTime = System.currentTimeMillis();
                LOG.info("StreamLambdaHandler onStartup");
                PrintStream systemOUT = System.out;
                try {
                    //redirecting System.out to a file because lucee logs a bunch of stuff with it
                    //this is causing sam local to have issues since it uses System.out to look for the 
                    //response
                    System.setOut(new PrintStream(new OutputStream() {
                        public void write(int b) {
                        //DO NOTHING
                        }
                    }));
                } catch (Exception e) {
                    LOG.error("Error redirecting system.out", e);
                }

                System.setProperty("lucee.web.dir", "/tmp/lucee/web/");
                System.setProperty("lucee.extensions.install", "false");
                System.setProperty("lucee.base.dir", "/tmp/lucee/server/");
                
                //felix configuration props: http://felix.apache.org/documentation/subprojects/apache-felix-framework/apache-felix-framework-configuration-properties.html
                String felix_cache_locking = System.getenv("FELIX_CACHE_LOCKING");
                if (felix_cache_locking != null) {
                    System.setProperty("felix.cache.locking", felix_cache_locking);
                }
                String felix_log_level = System.getenv("FELIX_LOG_LEVEL");
                if (felix_log_level != null) {
                    System.setProperty("felix.log.level", felix_log_level);
                }
                String felix_cache_bufsize = System.getenv("FELIX_CACHE_BUFSIZE");
                if (felix_cache_bufsize != null) {
                    System.setProperty("felix.cache.bufsize", felix_cache_bufsize);
                }
                try {

                    new File("/tmp/lucee/web/").mkdirs();
                    StreamLambdaHandler.cfmlServlet = new CFMLServlet();
                    ServletConfig servletConfig = new CFMLServletConfig(new ServletContextWrapper(servletContext));
                    cfmlServlet.init(servletConfig);
                    //CFMLEngine cfmlEngine = CFMLEngineFactory.getInstance(servletConfig);
                } catch (Throwable t) {
                    LOG.error("StreamLambdaHandler onStartup exception", t);
                }
                long startupComplete = System.currentTimeMillis();
                try {
                    //put System.out back to default PrintStream
                    System.setOut(systemOUT);
                } catch (Exception e) {
                    LOG.error("Error putting back system.out", e);
                }
                LOG.info("StreamLambdaHandler onStartup complete: " + (startupComplete-startTime));
            });
        } catch (ContainerInitializationException e) {
            // if we fail here. We re-throw the exception to force another cold start
            e.printStackTrace();
            throw new RuntimeException("StreamLambdaHandler Could not initialize the container", e);
        }
    }

    public static final HttpServlet getCFMLServlet() {
        return StreamLambdaHandler.cfmlServlet;
    }

    public StreamLambdaHandler() {
        
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        
        try {
            Thread.sleep(50000);    
        } catch(Exception e) {
            //no
        }
        
        handler.proxyStream(inputStream, outputStream, context);
        outputStream.close();
    }
}
