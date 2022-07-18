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

  void connectWebSocket(MatchListener listener) {

    WebSocket.Builder wsBuilder = this.httpClient.newWebSocketBuilder();
    WebSocket.Listener wsListener = new WebSocket.Listener() {
      private String readData = "";
      private boolean gaming = false;
      private boolean ending = false;

      @Override
      public void onOpen(WebSocket webSocket) {
        System.out.println("WebSocket opened");
        webSocket.sendText("{\"q\":\"type:normal\"}", true);
        webSocket.request(1000);
      }

      @Override
      public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        readData += data.toString();
        webSocket.request(1);

        if (last == false)
          return null;
        String json = readData;
        readData = "";
        // System.out.println("WebSocket received: " + readData + "\nlast? : " + last);
        var type = JsonUtil.parse(json, Classes.WsGameRes.class).type;
        Classes.Game game;
        System.out.println(type);
        if (type.equals("initial")) {
          game = JsonUtil.parse(json, Classes.WsGameInitialRes.class).games[0];
        } else if (type.equals("update")) {
          game = JsonUtil.parse(json, Classes.WsGameUpdateRes.class).game;
          // System.out.println(readData);
        } else {
          return null;
        }
        if (this.gaming == false && game.gaming == true) {
          this.gaming = true;
          listener.onStart(game);
        } else if (this.ending == false && game.ending == true) {
          this.ending = true;
          listener.onEnd(game);
        } else if (game.gaming) {
          listener.onTurn(game);
        }

        readData = "";
        return null;
      }

      @Override
      public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + statusCode + " " + reason);
        return null;
      }
    };

    CompletableFuture<WebSocket> future = wsBuilder.buildAsync(URI.create("ws://localhost:8880" + "/v1/ws/game"),
        wsListener);
    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }
}
