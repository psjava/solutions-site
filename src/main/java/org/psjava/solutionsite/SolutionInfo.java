package org.psjava.solutionsite;

import org.psjava.util.Pair;

public class SolutionInfo {
	private final String siteCode;
	private final String problemId;
	private final String description;
	private final String urlDir;

	public SolutionInfo(String siteCode, String problemId, String description, String urlDir) {
		this.siteCode = siteCode;
		this.problemId = problemId;
		this.description = description;
		this.urlDir = urlDir;
	}

	public String getDescription() {
		return description;
	}

	public String getProblemId() {
		return problemId;
	}

	public String getSiteCode() {
		return siteCode;
	}

	public String getUrlDir() {
		return urlDir;
	}
}
