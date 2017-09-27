package cz.morosystems.worklogger.test;

import static org.junit.Assert.assertEquals;

import cz.morosystems.worklogger.CLIRunner;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by moro on 9/26/2017.
 */
public class UnitTests {

  //Here will be a lot of unit tests

  @Test
  public void testCliHelp(){
    CLIRunner runner = new CLIRunner();
    assertEquals("Hello world", "Hello world");

  }

}
