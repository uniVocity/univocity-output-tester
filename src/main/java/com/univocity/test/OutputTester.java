/*******************************************************************************
 * Copyright 2014 Univocity Software Pty Ltd
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
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class OutputTester {

	private StringBuilder out = new StringBuilder();

	private final Class<?> testRoot;
	private final String packageName;
	private final String expectedOutputsDirPath;
	private final String expectedOutputEncoding;
	private boolean normalizeLineSeparators = true;
	private boolean dumpMismatchedOutputToFile = true;
	private File resourceDir;
	private String testResourcesFolder = "src/test/resources";
	private boolean updateExpectedOutputs = false;

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
	 * Used to indicate when all tests of the test class should have their outputs updated. If the {@link #getUpdateExpectedOutputs()}
	 * method evaluates to {@code true}, all calls to {@link #validate()} or {@link #printAndValidate()}
	 * will trigger the {@link #updateExpectedOutput()} method internally. No tests will fail but error messages will be
	 * printed out to the standard output to remind users that the outputs are not being validated.
	 *
	 * @param updateExpectedOutputs flag to indicate whether all expected outputs of a test case should be updated.
	 */
	public void setUpdateExpectedOutputs(boolean updateExpectedOutputs) {
		this.updateExpectedOutputs = updateExpectedOutputs;
	}

	/**
	 * Returns a flag indicating whether all tests of the test class should have their outputs updated. If {@code true},
	 * all calls to {@link #validate()} or {@link #printAndValidate()} will trigger the
	 * {@link #updateExpectedOutput()} method internally. No tests will fail but error messages will be
	 * printed out to the standard output to remind users that the outputs are not being validated.
	 *
	 * @return {@code true} to indicate whether all expected outputs of a test case should be updated, or {@code false} if
	 * the test case outputs will be validated.
	 */
	public boolean getUpdateExpectedOutputs() {
		return updateExpectedOutputs;
	}

	/**
	 * Prints the result to the standard output without validating its contents
	 *
	 * @param output     the result of the test case.
	 * @param methodArgs arguments passed to the test method. Used when testing with data providers
	 */
	public void printAndDontValidate(CharSequence output, Object... methodArgs) {
		printAndValidateOutput(false, true, output.toString(), methodArgs);
	}

	/**
	 * Prints the result to the standard output without validating its contents
	 *
	 * @param methodArgs arguments passed to the test method. Used when testing with data providers
	 */
	public void printAndDontValidate(Object... methodArgs) {
		printAndDontValidate(getOutputAndClear(), methodArgs);
	}

	/**
	 * Prints the result to the standard output and validates it against the expected output
	 * stored in {expectedOutputsDirPath}/{test_class_name}/{test_method_name}
	 *
	 * @param output     the result of the test case to be validated against the expected output.
	 * @param methodArgs arguments passed to the test method. Used when testing with data providers
	 */
	public void printAndValidate(CharSequence output, Object... methodArgs) {
		printAndValidateOutput(true, true, output.toString(), methodArgs);
	}

	/**
	 * Prints the result to the standard output and validates it against the expected output
	 * stored in {expectedOutputsDirPath}/{test_class_name}/{test_method_name}
	 */
	public void printAndValidate() {
		printAndValidate(getOutputAndClear());
	}

	/**
	 * Prints the result to the standard output and validates it against the expected output
	 * stored in {expectedOutputsDirPath}/{test_class_name}/{test_method_name}
	 *
	 * @param methodArgs arguments passed to the test method. Used when testing with data providers
	 */
	public void printAndValidateUsingArgs(Object... methodArgs) {
		printAndValidate(getOutputAndClear(), methodArgs);
	}

	/**
	 * Validates the result against the expected output
	 * stored in {expectedOutputsDirPath}/{test_class_name}/{test_method_name}
	 *
	 * @param output     the result of the test case to be validated against the expected output.
	 * @param methodArgs arguments passed to the test method. Used when testing with data providers
	 */
	public void validate(CharSequence output, Object... methodArgs) {
		printAndValidateOutput(true, false, output.toString(), methodArgs);
	}

	/**
	 * Validates the result against the expected output
	 * stored in {expectedOutputsDirPath}/{test_class_name}/{test_method_name}
	 *
	 * @param methodArgs arguments passed to the test method. Used when testing with data providers
	 */
	public void validateUsingArgs(Object... methodArgs) {
		validate(getOutputAndClear(), methodArgs);
	}

	/**
	 * Validates the result against the expected output
	 * stored in {expectedOutputsDirPath}/{test_class_name}/{test_method_name}
	 */
	public void validate() {
		validate(getOutputAndClear());
	}

	/**
	 * Returns the project's actual resource directory, where the expected output files are located. Used
	 * when {@link #updateExpectedOutput()} is called to determine where to update/create expected output files.
	 *
	 * @return the project's resource directory.
	 */
	public File getResourceDir() {
		return resourceDir;
	}

	/**
	 * Defines the project's actual resource directory, where the expected output files are located. Used
	 * when {@link #updateExpectedOutput()} is called to determine where to update/create expected output files.
	 *
	 * @param resourceDir project's resource directory.
	 */
	public void setResourceDir(File resourceDir) {
		if (resourceDir != null) {
			if (!resourceDir.exists() || !resourceDir.isDirectory()) {
				throw new IllegalArgumentException("Resource path: '" + resourceDir + "' is not a directory or does not exist");
			}
		}
		this.resourceDir = resourceDir;
	}

	/**
	 * Returns the project's test resources folder (relative to the project root), where the expected output files are located. Used
	 * when {@link #updateExpectedOutput()} is called to determine where to update/create expected output files, and {@link #getResourceDir()}
	 * evaluates to {@code null}
	 * Defaults to "src/test/resources"
	 *
	 * @return the test resources folder.
	 */
	public String getTestResourcesFolder() {
		return testResourcesFolder;
	}

	/**
	 * Defines the project's test resources folder (relative to the project root), where the expected output files are located. Used
	 * when {@link #updateExpectedOutput()} is called to determine where to update/create expected output files, and {@link #getResourceDir()}
	 * evaluates to {@code null};
	 * Defaults to "src/test/resources"
	 *
	 * @param testResourcesFolder the test resources folder.
	 */
	public void setTestResourcesFolder(String testResourcesFolder) {
		this.testResourcesFolder = testResourcesFolder;
	}

	/**
	 * Updates or creates the expected output file under the given expected output directory. This method will always
	 * trigger a validation and will always fail. It prints out the different expected and actual results if they are
	 * different, or fails if the expected output is already updated and the results match.
	 */
	public void updateExpectedOutput() {
		updateExpectedOutput(getOutputAndClear());
	}

	/**
	 * Updates or creates the expected output file under the given expected output directory. This method will always
	 * trigger a validation and will always fail. It prints out the different expected and actual results if they are
	 * different, or fails if the expected output is already updated and the results match.
	 *
	 * @param methodArgs arguments passed to the test method. Used when testing with data providers
	 */
	public void updateExpectedOutputUsingArgs(Object... methodArgs) {
		updateExpectedOutput(getOutputAndClear(), methodArgs);
	}


	/**
	 * Updates or creates the expected output file under the given expected output directory. This method will always
	 * trigger a validation and will always fail. It prints out the different expected and actual results if they are
	 * different, or fails if the expected output is already updated and the results match.
	 *
	 * @param output     the actual output whose contents will be used to generate/update the expected output file.
	 * @param methodArgs arguments passed to the test method. Used when testing with data providers
	 */
	public void updateExpectedOutput(CharSequence output, Object... methodArgs) {
		printAndValidateOutput(true, false, output.toString(), getExpectedOutputDir(), methodArgs);
	}

	private File getExpectedOutputDir() {
		String pathToExpectedOutputDir;
		if (resourceDir != null) {
			pathToExpectedOutputDir = resourceDir.getAbsolutePath();
		} else {
			pathToExpectedOutputDir = new File(testResourcesFolder).getAbsolutePath();
		}
		if (!expectedOutputsDirPath.startsWith(File.separator)) {
			pathToExpectedOutputDir += File.separatorChar + expectedOutputsDirPath;
		}

		File expectedOutputDir = new File(pathToExpectedOutputDir);
		if (!expectedOutputDir.exists()) {
			throw new IllegalArgumentException("Path to expected output directory '" + pathToExpectedOutputDir + "' does not exist");
		}
		if (!expectedOutputDir.isDirectory()) {
			throw new IllegalArgumentException("Path to expected output directory '" + pathToExpectedOutputDir + "' does not point to a file");
		}
		return expectedOutputDir;
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
	 * @param methodArgs     arguments passed to the test method. Used when testing with data providers
	 */
	private void printAndValidateOutput(boolean validate, boolean print, String producedOutput, Object[] methodArgs) {
		printAndValidateOutput(validate, print, producedOutput, null, methodArgs);
	}

	/**
	 * Finds out the test method being executed and compares the output against
	 * the expected output in {@code expectedOutputsDirPath}. If {@code expectedOutputToUpdate} is not null,
	 * the expected output file will be generated/updated to store the expected output, and the the test method will fail.
	 *
	 * @param validate          flag to indicate whether the output should be validated
	 * @param print             flag that indicates whether or not to print the output
	 * @param producedOutput    the output produced by an example
	 * @param expectedOutputDir directory of the expected output file to generate/update
	 * @param methodArgs        arguments passed to the test method. Used when testing with data providers
	 */
	private void printAndValidateOutput(boolean validate, boolean print, String producedOutput, File expectedOutputDir, Object[] methodArgs) {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		String classOfSkippedTestMethod = null;
		String skippedTestMethod = null;
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

				if (methodArgs.length == 0 && !isTestMethod(className, method)) {
					classOfSkippedTestMethod = className;
					skippedTestMethod = method;
					continue;
				}

				performValidation(validate, print, className, method, methodArgs, producedOutput, expectedOutputDir);

				return;
			}
		}
		if (classOfSkippedTestMethod != null) {
			performValidation(validate, print, classOfSkippedTestMethod, skippedTestMethod, methodArgs, producedOutput, expectedOutputDir);
		} else {
			throw new IllegalStateException("Could not load file with expected output");
		}
	}

	private void performValidation(boolean validate, boolean print, String className, String method, Object[] methodArgs, String producedOutput, File expectedOutputDir) {
		className = className.substring(className.lastIndexOf('.') + 1, className.length());

		if (validate) {
			method = ResultHelper.getMethodWithArgs(method, methodArgs);
			validateExampleOutput(className, method, producedOutput, expectedOutputDir);
		}

		if (print) {
			print(producedOutput, className, method);
		}
	}

	private boolean isTestMethod(String className, String methodName) {
		try {
			Class<?> clazz = Class.forName(className);
			//valid test methods are public and have no arguments.
			return clazz.getMethod(methodName) != null;
		} catch (NoSuchMethodException e) {
			//not a test method (couldn't find public, no-arg method with name).
			return false;
		} catch (Exception e) {
			//anything else, we assume it's a valid test and try to go with it.
			return true;
		}
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
				return new ByteArrayInputStream(("Could not load expected output from path: " + resultsPath + '/' + testMethod).getBytes());
			}
		}

		return input;
	}

	private void validateExampleOutput(String className, String testMethod, String producedOutput, File expectedOutputDir) {
		if (updateExpectedOutputs) {
			expectedOutputDir = getExpectedOutputDir();
		}

		InputStream input = getResultData(className, testMethod);
		String expectedOutput = ResultHelper.readExpectedResult(input, expectedOutputEncoding);

		producedOutput = cleanup(producedOutput);
		expectedOutput = cleanup(expectedOutput);

		if (!updateExpectedOutputs && !producedOutput.equals(expectedOutput)) {
			String message = "Outputs do not match:" + " expected [" + expectedOutput + "] but found [" + producedOutput + ']';

			if (dumpMismatchedOutputToFile || expectedOutputDir != null) {
				updateExpectedOutput(className, testMethod, producedOutput, expectedOutputDir);
			}

			throw new AssertionError(message);
		} else if (expectedOutputDir != null) {
			print(producedOutput, className, testMethod);
			String message = "Test case shouldn't call 'updateExpectedOutput(...)' once the expected output is up-to-date.";
			if (!updateExpectedOutputs) {
				throw new AssertionError(message);
			} else {
				updateExpectedOutput(className, testMethod, producedOutput, expectedOutputDir);
				new IllegalStateException(message).printStackTrace();
			}

		}
	}

	private void updateExpectedOutput(String className, String testMethod, String producedOutput, File expectedOutputDir) {
		try {
			ResultHelper.dumpOutput(producedOutput, className, testMethod, expectedOutputDir, expectedOutputEncoding);
		} catch (Exception e) {
			print(producedOutput, className, testMethod);
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new IllegalStateException(e);
			}
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
	 * If enabled (the default), the message {@code ">> Output dumped into temporary file: <tmp_dir>/<method_name>_<random_number>.txt"} will be
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
	 * If enabled (the default), the message {@code ">> Output dumped into temporary file: <tmp_dir>/<method_name>_<random_number>.txt"} will be
	 * produced before the assertion error is thrown.
	 *
	 * @param dumpMismatchedOutputToFile a flag indicating whether the output produced by failing test methods should
	 *                                   be saved into a temporary file.
	 */
	public void setDumpMismatchedOutputToFile(boolean dumpMismatchedOutputToFile) {
		this.dumpMismatchedOutputToFile = dumpMismatchedOutputToFile;
	}
}
