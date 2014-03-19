package org.psjava.solutions.site;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import org.psjava.solutions.site.util.ZipUtil;
import org.psjava.util.DataKeeper;

import play.api.Play;
import play.cache.Cached;
import play.libs.F.Function;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class SolutionsSiteController extends Controller {

	@Cached(key = "index")
	public static Result index() {
		return ok(index.render());
	}

	public static Promise<Result> showSolution(final String siteName, final String problemId, final String title) {
		if (containsUpper(siteName) || containsUpper(title))
			return createNotFoundPromise();
		final File zipFile = Play.getFile("file-resources/solutions-master.zip", Play.current());
		final String path = "solutions-master/src/main/java/org/psjava/solutions/code/" + siteName + "_" + problemId.toLowerCase() + "/" + "Main.java";
		return Promise.promise(new Function0<String>() {
			@Override
			public String apply() throws Throwable {
				return ZipUtil.loadUTF8StringInZipFileOrNull(zipFile, path);
			}
		}).map(new Function<String, Result>() {
			@Override
			public Result apply(String contentOrNull) throws Throwable {
				if (contentOrNull == null)
					return notFound("unknown problem");
				CompilationUnit cu = JavaParser.parse(new ByteArrayInputStream(contentOrNull.getBytes("UTF-8")), "UTF-8");
				final DataKeeper<Boolean> matchTitle = DataKeeper.create(false);
				final ArrayList<String> hints = new ArrayList<String>();
				new VoidVisitorAdapter<Object>() {
					@Override
					public void visit(ClassOrInterfaceDeclaration n, Object arg) {
						JavadocComment doc = n.getJavaDoc();
						Scanner scan = new Scanner(doc.getContent());
						while (scan.hasNextLine()) {
							String line = scan.nextLine();
							if (line.contains("@title")) {
								String titleInDoc = line.substring(line.indexOf("@title") + "@title".length()).trim();
								String adjusted = "";
								for (int i = 0; i < titleInDoc.length(); i++) {
									char c = titleInDoc.charAt(i);
									if (Character.isAlphabetic(c))
										adjusted += Character.toLowerCase(c);
									else if (c == ' ')
										adjusted += '-';
								}
								if (adjusted.equals(title))
									matchTitle.set(true);
							}
							if (line.contains("@hint"))
								hints.add(line.substring(line.indexOf("@hint") + 5).trim());
						}
						scan.close();
					}
				}.visit(cu, null);
				if (matchTitle.get())
					return ok(solution.render(contentOrNull, hints, siteName, problemId));
				else
					return notFound("unknown problem");
			}
		});
	}

	private static boolean containsUpper(String text) {
		return !text.toLowerCase().equals(text);
	}

	private static Promise<Result> createNotFoundPromise() {
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				return badRequest("bad request");
			}
		});
	}
}
