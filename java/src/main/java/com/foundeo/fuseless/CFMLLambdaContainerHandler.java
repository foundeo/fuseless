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


import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class CFMLLambdaContainerHandler<RequestType, ResponseType>
        extends AwsLambdaServletContainerHandler<RequestType, ResponseType, HttpServletRequest, AwsHttpServletResponse> {

    
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
            //LOG.debug("CFMLLambdaContainerHandler handleRequest: " + req.getRequestURI());
            StreamLambdaHandler.getCFMLServlet().service(req, httpServletResponse);
            
            
        } catch (Throwable t) {
            t.printStackTrace();

            StreamLambdaHandler.log("CFMLLambdaContainerHandler Servlet Request Threw Exception: ", t);

            if (seg != null) {
                ((Subsegment)seg).addException(t);
            }

            // Send a 500 so the client learns the request failed.
            //
            // Without this, the finally block below commits the response with no
            // status ever set. AwsHttpServletResponse.getStatus() returns
            // `statusCode <= 0 ? SC_OK : statusCode`, so a servlet that threw
            // before setting a status is reported to the client as HTTP 200 with
            // an empty body — a failure that looks like a success to the caller
            // and to any monitoring keyed on status codes.
            //
            // sendError() routes through flushBuffer(), which is the only caller
            // of countDown() on the latch, so this also releases the waiting
            // thread on its own.
            try {
                if (!httpServletResponse.isCommitted()) {
                    httpServletResponse.sendError(500);
                }
            } catch (Throwable responseFailure) {
                StreamLambdaHandler.log("Failed to send error response: ", responseFailure);
            }

            // Do not absorb JVM-level errors. An OutOfMemoryError or
            // StackOverflowError swallowed here leaves the container in an
            // unknown state while reporting a normal response; letting it
            // propagate lets the Lambda runtime fail the invocation loudly and
            // replace the execution environment.
            if (t instanceof Error) {
                throw (Error) t;
            }

        } finally {
            if (StreamLambdaHandler.ENABLE_XRAY) {
                AWSXRay.endSubsegment();
            }

            // Guarantee the response is committed before we return.
            //
            // AwsHttpServletResponse.flushBuffer() is the ONLY caller of
            // countDown() on the latch that LambdaContainerHandler.proxy() is
            // blocked on. If the servlet returns without committing, nothing
            // releases that latch and the invocation hangs until the Lambda
            // runtime interrupts it — surfacing as an InterruptedException at
            // CountDownLatch.await() and a 5xx to the client.
            //
            // aws-serverless-java-container guards against this itself, at the
            // end of AwsLambdaServletContainerHandler.doFilter(). FuseLess calls
            // service() directly rather than going through the filter chain — a
            // deliberate choice, since a CFML container has no servlet filters to
            // apply — so that guard is never reached and this replicates it.
            //
            // Also covers the case where sendError() above failed, and the case
            // where the servlet returned normally without committing.
            try {
                if (!httpServletResponse.isCommitted()) {
                    StreamLambdaHandler.log("Response not committed after service(); forcing flush. path=" + req.getRequestURI());
                    httpServletResponse.flushBuffer();
                }
            } catch (Throwable flushError) {
                // Never let the guard itself break the request. If the flush
                // fails, the latch stays uncounted and the original hang
                // returns — no worse than not having tried.
                StreamLambdaHandler.log("Failed to force flush of uncommitted response: ", flushError);
            }
        }
    }

    @Override
    public void initialize()
            throws ContainerInitializationException {
        
    }
}