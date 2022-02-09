module javamodulemigration.immutables {
  requires org.slf4j;
  
  // immutables
  requires static java.compiler;
  requires static org.immutables.value;
//  requires org.immutables.criteria.inmemory;
  requires org.immutables.mongo;
  requires org.immutables.gson;
  requires com.google.gson;

}