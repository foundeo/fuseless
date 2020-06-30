package com.foundeo.fuseless;

import java.io.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.Component;
import java.util.HashMap;

public class EventLambdaHandler implements RequestStreamHandler {
	
	private static String eventCFCPath = "/var/task/app/Application.cfc";
	private static String eventCFCMethod = "fuselessEvent";

	private static StreamLambdaHandler handler = null;

	static {
		if (System.getenv("FUSELESS_EVENT_CFC_PATH") != null) {
			eventCFCPath = System.getenv("FUSELESS_EVENT_CFC_PATH");
		}
		if (System.getenv("FUSELESS_EVENT_CFC_METHOD") != null) {
			eventCFCMethod = System.getenv("FUSELESS_EVENT_CFC_METHOD");
		}
		//init CFML Engine
        handler = new StreamLambdaHandler();

	}


	@Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        
        

        FuseLessContext ctx = new FuseLessContext(context);


        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        String inputString = result.toString("UTF-8");
        inputStream.close();
        
        ctx.setEventPayload(inputString);

        try {
            CFMLEngine engine = CFMLEngineFactory.getInstance(StreamLambdaHandler.getCFMLServlet().getServletConfig());
            HashMap attributes = new HashMap();
            attributes.put("lambdaContext", ctx);
            PageContext pc = engine.createPageContext(new java.io.File("/var/task/app/"), "localhost", "/index.cfm", "", null, null, null, attributes, null, Long.MAX_VALUE, true);
            
            
            Component cfc = engine.getCreationUtil().createComponentFromPath(pc, eventCFCPath);

            Object[] args = { inputString, ctx };
            Object eventResponse = cfc.call(pc, eventCFCMethod, args);
            outputStream.write( eventResponse.toString().getBytes());
        } catch (Throwable t) {
            throw new IOException(t);
        }
        

    }
	

}