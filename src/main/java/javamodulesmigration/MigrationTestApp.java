package javamodulesmigration;

import static org.elasticsearch.node.NodeBuilder.*;
import org.elasticsearch.node.Node;

public class MigrationTestApp {

  public static void main(String[] args) {
    Node server = nodeBuilder().build();
    server.start();
  }

}
