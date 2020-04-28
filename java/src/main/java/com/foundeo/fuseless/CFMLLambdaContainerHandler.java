package com.foundeo.fuseless;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.*;
import com.amazonaws.serverless.proxy.internal.servlet.AwsHttpServletResponse;
import com.amazonaws.serverless.proxy.internal.servlet.AwsLambdaServletContainerHandler;
import com.amazonaws.serverless.proxy.internal.servlet.AwsProxyHttpServletRequestReader;
import com.amazonaws.serverless.proxy.internal.servlet.AwsProxyHttpServletResponseWriter;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class CFMLLambdaContainerHandler<RequestType, ResponseType>
        extends AwsLambdaServletContainerHandler<RequestType, ResponseType, HttpServletRequest, AwsHttpServletResponse> {

    private static final Logger LOG = Logger.getLogger(CFMLLambdaContainerHandler.class);

    /**
     * Returns a new instance of an CFMLLambdaContainerHandler initialized to work with <code>AwsProxyRequest</code>
     * and <code>AwsProxyResponse</code> objects.
     *
     * @return a new instance of <code>CFMLLambdaContainerHandler</code>
     *
     * @throws ContainerInitializationException Throws this exception if we fail to initialize the Spark container.
     * This could be caused by the introspection used to insert the library as the default embedded container
     */
    public static CFMLLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> getAwsProxyHandler()
            throws ContainerInitializationException {
        CFMLLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> newHandler = new CFMLLambdaContainerHandler<>(AwsProxyRequest.class,
                                                                                         AwsProxyResponse.class,
                                                                                         new AwsProxyHttpServletRequestReader(),
                                                                                         new AwsProxyHttpServletResponseWriter(),
                                                                                         new AwsProxySecurityContextWriter(),
                                                                                         new AwsProxyExceptionHandler()
                                                                                         );
        newHandler.setLogFormatter(new ApacheCombinedServletLogFormatter<>());



        return newHandler;
    }


    public CFMLLambdaContainerHandler(Class<RequestType> requestTypeClass,
                                       Class<ResponseType> responseTypeClass,
                                       RequestReader<RequestType, HttpServletRequest> requestReader,
                                       ResponseWriter<AwsHttpServletResponse, ResponseType> responseWriter,
                                       SecurityContextWriter<RequestType> securityContextWriter,
                                       ExceptionHandler<ResponseType> exceptionHandler) {
        super(requestTypeClass, responseTypeClass, requestReader, responseWriter, securityContextWriter, exceptionHandler);

    }



    @Override
    protected AwsHttpServletResponse getContainerResponse(HttpServletRequest request, CountDownLatch latch) {
        return new AwsHttpServletResponse(request, latch);
    }


    @Override
    protected void handleRequest(HttpServletRequest httpServletRequest, AwsHttpServletResponse httpServletResponse, Context lambdaContext)
            throws Exception {
                
        //httpServletRequest.setServletContext(new ServletContextWrapper(getServletContext()));
        RequestWrapper req = new RequestWrapper((javax.servlet.http.HttpServletRequest)httpServletRequest);
        req.setAttribute("lambdaContext", lambdaContext);
        Object seg = null;
        try {
            if (StreamLambdaHandler.ENABLE_XRAY) {
                seg = AWSXRay.beginSubsegment("FuseLess " + req.getRequestURI());
                
                Map<String, Object> requestAttributes = new HashMap<String, Object>();
                requestAttributes.put("url", req.getRequestURL().toString());
                requestAttributes.put("method", req.getMethod());
                String header = req.getHeader("User-Agent");
                if (header != null) {
                    requestAttributes.put("user_agent", header);
                }
                header = req.getHeader("X-Forwarded-For");
                if (header != null) {
                    header = header.split(",")[0].trim();
                    requestAttributes.put("client_ip", header);
                    requestAttributes.put("x_forwarded_for", true);   
                } else {
                    if (req.getRemoteAddr() != null) {
                        requestAttributes.put("client_ip", req.getRemoteAddr());
                    }
                }
                ((Subsegment)seg).putHttp("request", requestAttributes);
                
            }
            LOG.debug("CFMLLambdaContainerHandler handleRequest: " + req.getRequestURI());
            StreamLambdaHandler.getCFMLServlet().service(req, httpServletResponse);
            
            
        } catch (Throwable t) {
            t.printStackTrace();

            LOG.error("CFMLLambdaContainerHandler Servlet Request Threw Exception: ");
            LOG.error(t);
            for (StackTraceElement st: t.getStackTrace()) {
                LOG.error("STE:" + st.toString());               
            }
            if (seg != null) {  
                ((Subsegment)seg).addException(t);
            }
        } finally {
            if (StreamLambdaHandler.ENABLE_XRAY) {
                AWSXRay.endSubsegment();
            }
        }
    }

    @Override
    public void initialize()
            throws ContainerInitializationException {
        
    }
}