component {

	remote numeric function add(numeric x, numeric y) {
		return x+y;
	}

	remote string function assertHTTPRequestMethod(string expectedMethod="") {
		var msg = "Expected #expectedMethod#, cgi.request_method=#cgi.request_method#";
		if (cgi.request_method != arguments.expectedMethod) {
			cfheader(statuscode="405", statustext=msg);
		}
		return msg;
	}

}