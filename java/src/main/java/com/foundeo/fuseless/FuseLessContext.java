package com.foundeo.fuseless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class FuseLessContext implements Context {
	private Context lambdaContext = null;
	private String payload = null;

	public FuseLessContext(Context lambdaContext) {
		this.lambdaContext = lambdaContext;
	}

	public boolean isAwsLambdaContext() {
		return this.lambdaContext != null;
	}

	public boolean hasEventPayload() {
		return this.payload != null;
	}

	public void setEventPayload(String payload){
		this.payload = payload;
	}

	public String getEventPayload(){ 
		return this.payload;
	}

	public String getAwsRequestId() {
		return this.lambdaContext.getAwsRequestId();
	}

	public String getLogGroupName() {
		return this.lambdaContext.getLogGroupName();
	}

	public String getLogStreamName() {
		return this.lambdaContext.getLogStreamName();
	}

	public String getFunctionName() {
		return this.lambdaContext.getFunctionName();
	}

	public String getFunctionVersion() {
		return this.lambdaContext.getFunctionVersion();
	}

	public String getInvokedFunctionArn() {
		return this.lambdaContext.getInvokedFunctionArn();
	}

	public CognitoIdentity getIdentity() {
		return this.lambdaContext.getIdentity();
	}

	
	public ClientContext getClientContext() {
		return this.lambdaContext.getClientContext();
	}

	
	public int getRemainingTimeInMillis() {
		return this.lambdaContext.getRemainingTimeInMillis();
	}

	
	public int getMemoryLimitInMB() {
		return this.lambdaContext.getMemoryLimitInMB();
	}

	
	public LambdaLogger getLogger() {
		return this.lambdaContext.getLogger();
	}



}