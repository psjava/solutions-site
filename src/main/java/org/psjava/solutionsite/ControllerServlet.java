package org.psjava.solutionsite;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ControllerServlet extends HttpServlet {

	@Override
	public void doGet(final HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.getWriter().println("XX");
	}

}
