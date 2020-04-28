<cfsilent>
	<cfparam name="url.requestMethod" default="">
	<cfparam name="url.requestContentType" default="">
	<cfparam name="url.requestBody" default="">
	<cfparam name="url.customResponseHeader" default="">
	<cfparam name="url.dump" default="false" type="boolean">
	<cfheader name="CustomResponseHeader" value="#url.customResponseHeader#">
	<cfheader statuscode="222" statustext="Test Pass">
	<cfset reqData = getHTTPRequestData()>
	<cfset contentType = "">
	<cfif structKeyExists(reqData.headers, "Content-Type")>
		<cfset contentType = reqData.headers["Content-Type"]>
	</cfif>
</cfsilent>
<cfoutput>
<cfset contentType = listFirst(contentType,';') />
<pre>
	#assert("cgi.request_method", url.requestMethod, cgi.request_method)#
	#assert("requestBody", url.requestBody, reqData.content)#
	#assert("Content-Type", url.requestContentType, contentType)#
</pre>
</cfoutput>

<cfif url.dump>
	<cfdump var="#cgi#">
	<cfdump var="#reqData#">
	<cfdump var="#form#">
	<cfdump var="#url#">
</cfif>

<cfscript>
	function assert(name, expected, actual) {
		var msg = "#name# Expected: #expected#, Actual: #actual#";
		if (expected != actual) {
			cfheader(statuscode="520", statustext="Test Failed");
			msg = "[FAIL] " & msg;
		} else {
			msg = "[PASS] " & msg;
		}
		return msg;
	}
</cfscript>