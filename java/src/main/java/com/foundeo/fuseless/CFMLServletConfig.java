package com.foundeo.fuseless;


import javax.servlet.*;
import java.util.Enumeration;

public class CFMLServletConfig implements javax.servlet.ServletConfig {
		private ServletContext sc;

		public CFMLServletConfig(ServletContext servletContext) {
			this.sc = servletContext;
		}

        @Override
        public String getServletName() {
            return "CFMLServlet";
        }

        @Override
        public ServletContext getServletContext() {
            return sc;
        }

        @Override
        public String getInitParameter(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return new Enumeration<String>() {
                @Override
                public boolean hasMoreElements() {
                    return false;
                }

                @Override
                public String nextElement() {
                    return null;
                }
            };
        }

}