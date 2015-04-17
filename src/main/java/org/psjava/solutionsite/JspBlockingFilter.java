package org.psjava.solutionsite;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JspBlockingFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) servletRequest;
		String path = req.getRequestURI().substring(req.getContextPath().length());
		if (path.endsWith(".jsp")) {
			filterChain.doFilter(servletRequest, servletResponse); // Goes to default servlet.
		} else {
			servletRequest.getRequestDispatcher("/servlet" + path).forward(servletRequest, servletResponse);
		}
	}

	@Override
	public void destroy() {
	}

}
