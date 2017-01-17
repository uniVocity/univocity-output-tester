package com.univocity.test;

import java.io.*;
import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
interface ResourceReader {

	boolean isCaseSensitive();

	InputStream open(String path);

	Set<String> listResourcesUnder(String path);
}