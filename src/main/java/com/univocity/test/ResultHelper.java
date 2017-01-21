/*
 * Copyright (c) 2013 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 */

package com.univocity.test;

import java.io.*;
import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
class ResultHelper {

	private static final FileLocator fileLocator = new FileLocator();

	static <T> T findExpectedResultFile(final String resultsPath, String testMethod, ResourceReader<T> reader) {
		Set<String> matchingResources = new TreeSet<String>();

		for (String name : reader.listResourcesUnder(resultsPath)) {
			if (name.toLowerCase().startsWith(testMethod.toLowerCase())) {
				if (name.equals(testMethod)) {
					return reader.open(resultsPath + '/' + name);
				}
				matchingResources.add(name);
			}
		}

		if (!matchingResources.isEmpty()) {
			if (!reader.isCaseSensitive()) {
				for (String name : matchingResources) {
					if (name.equalsIgnoreCase(testMethod)) { //result file has different case
						return reader.open(resultsPath + '/' + name);
					}
				}
			}

			for (String name : matchingResources) {
				if (name.length() > testMethod.length() && name.charAt(testMethod.length()) == '.') { //result file has extension
					if (reader.isCaseSensitive()) {
						if (name.substring(0, testMethod.length()).equals(testMethod)) { //case must match
							return reader.open(resultsPath + '/' + name);
						}
					} else {
						return reader.open(resultsPath + '/' + name);
					}
				}
			}
		}
		return null;
	}

	static String readExpectedResult(InputStream input, String encoding) {
		Scanner scanner = null;
		try {
			if (encoding == null) {
				scanner = new Scanner(input);
			} else {
				scanner = new Scanner(input, encoding);
			}
			scanner.useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}

	static void dumpOutput(String output, File targetDir, String className, String testMethod) {
		if (targetDir == null) {
			try {
				File tmp = File.createTempFile(testMethod + "_", ".txt");
				targetDir = new File(tmp.getParent());
				tmp.delete();
			} catch (Exception e) {
				throw new IllegalStateException("Could not dump expected output of method '" + testMethod + "': error creating temporary file.", e);
			}
		}

		File dirOfClassResult = new File(targetDir.getAbsolutePath() + File.separatorChar + className);
		if (!dirOfClassResult.exists()) {
			dirOfClassResult.mkdir();
		}

		File targetFile = findExpectedResultFile(dirOfClassResult.getAbsolutePath(), testMethod, fileLocator);
		if (targetFile == null) {
			targetFile = new File(dirOfClassResult.getAbsolutePath() + File.separatorChar + testMethod + ".txt");
		}


		try {
			FileWriter fw = new FileWriter(targetFile);
			try {
				fw.write(output);
				System.out.println("\n>> Output dumped into file: " + targetFile.getAbsolutePath());
			} finally {
				fw.close();
			}
		} catch (Exception e) {
			throw new IllegalStateException("Could not write expected output of method '" + testMethod + "': error writing content to file: " + targetFile.getAbsolutePath(), e);
		}
	}
}
