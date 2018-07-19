/*******************************************************************************
 * Copyright 2017 Univocity Software Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.univocity.test;

import java.io.*;
import java.util.*;

/**
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
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

	static void dumpOutput(String output, String className, String testMethod, File targetDir, String charset) {
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
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(targetFile), charset);
			try {
				out.write(output);
				System.out.println("\n>> Output dumped into file: " + targetFile.getAbsolutePath());
			} finally {
				out.close();
			}
		} catch (Exception e) {
			throw new IllegalStateException("Could not write expected output of method '" + testMethod + "': error writing content to file: " + targetFile.getAbsolutePath(), e);
		}
	}

	static String getMethodWithArgs(String method, Object[] args) {
		if (args == null || args.length == 0) {
			return method;
		}

		StringBuilder out = new StringBuilder(method);


		for (int i = 0; i < args.length; i++) {
			String arg = getSafeArg(args[i]);
			out.append('_');
			out.append(arg);
		}

		return out.toString();
	}

	static String getSafeArg(Object arg) {
		if (arg == null) {
			return "null";
		}
		String out = String.valueOf(arg);
		out = out.replaceAll("[^\\w .-]", "_");
		if (out.length() > 15) {
			out = out.substring(0, 15);
		}

		return out;
	}
}
