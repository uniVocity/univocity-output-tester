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

import java.text.*;
import java.util.*;

public class TestUtils {

	private static void assertLengths(Object[] expected, Object[] result) {
		if (expected == null) {
			if (result == null) {
				return;
			} else {
				throw new AssertionError(buildMismatchMessage(null, result));
			}
		} else if (result == null) {
			throw new AssertionError(buildMismatchMessage(expected, null));
		}

		if (expected.length != result.length) {
			throw new AssertionError(buildMismatchMessage(expected, result));
		}
	}

	public static <T> void assertLinesAreEqual(T[][] result, T[][] expected) {
		assertLengths(result, expected);
		for (int i = 0; i < expected.length; i++) {
			T[] expectedLine = expected[i];
			T[] resultLine = result[i];
			assertEquals(expectedLine, resultLine);
		}
	}

	public static <T> void assertEquals(T[] expected, T[] result) {
		assertLengths(expected, result);

		for (int i = 0; i < expected.length; i++) {
			if (result[i] == null || expected[i] == null) {
				if (result[i] != null || expected[i] != null) {
					fail(expected, result, i);
				} else {
					continue;
				}
			}

			if (result[i].getClass().isArray() && expected[i].getClass().isArray()) {
				assertEquals((T[]) expected[i], (T[]) result[i]);
			} else if (!result[i].equals(expected[i])) {
				fail(expected, result, i);
			}
		}
	}

	private static <T> void fail(T[] expected, T[] result, int position) {
		throw new AssertionError(
				"Arrays not equal. Element at position " + position + " should be " + printElement(expected[position]) + " but got " + printElement(result[position]) + ".\n"
						+ buildMismatchMessage(expected, result));
	}

	private static <T> String buildMismatchMessage(T[] expected, T[] result) {
		return "Outputs do not match: expected " + printArrayElements(expected) + " but found " + printArrayElements(result);
	}

	private static <T> String printElement(T o) {
		if (o == null) {
			return "null";
		}
		if (o.getClass().isArray()) {
			return printArrayElements((T[]) o);
		}
		return String.valueOf(o);
	}

	private static <T> String printArrayElements(T[] array) {
		if (array == null) {
			return "null";
		}
		if (array.length == 0) {
			return "[]";
		}
		StringBuilder out = new StringBuilder();

		printArrayElements(out, array, 0);

		return out.toString();
	}

	private static <T> void printArrayElements(StringBuilder out, T[] array, final int nestingLevel) {
		int tabs = nestingLevel;
		while (tabs-- > 0) {
			out.append('\t');
		}

		out.append('[');

		for (int i = 0; i < array.length; i++) {
			T value = array[i];
			if (out.length() != 1) {
				out.append(',');
			}
			if (value == null) {
				out.append("null");
			} else {
				if (value instanceof CharSequence || value instanceof Character) {
					out.append('\'').append(value).append('\'');
				} else if (value.getClass().isArray()) {
					out.append('\n');
					printArrayElements(out, (T[]) value, nestingLevel + 1);
				} else {
					out.append(value);
				}
			}
		}

		out.append(']');
	}

	public static <T> void assertEquals(Collection<T> result, T[] expected) {
		assertEquals(result.toArray(), expected);
	}

	public static <T> void assertEquals(T[] result, Collection<T> expected) {
		assertEquals(result, expected.toArray());
	}

	public static String formatDateNoTime(Date date) {
		return formatDate(date, "dd-MMM-yyyy");
	}

	public static String formatDate(Date date) {
		return formatDate(date, "dd-MMM-yyyy HH:mm:ss");
	}

	public static String formatDate(Date date, String format) {
		if (date == null) {
			return "null";
		}

		SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.ENGLISH);
		String formatted = formatter.format(date);
		return formatted;
	}
}
