package javamodulesmigration;

import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationTestApp {
  private static final Logger log = LoggerFactory.getLogger(MigrationTestApp.class);

  public static void main(String[] args) {
    log.info("Starting elastic search node");

    Node server = nodeBuilder().build();
    server.start();
  }

}
