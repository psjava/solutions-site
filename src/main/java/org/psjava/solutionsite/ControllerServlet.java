package org.psjava.solutionsite;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.json.JSONArray;
import org.psjava.util.DataKeeper;
import org.psjava.util.EventListener;
import org.psjava.util.ZeroTo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ControllerServlet extends HttpServlet {

	private static final String TARGET_REF = "v3";
	private static final String LISTING_URL = "https://api.github.com/repos/psjava/solutions/contents/src/main/java/org/psjava/solutions/code";

	@Override
	public void doGet(final HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if (path.equals("/")) {
			String body = CachedHttpClient.getBody(LISTING_URL + "?ref=" + TARGET_REF);
			JSONArray array = new JSONArray(body);
			List<String> dirNames = new ArrayList<>();
			for (int i : ZeroTo.get(array.length())) {
				String dirName = array.getJSONObject(i).getString("name");
				if (dirName.contains("_")) // TODO after migration finish, remove this condition
					dirNames.add(dirName);
			}

			List<SolutionInfo> list = new ArrayList<>();
			for (String dirName : dirNames) {
				String url = constructRawContentUrl(dirName);
				String content = CachedHttpClient.getBody(url);
				int indexOrN1 = dirName.indexOf('_');
				if (indexOrN1 == -1)
					continue;
				String siteCode = dirName.substring(0, indexOrN1);
				String problemId = dirName.substring(indexOrN1 + 1).replace('_', '-');
				String title;
				try {
					title = extractTitleInJavaDoc(parse(content));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				String urlDir = convertToUrlDir(title);
				String description = constructDescription(siteCode, problemId, title);
				list.add(new SolutionInfo(siteCode, problemId, description, urlDir));
			}
			req.setAttribute("solutionInfoList", list);
			forward(this, req, res, "index.jsp");
		} else {
			String[] token = path.split("/");
			String siteCode = token[1];
			String problemId = token[2];
			String titleInUrlDirFormat = token[3];

			String url = constructRawContentUrl(siteCode, problemId);
			String content = CachedHttpClient.getBody(url);
			CompilationUnit cu;
			try {
				cu = parse(content);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			String title = extractTitleInJavaDoc(cu);
			if (!convertToUrlDir(title).equals(titleInUrlDirFormat)) {
				throw new RuntimeException("unknown problem");
			}
			req.setAttribute("sourceCode", content);
			req.setAttribute("hints", extractHintsFromJavaDoc(cu));
			req.setAttribute("siteCode", siteCode);
			req.setAttribute("description", constructDescription(siteCode, problemId, title));
			forward(this, req, res, "solution.jsp");
		}
	}

	private static String constructRawContentUrl(final String siteCode, final String problemId) {
		return constructRawContentUrl(siteCode + "_" + problemId.replace('-', '_'));
	}

	private static String constructRawContentUrl(String problemDirName) {
		return "https://raw.github.com/psjava/solutions/" + TARGET_REF + "/src/main/java/org/psjava/solutions/code/" + problemDirName + "/Main.java";
	}

	private static CompilationUnit parse(String content) throws ParseException, UnsupportedEncodingException {
		return JavaParser.parse(new ByteArrayInputStream(content.getBytes("UTF-8")), "UTF-8");
	}

	private static String convertToUrlDir(String title) {
		String r = "";
		for (int i = 0; i < title.length(); i++) {
			char c = title.charAt(i);
			if (Character.isLowerCase(c) || Character.isUpperCase(c))
				r += Character.toLowerCase(c);
			else if (c == ' ')
				r += '-';
		}
		return r;
	}

	private static String extractTitleInJavaDoc(CompilationUnit cu) {
		final DataKeeper<String> r = DataKeeper.create("");
		visit(cu, new EventListener<String>() {
			@Override
			public void visit(String line) {
				String token = "@title";
				if (line.contains(token))
					r.set(line.substring(line.indexOf(token) + token.length()).trim());
			}
		});
		return r.get();
	}

	private static List<String> extractHintsFromJavaDoc(CompilationUnit cu) {
		final ArrayList<String> r = new ArrayList<>();
		visit(cu, new EventListener<String>() {
			@Override
			public void visit(String line) {
				String token = "@hint";
				if (line.contains(token))
					r.add(line.substring(line.indexOf(token) + token.length()).trim());
			}
		});
		return r;
	}

	private static void visit(CompilationUnit cu, final EventListener<String> lineVisitor) {
		new VoidVisitorAdapter<Object>() {
			@Override
			public void visit(ClassOrInterfaceDeclaration n, Object arg) {
				Scanner scan = new Scanner(n.getJavaDoc().getContent());
				while (scan.hasNextLine())
					lineVisitor.visit(scan.nextLine());
				scan.close();
			}
		}.visit(cu, null);
	}

	private static String constructDescription(String siteCode, String problemId, String title) {
		String description;
		if (siteCode.equals("spoj"))
			description = "SPOJ" + " " + problemId.toUpperCase() + " - " + title;
		else if (siteCode.equals("hackercup"))
			description = "Hacker Cup" + " " + getUrlDirResolved(problemId) + " - " + title;
		else if (siteCode.equals("codejam"))
			description = "Code Jam" + " " + getUrlDirResolved(problemId) + " - " + title;
		else if (siteCode.equals("lightoj"))
			description = "LightOJ" + " " + getUrlDirResolved(problemId) + " - " + title;
		else if (siteCode.equals("tju"))
			description = "TJU" + " " + getUrlDirResolved(problemId) + " - " + title;
		else if (siteCode.equals("poj"))
			description = "POJ" + " " + getUrlDirResolved(problemId) + " - " + title;
		else
			throw new RuntimeException(siteCode);
		return description;
	}

	private static String getUrlDirResolved(String problemId) {
		String r = "";
		for (int i = 0; i < problemId.length(); i++) {
			char c = problemId.charAt(i);
			if (c == '-')
				r += ' ';
			else if (i > 0 && problemId.charAt(i - 1) == '-')
				r += Character.toUpperCase(c);
			else
				r += c;
		}
		return r;
	}
	private static void forward(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, String jspFileName) throws ServletException, IOException {
		servlet.getServletContext().getRequestDispatcher(req.getContextPath() + "/WEB-INF/" + jspFileName).forward(req, res);
	}


}
