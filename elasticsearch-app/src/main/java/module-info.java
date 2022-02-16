module javamodulemigration.elasticsearch.app {
  requires org.slf4j;
  requires org.apache.logging.log4j;
  requires de.vagtsi.elasticsearch.restclient;
  requires org.apache.httpcomponents.httpcore;
  requires org.apache.httpcomponents.httpclient;
  requires org.apache.httpcomponents.httpasyncclient;
  opens javamodulesmigration.elasticsearch;
}