package bio.terra.spendprofilemanager;

import static bio.terra.spendprofilemanager.HelloWorld.helloWorld;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HelloWorldTest {

  @Test
  public void testHelloWorld() throws Throwable {
    assertEquals(helloWorld(), "Hello world");
  }
}
