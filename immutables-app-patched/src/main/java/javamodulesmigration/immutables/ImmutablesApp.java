package javamodulesmigration.immutables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImmutablesApp {
  private static final Logger log = LoggerFactory.getLogger(ImmutablesApp.class);

  public static void main(String[] args) {
    ExampleValue value = ImmutableExampleValue.builder().name("Bruce Lee").build();
    log.info("Successful created immutables value: {}", value);
  }

}
