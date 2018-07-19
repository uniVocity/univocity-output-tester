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
abstract class AbstractFileLocator<T> implements ResourceReader<T> {

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
	public final T open(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		return open(file);
	}

	protected abstract T open(File file);

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
