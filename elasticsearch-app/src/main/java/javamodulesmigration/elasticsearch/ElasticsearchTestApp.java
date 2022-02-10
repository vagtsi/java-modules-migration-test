package javamodulesmigration.elasticsearch;

import java.io.IOException;
import java.time.Duration;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticsearchTestApp {
  private static final Logger log = LoggerFactory.getLogger(ElasticsearchTestApp.class);

  public static void main(String[] args) {
    log.info("Connecting to local elastic search node");
    
    try (RestHighLevelClient client = createRestHighLevelClient()) {
      log.info("Successful connected to local elastic");
      IndicesClient indices = client.indices();
      
    } catch (IOException e) {
      log.error("Failed to close connection", e);
    }
    
    log.info("> finished elastic search example!");
  }

  private static RestHighLevelClient createRestHighLevelClient() {
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    String userName = System.getenv("ES_USER");
    String password = System.getenv("ES_PASSWORD");
    if (userName == null || password == null) {
      credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("", ""));
    }

    Duration.ofSeconds(2);
    RestClientBuilder clientBuilder = RestClient.builder(
        new HttpHost(
            "localhost",
            9200,
            "http"))
        .setHttpClientConfigCallback(
            httpClientBuilder ->
                httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setKeepAliveStrategy(
                        (HttpResponse response, HttpContext context) ->
                            Duration.ofSeconds(2).toMillis()))
        .setRequestConfigCallback(
            requestConfigBuilder ->
                requestConfigBuilder
                    .setConnectTimeout((int) Duration.ofSeconds(5).toMillis())
                    .setSocketTimeout((int) Duration.ofSeconds(60).toMillis())
                    .setConnectionRequestTimeout(0));
    return new RestHighLevelClient(clientBuilder);
  }
}
