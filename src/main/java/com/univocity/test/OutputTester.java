/*******************************************************************************
 * Copyright 2014 uniVocity Software Pty Ltd
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

/**
 * A very simple class to facilitate testing of outputs produced by test cases.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 */
public class OutputTester {

	private StringBuilder out = new StringBuilder();

	private final Class<?> testRoot;
	private final String packageName;
	private final String expectedOutputsDirPath;
	private final String expectedOutputEncoding;
	private boolean normalizeLineSeparators = true;
	private boolean dumpMismatchedOutputToFile = true;

	private final ResourceReader classLoaderReader = new ClassPathResourceReader() {
		final ClassLoader classloader = this.getClass().getClassLoader();

		@Override
		public InputStream open(String path) {
			return classloader.getResourceAsStream(path);
		}
	};

	private final ResourceReader classResourceReader = new ClassPathResourceReader() {
		@Override
		public InputStream open(String path) {
			return testRoot.getResourceAsStream('/' + path);
		}
	};

	private final ResourceReader fileReader = new FilesystemResourceReader();

	/**
	 * Creates an output tester to validate outputs produced by test methods of a subclass
	 *
	 * @param expectedOutputsDirPath the path to a file or classpath resource that contains the expected outputs
	 */
	public OutputTester(String expectedOutputsDirPath) {
		this(null, expectedOutputsDirPath, null);
	}

	/**
	 * Creates an output tester to validate outputs produced by test methods of a subclass
	 *
	 * @param expectedOutputsDirPath the path to a file or classpath resource that contains the expected outputs
	 * @param expectedOutputEncoding the encoding of the files stored in the given path
	 */
	public OutputTester(String expectedOutputsDirPath, String expectedOutputEncoding) {
		this(null, expectedOutputsDirPath, expectedOutputEncoding);
	}

	/**
	 * Creates an output tester to validate outputs produced by test methods of a given class.
	 *
	 * @param testRoot               the test class whose test methods' outputs will be validated
	 * @param expectedOutputsDirPath the path to a file or classpath resource that contains the expected outputs
	 */
	public OutputTester(Class<?> testRoot, String expectedOutputsDirPath) {
		this(testRoot, expectedOutputsDirPath, null);
	}

	/**
	 * Creates an output tester to validate outputs produced by test methods of a given class.
	 *
	 * @param testRoot               the test class whose test methods' outputs will be validated
	 * @param expectedOutputsDirPath the path to a file or classpath resource that contains the expected outputs
	 * @param expectedOutputEncoding the encoding of the files stored in the given path
	 */
	public OutputTester(Class<?> testRoot, String expectedOutputsDirPath, String expectedOutputEncoding) {
		expectedOutputsDirPath = expectedOutputsDirPath.trim();
		if (expectedOutputsDirPath.endsWith("/")) {
			expectedOutputsDirPath = expectedOutputsDirPath.substring(0, expectedOutputsDirPath.length() - 1);
		}
		this.testRoot = testRoot == null ? getClass() : testRoot;
		this.packageName = this.testRoot.getPackage().getName();
		this.expectedOutputsDirPath = expectedOutputsDirPath;
		this.expectedOutputEncoding = expectedOutputEncoding;
	}

	/**
	 * Prints the result to the standard output without validating its contents
	 *
	 * @param output the result of the test case.
	 */
	public void printAndDontValidate(CharSequence output) {
		printAndValidateOutput(false, true, output.toString());
	}

	/**
	 * Prints the result to the standard output without validating its contents
	 */
	public void printAndDontValidate() {
		printAndDontValidate(getOutputAndClear());
	}

	/**
	 * Prints the result to the standard output and validates it against the expected output
	 * stored in {expectedOutputsDirPath}/{test_class_name}/{test_method_name}
	 *
	 * @param output the result of the test case to be validated against the expected output.
	 */
	public void printAndValidate(CharSequence output) {
		printAndValidateOutput(true, true, output.toString());
	}

	/**
	 * Prints the result to the standard output and validates it against the expected output
	 * stored in {expectedOutputsDirPath}/{test_class_name}/{test_method_name}
	 */
	public void printAndValidate() {
		printAndValidate(getOutputAndClear());
	}

	/**
	 * Validates the result against the expected output
	 * stored in {expectedOutputsDirPath}/{test_class_name}/{test_method_name}
	 *
	 * @param output the result of the test case to be validated against the expected output.
	 */
	public void validate(CharSequence output) {
		printAndValidateOutput(true, false, output.toString());
	}

	/**
	 * Validates the result against the expected output
	 * stored in {expectedOutputsDirPath}/{test_class_name}/{test_method_name}
	 */
	public void validate() {
		validate(getOutputAndClear());
	}

	/**
	 * Updates or creates the expected output file under the given expected output directory. This method will always
	 * trigger a validation and will always fail. It prints out the different expected and actual results if they are
	 * different, or fails if the expected output is already updated and the results match.
	 *
	 * @param pathToExpectedOutputDir absolute path to the expected output directory.
	 */
	public void updateExpectedOutput(String pathToExpectedOutputDir) {
		updateExpectedOutput(getOutputAndClear(), pathToExpectedOutputDir);
	}

	/**
	 * Updates or creates the expected output file under the given expected output directory. This method will always
	 * trigger a validation and will always fail. It prints out the different expected and actual results if they are
	 * different, or fails if the expected output is already updated and the results match.
	 *
	 * @param output                  the actual output whose contents will be used to generate/update the expected output file.
	 * @param pathToExpectedOutputDir absolute path to the expected output directory.
	 */
	public void updateExpectedOutput(CharSequence output, String pathToExpectedOutputDir) {
		File expectedOutputDir = new File(pathToExpectedOutputDir);
		if (!expectedOutputDir.exists()) {
			throw new IllegalArgumentException("Path to expected output directory '" + pathToExpectedOutputDir + "' does not exist");
		}
		if (!expectedOutputDir.isDirectory()) {
			throw new IllegalArgumentException("Path to expected output directory '" + pathToExpectedOutputDir + "' does not point to a file");
		}
		printAndValidateOutput(true, false, output.toString(), expectedOutputDir);
	}

	/**
	 * Appends some content to the output and adds a newline at the end.
	 *
	 * @param out      the output to have content appended to.
	 * @param contents the contents to be appended
	 */
	public void println(StringBuilder out, Object contents) {
		out.append(contents).append('\n');
	}

	/**
	 * Appends some content to the output and adds a newline at the end.
	 *
	 * @param contents the contents to be appended
	 */
	public void println(Object contents) {
		out.append(contents).append('\n');
	}

	/**
	 * Appends a newline to the output
	 *
	 * @param out the output to have a newline appended
	 */
	public void println(StringBuilder out) {
		out.append('\n');
	}

	/**
	 * Appends a newline to the output
	 */
	public void println() {
		println(out);
	}

	/**
	 * Appends some content to the output.
	 *
	 * @param out      the output to have content appended to.
	 * @param contents the contents to be appended
	 */
	public void print(StringBuilder out, Object contents) {
		out.append(contents);
	}

	/**
	 * Appends some content to the output.
	 *
	 * @param contents the contents to be appended
	 */
	public void print(Object contents) {
		out.append(contents);
	}

	/**
	 * Finds out the test method being executed and compares the output against
	 * the expected output in {@code expectedOutputsDirPath}.
	 *
	 * @param validate       flag to indicate whether the output should be validated
	 * @param print          flag that indicates whether or not to print the output
	 * @param producedOutput the output produced by an example
	 */
	private void printAndValidateOutput(boolean validate, boolean print, String producedOutput) {
		printAndValidateOutput(validate, print, producedOutput, null);
	}

	/**
	 * Finds out the test method being executed and compares the output against
	 * the expected output in {@code expectedOutputsDirPath}. If {@code expectedOutputToUpdate} is not null,
	 * the expected output file will be generated/updated to store the expected output, and the the test method will fail.
	 *
	 * @param validate          flag to indicate whether the output should be validated
	 * @param print             flag that indicates whether or not to print the output
	 * @param producedOutput    the output produced by an example
	 * @param expectedOutputDir directory of the expected output file to generate/Øupdate
	 */
	private void printAndValidateOutput(boolean validate, boolean print, String producedOutput, File expectedOutputDir) {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stack) {
			String className = element.getClassName();

			if (className.endsWith("." + OutputTester.class.getSimpleName())) {
				continue;
			}

			if (!className.endsWith("." + testRoot.getSimpleName())) {
				continue;
			}

			if (className.startsWith(packageName)) {
				String method = element.getMethodName();

				if (method.toLowerCase().endsWith("validate")) {
					continue;
				}

				className = className.substring(className.lastIndexOf('.') + 1, className.length());

				if (validate) {
					validateExampleOutput(className, method, producedOutput, expectedOutputDir);
				}

				if (print) {
					print(producedOutput, className, method);
				}

				return;
			}
		}

		throw new IllegalStateException("Could not load file with expected output");
	}

	private void print(String output, String className, String method) {
		System.out.println("\n------[ Output produced by " + className + "." + method + " ]------");
		System.out.println(output);
		System.out.println("\n------[ End of output produced by " + className + "." + method + " ]------");
	}

	private InputStream getResultData(String className, String testMethod) {
		final String resultsPath = expectedOutputsDirPath + '/' + className;

		InputStream input = (InputStream) ResultHelper.findExpectedResultFile(resultsPath, testMethod, classLoaderReader);

		if (input == null) {
			input = (InputStream) ResultHelper.findExpectedResultFile(resultsPath, testMethod, classResourceReader);
		}

		if (input == null) {
			File file = new File(resultsPath + '/' + testMethod);
			if (file.exists()) {
				try {
					input = new FileInputStream(file);
				} catch (Exception ex) {
					return new ByteArrayInputStream(("Could not load expected output from path: " + file.getAbsolutePath() + ". " + ex.getMessage()).getBytes());
				}
			} else {
				input = (InputStream) ResultHelper.findExpectedResultFile(resultsPath, testMethod, fileReader);
				if (input != null) {
					return null;
				}
				return new ByteArrayInputStream(("Could not load expected output from path: " + file.getAbsolutePath()).getBytes());
			}
		}

		return input;
	}

	private void validateExampleOutput(String className, String testMethod, String producedOutput, File expectedOutputDir) {
		InputStream input = getResultData(className, testMethod);
		String expectedOutput = ResultHelper.readExpectedResult(input, expectedOutputEncoding);

		producedOutput = cleanup(producedOutput);
		expectedOutput = cleanup(expectedOutput);

		if (!producedOutput.equals(expectedOutput)) {
			String message = "Outputs do not match:" + " expected [" + expectedOutput + "] but found [" + producedOutput + ']';

			if (dumpMismatchedOutputToFile || expectedOutputDir != null) {
				try {
					ResultHelper.dumpOutput(producedOutput, expectedOutputDir, className, testMethod);
				} catch (Exception e) {
					print(producedOutput, className, testMethod);
					if (e instanceof RuntimeException) {
						throw (RuntimeException) e;
					} else {
						throw new IllegalStateException(e);
					}
				}
			}

			throw new AssertionError(message);
		} else if (expectedOutputDir != null) {
			print(producedOutput, className, testMethod);
			throw new AssertionError("Test case shouldn't call 'updateExpectedOutput(...)' once the expected output is up-to-date.");
		}
	}


	private String cleanup(String content) {
		if (normalizeLineSeparators) {
			content = content.replaceAll("\\r\\n", "\n");
			content = content.replaceAll("\\r", "\n");
		}
		return content;
	}

	/**
	 * Discards any output stored in the internal buffer.
	 */
	public void clear() {
		this.out = new StringBuilder();
	}

	private String getOutputAndClear() {
		String output = out.toString();
		clear();
		return output;
	}

	/**
	 * Indicates whether line separators in both produced and expected outputs will be normalized (i.e. set to {@code '\n'}).
	 *
	 * @return a flag indicating whether normalization of line separators is enabled
	 */
	public boolean isNormalizeLineSeparators() {
		return normalizeLineSeparators;
	}

	/**
	 * Enables/disables normalization of line separators. If enabled, the line separators in both produced and expected outputs will
	 * be set to {@code '\n'}.
	 *
	 * @param normalizeLineSeparators flag to enable or disable normalization of line separators
	 */
	public void setNormalizeLineSeparators(boolean normalizeLineSeparators) {
		this.normalizeLineSeparators = normalizeLineSeparators;
	}

	/**
	 * Indicates whether the output produced by a given test method should be written into a temporary file.
	 * This is useful for updating an expected output file, or obtain the initial output file.
	 *
	 * If enabled (the default), the message ">> Output dumped into temporary file: {@code <tmp_dir>/<method_name>_<random_number>.txt} will be
	 * produced before the assertion error is thrown.
	 *
	 * @return a flag indicating whether the output produced by failing test methods should be saved into a temporary file.
	 */
	public boolean isDumpMismatchedOutputToFile() {
		return dumpMismatchedOutputToFile;
	}

	/**
	 * Defines whether the output produced by a given test method should be written into a temporary file.
	 * This is useful for updating an expected output file, or obtain the initial output file.
	 *
	 * If enabled (the default), the message ">> Output dumped into temporary file: {@code <tmp_dir>/<method_name>_<random_number>.txt} will be
	 * produced before the assertion error is thrown.
	 *
	 * @param dumpMismatchedOutputToFile a flag indicating whether the output produced by failing test methods should
	 *                                   be saved into a temporary file.
	 */
	public void setDumpMismatchedOutputToFile(boolean dumpMismatchedOutputToFile) {
		this.dumpMismatchedOutputToFile = dumpMismatchedOutputToFile;
	}
}
