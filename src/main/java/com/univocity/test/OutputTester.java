/*******************************************************************************
 * Copyright 2014 uniVocity Software Pty Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
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
 * A very simple class to facilitate testing of outputs produced by test cases.
 * 
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 */
public class OutputTester {

	private StringBuilder out = new StringBuilder();
	
	private final Class<?> testRoot;
	private final String packageName;
	private final String expectedOutputsDirPath;
	private final String expectedOutputEncoding;
	private boolean normalizeLineSeparators = true;

	/**
	 * Creates an output tester to validate outputs produced by test methods of a subclass
	 * @param expectedOutputsDirPath the path to a file or classpath resource that contains the expected outputs
	 */
	public OutputTester(String expectedOutputsDirPath) {
		this(null, expectedOutputsDirPath, null);
	}
	
	/**
	 * Creates an output tester to validate outputs produced by test methods of a subclass
	 * @param expectedOutputsDirPath the path to a file or classpath resource that contains the expected outputs
	 * @param expectedOutputEncoding the encoding of the files stored in the given path
	 */
	public OutputTester(String expectedOutputsDirPath, String expectedOutputEncoding) {
		this(null, expectedOutputsDirPath, expectedOutputEncoding);
	}

	/**
	 * Creates an output tester to validate outputs produced by test methods of a given class.
	 * @param testRoot the test class whose test methods' outputs will be validated
	 * @param expectedOutputsDirPath the path to a file or classpath resource that contains the expected outputs
	 */
	public OutputTester(Class<?> testRoot, String expectedOutputsDirPath) {
		this(testRoot, expectedOutputsDirPath, null);
	}

	/**
	 * Creates an output tester to validate outputs produced by test methods of a given class.
	 * @param testRoot the test class whose test methods' outputs will be validated
	 * @param expectedOutputsDirPath the path to a file or classpath resource that contains the expected outputs
	 * @param expectedOutputEncoding the encoding of the files stored in the given path
	 */
	public OutputTester(Class<?> testRoot, String expectedOutputsDirPath, String expectedOutputEncoding) {
		expectedOutputsDirPath = expectedOutputsDirPath.trim();
		if (expectedOutputsDirPath.endsWith("/")) {
			expectedOutputsDirPath = expectedOutputsDirPath.substring(0, expectedOutputsDirPath.length() - 1);
		}
		this.testRoot = testRoot == null ? getClass() : testRoot;
		this.packageName = testRoot.getPackage().getName();
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
	 * Appends some content to the output and adds a newline at the end.
	 * @param out the output to have content appended to.
	 * @param contents the contents to be appended
	 */
	public void println(StringBuilder out, Object contents) {
		out.append(contents).append('\n');
	}
	
	/**
	 * Appends some content to the output and adds a newline at the end.
	 * @param contents the contents to be appended
	 */
	public void println(Object contents) {
		out.append(contents).append('\n');
	}

	/**
	 * Appends a newline to the output
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
	 * @param out the output to have content appended to.
	 * @param contents the contents to be appended
	 */
	public void print(StringBuilder out, Object contents) {
		out.append(contents);
	}
	
	/**
	 * Appends some content to the output.
	 * @param contents the contents to be appended
	 */
	public void print(Object contents) {
		out.append(contents);
	}

	
	/**
	 * Finds out the test method being executed and compares the output against
	 * the expected output in {expectedOutputsDirPath}
	 * @param validate flag to indicate whether the output should be validated
	 * @param print flag that indicates whether or not to print the output
	 * @param producedOutput the output produced by an example
	 */
	private void printAndValidateOutput(boolean validate, boolean print, String producedOutput) {
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
				String method = element.getMethodName().toLowerCase();

				if (method.endsWith("validate")) {
					continue;
				}

				className = className.substring(className.lastIndexOf('.') + 1, className.length());

				if (print) {
					System.out.println("\n===[ Output produced by " + className + "." + method + " ]===");
					System.out.println(producedOutput);
					System.out.println("\n===[ End of output produced by " + className + "." + method + " ]===");
				}

				if (validate) {
					validateExampleOutput(className, method, producedOutput);
				}

				return;
			}
		}

		throw new IllegalStateException("Could not load file with expected output");
	}

	private void validateExampleOutput(String className, String testMethod, String producedOutput) {
		String path = expectedOutputsDirPath + "/" + className + "/" + testMethod;

		InputStream input = this.getClass().getResourceAsStream(path);

		if (input == null) {
			File file = new File(path);
			if(file.exists()){
				try{
					input = new FileInputStream(file);
				} catch(Exception ex){
					throw new IllegalStateException("Could not load expected output from path: " + path, ex);
				}
			}
			throw new IllegalStateException("Could not load expected output from path: " + path);
		}

		String expectedOutput = "";

		Scanner scanner = null;
		try {
			if (expectedOutputEncoding == null) {
				scanner = new Scanner(input);
			} else {
				scanner = new Scanner(input, expectedOutputEncoding);
			}
			scanner.useDelimiter("\\A");
			expectedOutput = scanner.hasNext() ? scanner.next() : "";
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}

		if (producedOutput.isEmpty() && expectedOutput.isEmpty()) {
			return;
		}

		producedOutput = cleanup(producedOutput);
		expectedOutput = cleanup(expectedOutput);

		if (!producedOutput.equals(expectedOutput)) {
			String message = "Outputs do not match.\n -------[ Result ]-------\n" + producedOutput + "\n-------[ Expected ]-------\n" + expectedOutput + "\n-------[ End ]-------\n";
			throw new AssertionError(message);
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
	public void clear(){
		this.out = new StringBuilder();
	}
	
	private String getOutputAndClear(){
		String output = out.toString();
		clear();
		return output;
	}
}
