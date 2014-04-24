package org.psjava.solutions.site;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.psjava.ds.array.DynamicArray;
import org.psjava.solutions.site.util.HttpUtil;
import org.psjava.solutions.site.util.Util;
import org.psjava.util.DataKeeper;
import org.psjava.util.EventListener;
import org.psjava.util.Java1DArray;
import org.psjava.util.Pair;
import org.psjava.util.ZeroTo;

import play.libs.F.Function;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.WS.Response;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class SolutionsSiteController extends Controller {

	private static final String TARGET_REF = "v1";
	private static final String LISTING_URL = "https://api.github.com/repos/psjava/solutions/contents/src/main/java/org/psjava/solutions/code";

	public static Promise<Result> index() {
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("ref", TARGET_REF);
		return HttpUtil.createCacheableUrlFetchPromise(LISTING_URL, param).map(new Function<Response, List<String>>() {
			public List<String> apply(Response res) throws Throwable {
				JSONArray array = new JSONArray(res.getBody());
				ArrayList<String> dirNames = new ArrayList<String>();
				for (int i : ZeroTo.get(array.length())) {
					String dirName = array.getJSONObject(i).getString("name");
					if (dirName.contains("_")) // TODO after migration finish, remove this condition
						dirNames.add(dirName);
				}
				return dirNames;
			};
		}).flatMap(new Function<List<String>, Promise<Result>>() {
			@Override
			public Promise<Result> apply(final List<String> dirNames) throws Throwable {
				Promise<Response>[] promises = Java1DArray.create(Promise.class, dirNames.size());
				for (int i : ZeroTo.get(promises.length))
					promises[i] = HttpUtil.createCacheableUrlFetchPromise(constructRawContentUrl(dirNames.get(i)), new HashMap<String, String>());
				return Promise.sequence(promises).map(new Function<List<Response>, Result>() {
					@Override
					public Result apply(List<Response> resList) throws Throwable {
						DynamicArray<Pair<Pair<String, String>, Pair<String, String>>> r = DynamicArray.create();
						for (int i : ZeroTo.get(dirNames.size())) {
							String dirName = dirNames.get(i);
							int indexOrN1 = dirName.indexOf('_');
							if (indexOrN1 == -1)
								continue;
							String siteCode = dirName.substring(0, indexOrN1);
							String problemId = dirName.substring(indexOrN1 + 1).replace('_', '-');
							Response res = resList.get(i);
							if (res.getStatus() != 200)
								return notFound("invalid problem (" + dirName + ")");
							String title = extractTitleInJavaDoc(parse(res.getBody()));
							String urlDir = convertToUrlDir(title);
							String description = constructDescription(siteCode, problemId, title);
							r.addToLast(Pair.create(Pair.create(siteCode, problemId), Pair.create(description, urlDir)));
						}
						return ok(index.render(Util.toList(r)));
					}
				});
			}
		});
	}

	public static Promise<Result> showSolution(final String siteCode, final String problemId, final String titleInUrlDirFormat) {
		if (containsUpper(siteCode) || containsUpper(titleInUrlDirFormat) || containsUpper(problemId))
			return createNotFoundPromise();
		return HttpUtil.createCacheableUrlFetchPromise(constructRawContentUrl(siteCode, problemId), new HashMap<String, String>()).map(new Function<Response, Result>() {
			@Override
			public Result apply(Response res) throws Throwable {
				if (res.getStatus() != 200)
					return notFound("unknown problem");
				String content = res.getBody();
				CompilationUnit cu = parse(content);
				String title = extractTitleInJavaDoc(cu);
				if (convertToUrlDir(title).equals(titleInUrlDirFormat))
					return ok(solution.render(content, extractHintsFromJavaDoc(cu), siteCode, constructDescription(siteCode, problemId, title)));
				else
					return notFound("unknown problem");
			}
		});
	}

	public static Result clearCache() {
		HttpUtil.clearCached();
		return redirect("/");
	}

	private static boolean containsUpper(String text) {
		return !text.toLowerCase().equals(text);
	}

	private static String constructRawContentUrl(final String siteCode, final String problemId) {
		return constructRawContentUrl(siteCode + "_" + problemId.replace('-', '_'));
	}

	private static String constructRawContentUrl(String problemDirName) {
		return "https://raw.github.com/psjava/solutions/" + TARGET_REF + "/src/main/java/org/psjava/solutions/code/" + problemDirName + "/Main.java";
	}

	private static Promise<Result> createNotFoundPromise() {
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				return badRequest("bad request");
			}
		});
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
		final ArrayList<String> r = new ArrayList<String>();
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
		else if (siteCode.equals("codejam"))
			description = "Code Jam" + " " + getUrlDirResolved(problemId) + " - " + title;
		else
			throw new RuntimeException();
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
}
