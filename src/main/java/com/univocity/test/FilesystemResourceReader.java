package com.univocity.test;

import java.io.*;
import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
class FilesystemResourceReader implements ResourceReader {

	private static final boolean CASE_INSENSITIVE;
	private static final boolean CASE_SENSITIVE;

	static {
		String userDir = System.getProperty("user.dir");
		File tmp = null;
		if (userDir == null || userDir.trim().isEmpty()) {
			try {
				tmp = File.createTempFile("abc", ".tmp");
				userDir = tmp.getAbsolutePath();
			} catch (Exception e) {
				userDir = "abc";
			}
		}
		File original = new File(userDir);
		File upper = new File(userDir.toUpperCase());
		File lower = new File(userDir.toLowerCase());

		CASE_INSENSITIVE = original.exists() && upper.exists() && lower.exists();
		CASE_SENSITIVE = !CASE_INSENSITIVE;

		if (tmp != null) {
			tmp.delete();
		}
	}

	@Override
	public InputStream open(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		try {
			return new FileInputStream(file);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Set<String> listResourcesUnder(String path) {
		Set<String> names = new TreeSet<String>();
		File directory = new File(path);
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (File f : files) {
					String name = f.getName();
					names.add(name);
				}
			}
		}
		return names;
	}

	@Override
	public boolean isCaseSensitive() {
		return CASE_SENSITIVE;
	}
}
