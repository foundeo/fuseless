package com.foundeo.fuseless;

import com.amazonaws.serverless.proxy.AwsProxyExceptionHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.Headers;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

/**
 * The Lambda runtime interrupts the request-handling thread's CountDownLatch.await() as a
 * transient signal (not a container shutdown - the next request on the same container succeeds
 * normally). AwsProxyExceptionHandler's default handling of this returns a 502 with
 * Content-Type: application/json, which browsers render as a raw JSON blob and which HTML-expecting
 * clients (e.g. Cypress cy.visit()) reject outright. Override just this case to return HTML instead.
 */
public class FuseLessExceptionHandler extends AwsProxyExceptionHandler {

    private static final Headers HTML_HEADERS = new Headers();

    static {
        HTML_HEADERS.putSingle(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML);
    }

    @Override
    public AwsProxyResponse handle(Throwable ex) {
        if (ex instanceof InterruptedException) {
            Thread.currentThread().interrupt(); // restore interrupt status
            StreamLambdaHandler.log("Request interrupted by Lambda runtime", ex);
            return new AwsProxyResponse(503, HTML_HEADERS,
                "<html><body><p>Service temporarily unavailable. Please refresh the page.</p></body></html>");
        }
        return super.handle(ex);
    }
}
