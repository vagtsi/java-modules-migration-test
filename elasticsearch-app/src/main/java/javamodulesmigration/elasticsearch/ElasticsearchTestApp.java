package javamodulesmigration.elasticsearch;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Simple Java application as module example using Elasticsearch via
 * <a href="https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-getting-started.html">REST-high-level-client API</a>
 * for creating a indexing and executing some queries on a example
 * <a href="https://gist.github.com/ebaranov/41bf38fdb1a2cb19a781">Country - state list in JSON</a>
 * <p>
 * Note: the elasticsearch backend needs to be started locally, e.g. via Docker as documented in
 * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html">Install Elasticsearch with Docker</a>
 */
public class ElasticsearchTestApp {
  private static final Logger log = LoggerFactory.getLogger(ElasticsearchTestApp.class);
  private static final String INDEX_NAME = "countries";
  private static final String DOCUMENT_NAME = "/countries.json";

  public static void main(String[] args) {
    log.info("Connecting to local elastic search node");
    
    try (RestHighLevelClient client = createRestHighLevelClient()) {
      log.info("Successful connected to local elastic");
      
      //check if index of the example document already exists
      GetIndexRequest getIndexRequest = new GetIndexRequest("countries");
      if (client.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
        log.info("Example index '{}' does already exist", INDEX_NAME);
        
        //delete index
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(INDEX_NAME);
        client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        log.info("> deleted existing index '{}'", INDEX_NAME);
      }
      
      //create new index
      log.info("Creating new example index '{}'", INDEX_NAME);
      CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
      CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
      String id = createIndexResponse.index();
      log.info("> successful created index '{}'", id);
      
      //add document to index
      List<String> files = readFilesFromClasspath(DOCUMENT_NAME);
      log.info("Indexing {} files", files.size());
      long start = System.currentTimeMillis();
      for (String file : files) {
        IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
        indexRequest.source(file, XContentType.JSON);
        client.index(indexRequest, RequestOptions.DEFAULT);
      }
      log.info("> successful indexed {} documents within {} ms", files.size(), System.currentTimeMillis() - start);
      
      //refresh index
      RefreshRequest refreshRequest = new RefreshRequest(INDEX_NAME);
      RefreshResponse refreshResponse = client.indices().refresh(refreshRequest, RequestOptions.DEFAULT);
      log.info("> successful refreshed index in {} shards", refreshResponse.getSuccessfulShards());
      
      //count all documents in the index
      CountRequest countRequest = new CountRequest(INDEX_NAME); 
      countRequest.query(QueryBuilders.matchAllQuery());
      CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
      log.info("> {} documents contained in index '{}'", countResponse.getCount(), INDEX_NAME);
      
      //search for exact match of country
      SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
      SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
          //.postFilter(QueryBuilders.matchAllQuery()
         .postFilter(QueryBuilders.matchQuery("country", "Bahamas")
                                                      //.fuzziness(Fuzziness.AUTO)
                                                      //.prefixLength(3)
                                                      //.maxExpansions(10)
              ); 
      searchRequest.source(sourceBuilder);
      searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
      SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
      log.info("> search result: {}", searchResponse);
      SearchHits hits = searchResponse.getHits();
      log.info("> found {} countries: {}", hits.getTotalHits().value, hits.getHits());
      
    } catch (Exception e) {
      log.error("Error on accessing index", e);
    }
    
    log.info("> finished elastic search example!");
  }

 // -- private --
  
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
  
  private static List<String> readFilesFromClasspath(String filePathName) throws IOException, URISyntaxException {
    Path filePath = Paths.get(ElasticsearchTestApp.class.getResource(filePathName).toURI());
    String data = Files.readString(filePath);
    
    //split single example data into multiple JSON documents for indexing
    ObjectMapper mapper = new ObjectMapper();
    Countries countries = mapper.readValue(data, Countries.class);
    
    return countries.countries()
      .stream().map(c -> {
        try {
          return mapper.writeValueAsString(c);
        } catch (JsonProcessingException e) {
          throw new UncheckedIOException(e);
        }
      })
      .collect(Collectors.toList());
  }

  private static class Countries {
    @JsonProperty("countries")
    private List<Country> countries;
    
    public List<Country> countries() {
      return countries;
    }
  }

  private static class Country {
    @JsonProperty("country")
    private String name;
    
    @JsonProperty("states")
    private List<String> states;
    
    @SuppressWarnings("unused")
    public String name() {
      return name;
    }
    @SuppressWarnings("unused")
    public List<String> states() {
      return states;
    }
  }
}
