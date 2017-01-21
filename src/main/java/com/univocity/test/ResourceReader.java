package com.univocity.test;

import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
interface ResourceReader<T> {

	boolean isCaseSensitive();

	T open(String path);

	Set<String> listResourcesUnder(String path);
}