package service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class TwitterService {

  private String apiKey;
  private String apiSecret;

  public TwitterService(String key, String secret) {
    apiKey = key;
    apiSecret = secret;
  }

  public List<String> getTweets(String searchString, String startTime) throws IOException, URISyntaxException {
    List<String> tweets = new ArrayList<>();
    if (validParameters(apiKey, apiSecret)) {
      Map<String, Object> map = search(searchString, startTime);
      if (null != map) {
        ArrayList<Object> data = (ArrayList<Object>) map.get("data");
        for (int i = 0; i < data.size(); i++) {
          Map<String, Object> item = (Map<String, Object>) data.get(i);
          String id = (String) item.get("id");
          tweets.add(id);
        }
      }
    }
    return tweets;
  }

  /*
   * This method calls the recent search endpoint with a the search term passed to it as a query parameter
   * */
  private Map<String, Object> search(String searchString, String startTime) throws IOException, URISyntaxException {

    ObjectMapper mapper = new ObjectMapper();

    HttpClient httpClient = HttpClients.custom()
        .setDefaultRequestConfig(RequestConfig.custom()
            .setCookieSpec(CookieSpecs.STANDARD).build())
        .build();

    URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/labs/2/tweets/search");
    ArrayList<NameValuePair> queryParameters;
    queryParameters = new ArrayList<>();
    queryParameters.add(new BasicNameValuePair("query", searchString));
    queryParameters.add(new BasicNameValuePair("start_time", startTime));
    queryParameters.add(new BasicNameValuePair("expansions", "author_id"));
    uriBuilder.addParameters(queryParameters);

    HttpGet httpGet = new HttpGet(uriBuilder.build());
    httpGet.setHeader("Authorization", String.format("Bearer %s", getAccessToken()));
    httpGet.setHeader("Content-Type", "application/json");
    httpGet.setHeader("User-Agent", "LabsRecentSearchQuickStartJava");

    HttpResponse response = httpClient.execute(httpGet);
    HttpEntity entity = response.getEntity();
    return mapper.readValue(entity.getContent(), Map.class);
  }

  /*
   *
   * Helper method that generates bearer token by calling the /oauth2/token endpoint
   * */
  private String getAccessToken() throws IOException, URISyntaxException {
    String accessToken = null;

    HttpClient httpClient = HttpClients.custom()
        .setDefaultRequestConfig(RequestConfig.custom()
            .setCookieSpec(CookieSpecs.STANDARD).build())
        .build();

    URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/oauth2/token");
    ArrayList<NameValuePair> postParameters;
    postParameters = new ArrayList<>();
    postParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
    uriBuilder.addParameters(postParameters);

    HttpPost httpPost = new HttpPost(uriBuilder.build());
    httpPost.setHeader("Authorization", String.format("Basic %s", getBase64EncodedString()));
    httpPost.setHeader("Content-Type", "application/json");

    HttpResponse response = httpClient.execute(httpPost);
    HttpEntity entity = response.getEntity();

    if (null != entity) {
      try (InputStream inputStream = entity.getContent()) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonMap = mapper.readValue(inputStream, Map.class);
        accessToken = jsonMap.get("access_token").toString();
      }
    }
    return accessToken;
  }

  /*
   * Helper method that generates the Base64 encoded string to be used to obtain bearer token
   * */
  private String getBase64EncodedString() {
    String s = String.format("%s:%s", apiKey, apiSecret);
    return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
  }

  private boolean validParameters(String apiKey, String apiSecret) {
    return ((null != apiKey && null != apiSecret) && (apiKey != "" && apiSecret != ""));
  }

}
