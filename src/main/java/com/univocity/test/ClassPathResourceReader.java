package com.univocity.test;

import java.io.*;
import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
abstract class ClassPathResourceReader implements ResourceReader {

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

