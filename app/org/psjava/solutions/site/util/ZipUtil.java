package org.psjava.solutions.site.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipUtil {

	public static String loadUTF8StringInZipFileOrNull(File zipFile, String path) throws ZipException, IOException, UnsupportedEncodingException {
		ZipFile z = new ZipFile(zipFile);
		try {
			ZipEntry e = z.getEntry(path);
			if (e != null) {
				InputStream is = z.getInputStream(e);
				try {
					return FileUtil.loadUTF8(is).trim();
				} finally {
					is.close();
				}
			} else {
				return null;
			}
		} finally {
			z.close();
		}
	}

}
