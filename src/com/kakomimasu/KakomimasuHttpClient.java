package com.kakomimasu;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

  private <T> T get(String apiPath, Class<T> dto) {
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(this.host + apiPath));
    if (this.bearerToken != null) {
      builder.setHeader("Authorization", "Bearer " + this.bearerToken);
    }
    HttpRequest request = builder.build();
    HttpResponse<String> response = null;
    try {
      response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    String body = response.body();
    ObjectMapper mapper = new ObjectMapper();
    try {
      return (T) mapper.readValue(body, dto);
    } catch (IOException e) {
      return null;
    }
  }

  private <T> T post(String apiPath, Class<T> resDto, Object reqDto) {
    ObjectMapper mapper = new ObjectMapper();
    String reqJson = null;
    try {
      reqJson = mapper.writeValueAsString(reqDto);
    } catch (JsonProcessingException e) {
      // e.printStackTrace();
    }
    System.out.println(reqJson);

    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .POST(BodyPublishers.ofString(reqJson))
        .header("Content-Type", "application/json")
        .uri(URI.create(this.host + apiPath));
    if (this.bearerToken != null) {
      builder.setHeader("Authorization", "Bearer " + this.bearerToken);
    }
    HttpRequest request = builder.build();
    HttpResponse<String> response = null;
    try {
      response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    String body = response.body();
    System.out.println(body);
    try {
      return (T) mapper.readValue(body, resDto);
    } catch (IOException e) {
      return null;
    }
  }

  Classes.MatchRes match(Classes.MatchReq reqDto) {
    return this.post("/v1/match", Classes.MatchRes.class, reqDto);
  }

  void connectWebSocket() {

    WebSocket.Builder wsBuilder = this.httpClient.newWebSocketBuilder();
    WebSocket.Listener listener = new WebSocket.Listener() {
      @Override
      public void onOpen(WebSocket webSocket) {
        System.out.println("WebSocket opened");
        webSocket.sendText("{\"q\":\"type:normal\"}", true);
        webSocket.request(1000);
      }

      @Override
      public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        System.out.println("WebSocket received: " + data);
        webSocket.request(1);
        return null;
      }

      @Override
      public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + statusCode + " " + reason);
        return null;
      }
    };

    CompletableFuture<WebSocket> future = wsBuilder.buildAsync(URI.create("ws://localhost:8880" + "/v1/ws/game"),
        listener);
    // future.
    // while (true) {
    // future.join();
    // CompletableFuture.allOf(future).join();
    // System.out.println("wait");

    try {
      WebSocket ws = future.get();
      // ws.
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      // break;
    }
    // }
    // try {
    // WebSocket ws = future.get();
    // // ws.sendText("Hello World!!", true);
    // } catch (InterruptedException | ExecutionException e) {
    // e.printStackTrace();
    // }
  }
}
