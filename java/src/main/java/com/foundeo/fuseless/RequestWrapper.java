package com.foundeo.fuseless;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;


public class RequestWrapper extends HttpServletRequestWrapper {

	public RequestWrapper(HttpServletRequest request) {
		super(request);
	}

	
	@Override
    public String getServletPath() {
    	String requestURI = getRequestURI();
    	if (requestURI.endsWith(".cfm") || requestURI.endsWith(".cfc")) {
    		return requestURI;
    	} else {
    		return super.getServletPath();
    	}
    }

    @Override
    public boolean isSecure() {
        String proto = getHeader("X-Forwarded-Proto");
        if (proto != null && proto.equals("https")) {
            return true;
        }
        return false;
    }

    @Override
    public String getServerName() {
        try {
            String serverName = super.getServerName();
            if (serverName != null) {
                return serverName;
            } else {
                return "null";
            }
        } catch (NullPointerException e) {
            return "null";
        }
    }

    

    
    @Override
    public String getHeader(String name) {
        try {
            return super.getHeader(name);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public String getProtocol() {
        try {
            return super.getProtocol();
        } catch (NullPointerException e) {
            return "LAMBDA/1.0";
        }
    }

}