component {

	this.name="cfmlServerlessTest";
	this.sessionManagement=false;
	this.clientManagement=false;
	this.setClientCookies=false;

	public function onRequest(string path) {
		
		writeOutput("AWS Request ID: #getLambdaContext().getAwsRequestId()# #cgi.request_method# Lucee #server.lucee.version#");
		
		logger("Testing Logger");
	}

	public function getLambdaContext() {
		//see https://docs.aws.amazon.com/lambda/latest/dg/java-context-object.html
		return getPageContext().getRequest().getAttribute("lambdaContext");
	}

	public void function logger(string msg) {
		getLambdaContext().getLogger().log(arguments.msg);
	}

}