univocity-output-tester
=======================

This very simple project was created by **[uniVocity](http://www.univocity.com)** to help you validate the expected results of test cases that produce data samples and non-trivial outputs, such as XML, CSV, collections and arrays, etc.

It enforces a consistent and organized testing structure and enables you to easily see what is going on with your tests if you want to.

We use this project to test the output produced by our ETL framework, **[uniVocity](http://www.univocity.com/pages/about-univocity)**, and our parsing framework, **[uniVocity-parsers](http://www.univocity.com/pages/about-parsers)**.

This utility works with the following structure:

 1 - For each test class, there should be a directory with the exact same name.
 
 1.2 - For each test method of the class, that produces an output to validate, there should be a file that matches the exact name of the test method.
 
 2 - Your test class must either:
 
 2.1 - `Extend` from [OutputTester](./src/main/java/com/univocity/test/OutputTester.java)
 
 2.2 - Or `contain` an instance of [OutputTester](./src/main/java/com/univocity/test/OutputTester.java)
 
 3 - Your test methods must either:
 
 3.1 - Use the `print` methods provided by the [OutputTester](./src/main/java/com/univocity/test/OutputTester.java)
 
 3.2 - Or store the output results somehow. `StringWriter` and `StringBuilder` are your friends. For convenience, we 
 provided some `print` methods that take your `StringBuilder` as a parameter.
 
 4 - At the end of the test method, invoke `printAndValidate`, `printAndDontValidate` or `validate`. These will print 
 and/or validate the output of your tests. The file that matches your test method name will be read and its contents 
 will be compared against the output of your test. If a single character is different, your test will fail.
 A temporary file with the output produced will be generated.
  
 5 - To generate the expected output file from a sane test result, call `updateExpectedOutput` and it will be generated 
 or updated for you at your test resources folder (defaults to `src/test/resources` - use `setTestResourcesFolder` to 
 specify another location). The test will fail to remind you to call one of the validation methods instead. **Note**: 
 If your tests are parameterized, call `updateExpectedOutput` with the test parameter values. Output files named after
 the method and its parameter values will be generated. 

 6 - If you made a big change that affects many tests at once, use `setUpdateExpectedOutputs(true)` at the class level -
 or parent class level if applicable - to update all expected outputs in one go. No tests will fail so you must
  remember to revert back to `setUpdateExpectedOutputs(false)` or just remove the command.

**Note:** it is not a good practice to print the output of your tests unless you are debugging/trying to demonstrate something to someone (like we did in the following example).

## Using the [OutputTester](./src/main/java/com/univocity/test/OutputTester.java) as a parent class

Let's use as our **[schema mapping tutorial](https://github.com/uniVocity/univocity-examples/blob/master/src/test/java/com/univocity/examples/Tutorial003SchemaMapping.java)** as an example. It is essentially a test case with the following structure:


```java

	abstract class Example extends OutputTester {
		public Example() {
			super("examples/expectedOutputs/", "UTF-8");
		}
		
		//... other common stuff used by all examples
	}

	public class Tutorial003SchemaMapping extends Example {
		@Test
		public void example001SchemaMapping() {
		    //do stuff
		    print("something"); //just adds "something" to the OutputTester's internal buffer"
		    ...
		    printAndValidate("some_output");
		}
		
		@Test
		public void example002UpdateAgainstDataset() {
			//do more stuff
			println("something else"); //adds "something else" to the OutputTester's internal buffer"
			...
			printAndValidate("another_output");
		}
		
		@Test
		public void example003UpdatePrevention() {
			StringBuilder out = new StringBuilder();
		    //to other stuff
		    println(out, "something"); //adds "something" to your StringBuilder instance
			...
			printAndValidate(out);
		}
	}
	
```

In the above example, we created a test class hierarchy, with [OutputTester](./src/main/java/com/univocity/test/OutputTester.java) being the parent of all tests. In its constructor, you have to define a path to a directory that contains the expected results of each test where the methods `printAndValidate`, `printAndDontValidate` or `validate` are used.

Each test method produces some sort of output. The actual implementation in  **[Tutorial003SchemaMapping](https://github.com/uniVocity/univocity-examples/blob/master/src/test/java/com/univocity/examples/Tutorial003SchemaMapping.java)** writes the output of our tests to a `StringWriter` instead of a file.

When a method such as `printAndValidate` is invoked from within your test case, a file with the expected result will be read and its contents compared to the actual output of your test case. Under the `src/test/resources` directory of our examples, you will find the [examples/expectedOutputs](https://github.com/uniVocity/univocity-examples/tree/master/src/test/resources/examples/expectedOutputs/) directory.

In the `expectedOutputs` directory, you will find the [Tutorial003SchemaMapping](https://github.com/uniVocity/univocity-examples/tree/master/src/test/resources/examples/expectedOutputs/Tutorial003SchemaMapping) subdirectory, whose name matches exactly the name of the test class.

Inside this directory, you will find the following output files, whose names match the names of the test methods. Each file contains the corresponding expected output. In this case, there are 3 files:

 * [example001SchemaMapping](https://github.com/uniVocity/univocity-examples/blob/master/src/test/resources/examples/expectedOutputs/Tutorial003SchemaMapping/example001SchemaMapping)
 * [example002UpdateAgainstDataset](https://github.com/uniVocity/univocity-examples/blob/master/src/test/resources/examples/expectedOutputs/Tutorial003SchemaMapping/example002UpdateAgainstDataset)
 * [example003UpdatePrevention](https://github.com/uniVocity/univocity-examples/blob/master/src/test/resources/examples/expectedOutputs/Tutorial003SchemaMapping/example003UpdatePrevention)
 
They simply contain what the matching test method would produce normally, if it were to write to a temporary file, or to a String, or whatever. This is the sample output of [example002UpdateAgainstDataset](https://github.com/uniVocity/univocity-examples/blob/master/src/test/resources/examples/expectedOutputs/Tutorial003SchemaMapping/example002UpdateAgainstDataset) (which is in fixed-width format for easier visualization):

```
	
	===[ food_group ]===
	id___
	0____
	1____
	...
	
	===[ food_group_details ]===
	id___loc___description__________________________________________________________
	0____0_____Dairy and Egg Products_______________________________________________
	0____1_____Milk, eggs and stuff_________________________________________________
	1____0_____Spices and Herbs_____________________________________________________
	1____1_____Spices and Herbs_____________________________________________________
	...

```

## Using the [OutputTester](./src/main/java/com/univocity/test/OutputTester.java) as a component of another class

Let's rewrite the previous **[schema mapping tutorial](https://github.com/uniVocity/univocity-examples/blob/master/src/test/java/com/univocity/examples/Tutorial003SchemaMapping.java)** to `contain` an  [OutputTester](./src/main/java/com/univocity/test/OutputTester.java). 


```java

	abstract class Example extends  {
		private final OutputTester tester;
		public Example() {
			tester = new OutputTester(getClass(), "examples/expectedOutputs/", "UTF-8");
		}
		
		//... other common stuff used by all examples
	}

	public class Tutorial003SchemaMapping extends Example {
		@Test
		public void example001SchemaMapping() {
		    //do stuff
		    tester.print("something"); //just adds "something" to the OutputTester's internal buffer"
		    ...
		    tester.printAndValidate("some_output");
		}
		
		@Test
		public void example002UpdateAgainstDataset() {
			//do more stuff
			tester.println("something else"); //adds "something else" to the OutputTester's internal buffer"
			...
			tester.printAndValidate("another_output");
		}
		
		@Test
		public void example003UpdatePrevention() {
			StringBuilder out = new StringBuilder();
		    //to other stuff
		    tester.println(out, "something"); //adds "something" to your StringBuilder instance
			...
			tester.printAndValidate(out);
		}
	}
	
```

Things are pretty much the same, but now you must provide the `class` of your test class so the output tester can find the correct output files.

## Setting up the dependencies

All you have to do is to get the univocity-output-tester.jar. Download it directly from 
[here](http://oss.sonatype.org/content/repositories/releases/com/univocity/univocity-output-tester/2.1/univocity-output-tester-2.1.jar) or add the following to your 
`pom.xml`:


```xml
    
    <dependencies>
    ...
        <dependency>
            <groupId>com.univocity</groupId>
            <artifactId>univocity-output-tester</artifactId>
            <version>2.1</version>
            <type>jar</type>
            <scope>test</scope>
        </dependency>
    ...
    </dependencies>
    
```

### And that's all. It made our life much easier when testing complex test outputs. We hope this project is as useful to you as it is to us.
