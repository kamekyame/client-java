package com.kakomimasu;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KakomimasuHttpClient {
  private HttpClient httpClient;
  private String host;
  private String bearerToken;

  KakomimasuHttpClient(String host) {
    this.httpClient = HttpClient.newBuilder().build();
    this.host = host;
  }

  void setBearerToken(String bearerToken) {
    this.bearerToken = bearerToken;
  }

  void get(String apiPath) {
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(this.host + apiPath));
    if (this.bearerToken != null) {
      builder.setHeader("Authorization", "Bearer " + this.bearerToken);
    }
    HttpRequest request = builder.build();
    try {
      this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
