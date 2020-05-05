package service;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

public class ChimeService {

  private String webhook;

  public ChimeService(String url) {
    webhook = url;
  }

  public void postMessage(String url) throws IOException {
    if (validParameters(webhook)) {
      HttpClient httpClient = HttpClients.custom()
          .setDefaultRequestConfig(RequestConfig.custom()
              .setCookieSpec(CookieSpecs.STANDARD).build())
          .build();

      HttpPost request = new HttpPost(webhook);
      StringEntity params = new StringEntity(String.format("{\"Content\":\"%s\"}", url));
      request.addHeader("content-type", "application/json");
      request.setEntity(params);
      httpClient.execute(request);
    }
  }

  private boolean validParameters(String webhook) {
    return null != webhook && webhook != "";
  }

}
