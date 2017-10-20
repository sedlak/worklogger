package cz.morosystems.worklogger.test;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by moro on 9/26/2017.
 */
public class UnitTests {

	//Here will be a lot of unit tests

	@Test
	public void testCliHelp() {
		// CLIRunner runner = new CLIRunner();
		assertEquals("Hello world", "Hello world");
		testString("a", "b");
		//System.out.println("A2: "+ a);
	}

	private void testString(String a, String b) {
		a = "bla";
		System.out.println("A: " + a);

	}

}
