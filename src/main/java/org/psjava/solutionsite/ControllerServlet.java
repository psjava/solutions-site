package org.psjava.solutionsite;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ControllerServlet extends HttpServlet {

	@Override
	public void doGet(final HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if(path.equals("/"))
			forward(this, req, res, "index.jsp");
	}
	private static void forward(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, String jspFileName) throws ServletException, IOException {
		servlet.getServletContext().getRequestDispatcher(req.getContextPath() + "/WEB-INF/" + jspFileName).forward(req, res);
	}


}
