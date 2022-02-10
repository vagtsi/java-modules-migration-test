module javamodulemigration.immutables {
  // logging
  requires org.slf4j;
  
  // immutables with dependencies
  requires static java.compiler;
  requires static org.immutables.value;
  requires org.immutables.mongo;
  requires org.immutables.gson;
  requires com.google.gson;
  requires java.annotation;
  requires java.validation;
}