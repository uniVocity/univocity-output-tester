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
abstract class ClassPathResourceReader implements ResourceReader<InputStream> {

	@Override
	public boolean isCaseSensitive() {
		return true;
	}

	public Set<String> listResourcesUnder(String path) {
		Set<String> resources = new TreeSet<String>();

		InputStream in = null;
		BufferedReader br = null;
		try {
			try {
				in = open(path);
				br = new BufferedReader(new InputStreamReader(in));

				String resourceName;
				while ((resourceName = br.readLine()) != null) {
					resources.add(resourceName);
				}
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} finally {
					if (br != null) {
						br.close();
					}
				}
			}
		} catch (Exception e) {
			//ignore
		}
		return resources;
	}
}

