component {

	this.name="cfmlServerlessTest";
	this.sessionManagement=false;
	this.clientManagement=false;
	this.setClientCookies=false;

	public function onRequest(string path="") {
		if (arguments.path == "") {
			path="/test.cfm";
		}
		logger("REQ: #arguments.path#");
		include path;	
	}

	public string function fuselessEvent(event, fuselessContext) {
		if (!isJSON(event)) {
			return "Event Payload was not JSON";
		}
		local.eventObject = deserializeJSON(event);
		return "Event Name: #local.eventObject.Records[1].eventName#, AWS Request ID: #getLambdaContext().getAwsRequestId()#";
	}

	public function getLambdaContext() {
		//see https://docs.aws.amazon.com/lambda/latest/dg/java-context-object.html
		return getPageContext().getRequest().getAttribute("lambdaContext");
	}

	public void function logger(string msg) {
		getLambdaContext().getLogger().log(arguments.msg);
	}

	public function onError(err) {
		writeOutput("ERROR: #err.message# - #err.detail# #err.stacktrace#");
		logger(err.message & " - " & err.detail);	
	}

}